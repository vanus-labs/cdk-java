package com.linkall.vance.core.runtime.http;

import com.linkall.vance.config.SourceConfig;
import com.linkall.vance.core.Source;
import com.linkall.vance.core.Tuple;
import com.linkall.vance.core.runtime.ConnectorWorker;
import com.linkall.vance.util.EventUtil;
import io.cloudevents.http.vertx.VertxMessageFactory;
import io.cloudevents.jackson.JsonFormat;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SourceWorker implements ConnectorWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(SourceWorker.class);
    private WebClient webClient;
    private CircuitBreaker breaker;

    private Source source;
    private SourceConfig config;
    private ExecutorService executorService;
    private volatile boolean isRunning = true;
    private BlockingQueue<Tuple> queue;

    public SourceWorker(Source source, SourceConfig config) {
        this.source = source;
        this.config = config;
        Vertx vertx = Vertx.vertx();
        breaker = CircuitBreaker.create("my-circuit-breaker", vertx,
                new CircuitBreakerOptions()
                        .setMaxRetries(config.getSendEventAttempts() - 1)
                        .setTimeout(3000));
        webClient = WebClient.create(vertx, new WebClientOptions().setUserAgent("cdk-java-" + source.name()));
        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void start() {
        LOGGER.info("source worker starting");

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

    public void runLoop() {
        while (isRunning) {
            try {
                Tuple tuple = queue.poll(5, TimeUnit.SECONDS);
                if (tuple==null) {
                    continue;
                }
                LOGGER.info("new event:{}", tuple.getEvent().getId());
                breaker.execute(promise -> {
                    VertxMessageFactory.createWriter(webClient.postAbs(config.getTarget()))
                            .writeStructured(tuple.getEvent(), JsonFormat.CONTENT_TYPE)
                            .onSuccess(r -> {
                                promise.complete();
                            }).onFailure(r -> {
                                LOGGER.info("send event error {}", tuple.getEvent().getId(), r.getCause());
                            });
                }).onSuccess(r -> {
                    LOGGER.debug("send event success {}", EventUtil.eventToJson(tuple.getEvent()));
                    if (tuple.getSuccess()!=null) {
                        tuple.getSuccess().call();
                    }
                }).onFailure(r -> {
                    LOGGER.warn("send event failed {}", EventUtil.eventToJson(tuple.getEvent()));
                    if (tuple.getFailed()!=null) {
                        tuple.getFailed().call();
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
