package com.linkall.cdk.connector;

import io.cloudevents.CloudEvent;

import java.util.LinkedList;
import java.util.List;

public class Tuple {
    private final List<CloudEvent> event = new LinkedList<>();

    private SuccessCallback success;

    private FailedCallback failed;

    public List<CloudEvent> getEventList() {
        return this.event;
    }

    public SuccessCallback getSuccess() {
        return success;
    }

    public void setSuccess(SuccessCallback success) {
        this.success = success;
    }

    public FailedCallback getFailed() {
        return failed;
    }

    public void setFailed(FailedCallback failed) {
        this.failed = failed;
    }

    public Tuple(){}

    public Tuple(CloudEvent event) {
        this.event.add(event);
    }

    public Tuple(CloudEvent event, SuccessCallback success, FailedCallback failed) {
        this.event.add(event);
        this.success = success;
        this.failed = failed;
    }

    public void addEvent(CloudEvent event) {
        this.event.add(event);
    }
}

