/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.subscriber;

import com.elster.jupiter.messaging.Message;

import aQute.bnd.annotation.ConsumerType;

/**
 * Handler that will process a message from a (message) Destination.
 */
@ConsumerType
public interface MessageHandler {

    /**
     * Process the message.
     * This method is called in the same transaction as the dequeue operation. This means that if an exception is thrown from
     * this method, the complete transaction, including the dequeueing will be rolled back. The message will then be re-delivered
     * @param message The Message to handle (within the dequeuing transaction)
     */
    void process(Message message);

    default boolean validate(Message message) {
        return true;
    }

    /**
     * This method is called after the transaction that dequeued the Message and handled (through the process(Message) method)
     * has been successfully processed.
     * It is guaranteed that the handler that successfully processed the Message will also handle the onMessageDelete for that Message
     * and no other Message will be handled by that handler between the call to process(Message) and onMessageDelete(Message)
     * @param message The Message to handle (outside any transaction)
     */
    default void onMessageDelete(Message message) {
    }
}
