package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;

import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

/**
 * Creates XML: <tag>value</tag>
 * <p/>
 * Copyrights EnergyICT
 * Date: 24/10/13
 * Time: 10:23
 * Author: khe
 */
public class SimpleValueMessageEntry implements MessageEntryCreator {

    private final String tag;

    public SimpleValueMessageEntry(String tag) {
        this.tag = tag;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        MessageTag messageTag = new MessageTag(tag);
        messageTag.add(new MessageValue(offlineDeviceMessage.getDeviceMessageAttributes().get(0).getValue()));
        return new MessageEntry(writeTag(messagingProtocol, messageTag), offlineDeviceMessage.getTrackingId());
    }

    private String writeTag(Messaging messagingProtocol, MessageTag messageTag) {
        if (messagingProtocol == null) {
            return SimpleTagWriter.writeTag(messageTag);
        } else {
            return messagingProtocol.writeTag(messageTag);
        }
    }
}
