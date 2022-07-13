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
package com.linkall.vance.common.env;

import com.linkall.vance.common.constant.ConfigConstant;
import com.linkall.vance.common.constant.DefaultValues;
import com.sun.tools.doclint.Env;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;

public class EnvUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnvUtil.class);
    /**
     * Get users' config value according to a specific key
     * @param key
     * @return config value; return null if key is doesn't exist.
     */
    public static String getConfig(String key){
        return getEnvOrConfig(key);
    }

    /**
     * Get decoded value of users' secrets according to a specific key.
     * @param key
     * @return decoded secret value; return null if key doesn't exist.
     */
    public static String getSecret(String key){
        if(!key.startsWith("s_") && !key.startsWith("S_")){
            LOGGER.error("Secret key must start with s_");
            return null;
        }
        String ret;
        ret = System.getenv(key.toUpperCase());
        if(null == ret && null!= ConfigLoader.getUserSecret()){
            ret = ConfigLoader.getUserSecret().getString(key.toLowerCase());
        }
        return decodeBase64(ret);
    }
    private static String decodeBase64(String base64Str){
        if(null == base64Str) return null;
        String decoded="";
        byte[] base64Data = DatatypeConverter.parseBase64Binary(base64Str);
        try {
            decoded = new String(base64Data, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return decoded;
    }
    /**
     * EnvUtil retrieves data following the order of (1. user-set env, 2. user-set config)
     * So, the result value might either be a user-set variable or an value from the config file.
     * Note: This method may return a null if it cannot find data either in envs or configs.
     * @param name
     * @return
     */
    public static String getEnvOrConfig(String name){
        String ret;
        ret = System.getenv(name.toUpperCase());
        //read from userConfig if env doesn't exist
        if(null == ret && null!= ConfigLoader.getUserConfig()){
            ret = ConfigLoader.getUserConfig().getString(name.toLowerCase());
        }
        return ret;
    }

    /**
     * Same as {@link EnvUtil#getEnvOrConfig(String)}. This method retrieves data from env and config first.
     * If this method cannot find value from above positions, it will try to get
     * a default value in the SDK.
     * <p></p>
     * Note: Users should choose {@link EnvUtil#getEnvOrConfig(String)} to use
     * since getting a not existed default value may throw an exception.
     * @param name
     * @return
     */
    public static String getEnvOrConfigOrDefault(String name){
        String ret = getEnvOrConfig(name);
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
