/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.cynet;

/**
 * Implemented by parties who are interested by the messages that are received
 * by the module. A {@link MessageListener} is attached to a {@link CynetRFModule} by calling {@link CynetRFModule#addMessageListener(MessageListener)}.
 * 
 * @author alex
 * 
 */
public interface MessageListener {

    /**
     * Gets called when a message has been received by the module.
     * 
     * @param source
     *            The source node of the message.
     * @param payload
     *            The payload of the message.
     */
    void messageReceived(final ManufacturerId source, final byte[] payload);
}
