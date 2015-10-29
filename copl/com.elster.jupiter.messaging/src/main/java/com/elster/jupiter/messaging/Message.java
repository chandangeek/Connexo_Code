package com.elster.jupiter.messaging;

import aQute.bnd.annotation.ProviderType;

/**
 * Abstraction for a message on a queue.
 */
@ProviderType
public interface Message {

    /**
     * @return the raw bytes representing the payload of the message.
     */
    byte[] getPayload();
}
