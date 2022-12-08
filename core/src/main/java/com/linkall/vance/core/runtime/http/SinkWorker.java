package com.linkall.vance.core.runtime.http;

import com.linkall.vance.config.SinkConfig;
import com.linkall.vance.core.Result;
import com.linkall.vance.core.Sink;
import com.linkall.vance.core.runtime.Worker;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.message.MessageReader;
import io.cloudevents.http.vertx.VertxMessageFactory;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SinkWorker implements Worker {
    private static final Logger LOGGER = LoggerFactory.getLogger(SinkWorker.class);

    private Sink sink;
    private SinkConfig config;

    public SinkWorker(Sink sink, SinkConfig config) {
        this.sink = sink;
        this.config = config;
    }

    @Override
    public void start() {
        LOGGER.info("start connector {}", sink.name());
        Vertx vertx = Vertx.vertx();
        vertx.createHttpServer().requestHandler(request -> {
                    HttpServerResponse response = request.response();
                    VertxMessageFactory.createReader(request)
                            .map(MessageReader::toEvent)
                            .onSuccess(new EventHandler(sink, request))
                            .onFailure(t -> {
                                LOGGER.info("receive a non-CloudEvent data");
                                response.setStatusCode(404);
                                response.end("invalid CloudEvent format");
                            });
                })
                .listen(config.getPort(), server -> {
                    if (server.succeeded()) {
                        LOGGER.info("{} listening on port {} success", sink.name(), server.result().actualPort());
                    } else {
                        LOGGER.error("listening on port {} failed", config.getPort(), server.cause());
                    }
                });
    }

    @Override
    public void stop() {
        try {
            sink.destroy();
        } catch (Exception e) {
            LOGGER.error("sink destroy error", e);
        }
    }

    static class EventHandler implements Handler<CloudEvent> {
        private final Sink sink;
        private final HttpServerRequest request;

        public EventHandler(Sink sink, HttpServerRequest request) {
            this.sink = sink;
            this.request = request;
        }

        @Override
        public void handle(CloudEvent event) {
            HttpServerResponse response = request.response();
            try {
                Result result = this.sink.Arrived(event);
                if (result==null || result==Result.SUCCESS) {
                    request.response().end();
                } else {
                    response.setStatusCode(result.getCode());
                    response.end(result.getMsg());
                }
            } catch (Throwable t) {
                LOGGER.error("event {} process error", event, t);
                response.setStatusCode(500);
                response.end("event process failed");
            }
        }
    }
}
