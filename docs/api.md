---
title: Vance API
nav_order: 2
---

# Vance Connector API

## Connector Entrance

The cdk provides an easy way to launch your connector application:

```java
public class Entrance {
    public static void main(String[] args) {
        VanceApplication.run(MyConnector.class);
    }
}
```

`MyConnector` is the implementation of either a [Sink or Source](#connector-interface) interface.

## Connector Interface

The Sink and Source Interfaces reflect the Sink and Source Connectors respectively.

![connector](connector.png)

You have to implement the corresponding interfaces when developing a connector.
```java
public interface Sink {
    // write your codes in this start() method
    void start() throws Exception;
}
```

```java
public interface Source extends Sink{
    // A source connector must implement this method to specify an Adapter to tell how the connector will
    // transform incoming data into a CloudEvent.
    Adapter getAdapter();
}
```

## Adapter Interface

Adapter is an abstract concept used to demonstrate how the Source connector will transform incoming data into
a CloudEvent.

Currently, your concrete Adapter MUST implement either the Adapter1, or the Adapter2 interface.

![adapter](adapter.png)

Choose an appropriate Adapter interface to implement based on the number of your incoming data you need to generate a
CloudEvent.

For example, if the incoming data is a pure String, you should choose Adapter1 to use.

```java
public class StringAdapter implements Adapter1<String> {
    private static final CloudEventBuilder template = CloudEventBuilder.v1();
    @Override
    public CloudEvent adapt(String originalData) {
        template.withId(UUID.randomUUID().toString());
        URI uri = URI.create("sample-source");
        template.withSource(uri);
        template.withType("http");
        template.withDataContentType("application/json");
        template.withTime(OffsetDateTime.now());

        JsonObject data = new JsonObject();
        data.put("mydata",originalData);
        
        template.withData(data.toBuffer().getBytes());

        return template.build();
    }
}
```

If the incoming data is an HTTP request and, you need both headers and the body to generate a CloudEvent, 
then you should choose Adapter2 to use.

```java
class HttpAdapter implements Adapter2<HttpServerRequest,Buffer> {
    private static final CloudEventBuilder template = CloudEventBuilder.v1();
    @Override
    public CloudEvent adapt(HttpServerRequest req, Buffer buffer) {
        template.withId(UUID.randomUUID().toString());
        URI uri = URI.create("vance-http-source");
        template.withSource(uri);
        template.withType("http");
        template.withDataContentType("application/json");
        template.withTime(OffsetDateTime.now());

        JsonObject data = new JsonObject();
        JsonObject headers = new JsonObject();
        req.headers().forEach((m)-> headers.put(m.getKey(),m.getValue()));
        data.put("headers",headers);
        String contentType = req.getHeader("content-type");
        if(null != contentType && contentType.equals("application/json")){
            JsonObject body = buffer.toJsonObject();
            data.put("body",body);
        }else{
            String myData = new String(buffer.getBytes());
            JsonObject body = new JsonObject();
            body.put("data",myData);
            data.put("body",body);
        }
        template.withData(data.toBuffer().getBytes());

        return template.build();
    }
}
```