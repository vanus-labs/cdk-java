---
title: Home
nav_order: 1
The Java Connector-Development Kit (CDK) is a collection of Java packages to help you to build a new
[Vance Connector][vc] in minutes.
---

# Java CDK for Vance
{: .fs-9 }

## Introduction

The CDK currently offers utilities specific for developing source or sink connectors for:
- HTTP implementation (either to handle general HTTP requests or CloudEvents)
- Config implementation to load user-specific configs
- interaction with Vance operator

## Getting started

### Using the cdk-java

To use cdk-java, add following dependency to your pom.xml

```
<dependency>
    <groupId>com.linkall</groupId>
    <artifactId>cdk-java</artifactId>
    <version>0.1.0</version>
</dependency>
```

In order to know how to create a new Vance Connector, check out the [API Documentation][api].

[vc]: https://github.com/linkall-labs/vance-docs/blob/main/docs/concept.md
[api]: https://linkall-labs.github.io/cdk-java/api.html