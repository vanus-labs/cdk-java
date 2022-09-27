package com.linkall.vance.common.config;

import com.linkall.vance.common.annotation.Tag;

public class SourceConfig {

    @Tag(key = "v_port")
    private int port;

    @Tag(key = "v_target")
    private String target;

    public SourceConfig() {
    }


    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }


    @Override
    public String toString() {
        return "SourceConfig{" +
                "port='" + port + '\'' +
                ", target='" + target + '\'' +
                '}';
    }
}
