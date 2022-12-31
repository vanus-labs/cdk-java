package com.linkall.cdk.store;

public interface KVStore {

    void set(String key, byte[] value) throws Exception;

    byte[] get(String key);

    void delete(String key) throws Exception;

    void close();
}


