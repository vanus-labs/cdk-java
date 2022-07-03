package com.linkall.vance.common.store;

import com.google.common.io.Files;
import com.linkall.vance.common.env.EnvUtil;
import com.linkall.vance.common.file.GenericFileUtil;
import com.linkall.vance.core.KVStore;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class KVStoreFactory {
    private static JsonObject stores = new JsonObject(GenericFileUtil.readResource("kvstore.json"));

    public static KVStore createKVStore()  {
        String name = stores.getString(EnvUtil.getKVStore());
        Class<?> c = null;
        try {
            c = Class.forName(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        KVStore instance = null;
        try {
            instance = ((KVStore) c.newInstance());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return instance;
    }

    public static void main(String[] args) {
        KVStore kv = KVStoreFactory.createKVStore();
        //kv.put("aaa","bbb");
        System.out.println(kv.get("aaa"));
        kv.put("aaa","bbb");
        System.out.println(kv.get("aaa"));
        kv.put("bbb","ccc");
        System.out.println(kv.get("bbb"));
    }
}
