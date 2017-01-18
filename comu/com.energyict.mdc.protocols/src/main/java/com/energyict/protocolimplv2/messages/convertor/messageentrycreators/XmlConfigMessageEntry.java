package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.Messaging;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

/**
 * Copyrights EnergyICT
 * Date: 2/05/13
 * Time: 10:04
 */
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
