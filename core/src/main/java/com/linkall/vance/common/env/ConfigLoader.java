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

import com.linkall.vance.common.file.GenericFileUtil;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ConfigLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigLoader.class);
    public static JsonObject userConfig = null;
    static{
        String configPath = EnvUtil.getConfigPath();
        try {
            userConfig =  new JsonObject(GenericFileUtil.readFile(configPath));
        } catch (IOException e) {
            //System.out.println("READ user config failed");
            LOGGER.info("READ user config<"+configPath+"> failed");
        }
        if(null == userConfig){
            String localRes = GenericFileUtil.readResource("config.json");
            if(null !=localRes) userConfig = new JsonObject(localRes);
        }
    }

    /**
     * load user config("/vance/config.json") and transform content to a JsonObject
     * if "/vance/config.json" doesn't exist
     * then load local resource "resources/config.json"
     * @return JsonObject of the user config, It can be true, if we cannot find config files
     */
    public static JsonObject getUserConfig(){
        return userConfig;
    }
}
