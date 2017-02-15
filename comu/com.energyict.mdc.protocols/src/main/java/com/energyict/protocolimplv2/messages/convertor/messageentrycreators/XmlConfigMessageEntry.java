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

import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

public class XmlConfigMessageEntry implements MessageEntryCreator {

    private final String xmlConfigAttributeName;

    public XmlConfigMessageEntry(String xmlConfigAttributeName) {
        this.xmlConfigAttributeName = xmlConfigAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute xmlConfigurationAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, xmlConfigAttributeName);
        MessageTag messageTag = new MessageTag(RtuMessageConstant.XMLCONFIG);
        messageTag.add(new MessageValue(xmlConfigurationAttribute.getDeviceMessageAttributeValue()));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
