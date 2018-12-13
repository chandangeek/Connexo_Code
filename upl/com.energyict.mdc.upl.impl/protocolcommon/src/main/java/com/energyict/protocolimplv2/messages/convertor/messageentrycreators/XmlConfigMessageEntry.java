package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.Messaging;

import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

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
        messageTag.add(new MessageValue(xmlConfigurationAttribute.getValue()));
        return MessageEntry.fromContent(messagingProtocol.writeTag(messageTag)).andMessage(offlineDeviceMessage).finish();
    }
}
