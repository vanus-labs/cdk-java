---
layout: default
title: Home
nav_order: 1
description: "The Java Connector-Development Kit (CDK) is a collection of Java packages to help you to build a new [Vance Connector][vc] in minutes."
permalink: /
---

# Java CDK for Vance
{: .fs-9 }

## Introduction

The cdk currently offers utilities to develop a source or a sink connectors including:
- HTTP implementations (either to handle general HTTP requests or CloudEvents)
- Config implementation to load user-specific configs
- The ability to interact with the Vance operator

## Getting started

### Using the cdk-java

To use the cdk-java, add following dependency to your pom.xml

```
<dependency>
    <groupId>com.linkall</groupId>
    <artifactId>cdk-java</artifactId>
    <version>0.1.0</version>
</dependency>
```

In order to know how to create a new Vance Connector, check out the [API Documentation][api].

If you want to know more about the default HTTP implementation for handling requests or sending CloudEvents out, check out the 
[HTTP implementations][http].

### Connector Examples

Here are some connector examples developed by cdk-java.

| Connector<div style="width:90px">         | Type          | Description |
|:-------------|:------------------|:------|
| [sink-http]    | sink | The HTTP Sink is a Vance Connector which aims to handle incoming CloudEvents in a way that extracts the data part of the original event and deliver these extracted data to the target URL.  |
| [source-http] | source   | The HTTP Source is a Vance Connector which aims to generate CloudEvents in a way that wraps all headers and body of the original request into the data field of a new CloudEvent. And deliver these CloudEvents to the target URL.  |

Use them as samples when you want to write a sink or source connector.

[vc]: https://github.com/linkall-labs/vance-docs/blob/main/docs/concept.md
[api]: https://linkall-labs.github.io/cdk-java/api.html
[http]: https://linkall-labs.github.io/cdk-java/http.html
[sink-http]: https://github.com/linkall-labs/vance/tree/main/connectors/sink-http
[source-http]: https://github.com/linkall-labs/vance/tree/main/connectors/source-http