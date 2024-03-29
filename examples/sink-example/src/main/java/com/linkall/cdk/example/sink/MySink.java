package com.linkall.cdk.example.sink;

import com.linkall.cdk.config.Config;
import com.linkall.cdk.connector.Result;
import com.linkall.cdk.connector.Sink;
import com.linkall.cdk.util.EventUtil;
import io.cloudevents.CloudEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class MySink implements Sink {

    private static final Logger LOGGER = LoggerFactory.getLogger(MySink.class);
    private AtomicInteger eventNum;

    @Override
    public Class<? extends Config> configClass() {
        return ExampleConfig.class;
    }

    @Override
    public void initialize(Config config) throws Exception {
        ExampleConfig cfg = (ExampleConfig) config;
        eventNum = new AtomicInteger(cfg.getNum());
    }

    @Override
    public String name() {
        return "ExampleSink";
    }

    @Override
    public void destroy() {

    }

    @Override
    public Result Arrived(CloudEvent... events) {
        for (CloudEvent event : events) {
            int num = eventNum.addAndGet(1);
            // print number of received events
            LOGGER.info("receive a new event, in total: " + num);
            LOGGER.info(EventUtil.eventToJson(event));
        }
        return null;
    }
}
