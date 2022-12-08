package com.linkall.vance.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class SourceConfig extends Config {

    @JsonProperty("v_target")
    private String target;

    @JsonProperty("send_event_attempts")
    private Integer sendEventAttempts;

    public String getTarget() {
        if (target!=null && !target.isEmpty()) {
            return target;
        }
        return System.getenv(Constants.ENV_TARGET);
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Integer getSendEventAttempts() {
        if (sendEventAttempts==null) {
            return Constants.DEFAULT_ATTEMPT;
        }
        return sendEventAttempts;
    }

    public void setSendEventAttempts(Integer sendEventAttempts) {
        this.sendEventAttempts = sendEventAttempts;
    }
}
