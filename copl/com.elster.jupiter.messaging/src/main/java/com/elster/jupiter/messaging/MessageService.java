package com.elster.jupiter.messaging;

import com.google.common.base.Optional;

/**
 * Main service of the MSG module that allows managing messaging.
 */
public interface MessageService {

    /**
     * Creates a new persisted and activated QueueTableSpec
     * @param name
     * @param payloadType
     * @param multiConsumer
     * @return
     */
    QueueTableSpec createQueueTableSpec(String name, String payloadType, boolean multiConsumer);

    /**
     * @param name
     * @return the QueueTableSpec with the given name, optional, as it may not exist.
     */
    Optional<QueueTableSpec> getQueueTableSpec(String name);

    /**
     * @param name
     * @return the DestinationSpec with the given name, optional, as it may not exist
     */
    Optional<DestinationSpec> getDestinationSpec(String name);

    /**
     *
     * @param destinationSpecName
     * @param name
     * @return the SubscriberSpec with the given name for the Destination with the given name, optional, as it may not exist
     */
    Optional<SubscriberSpec> getSubscriberSpec(String destinationSpecName, String name);
}
