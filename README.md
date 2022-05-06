# Java CDK for Vance Connectors

[![License](https://img.shields.io/:license-Apache2-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

The Java Connector-Development Kit (CDK) is a collection of Java packages to help you build a new
Vance connector in minutes.

In Vance, a connector is either a Source or a Sink.

A valid Vance Source generally:
- Retrieves data from real world data producers
- Transforms retrieved data into CloudEvents
- Delivers transformed CloudEvents to a HTTP target 
  
And a valid Vance Sink generally:
- Retrieves CloudEvents via HTTP requests
- Uses retrieved CloudEvents in specific business logics


