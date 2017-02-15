/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.Messaging;

import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

public class EnableOrDisableDSTMessageEntry implements MessageEntryCreator {

    private static final String ENABLE_DST = "EnableDST";

    private final String enableDSTAttributeName;

    /**
     * Default constructor
     *
     * @param enableDSTAttributeName the name of the OfflineDeviceMessageAttribute representing the boolean
     */
    public EnableOrDisableDSTMessageEntry(String enableDSTAttributeName) {
        this.enableDSTAttributeName = enableDSTAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute attribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, enableDSTAttributeName);
        MessageTag messageTag = new MessageTag(ENABLE_DST);
        messageTag.add(new MessageValue(attribute.getDeviceMessageAttributeValue()));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}