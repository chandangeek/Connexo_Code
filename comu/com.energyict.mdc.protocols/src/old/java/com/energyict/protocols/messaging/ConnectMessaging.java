package com.energyict.protocols.messaging;

import com.energyict.mdc.protocol.api.messaging.AdvancedMessaging;

import com.energyict.protocols.messaging.ConnectMessageBuilder;
import com.energyict.protocols.messaging.MessageBuilder;

/**
 * This interface is intended to be implemented by protocols that support sending a connect message.
 *
 * @author Isabelle
 */
public interface ConnectMessaging extends AdvancedMessaging {

    /**
     * Returns the message builder capable of generating and parsing connect messages.
     *
     * @return The {@link MessageBuilder} capable of generating and parsing connect messages.
     */
    ConnectMessageBuilder getConnectMessageBuilder();

}
