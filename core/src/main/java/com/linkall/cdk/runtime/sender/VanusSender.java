package com.linkall.cdk.runtime.sender;

import com.linkall.cdk.proto.BatchEvent;
import com.linkall.cdk.proto.CloudEventBatch;
import com.linkall.cdk.proto.CloudEventsGrpc;
import com.linkall.cdk.runtime.SourceWorker;
import io.cloudevents.CloudEvent;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VanusSender implements Sender {
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
    public void sendEvents(CloudEvent[] events) {

        List<com.linkall.cdk.proto.CloudEvent> list = new ArrayList<>(events.length);
        for (int i = 0; i < events.length; i++) {
            list.add(Codec.ToProto(events[i]));
        }

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
