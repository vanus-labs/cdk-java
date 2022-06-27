package com.vance.sink;

import com.linkall.vance.common.json.JsonMapper;
import com.linkall.vance.core.Sink;
import com.linkall.vance.core.http.HttpClient;
import com.linkall.vance.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.linkall.vance.core.Sink;

import java.util.concurrent.atomic.AtomicInteger;

public class MySink implements Sink {

    private static final Logger LOGGER = LoggerFactory.getLogger(MySink.class);
    private static final AtomicInteger eventNum = new AtomicInteger(0);

    @Override
    public void start(){
        // TODO write a HTTP Server which can handle requests based on CloudEvents format
        HttpServer server = HttpServer.createHttpServer();
        // Use ceHandler method to tell HttpServer logics you want to do with an incoming CloudEvent
        server.ceHandler(event -> {
            int num = eventNum.addAndGet(1);
            // print number of received events
            LOGGER.info("receive a new event, in total: "+num);
            // Use JsonMapper to wrap a CloudEvent into a JsonObject for better printing
            JsonObject js = JsonMapper.wrapCloudEvent(event);
            LOGGER.info(js.encodePrettily());
        });
        server.listen();
    }
}
