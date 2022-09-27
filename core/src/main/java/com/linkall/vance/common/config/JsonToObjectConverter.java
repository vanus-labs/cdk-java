package com.linkall.vance.common.config;

import com.linkall.vance.common.annotation.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public class JsonToObjectConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonToObjectConverter.class);

    public Object getObjectFromJson(Object object) throws IllegalAccessException {
        Class<?> clazz = object.getClass();
        for(Field field : clazz.getDeclaredFields()){
            field.setAccessible(true);
            if(field.isAnnotationPresent(Tag.class)){
                String key = getKey(field);
                Class<?> type = field.getType();
                if(type.getName() == "int"){
                    int value = ConfigUtil.getInt(key);
                    field.set(object, value);
                }else if(type.getName() == "java.lang.String"){
                    String value = ConfigUtil.getString(key);
                    field.set(object, value);
                }else{
                    LOGGER.error("The data type is not supported.");
                }
            }
        }
        return object;
    }

    private String getKey(Field field){
        String key = field.getAnnotation(Tag.class).key();
        return key.isEmpty() ? field.getName() : key;
    }

    public static void main(String[] args) throws Exception {
        JsonToObjectConverter converter = new JsonToObjectConverter();
        SourceConfig config = new SourceConfig();
        config = (SourceConfig) converter.getObjectFromJson(config);
        System.out.println(config);
    }

}
