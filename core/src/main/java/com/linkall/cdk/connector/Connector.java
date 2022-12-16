package com.linkall.cdk.connector;

import com.linkall.cdk.config.Config;

public interface Connector {

    Class<? extends Config> configClass();

    void initialize(Config config) throws Exception;

    String name();

    void destroy() throws Exception;
}
