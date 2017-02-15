/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.Messaging;

public interface MessageEntryCreator {

    /**
     * Creates a <i>legacy</i> MessageEntry from the MessagingProtocol and the OfflineDeviceMessage.
     *
     * @param messagingProtocol    the protocol which contains the <i>old</i> message-formatting
     * @param offlineDeviceMessage the new DeviceMessage
     * @return the old MessageEntry which is mapped from the offlineDeviceMessage
     */
    public MessageEntry createMessageEntry(final Messaging messagingProtocol, final OfflineDeviceMessage offlineDeviceMessage);

}