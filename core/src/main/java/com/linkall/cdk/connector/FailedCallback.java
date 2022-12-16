package com.linkall.cdk.connector;

@FunctionalInterface
public interface FailedCallback {
    void call(String msg);
}
