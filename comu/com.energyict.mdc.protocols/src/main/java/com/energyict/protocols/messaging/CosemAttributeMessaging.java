/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.messaging;

/**
 * This interface is intended to be implemented by protocols that support sending messages to
 * update a Cosem attribute for a given Cosem object (class).
 *
 * @author Isabelle
 */
public interface CosemAttributeMessaging extends CosemMessaging {

    /**
     * Returns the message builder capable of generating and parsing 'cosem attribute' messages.
     *
     * @return The {@link MessageBuilder} capable of generating and parsing 'cosem attribute' messages.
     */
    CosemAttributeMessageBuilder getCosemAttributeMessageBuilder();

}
