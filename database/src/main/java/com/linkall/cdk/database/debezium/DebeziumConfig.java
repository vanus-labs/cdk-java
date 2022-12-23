package com.linkall.cdk.database.debezium;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.linkall.cdk.config.SourceConfig;

import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class DebeziumConfig extends SourceConfig {
    @JsonProperty("dbz_properties")
    private Properties customDebezium;

    protected abstract Properties getDebeziumProperties();

    protected String tableFormat(String name, Stream<String> table) {
        return table
                .map(stream -> name + "." + stream)
                .collect(Collectors.joining(","));
    }

    // common configuration
    protected Properties getProperties() {
        final Properties props = new Properties();
//        props.setProperty("test.disable.global.locking","true");
        // snapshot config
        props.setProperty("snapshot.mode", "initial");
        // DO NOT include schema change, e.g. DDL
        props.setProperty("include.schema.changes", "false");
        // disable tombstones
        props.setProperty("tombstones.on.delete", "false");

        props.setProperty("offset.storage", KvStoreOffsetBackingStore.class.getCanonicalName());
//        if (getOffsetKey()!=null) {
//            props.setProperty(
//                    KvStoreOffsetBackingStore.OFFSET_STORAGE_KV_STORE_KEY_CONFIG, getOffsetKey());
//        }
//        Map<String, Object> configOffset = getConfigOffset();
//        if (configOffset!=null && configOffset.size() > 0) {
//            Converter valueConverter = new JsonConverter();
//            Map<String, Object> valueConfigs = new HashMap<>();
//            valueConfigs.put(JsonConverterConfig.SCHEMAS_ENABLE_CONFIG, false);
//            valueConverter.configure(valueConfigs, false);
//            byte[] offsetValue = valueConverter.fromConnectData(dbConfig.getDatabase(), null, configOffset);
//            props.setProperty(
//                    KvStoreOffsetBackingStore.OFFSET_CONFIG_VALUE,
//                    new String(offsetValue, StandardCharsets.UTF_8));
//        }

        props.setProperty("offset.flush.interval.ms", "1000");

        // https://debezium.io/documentation/reference/configuration/avro.html
        props.setProperty("key.converter", "org.apache.kafka.connect.json.JsonConverter");
        props.setProperty("key.converter.schemas.enable", "false");
        props.setProperty("value.converter", "io.debezium.converters.CloudEventsConverter");
        props.setProperty("value.converter.data.serializer.type", "json");
        props.setProperty("value.converter.json.schemas.enable", "false");

        props.putAll(getDebeziumProperties());
        if (customDebezium!=null) {
            props.putAll(customDebezium);
        }
        return props;
    }
}
