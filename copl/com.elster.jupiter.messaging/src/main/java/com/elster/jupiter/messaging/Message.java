package com.elster.jupiter.messaging;

/**
 * Abstraction for a message on a queue.
 */
public interface Message {

    /**
     * @return the raw bytes representing the payload of the message.
     */
    byte[] getPayload();
}
