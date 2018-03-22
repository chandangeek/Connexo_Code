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

    void delete();

    default DestinationSpec createDestinationSpec(String name, int retryDelay) {
        return createDestinationSpec(name, retryDelay, 5, true, name, false);
    }

    default DestinationSpec createDestinationSpec(String name, int retryDelay, int retries) {
        return createDestinationSpec(name, retryDelay, 5, true, name, false);
    }

    default DestinationSpec createDestinationSpec(String name, int retryDelay, boolean isExtraQueueCreationEnabled) {
        return createBufferedDestinationSpec(name, retryDelay, 5, true, name, isExtraQueueCreationEnabled);
    }

    default DestinationSpec createBufferedDestinationSpec(String name, int retryDelay) {
        return createBufferedDestinationSpec(name, retryDelay, 5, true, name, false);
    }

    DestinationSpec createDestinationSpec(String name, int retryDelay, int retries, boolean isDefault, String queueTypeName, boolean isExtraQueueCreationEnabled);

    DestinationSpec createBufferedDestinationSpec(String name, int retryDelay, int retries, boolean isDefault, String queueTypeName, boolean isExtraQueueCreationEnabled);

}
