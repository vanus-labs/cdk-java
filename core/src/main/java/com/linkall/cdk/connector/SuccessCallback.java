package com.linkall.cdk.connector;

@FunctionalInterface
public interface SuccessCallback {
    void call() throws InterruptedException;
}
