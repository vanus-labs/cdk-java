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

Currently, your concrete Adapter MUST implement either the Adapter1 or the Adapter2 interface.

![adapter](adapter.png)