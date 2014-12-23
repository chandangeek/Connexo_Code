package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.messaging.SubscriberSpec;

public class SubscriberSpecInfo {

    public String destination;
    public String subscriber;

    public SubscriberSpecInfo() {
    }

    public SubscriberSpecInfo(String destination, String subscriber) {
        this.destination = destination;
        this.subscriber = subscriber;
    }

    public static SubscriberSpecInfo of(SubscriberExecutionSpec executionSpec) {
        return of(executionSpec.getSubscriberSpec());
    }

    public static SubscriberSpecInfo of(SubscriberSpec subscriberSpec) {
        return new SubscriberSpecInfo(subscriberSpec.getDestination().getName(), subscriberSpec.getName());
    }

    public String getDestination() {
        return destination;
    }

    public String getSubscriber() {
        return subscriber;
    }

    public boolean matches(SubscriberSpec subscriberSpec) {
        return subscriberSpec.getName().equals(subscriber) && subscriberSpec.getDestination().getName().equals(destination);
    }
}
