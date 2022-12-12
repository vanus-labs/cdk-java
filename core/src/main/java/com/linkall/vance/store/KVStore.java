package com.linkall.vance.store;

public interface KVStore {

    void put(String key, byte[] value) throws Exception;

    byte[] get(String key);

    void delete(String key) throws Exception;

    void close() ;
}


