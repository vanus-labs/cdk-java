package com.linkall.cdk.runtime.http;

import com.linkall.cdk.config.SourceConfig;
import com.linkall.cdk.connector.Source;
import com.linkall.cdk.connector.Tuple;
import com.linkall.cdk.runtime.ConnectorWorker;
import com.linkall.cdk.util.EventUtil;
import com.linkall.cdk.util.Sleep;
import io.cloudevents.CloudEvent;
import io.cloudevents.http.vertx.VertxMessageFactory;
import io.cloudevents.jackson.JsonFormat;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static java.net.HttpURLConnection.*;

public class SourceWorker implements ConnectorWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(SourceWorker.class);
    private final WebClient webClient;

    private final Source source;
    private final SourceConfig config;
    private final ExecutorService executorService;
    private volatile boolean isRunning = true;
    private BlockingQueue<Tuple> queue;

    public SourceWorker(Source source, SourceConfig config) {
        this.source = source;
        this.config = config;
        Vertx vertx = Vertx.vertx();
        webClient = WebClient.create(vertx, new WebClientOptions().setUserAgent("cdk-java-" + source.name()));
        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void start() {
        LOGGER.info("source worker starting");
        LOGGER.info("event target is {}", config.getTarget());
        try {
            new URL(config.getTarget());
        } catch (MalformedURLException e) {
            throw new RuntimeException(String.format("target is invalid %s", config.getTarget()), e);
        }
        queue = source.queue();
        executorService.execute(this::runLoop);
        LOGGER.info("source worker started");
    }

    @Override
    public void stop() {
        LOGGER.info("source worker stopping");
        isRunning = false;
        executorService.shutdown();
        try {
            source.destroy();
        } catch (Exception e) {
            LOGGER.error("source destroy error", e);
        }
        LOGGER.info("source worker stopped");
    }

    private boolean needAttempt(int attempt) {
        if (config.getSendEventAttempts() <= 0) {
            return true;
        }
        return attempt < config.getSendEventAttempts();
    }

    private void sendEvent(CloudEvent event) throws Throwable {
        int attempt = 0;
        AtomicReference<Throwable> error = new AtomicReference<>();
        for (; ; ) {
            CountDownLatch latch = new CountDownLatch(1);
            Future<HttpResponse<Buffer>> future = VertxMessageFactory.createWriter(webClient.postAbs(config.getTarget()))
                    .writeStructured(event, JsonFormat.CONTENT_TYPE);
            attempt++;
            future.onComplete(ar -> {
                if (ar.failed()) {
                    error.set(ar.cause());
                } else if (ar.result().statusCode()!=HTTP_OK
                        && ar.result().statusCode()!=HTTP_NO_CONTENT
                        && ar.result().statusCode()!=HTTP_ACCEPTED) {
                    error.set(new Exception(String.format("response failed: code %d, body [%s]", ar.result().statusCode(), ar.result().bodyAsString())));
                }
                latch.countDown();
            });
            latch.await(10, TimeUnit.SECONDS);
            Throwable t = error.get();
            if (t==null) {
                LOGGER.debug("send event success, attempt:{}, event:{}", attempt, EventUtil.eventToJson(event));
                return;
            }
            if (!isRunning || !needAttempt(attempt)) {
                LOGGER.warn("send event failed, attempt:{}, event:{}", attempt, EventUtil.eventToJson(event), t);
                throw t;
            }
            LOGGER.info("send event has error will retry, attempt:{}, event:{},error:{}", attempt, event.getId(), t);
            Thread.sleep(Sleep.Backoff(attempt, 5000));
        }
    }

    public void runLoop() {
        while (isRunning) {
            try {
                Tuple tuple = queue.poll(5, TimeUnit.SECONDS);
                if (tuple==null) {
                    continue;
                }
                LOGGER.debug("new event:{}", tuple.getEvent().getId());
                try {
                    sendEvent(tuple.getEvent());
                    if (tuple.getSuccess()!=null) {
                        tuple.getSuccess().call();
                    }
                } catch (Throwable t) {
                    if (tuple.getFailed()!=null) {
                        tuple.getFailed().call(t.getMessage());
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
