package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.messaging.SubscriberSpec;

public class SubscriberSpecInfo {

    public String destination;
    public String subsriber;

    public SubscriberSpecInfo() {
    }

    public SubscriberSpecInfo(String destination, String subsriber) {
        this.destination = destination;
        this.subsriber = subsriber;
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

    public String getSubsriber() {
        return subsriber;
    }
}
