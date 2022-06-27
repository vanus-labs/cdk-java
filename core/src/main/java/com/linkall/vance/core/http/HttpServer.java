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

import com.linkall.vance.core.Adapter;
import com.linkall.vance.core.Adapter2;
import io.cloudevents.CloudEvent;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

/**
 * A wrapped vert.x HTTP server.
 * You receive HTTP requests by providing a requestHandler.
 * As requests arrive on the server the handler will be called with the requests.
 */
public interface HttpServer {
    static HttpServer createHttpServer() {
        HttpServerImpl httpServer = new HttpServerImpl();
        httpServer.init();
        return httpServer;
    }

    /**
     * This method registers a handler which both handles requests and adapters.
     * <p></p>
     * Note: if you don't want to handle requests, you should use
     * {@link HttpServer#simpleHandler(Adapter2)}
     * to simply tell SDK how to generate a CloudEvent caring about nothing else.
     * @param handler
     * @param <T> extends {@link Handler} and {@link Adapter}
     */
    <T extends Handler<RoutingContext> & Adapter>void handler(T handler);
    //route to a specific path
    <T extends Handler<RoutingContext> & Adapter> void handler(String path,T handler);

    /**
     * simpleHandler method helps you to handle incoming requests and deliver
     * CloudEvents to your target.
     * The only thing you need to provide is an adapter to specify how to
     * transform requests into a CloudEvent.
     * <p></p>
     * Also see {@link HttpServer#simpleHandler(Adapter2, HttpResponseInfo)} if
     * you want to specify your response details.
     * @param adapter
     */
    void simpleHandler(Adapter2<HttpServerRequest, Buffer> adapter);

    /**
     * Same as {@link HttpServer#simpleHandler(Adapter2)} but allows users to
     * specify their response details.
     * @param adapter
     * @param info
     */
    void simpleHandler(Adapter2<HttpServerRequest, Buffer> adapter, HttpResponseInfo info);

    /**
     * If your HttpServe only receives CloudEvents, you can use this method to handle
     * received CloudEvents.
     * <p></p>
     * Also see {@link HttpServer#ceHandler(Handler, HttpResponseInfo)} if
     * you want to specify your response details.
     * @param handler
     */
    void ceHandler(Handler<CloudEvent> handler);

    /**
     * Same as {@link HttpServer#ceHandler(Handler)} but allows users to
     * specify their response details.
     * @param handler
     * @param info
     */
    void ceHandler(Handler<CloudEvent> handler,final HttpResponseInfo info);

    void listen();
}
