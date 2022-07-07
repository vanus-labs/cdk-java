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
package com.linkall.vance.core;

import com.linkall.vance.common.env.ConfigPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VanceApplication {
    private static final String SRC_INTERFACE_NAME = "interface com.linkall.vance.core.Source";
    private static final String SINK_INTERFACE_NAME = "interface com.linkall.vance.core.Sink";
    private static final Logger LOGGER = LoggerFactory.getLogger(VanceApplication.class);
    public static void run (Class<? extends Sink> conClass){
        String interfaceName = conClass.getGenericInterfaces()[0].toString();
        Source source = null;
        Sink sink = null;
        if(SRC_INTERFACE_NAME.equals(interfaceName)){
            try {
                source = (Source) conClass.newInstance();
            } catch (InstantiationException e) {
                LOGGER.info("generate connector instance failed");
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                LOGGER.info("connector instance access failed");
                e.printStackTrace();
            }
            Adapter adapter = source.getAdapter();
            boolean targetFound = adapter instanceof Adapter1;
            if(!targetFound) targetFound = adapter instanceof Adapter2;
            if(!targetFound){
                LOGGER.error("getAdapter() must return an instance either of Adapter1 or Adapter2");
                return;
            }
            if(null!=source){
                ConfigPrinter.printVanceConf();
                try {
                    source.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }else if(SINK_INTERFACE_NAME.equals(interfaceName)){
            try {
                sink =  conClass.newInstance();
            } catch (InstantiationException e) {
                LOGGER.info("generate connector instance failed");
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                LOGGER.info("connector instance access failed");
                e.printStackTrace();
            }
            if(null!=sink){
                ConfigPrinter.printVanceConf();
                try {
                    sink.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }else{
            LOGGER.error("the first interface conClass implements must be <com.linkall.core.Sink>. "+
                    "current first interface is: "+interfaceName);
            return;
        }
    }
}
