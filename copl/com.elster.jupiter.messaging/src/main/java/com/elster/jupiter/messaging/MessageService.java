package com.elster.jupiter.messaging;

import java.util.List;
import java.util.Optional;

/**
 * Main service of the MSG module that allows managing messaging.
 */
public interface MessageService {

    String DESTINATION_NAME = "destination";
    String SUBSCRIBER_NAME = "subscriber";
    String COMPONENTNAME = "MSG";

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

    /**
     * @return a List containing all SubscriberSpecs
     */
    List<SubscriberSpec> getSubscribers();
}
