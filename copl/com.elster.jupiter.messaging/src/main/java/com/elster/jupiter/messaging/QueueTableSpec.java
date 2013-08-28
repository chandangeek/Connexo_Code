package com.elster.jupiter.messaging;

import com.elster.jupiter.util.HasName;

/**
 * Abstraction for the space in which queues and topics exist.
 */
public interface QueueTableSpec extends HasName {

    /**
     * Activates this QueueTableSpec, has no effect on an already active QueueTableSpec.
     */
    void activate();

    /**
     * Deactivates this QueueTableSpec, has no effect on an already inactive QueueTableSpec.
     */
    void deactivate();

    String getPayloadType();

    boolean isMultiConsumer();

    boolean isActive();

    DestinationSpec createDestinationSpec(String name, int retryDelay);

    boolean isJms();
}
