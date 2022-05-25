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
package com.linkall.core.http;

import com.linkall.common.env.EnvUtil;
import com.linkall.core.Adapter;
import com.linkall.core.Adapter2;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.message.MessageReader;
import io.cloudevents.http.vertx.VertxMessageFactory;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HttpServerImpl implements HttpServer{
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerImpl.class);
    public static final Vertx vertx = Vertx.vertx();
    private final io.vertx.core.http.HttpServer server = vertx.createHttpServer();
    private final Router router = Router.router(vertx);
    private HttpResponseInfo ceHandlerRI = new HttpResponseInfo(200,
            "receive CloudEvent success",
            500,
            "invalid CloudEvent format");
    private HttpResponseInfo simHandlerRI = new HttpResponseInfo(200,
            "receive success, deliver CloudEvent to "+EnvUtil.getVanceSink()+" success",
            500,
            "receive success, deliver CloudEvent to "+EnvUtil.getVanceSink()+" failed");

    public HttpServerImpl() {

    }
    public void init(){
        server.requestHandler(router);
    }

    @Override
    public <T extends Handler<RoutingContext> & Adapter> void handler(T handler) {
        handler("/",handler);
    }
    @Override
    public <T extends Handler<RoutingContext> & Adapter> void handler(String path, T handler){
        router.route(path).handler(handler);
    }

    @Override
    public void simpleHandler(Adapter2<HttpServerRequest, Buffer> adapter) {
        simpleHandler(adapter,null);
    }

    @Override
    public void simpleHandler(Adapter2<HttpServerRequest, Buffer> adapter,  HttpResponseInfo info) {
        router.route("/").handler(context->{
            HttpServerRequest request = context.request();
            request.bodyHandler(buffer->{
                CloudEvent ce = adapter.adapt(request,buffer);
                boolean ret = HttpClient.deliver(ce);
                String vanceSink = EnvUtil.getVanceSink();
                HttpResponseInfo i= info;
                if(null == info){
                    i = simHandlerRI;
                }

                request.response().setStatusCode(i.getSuccessCode());
                request.response().end(i.getSuccessChunk());

            });
        });
    }

    @Override
    public void ceHandler(Handler<CloudEvent> handler) {
        ceHandler(handler,null);
    }

    @Override
    public void ceHandler(Handler<CloudEvent> handler,  HttpResponseInfo info) {
        router.route("/").handler(context->{
            HttpServerRequest request = context.request();
            VertxMessageFactory.createReader(request)
                    .map(MessageReader::toEvent)
                    .onSuccess(handler)
                    .onSuccess(ce->{
                        HttpResponseInfo i= info;
                        if(null == info){
                            i = ceHandlerRI;
                        }
                        request.response().setStatusCode(i.getSuccessCode());
                        request.response().end(i.getSuccessChunk());
                    })
                    .onFailure(t->{
                        LOGGER.error("Receive a non-CloudEvent data");
                        HttpResponseInfo i= info;
                        if(null == info){
                            i = ceHandlerRI;
                        }
                        request.response().setStatusCode(i.getFailureCode());
                        request.response().end(i.getFailureChunk());
                    });
        });
    }

    @Override
    public void listen() {
        int port = Integer.parseInt(EnvUtil.getPort());
        server.listen(port, server -> {
            if (server.succeeded()) {
                LOGGER.info("HttpServer is listening on port: "+server.result().actualPort());
            }else{
                LOGGER.error(server.cause().getMessage());
            }
        });
    }

}
