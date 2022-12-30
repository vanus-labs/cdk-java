package com.linkall.cdk.runtime;

import com.linkall.cdk.config.SourceConfig;
import com.linkall.cdk.connector.Source;
import com.linkall.cdk.connector.Tuple;
import com.linkall.cdk.runtime.sender.HTTPSender;
import com.linkall.cdk.runtime.sender.Sender;
import com.linkall.cdk.runtime.sender.VanusSender;
import com.linkall.cdk.util.Sleep;
import io.cloudevents.CloudEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SourceWorker implements ConnectorWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(SourceWorker.class);

    private final Source source;
    private final SourceConfig config;
    private final ExecutorService executorService;
    private final ExecutorService senderExecutorService;
    private final AtomicLong total;
    private final Sender sender;

    private volatile boolean isRunning = true;
    private BlockingQueue<Tuple> queue;

    public SourceWorker(Source source, SourceConfig config) {
        this.source = source;
        this.config = config;
        this.executorService = Executors.newSingleThreadExecutor();
        this.senderExecutorService =Executors.newSingleThreadExecutor();
        this.total = new AtomicLong();
        if (config.getVanusConfig() != null) {
            this.sender = new VanusSender(config.getVanusConfig().getEndpoint(), config.getVanusConfig().getEventbus());
        } else {
            this.config.setBatchSize(1);
            this.sender = new HTTPSender(config.getTarget());
        }
    }

    @Override
    public void start() {
        LOGGER.info("source worker starting");
        LOGGER.info("event target is {}", config.getTarget());
        queue = source.queue();
        executorService.execute(this::runLoop);
//        senderExecutorService.execute(this::sendLoop);
        LOGGER.info("source worker started");
    }

    @Override
    public void stop() {
        LOGGER.info("source worker stopping");
        executorService.shutdown();
        this.senderExecutorService.shutdown();
        try {
            source.destroy();
        } catch (Exception e) {
            LOGGER.error("source destroy", e);
        }
        isRunning = false;
        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
            senderExecutorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.error("awaitTermination", e);
        }
        try {
            sender.close();
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

    public void runLoop() {
        while (isRunning) {
            try {
                Tuple tuple = queue.poll(5, TimeUnit.SECONDS);
                if (tuple==null) {
                    continue;
                }
                this.doSend(tuple);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void doSend(Tuple tuple) {
        List<CloudEvent> events = tuple.getEventList();

        this.total.addAndGet(events.size());

        int attempt = 0;
        Throwable t;

        CloudEvent[] eventsArr = new CloudEvent[events.size()];

        for (int i = 0; i< events.size();i++) {
            eventsArr[i]=events.get(i);
        }

        for (; ; ) {
            attempt++;
            t = null;
            long start = System.currentTimeMillis();
            try {
                this.sender.sendEvents(eventsArr);
                long spent = System.currentTimeMillis() - start;
                if (spent > 1000) {
                    LOGGER.warn("sent event too slow, spent: {}, attempt:{}, numbers: {}", spent, attempt, eventsArr.length);
                }
                break;
            } catch (Throwable e) {
                t = e;
            }

            if (!isRunning || !needAttempt(attempt)) {
                LOGGER.warn("send event failed, attempt:{}, numbers: {}, attempts: {}, events: {}",
                        attempt, eventsArr.length, attempt, events, t);
                break;
            }
            LOGGER.info("send event has error will retry, attempt:{}, numbers: {}, attempts: {}, error: {}",
                    attempt, eventsArr.length, attempt, t.getMessage());

            try {
                Thread.sleep(Sleep.Backoff(attempt, 5000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (t==null) {
            tuple.getSuccess().call();
        } else {
            tuple.getFailed().call(t.getMessage());
        }
    }
}
