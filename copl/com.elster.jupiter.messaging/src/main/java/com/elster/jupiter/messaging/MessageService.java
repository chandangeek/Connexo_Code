package com.elster.jupiter.messaging;

import com.google.common.base.Optional;

public interface MessageService {

    QueueTableSpec createQueueTableSpec(String name, String payloadType, boolean multiConsumer);

    QueueTableSpec getQueueTableSpec(String name);

    Optional<DestinationSpec> getDestinationSpec(String name);

    Optional<SubscriberSpec> getSubscriberSpec(String destinationSpecName, String name);
}
