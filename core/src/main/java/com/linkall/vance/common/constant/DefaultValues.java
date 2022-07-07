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

import java.util.HashMap;
import static com.linkall.vance.common.constant.ConfigConstant.*;

public class DefaultValues {
    public static HashMap<String,String> data = new HashMap<>();
    static {
        data.put(VANCE_SINK,VANCE_SINK_DV);
        data.put(VANCE_PORT,VANCE_PORT_DV);
        data.put(VANCE_CONFIG_PATH,VANCE_CONFIG_PATH_DV);
        data.put(VANCE_KV_FILE,VANCE_KV_FILE_DV);
        data.put(VANCE_KV,VANCE_KV_DV);
        data.put(ETCD_URL,ETCD_URL_DV);
    }
}
