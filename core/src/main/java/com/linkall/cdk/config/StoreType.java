package com.linkall.cdk.config;

public enum StoreType {
    MEMORY("memory"),
    FILE("file");
    private String name;

    StoreType(String name) {
        this.name = name;
    }
}
