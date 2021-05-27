/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.subscriber;

import aQute.bnd.annotation.ConsumerType;

/**
 * Factory interface to create MessageHandlers.
 * Implementation must register the factory in the OSGi container with following properties:
 * <ul>
 *     <li>destination: name of the queue/destination on which MessageHandler will be listening</li>
 *     <li>subscriber: name of the subscriber on that destination on which MessageHandler will be listening</li>
 * </ul>
 * Remark: the destination and the subscriber must be created by an installer
 */
@ConsumerType
public interface MessageHandlerFactory {

    /**
     * Create a new message handler
     * @return new MessageHandler
     */
    MessageHandler newMessageHandler();

    /**
     * Allow component dequeue message only by the same AppServer which create the message
     */
    default boolean allowsMessageValidation() {
        return false;
    }
}
