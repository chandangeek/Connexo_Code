/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.Messaging;

import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

public class SetTimeMessageEntry implements MessageEntryCreator {

    private final String dateAttributeName;

    /**
     * Default constructor
     *
     * @param dateAttributeName the name of the OfflineDeviceMessageAttribute representing the time to set
     */
    public SetTimeMessageEntry(String dateAttributeName) {
        this.dateAttributeName = dateAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute dateAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, dateAttributeName);
        MessageTag messageTag = new MessageTag(RtuMessageConstant.SET_TIME);
        messageTag.add(new MessageAttribute(RtuMessageConstant.SET_TIME_VALUE, dateAttribute.getDeviceMessageAttributeValue()));
        messageTag.add(new MessageValue(" "));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}