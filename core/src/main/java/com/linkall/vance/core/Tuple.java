package com.linkall.vance.core;

import io.cloudevents.CloudEvent;

public class Tuple {
    private CloudEvent event;

    private Callback success;

    private Callback failed;

    public CloudEvent getEvent() {
        return event;
    }

    public void setEvent(CloudEvent event) {
        this.event = event;
    }

    public Callback getSuccess() {
        return success;
    }

    public void setSuccess(Callback success) {
        this.success = success;
    }

    public Callback getFailed() {
        return failed;
    }

    public void setFailed(Callback failed) {
        this.failed = failed;
    }

    public Tuple(CloudEvent event) {
        this.event = event;
    }

    public Tuple(CloudEvent event, Callback success, Callback failed) {
        this.event = event;
        this.success = success;
        this.failed = failed;
    }
}

