/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.messaging;

import com.energyict.mdc.protocol.api.messaging.AdvancedMessaging;

/**
 * This interface is intended to be implemented by protocols that support firmware upgrades. It is needed to describe the capabilities supported
 * by the protocol, and is used to create a fitting interface in EIServer.
 *
 * @author alex
 */
public interface FirmwareUpdateMessaging extends AdvancedMessaging {

    /**
     * This method is needed to describe the capabilities supported by the protocol,
     * and is used to create a fitting interface in EIServer.
     *
     * @return The {@link FirmwareUpdateMessagingConfig} containing all the capabillities of the protocol.
     */
    FirmwareUpdateMessagingConfig getFirmwareUpdateMessagingConfig();

    /**
     * Returns the message builder capable of generating and parsing messages.
     *
     * @return The {@link MessageBuilder} capable of generating and parsing messages.
     */
    FirmwareUpdateMessageBuilder getFirmwareUpdateMessageBuilder();

}