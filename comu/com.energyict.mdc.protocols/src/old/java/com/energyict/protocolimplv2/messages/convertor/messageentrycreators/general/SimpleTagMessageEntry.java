package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general;

import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.Messaging;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
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
