package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.nls.Thesaurus;

public class SubscriberSpecInfo {

    public String destination;
    public String subscriber;
    public String displayName;

    public SubscriberSpecInfo() {
    }

    public SubscriberSpecInfo(String destination, String subscriber, Thesaurus thesaurus) {
        this.destination = destination;
        this.subscriber = subscriber;
        this.displayName = thesaurus.getStringBeyondComponent(subscriber, subscriber);
    }

    public static SubscriberSpecInfo of(SubscriberExecutionSpec executionSpec, Thesaurus thesaurus) {
        return of(executionSpec.getSubscriberSpec(), thesaurus);
    }

    public static SubscriberSpecInfo of(SubscriberSpec subscriberSpec, Thesaurus thesaurus) {
        return new SubscriberSpecInfo(subscriberSpec.getDestination().getName(), subscriberSpec.getName(), thesaurus);
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
