package com.linkall.vance.core;

public interface KVStore {

    void put(String key, String value);

    String get(String key);
}
