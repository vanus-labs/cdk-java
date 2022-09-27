/*
 * Copyright 2022-Present The Vance Authors
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.linkall.vance.common.config;

import com.linkall.vance.common.constant.ConfigConstant;
import com.linkall.vance.common.constant.DefaultValues;
import io.vertx.core.json.JsonArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class ConfigUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigUtil.class);
    /**
     * Get a config value according to a specific key
     * @param key
     * @return config value; return null if key is doesn't exist.
     */
    public static String getString(String key){
        String ret;
        ret = System.getenv(key.toUpperCase());
        //read from userConfig if env doesn't exist
        if(null == ret && null!= ConfigLoader.getUserConfig()){
            ret = ConfigLoader.getUserConfig().getString(key);
        }
        return ret;
    }

    public static int getInt(String key){
        int ret = -1;
        if(null != ConfigLoader.getUserConfig()){
            ret = ConfigLoader.getUserConfig().getInteger(key);
        }
        return ret;
    }

    public static List<String> getStringArray(String key){
        String ret = System.getenv(key.toUpperCase());
        if(null != ret){
            if(!ret.contains(",")){
                LOGGER.error("env "+key+" doesn't have an array Value. It must have a comma to split elements");
            }else{
                String[] strArr = ret.split(",");
                List<String> arr = new ArrayList<>(strArr.length);
                for (String s: strArr) {
                    arr.add(s);
                }
                return arr;
            }
        }
        if(null!= ConfigLoader.getUserConfig()){
            JsonArray jsonArray = ConfigLoader.getUserConfig().getJsonArray(key);
            List<String> arr = new ArrayList<>(jsonArray.size());
            jsonArray.forEach((event)->{
                arr.add(event.toString());
            });
            return arr;
        }

        return null;
    }

    /**
     * Same as {@link ConfigUtil#getString(String)}. This method retrieves data from env and config first.
     * If this method cannot find value from above positions, it will try to get
     * a default value in the SDK.
     * <p></p>
     * Note: Users should choose {@link ConfigUtil#getString(String)} to use
     * since getting a not existed default value may throw an exception.
     * @param name
     * @return
     */
    public static String getEnvOrConfigOrDefault(String name){
        String ret = getString(name);
        if(null == ret){
            ret = DefaultValues.data.get(name.toLowerCase());
        }
        return ret;
    }

    /**
     * Get config-path, it could either be a user-set env or default config-path value
     * @return
     */
    public static String getConfigPath(){
        String path =  System.getenv(ConfigConstant.VANCE_CONFIG_PATH.toUpperCase());
        if(null == path){
            path = DefaultValues.data.get(ConfigConstant.VANCE_CONFIG_PATH.toLowerCase());
        }
        return path;
    }
    /**
     * Get vance_sink
     * @return
     */
    public static String getVanceSink(){
        return getEnvOrConfigOrDefault(ConfigConstant.VANCE_SINK);
    }
    /**
     * Get the port the process needs to listen to.
     * @return
     */
    public static String getPort(){
        return getEnvOrConfigOrDefault(ConfigConstant.VANCE_PORT);
    }

    public static String getKVStore(){ return getEnvOrConfigOrDefault(ConfigConstant.VANCE_KV); }
}
