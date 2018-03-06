/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.util.HasName;

/**
 * Abstraction for the space in which queues and topics exist.
 */
@ProviderType
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

    boolean isJms();

    void save();

    default DestinationSpec createDestinationSpec(String name, int retryDelay) {
        return createDestinationSpec(name, retryDelay, 5, true, name);
    }

    DestinationSpec createDestinationSpec(String name, int retryDelay, int retries);

    default DestinationSpec createBufferedDestinationSpec(String name, int retryDelay) {
        return createBufferedDestinationSpec(name, retryDelay, 5, true, name);
    }

    DestinationSpec createBufferedDestinationSpec(String name, int retryDelay, int retries);

    DestinationSpec createDestinationSpec(String name, int retryDelay, int retries, boolean isDefault, String queueTypeName);

    DestinationSpec createBufferedDestinationSpec(String name, int retryDelay, int retries, boolean isDefault, String queueTypeName);

}
