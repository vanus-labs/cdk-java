package com.linkall.vance.common.store;

import com.linkall.vance.common.constant.ConfigConstant;
import com.linkall.vance.common.config.ConfigUtil;
import com.linkall.vance.core.KVStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.*;

public class FileKVStoreImpl implements KVStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileKVStoreImpl.class);
    private static String fileName = ConfigUtil.getEnvOrConfigOrDefault(ConfigConstant.VANCE_KV_FILE);
    private static File f = new File(fileName);
    private static Connection connection = null;
    private static Statement statement = null;
    static{
        if(f.exists()){
            try {
                connection = DriverManager.getConnection("jdbc:sqlite:"+fileName);
                statement = connection.createStatement();
                statement.setQueryTimeout(30);
                statement.executeUpdate("create table if not exists data (key string primary key, value string)");

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }else{
            LOGGER.info("File <"+fileName+"> doesn't exist.\n" +
                    "Please mount a file or choose other store implementation");
        }
    }

    @Override
    public void put(String key, String value) {
        try {
            statement.executeUpdate("insert into data values('"+key+"', '"+value+"')"+
                    "on conflict(key)"+
                    "do update set value = '"+ value+"'");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public String get(String key) {
        ResultSet rs = null;
        try {
            rs = statement.executeQuery("select value from data where key='"+key+"'");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        String s=null;
        try {
            if(rs.next()){
                s = rs.getString("value");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return s;
    }
}
