package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general;

import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

/**
 * Creates XML: <tag attributeName="value"> </tag>
 * <p/>
 * Copyrights EnergyICT
 * Date: 24/10/13
 * Time: 9:48
 * Author: khe
 */
public class SimpleAttributeMessageEntry implements MessageEntryCreator {

    private final String attributeName;
    private final String tag;

    public SimpleAttributeMessageEntry(String tag, String attributeName) {
        this.attributeName = attributeName;
        this.tag = tag;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        MessageTag messageTag = new MessageTag(tag);
        messageTag.add(new MessageValue(" "));
        messageTag.add(new MessageAttribute(attributeName, offlineDeviceMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue()));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}