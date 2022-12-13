package com.linkall.vance.example.source;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkall.vance.config.Config;
import com.linkall.vance.core.Source;
import com.linkall.vance.core.Tuple;
import com.linkall.vance.util.EventUtil;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class MySource implements Source {
    private static final Logger LOGGER = LoggerFactory.getLogger(MySource.class);
    private static final CloudEventBuilder template = CloudEventBuilder.v1();
    private final ObjectMapper objectMapper;

    private final BlockingQueue<Tuple> queue;
    private ExampleConfig config;
    private final ScheduledExecutorService executor;
    private int num;

    public MySource() {
        objectMapper = new ObjectMapper();
        queue = new ArrayBlockingQueue<>(100);
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    public CloudEvent makeEvent(int i) throws JsonProcessingException {
        template.withId(UUID.randomUUID().toString());
        URI uri = URI.create(config.getSource());
        template.withSource(uri);
        template.withType("testType");
        template.withDataContentType("application/json");
        template.withTime(OffsetDateTime.now());
        Map<String, Object> data = new HashMap<>();
        data.put("number", i);
        data.put("string", "Event Num " + i);
        template.withData("application/json", objectMapper.writeValueAsBytes(data));
        return template.build();
    }

    public void start() {
        executor.scheduleAtFixedRate(() -> {
            try {
                CloudEvent event = makeEvent(num++);
                queue.put(new Tuple(event, () ->
                        LOGGER.info("send event success {}", EventUtil.eventToJson(event))
                        , () -> LOGGER.info("send event failed {}", EventUtil.eventToJson(event))));
            } catch (Exception e) {
                LOGGER.error("error", e);
            }
        }, 3, 10, TimeUnit.SECONDS);
    }

    @Override
    public Class<? extends Config> configClass() {
        return ExampleConfig.class;
    }

    @Override
    public void initialize(Config config) {
        this.config = (ExampleConfig) config;
        this.num = this.config.getNum();
        start();
    }

    @Override
    public String name() {
        return "ExampleSource";
    }

    @Override
    public void destroy() {
        executor.shutdown();
    }

    @Override
    public BlockingQueue<Tuple> queue() {
        return queue;
    }
}

