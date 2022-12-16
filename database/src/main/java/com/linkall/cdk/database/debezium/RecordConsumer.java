package com.linkall.cdk.database.debezium;

import com.linkall.cdk.connector.Tuple;
import com.linkall.cdk.util.EventUtil;
import io.cloudevents.CloudEvent;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import org.apache.kafka.connect.source.SourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class RecordConsumer
        implements DebeziumEngine.ChangeConsumer<ChangeEvent<SourceRecord, SourceRecord>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecordConsumer.class);
    private final BlockingQueue<Tuple> events;
    private final Adapter adapter;


    public RecordConsumer(BlockingQueue<Tuple> events, Adapter adapter) {
        this.adapter = adapter;
        this.events = events;
    }

    @Override
    public void handleBatch(List<ChangeEvent<SourceRecord, SourceRecord>> records,
                            DebeziumEngine.RecordCommitter<ChangeEvent<SourceRecord, SourceRecord>> committer)
            throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(records.size());
        for (ChangeEvent<SourceRecord, SourceRecord> record : records) {
            LOGGER.debug("Received event '{}'", record);
            if (record.value()==null) {
                latch.countDown();
                continue;
            }
            CloudEvent ceEvent = this.adapter.adapt(record.value());
            events.put(new Tuple(ceEvent, () -> {
                commit(latch, record, committer);
            }, (msg) -> {
                LOGGER.error("event send failed:{},{}", msg, EventUtil.eventToJson(ceEvent));
                commit(latch, record, committer);
            }));
        }
        latch.await();
        committer.markBatchFinished();
    }

    private void commit(
            CountDownLatch latch,
            ChangeEvent<SourceRecord, SourceRecord> record,
            DebeziumEngine.RecordCommitter<ChangeEvent<SourceRecord, SourceRecord>> committer) {
        try {
            committer.markProcessed(record);
        } catch (InterruptedException e) {
            LOGGER.warn(
                    "Failed to mark processed record: {},error: {}",
                    record.value().sourceOffset(),
                    e);
        }
        latch.countDown();
    }
}
