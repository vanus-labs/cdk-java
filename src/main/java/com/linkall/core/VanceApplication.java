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
package com.linkall.core;

import com.linkall.common.env.ConfigPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VanceApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(VanceApplication.class);
    public static void run (Class<? extends Connector> conClass){
        Connector connector = null;
        try {
            connector = conClass.newInstance();
        } catch (InstantiationException e) {
            LOGGER.info("generate connector instance failed");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            LOGGER.info("connector instance access failed");
            e.printStackTrace();
        }
        if(null!=connector){
            Adapter adapter = connector.getAdapter();
            boolean targetFound = adapter instanceof Adapter1;
            if(!targetFound) targetFound = adapter instanceof Adapter2;
            if(!targetFound){
                LOGGER.error("getAdapter() must return an instance either of Adapter1 or Adapter2");
            }else{
                ConfigPrinter.printVanceConf();
                try {
                    connector.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            /*boolean targetFound = false;
            targetFound = adapter instanceof Adapter2;
            adapter instanceof Adapter1*/
        }else{
            LOGGER.error("Vance app launches failed, connector instance is null");
        }
    }
}
