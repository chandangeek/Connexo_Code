package com.energyict.protocols.messaging;

import com.energyict.mdc.protocol.api.messaging.AdvancedMessaging;

/**
 * This interface is intended to be implemented by protocols that support sending a 'time of use' message.
 * This is a message containing a tarif calendar for the meter.
 *
 * @author Isabelle
 */
public interface TimeOfUseMessaging extends AdvancedMessaging {

    /**
     * Returns the message builder capable of generating and parsing 'time of use' messages.
     *
     * @return The {@link MessageBuilder} capable of generating and parsing 'time of use' messages.
     */
    TimeOfUseMessageBuilder getTimeOfUseMessageBuilder();

    /**
     * Get the TimeOfUseMessagingConfig object that contains all the capabilities for the current protocol
     *
     * @return the config object
     */
    TimeOfUseMessagingConfig getTimeOfUseMessagingConfig();

}
