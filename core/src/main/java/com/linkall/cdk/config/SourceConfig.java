package com.linkall.cdk.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class SourceConfig extends Config {

    @JsonProperty("target")
    private String target;

    @JsonProperty("send_event_attempts")
    private Integer sendEventAttempts;

    @JsonProperty("vanus")
    private Vanus vanus;

    @JsonProperty("batch_size")
    private int batchSize;

    public String getTarget() {
        if (target!=null && !target.isEmpty()) {
            return target;
        }
        return System.getenv(Constants.ENV_TARGET);
    }

    public Integer getSendEventAttempts() {
        if (sendEventAttempts==null) {
            return Constants.DEFAULT_ATTEMPT;
        }
        return sendEventAttempts;
    }

    public Vanus getVanusConfig() {
        return this.vanus;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getBatchSize() {
        return this.batchSize;
    }
}
