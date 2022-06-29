---
title: Source Example
nav_order: 3
---

# Connector Samples

Now, you're getting more familiar with the basic concepts of vance APIs.

Let's go through the details of provided examples (there are two connector samples under `examples` directory).

## Source Example

### com.vance.source.Entrance

`Entrance.java` is the entrance of the source connector.

```java
01 public class Entrance {
02     public static void main(String[] args) {
03         VanceApplication.run(MyConnector.class);
04     }
05 }
```

The `main` method uses a one-line code to easily launch the connector programme.

`VanceApplication.run()` only needs one parameter, which is the implementation of either a [Sink or Source](api.md#connector-interface) interface.

### com.vance.source.MySource

`MySource` implemented all methods of `Source` interface.

```java
01 @Override
02 public void start(){
03    // TODO Initialize your Adapter
04    MyAdapter adapter = (MyAdapter) getAdapter();
05
06    // TODO receive your original data and transform it into a CloudEvent via your Adapter
07    // In this sample, we use a String as the original data
08    for (int i = 0; i < NUM_EVENTS; i++) {
09        String data = "Event number " + i;
10        // TODO: construct CloudEvents
11        CloudEvent event = adapter.adapt(data);
12        // Use EnvUtil to get the target URL the source will send to
13        // You can replace the default sink URL with yours in resources/config.json
14        String sink = EnvUtil.getVanceSink();
15        // TODO: deliver CloudEvents to endpoint ${V_TARGET}
16        sendCloudEvent(event,sink);
17    }
18 }
```

`start()` method is one of the methods declared in `Source` interface. It did three things here:
1. Using a for loop to generate original data, which is a String consisted of "Event number" and the index of the loop
2. Using `adapt()` method from `MyAdapter` to transform a String into a CloudEvent
3. Sending CloudEvents to the URL which specified in `resources/config.json`

```java
01 public Adapter getAdapter() {
02     return new MyAdapter();
03 }
```

`getAdapter()` method is another method declared in `Source` interface. Its purpose is to return an instance of `Adapter` interface.

```java
01 private void sendCloudEvent(CloudEvent event, String targetURL){
02    Future<HttpResponse<Buffer>> responseFuture;
03    // Send CloudEvent to vance_sink
04    responseFuture = VertxMessageFactory.createWriter(webClient.postAbs(targetURL))
05            .writeStructured(event, JsonFormat.CONTENT_TYPE); // Use structured mode.
06    responseFuture.onSuccess(resp->{
07        LOGGER.info("send CloudEvent success");
08    }).onFailure(t-> LOGGER.info("send task failed"));
09 }
```

`sendCloudEvent()` is a method used to send a CloudEvent to the target URL. In this example, I use the Vert.x as the HTTP framework, but you can choose whatever you want to POST the HTTP request. 

> It's strongly recommended to use one of the HTTP implementations CloudEvent-sdk provided.

### com.vance.source.MyAdapter

`MyAdapter` is the implementation to convert the original data into a CloudEvent.

>⚠️ Note: Don't directly implement `Adapter` interface️. Instead, Implement `Adapter1` or `Adapter2` based on the number of types you need to construct a CloudEvent.

You can write your own logics to obtain original data, and implement your Adapter based on your needs to construct a CloudEvent.
