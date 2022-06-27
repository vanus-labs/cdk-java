package com.vance.samples;

import com.linkall.vance.common.env.EnvUtil;
import com.linkall.vance.core.Adapter;
import com.linkall.vance.core.Adapter1;
import com.linkall.vance.core.Source;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.http.vertx.VertxMessageFactory;
import io.cloudevents.jackson.JsonFormat;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
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
        // Use Vertx to send HTTP requests, you can choose any HTTP frameworks as you want
        final Vertx vertx = Vertx.vertx();
        final WebClient webClient = WebClient.create(vertx);

        // TODO Initialize your Adapter
        MyAdapter adapter = (MyAdapter) getAdapter();

        // TODO receive your original data and transform it into a CloudEvent via your Adapter
        // In this sample, we use a String as the original data
        for (int i = 0; i < NUM_EVENTS; i++) {
            String data = "Event number " + i;
            // TODO: obtain CloudEvents
            CloudEvent event = adapter.adapt(data);
            // TODO: deliver CloudEvents to endpoint ${V_TARGET}
            Future<HttpResponse<Buffer>> responseFuture;
            // Use EnvUtil to get the target URL the source will send to
            String sink = EnvUtil.getVanceSink();
            responseFuture = VertxMessageFactory.createWriter(webClient.postAbs(sink))
                    .writeStructured(event, JsonFormat.CONTENT_TYPE); // Use structured mode.
            responseFuture.onSuccess(resp->{
                LOGGER.info("send CloudEvent success");
            }).onFailure(t->{
                LOGGER.info("send task failed");
            });
        }
    }

    @Override
    public Adapter getAdapter() {
        return new MyAdapter();
    }
}

