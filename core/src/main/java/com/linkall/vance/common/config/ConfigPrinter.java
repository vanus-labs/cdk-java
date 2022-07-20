package com.linkall.vance.common.config;

import com.linkall.vance.common.constant.ConfigConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigPrinter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigPrinter.class);
    public static void printVanceConf(){
        LOGGER.info("vance configs-"+ ConfigConstant.VANCE_SINK+": "+ ConfigUtil.getVanceSink());
        LOGGER.info("vance configs-"+ ConfigConstant.VANCE_PORT+": "+ ConfigUtil.getPort());
        LOGGER.info("vance configs-"+ ConfigConstant.VANCE_CONFIG_PATH+": "+ ConfigUtil.getConfigPath());
    }
}
