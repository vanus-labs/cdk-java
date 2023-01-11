// Copyright 2022 Linkall Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.linkall.cdk.database.debezium;

import com.linkall.cdk.config.Config;
import com.linkall.cdk.connector.Element;
import com.linkall.cdk.connector.FailedCallback;
import com.linkall.cdk.connector.Source;
import com.linkall.cdk.connector.Tuple;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.debezium.connector.AbstractSourceInfo;
import io.debezium.data.Envelope;
import io.debezium.embedded.Connect;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.spi.OffsetCommitPolicy;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.json.JsonConverter;
import org.apache.kafka.connect.json.JsonConverterConfig;
import org.apache.kafka.connect.source.SourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public abstract class DebeziumSource implements Source, DebeziumEngine.ChangeConsumer<ChangeEvent<SourceRecord, SourceRecord>> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(DebeziumSource.class);
    private final BlockingQueue<Tuple> events;
    protected DebeziumConfig debeziumConfig;
    private DebeziumEngine<ChangeEvent<SourceRecord, SourceRecord>> engine;
    private ExecutorService executor;
    protected final JsonConverter jsonDataConverter = new JsonConverter();
    protected static final String EXTENSION_NAME_PREFIX = "xvdebezium";

    public DebeziumSource() {
        this.events = new LinkedBlockingQueue<>();
        Map<String, String> ceJsonConfig = new HashMap<>();
        ceJsonConfig.put(JsonConverterConfig.SCHEMAS_ENABLE_CONFIG, "false");
        jsonDataConverter.configure(ceJsonConfig, false);
    }

    @Override
    final public void destroy() throws Exception {
        if (engine!=null) {
            engine.close();
        }
        executor.shutdown();
    }

    @Override
    final public BlockingQueue<Tuple> queue() {
        return events;
    }

    @Override
    final public void initialize(Config config) throws Exception {
        Class<? extends Config> c = this.configClass();
        if (!c.isInstance(config)) {
            throw new Exception("invalid config class, " + c.getName() + " is needed");
        }
        this.debeziumConfig = (DebeziumConfig) config;
        this.start();
    }

    @Override
    final public void handleBatch(List<ChangeEvent<SourceRecord, SourceRecord>> records,
                                  DebeziumEngine.RecordCommitter<ChangeEvent<SourceRecord, SourceRecord>> committer)
            throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        LOGGER.info("Received event count {}", records.size());
        Tuple t = new Tuple();

        t.setSuccess(() -> {
            LOGGER.debug("success to send {} records", records.size());
            committer.markProcessed(records.get(records.size() - 1));
            latch.countDown();
        });

        t.setFailed((FailedCallback<ChangeEvent<SourceRecord, SourceRecord>>) (success, failed, error) -> {
            LOGGER.error("event send failed:{}, {}", error, failed);
            committer.markProcessed(success.get(success.size() - 1).getOriginal());
            latch.countDown();
        });

        for (ChangeEvent<SourceRecord, SourceRecord> record : records) {
            if (record.value()==null) {
                continue;
            }
            try {
                t.addElement(new Element<>(this.convert(record.value()), record));
            } catch (IOException e) {
                latch.countDown(); // How to process offset?
                LOGGER.error("failed to parse record data {} to json, error: {}", record.value(), e);
            }
        }
        this.events.put(t);
        LOGGER.info("Received event count await {}", records.size());
        latch.await();
        committer.markBatchFinished();
        LOGGER.info("Received event count end {}", records.size());
    }

    final protected void start() {
        engine = DebeziumEngine.create(Connect.class)
                .using(this.debeziumConfig.getProperties())
                .using(OffsetCommitPolicy.always())
                .notifying(this)
                .using((success, message, error) ->
                        LOGGER.info("Debezium engine shutdown,success: {}, message: {}", success, message, error))
                .build();
        executor = Executors.newSingleThreadExecutor();
        executor.execute(engine);
    }

    protected CloudEvent convert(SourceRecord record) throws IOException {
        CloudEventBuilder builder = CloudEventBuilder.v1();
        Struct struct = (Struct) record.value();
        Struct source = struct.getStruct(Envelope.FieldName.SOURCE);
        String connectorType = source.getString(AbstractSourceInfo.DEBEZIUM_CONNECTOR_KEY);
        String name = source.getString(AbstractSourceInfo.SERVER_NAME_KEY);
        builder.withId(UUID.randomUUID().toString())
                .withSource(URI.create("/debezium/" + connectorType + "/" + name))
                .withType("debezium." + connectorType + ".datachangeevent")
                .withTime(OffsetDateTime.ofInstant(
                        Instant.ofEpochMilli(struct.getInt64(Envelope.FieldName.TIMESTAMP)), ZoneOffset.UTC))
                .withData("application/json", eventData(struct))
                .withExtension(extensionName("op"), struct.getString(Envelope.FieldName.OPERATION))
                .withExtension(extensionName("name"), name);
        eventExtension(builder, struct);
        return builder.build();
    }

    protected static String extensionName(String name) {
        return EXTENSION_NAME_PREFIX + name;
    }

    /**
     * build event extension
     *
     * @param builder CloudEventBuilder
     * @param struct  SourceRecord value
     */
    protected abstract void eventExtension(CloudEventBuilder builder, Struct struct);

    protected byte[] eventData(Struct struct) {
        String fieldName = Envelope.FieldName.AFTER;
        Object dataValue = struct.get(fieldName);
        if (dataValue==null) {
            fieldName = Envelope.FieldName.BEFORE;
            dataValue = struct.get(fieldName);
        }
        Schema dataSchema = struct.schema().field(fieldName).schema();
        return jsonDataConverter.fromConnectData("debezium", dataSchema, dataValue);
    }

}
