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

import com.linkall.vance.common.file.GenericFileUtil;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.linkall.vance.common.constant.ConfigConstant.VANCE_SECRET_PATH_DV;

public class ConfigLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigLoader.class);
    public static JsonObject userConfig = null;
    public static JsonObject userSecret = null;
    static{
        String configPath = ConfigUtil.getConfigPath();
        try {
            userConfig =  new JsonObject(GenericFileUtil.readFile(configPath));
        } catch (IOException e) {
            LOGGER.info("READ user config<"+configPath+"> failed");
        }
        if(null == userConfig){
            String localRes = GenericFileUtil.readResource("config.json");
            if(null !=localRes) userConfig = new JsonObject(localRes);
        }
        try {
            userSecret =  new JsonObject(GenericFileUtil.readFile(VANCE_SECRET_PATH_DV));
        } catch (IOException e) {
            LOGGER.info("READ user secret<"+VANCE_SECRET_PATH_DV+"> failed");
        }
        if(null == userSecret){
            String localRes = GenericFileUtil.readResource("secret.json");
            if(null !=localRes) userSecret = new JsonObject(localRes);
        }
    }

    /**
     * Load users' secret file from "/vance/config.json".
     * It will load "resources/config.json" if config file doesn't exist.
     * @return JsonObject of the user config, return null if config doesn't exist.
     */
    public static JsonObject getUserConfig(){
        return userConfig;
    }

    /**
     * Load users' secret file from "/vance/secret/secret.json".
     * It will load "resources/secret.json" if secret file doesn't exist.
     * @return JsonObject of users' secret, return null if secret doesn't exist.
     */
    public static JsonObject getUserSecret(){
        return userSecret;
    }
}
