package com.linkall.cdk.runtime.http;

import com.linkall.cdk.config.SourceConfig;
import com.linkall.cdk.connector.Source;
import com.linkall.cdk.connector.Tuple;
import com.linkall.cdk.runtime.ConnectorWorker;
import com.linkall.cdk.util.EventUtil;
import com.linkall.cdk.util.Sleep;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.message.MessageWriter;
import io.cloudevents.http.HttpMessageFactory;
import io.cloudevents.jackson.JsonFormat;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static java.net.HttpURLConnection.*;

public class SourceWorker implements ConnectorWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(SourceWorker.class);
    private static final int TIMEOUT_MS = 10_000;
    private final CloseableHttpClient httpClient;
    private URI target;

    private final Source source;
    private final SourceConfig config;
    private final ExecutorService executorService;
    private volatile boolean isRunning = true;
    private BlockingQueue<Tuple> queue;
    private AtomicLong total;

    public SourceWorker(Source source, SourceConfig config) {
        this.source = source;
        this.config = config;
        this.httpClient = HttpClientBuilder.create().setDefaultRequestConfig(RequestConfig.custom()
                .setConnectionRequestTimeout(TIMEOUT_MS)
                .setConnectTimeout(TIMEOUT_MS)
                .setSocketTimeout(TIMEOUT_MS).build()).build();
        executorService = Executors.newSingleThreadExecutor();
        total = new AtomicLong();
    }

    @Override
    public void start() {
        LOGGER.info("source worker starting");
        LOGGER.info("event target is {}", config.getTarget());
        try {
            target = new URI(config.getTarget());
        } catch (URISyntaxException e) {
            throw new RuntimeException(String.format("target is invalid %s", config.getTarget()), e);
        }
        queue = source.queue();
        executorService.execute(this::runLoop);
        LOGGER.info("source worker started");
    }

    @Override
    public void stop() {
        LOGGER.info("source worker stopping");
        executorService.shutdown();
        try {
            source.destroy();
        } catch (Exception e) {
            LOGGER.error("source destroy", e);
        }
        isRunning = false;
        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.error("awaitTermination", e);
        }
        try {
            httpClient.close();
        } catch (IOException e) {
            LOGGER.error("httpClient close", e);
        }
        LOGGER.info("source worker stopped");
    }

    private boolean needAttempt(int attempt) {
        if (config.getSendEventAttempts() <= 0) {
            return true;
        }
        return attempt < config.getSendEventAttempts();
    }

    private MessageWriter createWriter(HttpPost httpPost) {
        return HttpMessageFactory.createWriter(httpPost::addHeader, body -> {
            httpPost.setEntity(new ByteArrayEntity(body));
            try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code!=HTTP_OK && code!=HTTP_ACCEPTED && code!=HTTP_NO_CONTENT) {
                    throw new RuntimeException(String.format("response failed: code %d, body:[%s]", code, EntityUtils.toString(httpResponse.getEntity())));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void sendEvent(CloudEvent event) throws Throwable {
        int attempt = 0;
        Throwable t;
        for (; ; ) {
            attempt++;
            t = null;
            HttpPost httpPost = new HttpPost(target);
            long start = System.currentTimeMillis();
            try {
                createWriter(httpPost).writeStructured(event, JsonFormat.CONTENT_TYPE);
            } catch (Throwable error) {
                t = error;
            }
            long spent = System.currentTimeMillis() - start;
            if (spent > 1000) {
                LOGGER.info("sent event too slow, spent: {}, attempt:{}, eventId:{}", spent, attempt, event.getId());
            }
            if (t==null) {
                LOGGER.info("send event success, total:{}, attempt:{}, eventId:{}", total.addAndGet(1), attempt, event.getId());
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
                LOGGER.info("new event: {}", tuple.getEvent().getId());
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
