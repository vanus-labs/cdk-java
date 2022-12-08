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
package com.linkall.vance.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.net.URL;


public class ConfigUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigUtil.class);

    public static Config parse(Class<? extends Config> c) throws Exception {
        if (c==null) {
            return null;
        }
        Config cfg = parseConfig(c);
        if (cfg.secretClass()!=null) {
            Object secret = null;
            try {
                secret = parseSecret(cfg.secretClass());
            } catch (FileNotFoundException e) {
                LOGGER.warn("ignored: no secret.yml");
                return cfg;
            }
            for (Method method : c.getMethods()) {
                Class[] clazz = method.getParameterTypes();
                if (clazz!=null && clazz.length==1 && clazz[0].equals(cfg.secretClass())) {
                    method.invoke(cfg, secret);
                }
            }
            LOGGER.warn("config no exist secret type {} property", cfg.secretClass());
            return cfg;
        }
        return cfg;
    }

    private static Config parseConfig(Class<? extends Config> c) throws Exception {
        String configFile = System.getenv(Constants.ENV_CONFIG_FILE);
        if (configFile==null || configFile.isEmpty()) {
            configFile = "config.yaml";
        }
        return (Config) parse(c, configFile);
    }

    private static Object parseSecret(Class c) throws Exception {
        String secretFile = System.getenv(Constants.ENV_SECRET_FILE);
        if (secretFile==null || secretFile.isEmpty()) {
            secretFile = "secret.yaml";
        }
        return parse(c, secretFile);
    }

    private static Object parse(Class c, String filePathName) throws Exception {
        ObjectMapper objectMapper;
        if (filePathName.endsWith("json")) {
            objectMapper = new ObjectMapper();
        } else {
            objectMapper = new ObjectMapper(new YAMLFactory());
        }
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        File file = new File(filePathName);
        if (file.exists()) {
            return objectMapper.readValue(file, c);
        }
        URL url = ConfigUtil.class.getClassLoader().getResource(filePathName);
        if (url==null) {
            throw new FileNotFoundException(filePathName);
        }
        return objectMapper.readValue(url, c);
    }
}
