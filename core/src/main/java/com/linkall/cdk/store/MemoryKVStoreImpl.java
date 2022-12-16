package com.linkall.cdk.store;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryKVStoreImpl implements KVStore {

    protected Map<String, byte[]> data = new ConcurrentHashMap<>();

    @Override
    public void set(String key, byte[] value) throws Exception {
        data.put(key, value);
        save();
    }

    @Override
    public byte[] get(String key) {
        return data.get(key);
    }

    @Override
    public void delete(String key) throws Exception {
        data.remove(key);
        save();
    }

    @Override
    public void close() {
        data.clear();
    }

    public void save() throws Exception {

    }
}
