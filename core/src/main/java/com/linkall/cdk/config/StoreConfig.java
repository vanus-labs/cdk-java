package com.linkall.cdk.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StoreConfig {

    @JsonProperty("type")
    private StoreType type;

    @JsonProperty("pathname")
    private String storeFile;

    public StoreType getType() {
        return type;
    }

    public void setType(StoreType type) {
        this.type = type;
    }

    public String getStoreFile() {
        return storeFile;
    }

    public void setStoreFile(String storeFile) {
        this.storeFile = storeFile;
    }
}
