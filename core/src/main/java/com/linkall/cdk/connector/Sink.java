package com.linkall.cdk.connector;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.cloudevents.CloudEvent;

public interface Sink extends Connector{
    /**
     * when receive will call this method
     *
     * @param events
     */
    Result Arrived(CloudEvent... events) throws JsonProcessingException;

}
