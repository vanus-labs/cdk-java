package com.linkall.cdk.util;

import io.cloudevents.CloudEvent;
import io.cloudevents.jackson.JsonFormat;

public class EventUtil {
    private static final JsonFormat eventFormat = new JsonFormat();
    public static String eventToJson(CloudEvent event){
        return new String(eventFormat.serialize(event));
    }
}
