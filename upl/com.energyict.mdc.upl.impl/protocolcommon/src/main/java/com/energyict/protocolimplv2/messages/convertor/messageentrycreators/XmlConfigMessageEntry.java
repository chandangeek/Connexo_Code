package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineDeviceMessageAttribute;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.Messaging;
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
