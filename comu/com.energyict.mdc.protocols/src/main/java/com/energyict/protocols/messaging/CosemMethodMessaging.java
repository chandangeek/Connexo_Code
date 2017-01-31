/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.messaging;

/**
 * This interface is intended to be implemented by protocols that support sending messages to
 * execute custom Cosem methods.
 *
 * @author Isabelle
 */
public interface CosemMethodMessaging extends CosemMessaging {

    /**
     * Returns the message builder capable of generating and parsing 'cosem method' messages.
     *
     * @return The {@link MessageBuilder} capable of generating and parsing 'cosem method' messages.
     */
    CosemMethodMessageBuilder getCosemMethodMessageBuilder();

}
