package com.linkall.cdk.store;

import com.linkall.cdk.config.StoreConfig;

public class KVStoreFactory {
    private static KVStore kvStore;
    private static StoreConfig storeConfig;

    public static void setStoreConfig(StoreConfig storeConfig) {
        KVStoreFactory.storeConfig = storeConfig;
    }

    public synchronized static KVStore createKVStore() throws Exception {
        if (kvStore == null) {
            initKVStore();
        }
        return kvStore;
    }

    public static void initKVStore() throws Exception {
        if (storeConfig == null) {
            kvStore = new MemoryKVStoreImpl();
            return;
        }
        switch (storeConfig.getType()) {
            case FILE:
                kvStore = new FileKVKVStoreImpl(storeConfig.getStoreFile());
                return;
            default:
                kvStore = new MemoryKVStoreImpl();
                return;
        }
    }
}
