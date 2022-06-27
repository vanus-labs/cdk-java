package com.vance.samples;

import com.linkall.vance.core.Adapter;
import com.linkall.vance.core.Adapter1;
import com.linkall.vance.core.Source;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class MySource implements Source {
    private static final Logger LOGGER = LoggerFactory.getLogger(MySource.class);
    private static final AtomicInteger eventNum = new AtomicInteger(0);
    private static final int NUM_EVENTS = 20;

    @SuppressWarnings("unchecked")
    @Override
    public void start(){

        // TODO Initialize your Adapter
        MyAdapter adapter = (MyAdapter) getAdapter();

        // TODO receive your original data and transform it into a CloudEvent via your Adapter
        // In this sample, we use data as original data
        for (int i = 0; i < NUM_EVENTS; i++) {
            String data = "Event number " + i;
            // TODO obtain CloudEvents
            CloudEvent event = adapter.adapt(data);

        }
    }
    @Override
    public Adapter getAdapter() {
        return new MyAdapter();
    }
}
class MyAdapter implements Adapter1<String> {
    private static final CloudEventBuilder template = CloudEventBuilder.v1();
    @Override
    public CloudEvent adapt(String data) {
        template.withId(UUID.randomUUID().toString());
        URI uri = URI.create("vance-http-source");
        template.withSource(uri);
        template.withType("http");
        template.withDataContentType("application/json");
        template.withTime(OffsetDateTime.now());
        template.withData(data.getBytes());

        return template.build();
    }
}
