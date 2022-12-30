package com.linkall.cdk.runtime.sender;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.message.MessageWriter;
import io.cloudevents.http.HttpMessageFactory;
import io.cloudevents.jackson.JsonFormat;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

import static java.net.HttpURLConnection.*;

public class HTTPSender implements Sender{
    private final CloseableHttpClient httpClient;
    private static final int TIMEOUT_MS = 10_000;
    private String target;

    public HTTPSender(String target) {
        this.target = target;
        this.httpClient = HttpClientBuilder.create().setDefaultRequestConfig(RequestConfig.custom()
                .setConnectionRequestTimeout(TIMEOUT_MS)
                .setConnectTimeout(TIMEOUT_MS)
                .setSocketTimeout(TIMEOUT_MS).build()).build();
    }

    @Override
    public void sendEvents(CloudEvent[] events) {
        if ((events.length) == 0) {
            return;
        }
        HttpPost httpPost = new HttpPost(this.target);
        createWriter(httpPost).writeStructured(events[0], JsonFormat.CONTENT_TYPE);
    }

    @Override
    public void close() {}

    private MessageWriter createWriter(HttpPost httpPost) {
        return HttpMessageFactory.createWriter(httpPost::addHeader, body -> {
            httpPost.setEntity(new ByteArrayEntity(body));
            try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code!=HTTP_OK && code!=HTTP_ACCEPTED && code!=HTTP_NO_CONTENT) {
                    throw new RuntimeException(String.format("response failed: code %d, body:[%s]", code, EntityUtils.toString(httpResponse.getEntity())));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
