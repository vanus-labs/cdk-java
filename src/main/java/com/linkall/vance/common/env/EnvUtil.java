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

public class EnvUtil {
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

}
