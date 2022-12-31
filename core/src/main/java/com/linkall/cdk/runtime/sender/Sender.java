package com.linkall.cdk.runtime.sender;

import io.cloudevents.CloudEvent;

import java.io.IOException;
import java.util.List;

public interface Sender {
    void sendEvents(List<CloudEvent> events) throws Throwable;

    void close() throws IOException;
}
