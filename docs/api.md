---
title: Vance APIs
nav_order: 2
---

# Vance APIs

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

The `Sink` and `Source` Interfaces reflect the [Sink and Source Concepts][concept] respectively.

![connector](images/connector.png)

You have to implement the corresponding interface when developing a connector.
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

`Adapter` is an abstract concept used to demonstrate how the Source connector will transform incoming data into
a CloudEvent.

Currently, your concrete Adapter MUST implement either the `Adapter1`, or the `Adapter2` interface.

![adapter](images/adapter.png)

Choose an appropriate `Adapter` interface to implement based on the number of your incoming data you need to generate a
CloudEvent.

For example, if the incoming data is a pure String, you should choose `Adapter1`, and use `String` as its generic type.

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
then you should choose `Adapter2`, with `HttpServerRequest` and `Buffer` as its generic type, to use.

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
        JsonObject body = buffer.toJsonObject();
        data.put("body",body);
        
        template.withData(data.toBuffer().getBytes());

        return template.build();
    }
}
```
## Config

### ConfigUtil

ConfigUtil is a class which provides static methods to load user-specific configs. The following methods are supported by the ConfigUtil.class:
| Number         | Method          | Description |
|:-------------|:------------------|:------|
| 1 | public static String getString(String key) | Get a config value according to a specific key  |
| 2 | public static List<String> getStringArray(String key)   | Get a list of configs according to a specific key |
| 3 | public static String getEnvOrConfigOrDefault(String name) | Same as ConfigUtil.getString(String). This method retrieves data from env and config first.If this method cannot find value from above positions, it will try to get a default value in the SDK.  |
| 4 | public static String getConfigPath()   | Get config-path, it could either be a user-set env or default config-path value |
| 5 |     public static String getVanceSink() | Get the sink of vance, its default value is v_target value  |
| 6 | public static String getPort()   | Get the port the process needs to listen to. |
| 7 | public static String getKVStore() | Get the name of KVStore.                    |

[concept]: https://github.com/linkall-labs/vance-docs/blob/main/docs/concept.md