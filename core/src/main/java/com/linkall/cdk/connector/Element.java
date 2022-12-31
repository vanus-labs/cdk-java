package com.linkall.cdk.connector;

import io.cloudevents.CloudEvent;

public class Element<T> {
    private CloudEvent event;
    private T original;

    public Element(CloudEvent event, T ori) {
        this.event = event;
        this.original = ori;
    }

    public CloudEvent getEvent() {
        return event;
    }

    public T getOriginal() {
        return original;
    }
}
