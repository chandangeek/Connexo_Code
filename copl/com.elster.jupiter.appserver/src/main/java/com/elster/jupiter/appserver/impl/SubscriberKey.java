/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.messaging.SubscriberSpec;

import java.util.Objects;

final class SubscriberKey {
    private final String destination;
    private final String subscriber;

    private SubscriberKey(String destination, String subscriber) {
        this.destination = destination;
        this.subscriber = subscriber;
    }

    static SubscriberKey of(String destinationName, String subscriberName) {
        if (destinationName == null || subscriberName == null) {
            throw new IllegalArgumentException();
        }
        return new SubscriberKey(destinationName, subscriberName);
    }

    static SubscriberKey of(SubscriberExecutionSpec executionSpec) {
        return of(executionSpec.getSubscriberSpec());
    }

    static SubscriberKey of(SubscriberSpec subscriberSpec) {
        return of(subscriberSpec.getDestination().getName(), subscriberSpec.getName());
    }

    String getDestination() {
        return destination;
    }

    String getSubscriber() {
        return subscriber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SubscriberKey that = (SubscriberKey) o;

        return destination.equals(that.destination) && subscriber.equals(that.subscriber);

    }

    @Override
    public int hashCode() {
        return Objects.hash(destination, subscriber);
    }

    @Override
    public String toString() {
        return destination + ' ' + subscriber;
    }

    boolean matches(SubscriberExecutionSpec executionSpec) {
        return matches(executionSpec.getSubscriberSpec());
    }

    boolean matches(SubscriberSpec subscriberSpec) {
        return subscriberSpec != null && subscriberSpec.getDestination().getName().equals(destination) && subscriberSpec.getName().equals(subscriber);
    }

}