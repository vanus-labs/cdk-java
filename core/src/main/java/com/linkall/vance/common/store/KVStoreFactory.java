package com.linkall.vance.common.store;

import com.linkall.vance.common.env.EnvUtil;
import com.linkall.vance.common.file.GenericFileUtil;
import com.linkall.vance.core.KVStore;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class KVStoreFactory {
    private static JsonObject stores = new JsonObject(GenericFileUtil.readResource("kvstore.json"));
    private static HashMap<String,Class<?>> classMap = new HashMap<>();
    private static HashMap<String,KVStore> instances = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(KVStoreFactory.class);

    private static Class<?> getOrCreateClazz(String name){
        if(classMap.containsKey(name)){
            return classMap.get(name);
        }else{
            Class<?> c = null;
            try {
                c = Class.forName(name);
            } catch (ClassNotFoundException e) {
                LOGGER.error(e.getMessage());
                e.printStackTrace();
            }
            classMap.put(name,c);
            return c;
        }
    }

    private static KVStore getOrCreateInstance(String name){
        if(instances.containsKey(name)){
            return instances.get(name);
        }else{
            KVStore kv = null;
            Class<?> c = getOrCreateClazz(name);
            try {
                kv = ((KVStore) c.newInstance());
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            instances.put(name,kv);
            return kv;
        }
    }

    public static KVStore createKVStore()  {
        return getOrCreateInstance(stores.getString(EnvUtil.getKVStore()));
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
