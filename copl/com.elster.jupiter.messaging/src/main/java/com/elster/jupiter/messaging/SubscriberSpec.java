package com.elster.jupiter.messaging;

public interface SubscriberSpec {

    DestinationSpec getDestination();

    String getName();
}
