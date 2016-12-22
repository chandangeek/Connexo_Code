package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.Messaging;

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
        return MessageEntry
                    .fromContent(writeTag(messagingProtocol, messageTag))
                    .andMessage(offlineDeviceMessage)
                    .finish();
    }

    private String writeTag(Messaging messagingProtocol, MessageTag messageTag) {
        if (messagingProtocol == null) {
            return SimpleTagWriter.writeTag(messageTag);
        } else {
            return messagingProtocol.writeTag(messageTag);
        }
    }
}
