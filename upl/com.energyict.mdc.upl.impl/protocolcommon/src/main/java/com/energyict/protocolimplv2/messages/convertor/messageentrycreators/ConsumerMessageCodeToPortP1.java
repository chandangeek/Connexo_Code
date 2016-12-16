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
 * Time: 9:03
 */
public class ConsumerMessageCodeToPortP1 implements MessageEntryCreator {

    private final String p1InformationAttributeName;

    public ConsumerMessageCodeToPortP1(String p1InformationAttributeName) {
        this.p1InformationAttributeName = p1InformationAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute p1Message = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, p1InformationAttributeName);
        MessageTag messageTag = new MessageTag(RtuMessageConstant.P1CODEMESSAGE);
        messageTag.add(new MessageAttribute(RtuMessageConstant.P1CODE, p1Message.getValue()));
        messageTag.add(new MessageValue(" "));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
