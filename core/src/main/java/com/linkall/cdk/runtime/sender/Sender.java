package com.linkall.cdk.runtime.sender;
import io.cloudevents.CloudEvent;

import java.io.IOException;

public interface Sender {
     void sendEvents(CloudEvent[] events) throws Throwable ;
     void close() throws IOException;
}
