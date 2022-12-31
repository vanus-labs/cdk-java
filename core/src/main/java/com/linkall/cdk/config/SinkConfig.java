package com.linkall.cdk.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SinkConfig extends Config {

    @JsonProperty("port")
    private Integer port;

    private Integer grpcPort;

    public Integer getPort() {
        if (port != null) {
            return port;
        }
        String portStr = System.getenv(Constants.ENV_PORT);
        if (portStr != null && !portStr.isEmpty()) {
            try {
                Integer port = Integer.parseInt(portStr);
                return port;
            } catch (Exception e) {
            }
        }
        return Constants.DEFAULT_PORT;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getGRPCPort() {
        return this.grpcPort;
    }
}
