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

import com.linkall.vance.config.Config;
import com.linkall.vance.config.ConfigUtil;
import com.linkall.vance.config.SinkConfig;
import com.linkall.vance.config.SourceConfig;
import com.linkall.vance.core.runtime.ConnectorWorker;
import com.linkall.vance.core.runtime.http.SinkWorker;
import com.linkall.vance.core.runtime.http.SourceWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VanceApplication {
    private static final String SRC_INTERFACE_NAME = "interface com.linkall.vance.core.Source";
    private static final String SINK_INTERFACE_NAME = "interface com.linkall.vance.core.Sink";
    private static final Logger LOGGER = LoggerFactory.getLogger(VanceApplication.class);

    public static void run(Class<? extends Connector> clazz) {
        Connector connector;
        try {
            connector = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            LOGGER.error("new connector error", e);
            return;
        }
        Config config;
        try {
            config = ConfigUtil.parse(connector.configClass());
        } catch (Exception e) {
            LOGGER.error("parse config error", e);
            return;
        }
        try {
            connector.initialize(config);
        } catch (Exception e) {
            LOGGER.error("connector {} initialize error", connector.name(), e);
            return;
        }

        ConnectorWorker worker;
        if (isSink(clazz)) {
            worker = new SinkWorker((Sink) connector, (SinkConfig) config);
        } else if (isSource(clazz)) {
            worker = new SourceWorker((Source) connector, (SourceConfig) config);
        } else {
            LOGGER.error("class {} is not sink and source", clazz);
            return;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            worker.stop();
            LOGGER.info("connector {} stopped", connector.name());
        }));
        worker.start();
        LOGGER.info("connector {} started", connector.name());
    }

    private static boolean isAssignableFrom(Class c, Class cls) {
        Class[] clazzArr = c.getInterfaces();
        if (clazzArr!=null) {
            for (Class clazz : clazzArr) {
                if (clazz.isAssignableFrom(cls)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isSink(Class c) {
        while (c!=null && c!=Object.class) {
            if (isAssignableFrom(c, Sink.class)) {
                return true;
            }
            c = c.getSuperclass();
        }
        return false;
    }

    private static boolean isSource(Class c) {
        while (c!=null && c!=Object.class) {
            if (isAssignableFrom(c, Source.class)) {
                return true;
            }
            c = c.getSuperclass();
        }
        return false;
    }
}
