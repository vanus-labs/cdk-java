package com.linkall.cdk.connector;

import java.util.List;

@FunctionalInterface
public interface FailedCallback<T> {
    void call(List<Element<T>> success, List<Element<T>> failed, String error) throws InterruptedException;
}
