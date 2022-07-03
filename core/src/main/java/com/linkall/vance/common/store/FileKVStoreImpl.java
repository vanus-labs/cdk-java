package com.linkall.vance.common.store;

import com.google.common.io.Files;
import com.linkall.vance.common.constant.ConfigConstant;
import com.linkall.vance.common.file.GenericFileUtil;
import com.linkall.vance.core.KVStore;
import com.linkall.vance.core.VanceApplication;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FileKVStoreImpl implements KVStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileKVStoreImpl.class);
    private static JsonObject map;
    private static File f = new File(ConfigConstant.VANCE_KV_FILE);
    static{
        if(f.exists()){
            try {
                map = new JsonObject(GenericFileUtil.readFile(ConfigConstant.VANCE_KV_FILE));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            LOGGER.info("File <"+ConfigConstant.VANCE_KV_FILE+"> doesn't exist.\n" +
                    "Please mount a file or choose other store implementation");
            map = new JsonObject();
        }
    }

    @Override
    public void put(String key, String value) {
        map.put(key,value);
        if(f.exists()){
            try {
                Files.write(map.toString().getBytes(StandardCharsets.UTF_8),f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String get(String key) {
        return map.getString(key);
    }
}
