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
package com.linkall.vance.common.constant;

/**
 * config-related constants
 */
public class ConfigConstant {
    public final static String VANCE_CONFIG_PATH = "v_config_path";
    public final static String VANCE_CONFIG_PATH_DV = "/vance/config/config.json";
    public final static String VANCE_SINK = "v_target";
    public final static String VANCE_SINK_DV = "v_target";
    public final static String VANCE_PORT = "v_port";
    public final static String VANCE_PORT_DV = "8080";
    public final static String VANCE_KV = "v_store";
    public final static String VANCE_KV_DV = KVImpl.LOCAL_KV.getValue();
    public final static String VANCE_KV_FILE = "v_store_file";
    public final static String VANCE_KV_FILE_DV = "/vance/data/data.file";
    public final static String ETCD_URL = "etcd_url";
    public final static String ETCD_URL_DV = "http://localhost:2379";
}
