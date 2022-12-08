package com.linkall.vance.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SinkConfig extends Config {

    @JsonProperty("v_port")
    private Integer port;

    public Integer getPort() {
        if (port!=null) {
            return port;
        }
        String portStr = System.getenv(Constants.ENV_PORT);
        if (portStr!=null && !portStr.isEmpty()) {
            try {
                Integer port = Integer.parseInt(portStr);
                return port;
            } catch (Exception e) {
            }
        }
        return 8080;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
