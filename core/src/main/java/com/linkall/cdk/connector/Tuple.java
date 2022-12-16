package com.linkall.cdk.connector;

import io.cloudevents.CloudEvent;

public class Tuple {
    private CloudEvent event;

    private SuccessCallback success;

    private FailedCallback failed;

    public CloudEvent getEvent() {
        return event;
    }

    public void setEvent(CloudEvent event) {
        this.event = event;
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

    public Tuple(CloudEvent event) {
        this.event = event;
    }

    public Tuple(CloudEvent event, SuccessCallback success, FailedCallback failed) {
        this.event = event;
        this.success = success;
        this.failed = failed;
    }
}

