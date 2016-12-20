package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;

import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

/**
 * Creates a MessageEntry based on the "EnableDST" xml tag with a value
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 14:59
 */
public class EnableOrDisableDSTMessageEntry implements MessageEntryCreator {

    private static final String ENABLE_DST = "EnableDST";

    private final String enableDSTAttributeName;

    /**
     * Default constructor
     *
     * @param enableDSTAttributeName the name of the OfflineDeviceMessageAttribute representing the boolean
     */
    public EnableOrDisableDSTMessageEntry(String enableDSTAttributeName) {
        this.enableDSTAttributeName = enableDSTAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute attribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, enableDSTAttributeName);
        MessageTag messageTag = new MessageTag(ENABLE_DST);
        messageTag.add(new MessageValue(attribute.getValue()));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}