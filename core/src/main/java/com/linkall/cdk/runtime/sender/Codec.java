package com.linkall.cdk.runtime.sender;

import com.google.protobuf.Timestamp;
import com.linkall.cdk.proto.CloudEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Codec {
    private static final String contentTypeProtobuf = "application/protobuf";
    private static final String datacontenttype = "datacontenttype";
    private static final String dataschema = "dataschema";
    private static final String subject = "subject";
    private static final String timeStr = "time";

    public static CloudEvent ToProto(io.cloudevents.CloudEvent event) {
        CloudEvent.Builder builder = CloudEvent.newBuilder()
                .setId(event.getId())
                .setSource(event.getSource().toString())
                .setSpecVersion(event.getSpecVersion().toString())
                .setType(event.getType());

        Map<String, CloudEvent.CloudEventAttributeValue> attrs = new HashMap<>();
        if (event.getDataContentType() != null) {
            attrs.put(datacontenttype, attributeFor(event.getDataContentType()));
        }

        if (event.getDataSchema() != null) {
            attrs.put(dataschema, attributeFor(event.getDataSchema()));
        }

        if (event.getSubject() != null) {
            attrs.put(subject, attributeFor(event.getSubject()));
        }

        if (event.getTime() != null) {
            attrs.put(timeStr, attributeFor(event.getTime()));
        }

        for (String name : event.getExtensionNames()) {
            CloudEvent.CloudEventAttributeValue attr = attributeFor(Objects.requireNonNull(event.getExtension(name)));
            if (attr != null) {
                attrs.put(name, attr);
            }
        }

        builder.setBinaryData(com.google.protobuf.ByteString.copyFrom(Objects.requireNonNull(event.getData()).toBytes()));

        if (event.getDataContentType() == contentTypeProtobuf) {
            // TODO
        }

        builder.putAllAttributes(attrs);
        return builder.build();
    }

    private static CloudEvent.CloudEventAttributeValue attributeFor(Object obj) {

        switch (obj.getClass().getName()) {
            case "java.lang.Boolean":
                return CloudEvent.CloudEventAttributeValue.newBuilder().setCeBoolean((Boolean) obj).build();
            case "java.lang.Integer":
                return CloudEvent.CloudEventAttributeValue.newBuilder().setCeInteger((Integer) obj).build();
            case "java.lang.String":
                return CloudEvent.CloudEventAttributeValue.newBuilder().setCeString(obj.toString()).build();
            case "java.net.URI":
                return CloudEvent.CloudEventAttributeValue.newBuilder().setCeUri(obj.toString()).build();
            case "URIRef":
                // TODO
                return CloudEvent.CloudEventAttributeValue.newBuilder().setCeUriRef(obj.toString()).build();
            case "java.time.OffsetDateTime":
                java.time.OffsetDateTime t = (java.time.OffsetDateTime) obj;
                Timestamp tm = Timestamp.newBuilder().setSeconds(t.getSecond()).setNanos(t.getNano()).build();
                return CloudEvent.CloudEventAttributeValue.newBuilder().setCeTimestamp(tm).build();
            default:
                // TODO
                return CloudEvent.CloudEventAttributeValue.newBuilder().setCeString(obj.toString()).build();
        }
    }
}
