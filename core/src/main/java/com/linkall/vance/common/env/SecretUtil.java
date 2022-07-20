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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

public class SecretUtil {

    /**
     * Get decoded value of users' secrets according to a specific key.
     * @param key
     * @return decoded secret value; return null if key doesn't exist.
     */
    public static String getString(String key){
        String ret;
        ret = System.getenv(key.toUpperCase());
        if(null == ret && null!= ConfigLoader.getUserSecret()){
            ret = ConfigLoader.getUserSecret().getString(key);
        }
        if (null == ret) return null;
        return new String(Base64.getDecoder().decode(ret));
    }


}
