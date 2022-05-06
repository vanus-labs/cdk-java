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
package com.linkall.common.json;

import io.cloudevents.CloudEvent;
import io.vertx.core.json.JsonObject;

public class JsonMapper {
    /**
     * Wrap a CloudEvent into a JsonObject
     * @param event
     * @return
     */
    public static JsonObject wrapCloudEvent(CloudEvent event){
        JsonObject js = new JsonObject();
        js.put("id",event.getId());
        js.put("source",event.getSource());
        js.put("specversion",event.getSpecVersion());
        js.put("type",event.getType());
        if(null !=event.getDataContentType())
            js.put("datacontenttype",event.getDataContentType());
        if(null !=event.getDataSchema())
            js.put("dataschema",event.getDataSchema());
        if(null !=event.getSubject())
            js.put("subject",event.getSubject());
        if(null !=event.getTime())
            js.put("time",event.getTime().toString());
        //System.out.println(event.getDataContentType());
        if("application/json".equals(event.getDataContentType())) {
            JsonObject data = new JsonObject(new String(event.getData().toBytes()));
            js.put("data", data);
        }else{
            js.put("data",new String(event.getData().toBytes()));
        }
        return js;
    }
}
