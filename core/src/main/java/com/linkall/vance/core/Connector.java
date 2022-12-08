package com.linkall.vance.core;

import com.linkall.vance.config.Config;

import java.io.IOException;

public interface Connector {

    Class<? extends Config> configClass();

    void initialize(Config config) throws Exception;

    String name();

    void destroy() throws Exception;
}
