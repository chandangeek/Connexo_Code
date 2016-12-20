package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;

import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.Messaging;

/**
 * Creates a MessageEntry that consists of one tag and one value. E.g.: <Description>1</Description>
 * The tag name is included in the name of the offlineDeviceMessage enum instance
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 14:59
 */
public class SimpleEIWebMessageEntry extends AbstractEIWebMessageEntry {


    /**
     * Default constructor
     */
    public SimpleEIWebMessageEntry() {
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        String messageName = getMessageName(offlineDeviceMessage);
        MessageTag messageTag = new MessageTag(messageName);
        messageTag.add(new MessageValue(offlineDeviceMessage.getDeviceMessageAttributes().get(0).getValue()));
        return createMessageEntry(messageTag, offlineDeviceMessage.getTrackingId());
    }
}