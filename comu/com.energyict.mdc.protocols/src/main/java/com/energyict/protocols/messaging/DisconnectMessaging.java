/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.messaging;

import com.energyict.mdc.protocol.api.messaging.AdvancedMessaging;

/**
 * This interface is intended to be implemented by protocols that support sending a disconnect message.
 *
 * @author Isabelle
 */
public interface DisconnectMessaging extends AdvancedMessaging {

    /**
     * Returns the message builder capable of generating and parsing disconnect messages.
     *
     * @return The {@link MessageBuilder} capable of generating and parsing disconnect messages.
     */
    DisconnectMessageBuilder getDisconnectMessageBuilder();

}
