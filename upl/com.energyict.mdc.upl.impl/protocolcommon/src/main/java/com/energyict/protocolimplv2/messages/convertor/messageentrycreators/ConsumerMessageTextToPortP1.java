package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;

import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

/**
 * Copyrights EnergyICT
 * Date: 3/04/13
 * Time: 9:04
 */
public class ConsumerMessageTextToPortP1 implements MessageEntryCreator {

    private final String p1InformationAttributeName;

    public ConsumerMessageTextToPortP1(String p1InformationAttributeName) {
        this.p1InformationAttributeName = p1InformationAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute p1Message = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, p1InformationAttributeName);
        MessageTag messageTag = new MessageTag(RtuMessageConstant.P1TEXTMESSAGE);
        messageTag.add(new MessageAttribute(RtuMessageConstant.P1TEXT, p1Message.getValue()));
        messageTag.add(new MessageValue(" "));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
