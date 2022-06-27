package com.vance.source;

import com.linkall.vance.common.env.EnvUtil;
import com.linkall.vance.core.Adapter;
import com.linkall.vance.core.Source;
import io.cloudevents.CloudEvent;
import io.cloudevents.http.vertx.VertxMessageFactory;
import io.cloudevents.jackson.JsonFormat;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySource implements Source {
    private static final Logger LOGGER = LoggerFactory.getLogger(MySource.class);
    private static final int NUM_EVENTS = 20;
    private final Vertx vertx = Vertx.vertx();  // Use Vertx to send HTTP requests, you can choose any HTTP frameworks as you want
    private final WebClient webClient = WebClient.create(vertx);

    @Override
    public void start(){
        // TODO Initialize your Adapter
        MyAdapter adapter = (MyAdapter) getAdapter();

        // TODO receive your original data and transform it into a CloudEvent via your Adapter
        // In this sample, we use a String as the original data
        for (int i = 0; i < NUM_EVENTS; i++) {
            String data = "Event number " + i;
            // TODO: obtain CloudEvents
            CloudEvent event = adapter.adapt(data);
            // Use EnvUtil to get the target URL the source will send to
            // You can replace the default sink URL with yours in resources/config.json
            String sink = EnvUtil.getVanceSink();
            // TODO: deliver CloudEvents to endpoint ${V_TARGET}
            sendCloudEvent(event,sink);
        }
    }

    // Use Vertx to send HTTP requests, you can choose any HTTP frameworks as you want
    private void sendCloudEvent(CloudEvent event, String targetURL){
        Future<HttpResponse<Buffer>> responseFuture;
        // Send CloudEvent to vance_sink
        responseFuture = VertxMessageFactory.createWriter(webClient.postAbs(targetURL))
                .writeStructured(event, JsonFormat.CONTENT_TYPE); // Use structured mode.
        responseFuture.onSuccess(resp->{
            LOGGER.info("send CloudEvent success");
        }).onFailure(t-> LOGGER.info("send task failed"));
    }

    @Override
    public Adapter getAdapter() {
        return new MyAdapter();
    }
}

