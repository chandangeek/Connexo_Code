package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.Messaging;

/**
 * Creates a MessageEntry based on the "SET_DISPLAY_MESSAGE" xml tag with a value
 * <p/>
 */
public class SetDisplayMessageEntry implements MessageEntryCreator {

    private final String displayMessageAttributeName;

    /**
     * Default constructor
     *
     * @param displayMessageAttributeName the name of the OfflineDeviceMessageAttribute representing the message to show on the display
     */
    public SetDisplayMessageEntry(String displayMessageAttributeName) {
        this.displayMessageAttributeName = displayMessageAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute thresholdAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, displayMessageAttributeName);
        MessageTag messageTag = new MessageTag("SET_DISPLAY_MESSAGE");
        messageTag.add(new MessageValue(thresholdAttribute.getValue()));
        return MessageEntry.fromContent(messagingProtocol.writeTag(messageTag)).andMessage(offlineDeviceMessage).finish();
    }
}
