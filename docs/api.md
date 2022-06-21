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

You have to implement corresponding interfaces when developing a connector.
```java
public interface Sink {
    void start() throws Exception;
}
```

```java
public interface Source extends Sink{
    Adapter getAdapter();
}
```