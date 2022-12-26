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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkall.cdk.config.Config;
import com.linkall.cdk.connector.Source;
import com.linkall.cdk.connector.Tuple;
import com.linkall.cdk.util.EventUtil;
import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import io.cloudevents.core.v1.CloudEventBuilder;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.CloudEvents;
import io.debezium.engine.spi.OffsetCommitPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public abstract class DebeziumSource implements Source, DebeziumEngine.ChangeConsumer<ChangeEvent<String, String>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DebeziumSource.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final BlockingQueue<Tuple> events;
    protected DebeziumConfig debeziumConfig;
    private DebeziumEngine<ChangeEvent<String, String>> engine;
    private ExecutorService executor;

    public DebeziumSource() {
        this.events = new LinkedBlockingQueue<>();
    }

    protected void adapt(CloudEventBuilder builder, String key, Object value) throws IOException {
        switch (key) {
            case "id":
                builder.withId(UUID.randomUUID().toString());
                break;
            case "source":
                builder.withSource(URI.create(value.toString()));
                break;
            case "specversion":
                break;
            case "type":
                builder.withType(value.toString());
                break;
            case "datacontenttype":
                builder.withDataContentType(value.toString());
                break;
            case "dataschema":
                builder.withDataSchema(URI.create(value.toString()));
                break;
            case "subject":
                builder.withSubject(value.toString());
                break;
            case "time":
                builder.withTime(OffsetDateTime.parse(value.toString()));
                break;
            case "data":
                builder.withData(convertData(value));
                break;
            default:
                builder.withExtension(key, value.toString());
                break;
        }
    }

    abstract protected CloudEventData convertData(Object data) throws IOException;

    @Override
    final public void destroy() throws Exception {
        if (engine!=null)
            engine.close();
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
    final public void handleBatch(List<ChangeEvent<String, String>> records,
                                  DebeziumEngine.RecordCommitter<ChangeEvent<String, String>> committer)
            throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(records.size());
        LOGGER.info("Received event count {}", records.size());
        int i = 0;
        for (ChangeEvent<String, String> record : records) {
            i++;
            LOGGER.info("Received event detail {} {}", i, record);
            if (record.value()==null) {
                latch.countDown();
                continue;
            }
            try {
                CloudEvent ceEvent = this.convert(record.value());
                events.put(
                        new Tuple(ceEvent,
                                () -> commit(latch, record, committer),
                                (msg) -> {
                                    LOGGER.error("event send failed:{},{}", msg, EventUtil.eventToJson(ceEvent));
                                    commit(latch, record, committer);
                                }
                        )
                );
            } catch (IOException e) {
                latch.countDown(); // How to process offset?
                LOGGER.error("failed to parse record data {} to json, error: {}", record.value(), e);
            }
        }
        latch.await();
        LOGGER.info("Received event count await {}", records.size());
        committer.markBatchFinished();
        LOGGER.info("Received event count end {}", records.size());

    }

    final protected void start() {
        engine = DebeziumEngine.create(CloudEvents.class)
                .using(this.debeziumConfig.getProperties())
                .using(OffsetCommitPolicy.always())
                .notifying(this)
                .using((success, message, error) ->
                        LOGGER.info("Debezium engine shutdown,success: {}, message: {}", success, message, error))
                .build();
        executor = Executors.newSingleThreadExecutor();
        executor.execute(engine);
    }

    private CloudEvent convert(String record) throws IOException {
        Map<String, Object> m = this.mapper.readValue(record.getBytes(StandardCharsets.UTF_8), Map.class);
        CloudEventBuilder builder = new CloudEventBuilder();
        for (Map.Entry<String, Object> entry : m.entrySet()) {
            if (entry.getValue()==null) {
                continue;
            }
            this.adapt(builder, entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    private void commit(
            CountDownLatch latch,
            ChangeEvent<String, String> record,
            DebeziumEngine.RecordCommitter<ChangeEvent<String, String>> committer) {
        LOGGER.info("commit record:{}", record);
        try {
            committer.markProcessed(record);
        } catch (InterruptedException e) {
            LOGGER.warn("Failed to mark processed record: {},error: {}", record.value(), e);
        }
        latch.countDown();
    }
}
