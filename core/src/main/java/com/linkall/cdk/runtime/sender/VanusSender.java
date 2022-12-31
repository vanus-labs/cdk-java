package com.linkall.cdk.runtime.sender;

import com.linkall.cdk.proto.BatchEvent;
import com.linkall.cdk.proto.CloudEventBatch;
import com.linkall.cdk.proto.CloudEventsGrpc;
import io.cloudevents.CloudEvent;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VanusSender implements Sender {
    private static final int MAX_REQUEST_SIZE = 4 * 1024 * 1024; // 4MB
    private static final Logger LOGGER = LoggerFactory.getLogger(VanusSender.class);
    private final String endpoint;
    private final String eventbus;

    ManagedChannel channel;
    private CloudEventsGrpc.CloudEventsBlockingStub stub;

    public VanusSender(String endpoint, String eventbus) {
        this.endpoint = endpoint;
        this.eventbus = eventbus;
        this.channel = ManagedChannelBuilder.forTarget(endpoint).usePlaintext().build();
        this.stub = CloudEventsGrpc.newBlockingStub(this.channel);
        LOGGER.info("init VanusSender, endpoint: {}, eventbus: {}", this.endpoint, this.eventbus);
    }

    @Override
    public void sendEvents(List<CloudEvent> events) {
        List<com.linkall.cdk.proto.CloudEvent> list = new ArrayList<>(events.size());
        int size = 0;
        for (CloudEvent event : events) {
            com.linkall.cdk.proto.CloudEvent pe = Codec.ToProto(event);
            if (size + pe.getSerializedSize() > MAX_REQUEST_SIZE) {
                this.send(list);
                size = 0;
                list.clear();
            }
            size += pe.getSerializedSize();
            list.add(Codec.ToProto(event));
        }

        if (list.size() > 0) {
            this.send(list);
        }
    }

    private void send(List<com.linkall.cdk.proto.CloudEvent> list) {
        BatchEvent batchEvent = BatchEvent.newBuilder()
                .setEventbusName(this.eventbus)
                .setEvents(CloudEventBatch.newBuilder().addAllEvents(list))
                .build();

        this.stub.send(batchEvent);
    }

    @Override
    public void close() throws IOException {
    }
}
