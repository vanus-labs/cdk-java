package com.linkall.cdk.database.debezium;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkall.cdk.config.SourceConfig;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class DebeziumConfig extends SourceConfig {
    @JsonProperty("dbz_properties")
    private Properties customDebezium;

    protected abstract Properties getDebeziumProperties();

    protected abstract Object getOffset();

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
        Object offset = getOffset();
        if (offset!=null) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            byte[] offsetValue;
            try {
                offsetValue = objectMapper.writeValueAsBytes(offset);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("offset convert to json error", e);
            }
            props.setProperty(
                    KvStoreOffsetBackingStore.OFFSET_CONFIG_VALUE,
                    new String(offsetValue, StandardCharsets.UTF_8));
        }
        props.setProperty("offset.flush.interval.ms", "1000");

        props.setProperty("key.converter", "org.apache.kafka.connect.json.JsonConverter");
        props.setProperty("key.converter.schemas.enable", "false");
        props.setProperty("value.converter.schemas.enable", "false");

        props.putAll(getDebeziumProperties());
        if (customDebezium!=null) {
            props.putAll(customDebezium);
        }
        return props;
    }
}
