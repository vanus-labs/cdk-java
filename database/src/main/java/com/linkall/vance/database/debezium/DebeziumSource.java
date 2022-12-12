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

package com.linkall.vance.database.debezium;

import com.linkall.vance.config.Config;
import com.linkall.vance.core.Source;
import com.linkall.vance.core.Tuple;
import io.debezium.embedded.Connect;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.spi.OffsetCommitPolicy;
import io.vertx.core.json.JsonObject;
import org.apache.kafka.connect.json.JsonConverter;
import org.apache.kafka.connect.json.JsonConverterConfig;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.storage.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class DebeziumSource implements Source {
    protected static final Logger LOGGER = LoggerFactory.getLogger(DebeziumSource.class);
    private DebeziumEngine.ChangeConsumer<ChangeEvent<SourceRecord, SourceRecord>> consumer;
    private DebeziumEngine<ChangeEvent<SourceRecord, SourceRecord>> engine;
    private ExecutorService executor;
    protected DbConfig dbConfig;
    protected BlockingQueue<Tuple> events;

    public DebeziumSource() {
        events = new ArrayBlockingQueue<>(100);
    }

    public abstract Adapter getAdapter();

    public abstract DbConfig getDbConfig();

    public abstract String getConnectorClass();

    public abstract Map<String, Object> getConfigOffset();

    public abstract Properties getDebeziumProperties();

    public abstract Set<String> getSystemExcludedTables();

    public String getOffsetKey() {
        return null;
    }

    public void start() {
        dbConfig = getDbConfig();
        consumer = new RecordConsumer(events, getAdapter());
        engine =
                DebeziumEngine.create(Connect.class)
                        .using(getProperties())
                        .using(OffsetCommitPolicy.always())
                        .notifying(consumer)
                        .using((success, message, error) ->
                                LOGGER.info("Debezium engine shutdown,success: {}, message: {}", success, message, error))
                        .build();
        executor = Executors.newSingleThreadExecutor();
        executor.execute(engine);
    }

    private Properties getProperties() {
        final Properties props = new Properties();

//        props.setProperty("test.disable.global.locking","true");
        // debezium engine configuration
        props.setProperty("connector.class", getConnectorClass());
        // snapshot config
        props.setProperty("snapshot.mode", "initial");
        // DO NOT include schema change, e.g. DDL
        props.setProperty("include.schema.changes", "false");
        // disable tombstones
        props.setProperty("tombstones.on.delete", "false");

        // offset
        props.setProperty("offset.storage", KvStoreOffsetBackingStore.class.getCanonicalName());
        if (getOffsetKey()!=null) {
            props.setProperty(
                    KvStoreOffsetBackingStore.OFFSET_STORAGE_KV_STORE_KEY_CONFIG, getOffsetKey());
        }
        Map<String, Object> configOffset = getConfigOffset();
        if (configOffset!=null && configOffset.size() > 0) {
            Converter valueConverter = new JsonConverter();
            Map<String, Object> valueConfigs = new HashMap<>();
            valueConfigs.put(JsonConverterConfig.SCHEMAS_ENABLE_CONFIG, false);
            valueConverter.configure(valueConfigs, false);
            byte[] offsetValue = valueConverter.fromConnectData(dbConfig.getDatabase(), null, configOffset);
            props.setProperty(
                    KvStoreOffsetBackingStore.OFFSET_CONFIG_VALUE,
                    new String(offsetValue, StandardCharsets.UTF_8));
        }

        props.setProperty("offset.flush.interval.ms", "1000");

        // https://debezium.io/documentation/reference/configuration/avro.html
        props.setProperty("key.converter.schemas.enable", "false");
        props.setProperty("value.converter.schemas.enable", "false");

        // debezium names
        props.setProperty("name", dbConfig.getDatabase());
        props.setProperty("database.server.name", dbConfig.getDatabase());

        // db connection configuration
        props.setProperty("database.hostname", dbConfig.getHost());
        props.setProperty("database.port", dbConfig.getPort());
        props.setProperty("database.user", dbConfig.getUsername());
        props.setProperty("database.dbname", dbConfig.getDatabase());
        props.setProperty("database.password", dbConfig.getPassword());

        props.putAll(getDebeziumProperties());
        return props;
    }

    public Properties getDebeziumProperties(JsonObject debezium) {
        final Properties debeziumProperties = new Properties();

        debezium.stream().forEach(k -> {
            debeziumProperties.put(k.getKey(), debezium.getString(k.getKey()));
        });

        return debeziumProperties;
    }

    public Set<String> getExcludedTables(Set<String> excludeTables) {
        Set<String> exclude = new HashSet<>(getSystemExcludedTables());
        exclude.addAll(excludeTables);
        return exclude;
    }

    public String tableFormat(String name, Stream<String> table) {
        return table
                .map(stream -> name + "." + stream)
                .collect(Collectors.joining(","));
    }

    @Override
    public Class<? extends Config> configClass() {
        return null;
    }

    @Override
    public void initialize(Config config) throws Exception {
        start();
    }


    @Override
    public void destroy() throws Exception {
        if (engine!=null)
            engine.close();
        executor.shutdown();
    }

    @Override
    public BlockingQueue<Tuple> queue() {
        return events;
    }
}
