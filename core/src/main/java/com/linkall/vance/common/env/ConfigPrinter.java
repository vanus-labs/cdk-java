package com.linkall.vance.common.env;

import com.linkall.vance.common.constant.ConfigConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigPrinter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigPrinter.class);
    public static void printVanceConf(){
        LOGGER.info("vance configs-"+ ConfigConstant.VANCE_SINK+": "+EnvUtil.getVanceSink());
        LOGGER.info("vance configs-"+ ConfigConstant.VANCE_PORT+": "+EnvUtil.getPort());
        LOGGER.info("vance configs-"+ ConfigConstant.VANCE_CONFIG_PATH+": "+EnvUtil.getConfigPath());
    }
}
