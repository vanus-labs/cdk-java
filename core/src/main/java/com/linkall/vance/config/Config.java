package com.linkall.vance.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class Config {

    @JsonProperty("store_config")
    private StoreConfig storeConfig;

    public Class<?> secretClass() {
        return null;
    }

    public StoreConfig getStoreConfig() {
        return storeConfig;
    }

    public void setStoreConfig(StoreConfig storeConfig) {
        this.storeConfig = storeConfig;
    }
}