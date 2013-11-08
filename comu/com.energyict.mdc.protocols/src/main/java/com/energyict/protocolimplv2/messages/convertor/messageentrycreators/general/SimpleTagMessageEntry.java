package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general;

import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

/**
 * Creates XML: <tag> </tag>
 * <p/>
 * Copyrights EnergyICT
 * Date: 24/10/13
 * Time: 10:23
 * Author: khe
 */
public class SimpleTagMessageEntry implements MessageEntryCreator {

    private final String tag;
    private final boolean includeSpace;

    public SimpleTagMessageEntry(String tag) {
        this(tag, true);
    }

    public SimpleTagMessageEntry(String tag, boolean includeSpace) {
        this.tag = tag;
        this.includeSpace = includeSpace;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        MessageTag messageTag = new MessageTag(tag);
        if (includeSpace) {
            messageTag.add(new MessageValue(" "));
        }
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
