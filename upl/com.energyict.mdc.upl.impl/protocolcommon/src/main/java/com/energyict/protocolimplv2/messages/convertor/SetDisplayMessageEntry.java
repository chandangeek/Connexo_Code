package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineDeviceMessageAttribute;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.Messaging;

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
        messageTag.add(new MessageValue(thresholdAttribute.getDeviceMessageAttributeValue()));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
