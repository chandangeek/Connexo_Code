package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;


public class NumberMessageEntry implements MessageEntryCreator {

    private final String messageAttributeName;

    /**
     * Default constructor
     *
     * @param messageAttributeName the name of the OfflineDeviceMessageAttribute representing the number value
     */
    public NumberMessageEntry(String messageAttributeName) {
        this.messageAttributeName = messageAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute thresholdAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, messageAttributeName);
        // use messageAttributeName as the tag since this will contain just one attribute, no need for duplication
        MessageTag messageTag = new MessageTag(messageAttributeName);
        messageTag.add(new MessageValue(thresholdAttribute.getValue()));
        return MessageEntry.fromContent(messagingProtocol.writeTag(messageTag)).andMessage(offlineDeviceMessage).finish();
    }
}

