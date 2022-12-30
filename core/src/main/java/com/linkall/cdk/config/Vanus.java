package com.linkall.cdk.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Vanus {
    @JsonProperty("endpoint")
    private String endpoint;

    @JsonProperty("eventbus")
    private String eventbus;

    public String getEventbus() {
        return eventbus;
    }

    public String getEndpoint() {
        return endpoint;
    }
}
