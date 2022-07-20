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
package com.linkall.vance.core.http;

import com.linkall.vance.common.env.ConfigUtil;
import com.linkall.vance.common.net.URITool;
import io.cloudevents.CloudEvent;
import io.cloudevents.http.vertx.VertxMessageFactory;
import io.cloudevents.jackson.JsonFormat;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.impl.headers.HeadersMultiMap;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.slf4j.LoggerFactory;

import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class HttpClient {
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpClient.class);
    private final static WebClientOptions options = new WebClientOptions()
            .setUserAgent("VanceSDK-HttpClient/1.0.0");
    private final static WebClient webClient = WebClient.create(HttpServerImpl.vertx,options);
    private static Handler<HttpResponse<Buffer>> successHandler = null;
    private final static AtomicBoolean ret = new AtomicBoolean(false);
    public static void setDeliverSuccessHandler(Handler<HttpResponse<Buffer>> handler){
        successHandler = handler;
    }

    private static <T>boolean innerDeliver(HttpClientInner is, T data){
        //LOGGER.info("vanceSink "+vanceSink);
        CircuitBreaker breaker = CircuitBreaker.create("my-circuit-breaker", HttpServerImpl.vertx,
                new CircuitBreakerOptions()
                        .setMaxRetries(3)
                        .setTimeout(3000) // consider a failure if the operation does not succeed in time
        );
        breaker.<String>execute(promise -> {
            // LOGGER.info("try to send request");
            Future<HttpResponse<Buffer>> responseFuture = is.send(data);
            /*Future<HttpResponse<Buffer>> responseFuture = VertxMessageFactory.createWriter(webClient.postAbs(vanceSink))
                    .writeStructured(event, JsonFormat.CONTENT_TYPE);*/
            if(null!=successHandler){
                responseFuture.onSuccess(successHandler);
            }
            responseFuture.onSuccess(resp-> {
                promise.complete();
                LOGGER.info("send task success");
                ret.set(true);
            });
            responseFuture.onFailure(System.err::println);
        }).onFailure(t->{
            LOGGER.info("send task failed");
        });

        breaker.close();
        return ret.get();
    }

    public static boolean deliver(JsonObject json){
        String vanceSink = ConfigUtil.getVanceSink();
        HttpClientInner<JsonObject> is = (s)->{
            return webClient.postAbs(vanceSink).sendJsonObject(s);
        };
        return innerDeliver(is,json);
    }
    public static boolean deliver(Buffer buffer){
        String vanceSink = ConfigUtil.getVanceSink();
        HttpClientInner<Buffer> is = (s)->{
            return webClient.postAbs(vanceSink).sendBuffer(s);
        };
        return innerDeliver(is,buffer);
    }
    public static boolean deliver(CloudEvent event){
        String vanceSink = ConfigUtil.getVanceSink();
        HttpClientInner<CloudEvent> is = (s)->{
            return VertxMessageFactory.createWriter(webClient.postAbs(vanceSink))
                    .writeStructured(s, JsonFormat.CONTENT_TYPE);
        };
        return innerDeliver(is,event);
    }
    private static HttpRequest<Buffer> obtainGetReq(String uri){
        HttpRequest<Buffer> req = webClient.get(uri);
        String host = URITool.getHost(URITool.getURI(uri));
        LOGGER.info("Host: "+host);
        if(!URITool.isIP(host)){
            req = req.host(host);
        }
        return req;
    }
    public static Future<HttpResponse<Buffer>> sendGetRequest(String uri){
        return obtainGetReq(uri).send();
    }

    public static Future<HttpResponse<Buffer>> sendGetRequest(String uri, Map<String,String> headers){
        HeadersMultiMap multiMap = HeadersMultiMap.httpHeaders();
        multiMap.addAll(headers);
        multiMap.forEach(e->{
            LOGGER.info("> "+e.getKey()+": "+e.getValue());
        });
        return obtainGetReq(uri).putHeaders(multiMap).send();
    }
}

