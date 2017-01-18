package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general;

import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.Messaging;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

/**
 * Creates XML: <tagX> </tagX>, where X is the value of the (only) device message attribute.
 * E.g. <ConnectRelay1></ConnectRelay1>, when the user chooses the "Connect relay" message and attribute value "1".
 * <p/>
 * Copyrights EnergyICT
 * Date: 24/10/13
 * Time: 10:23
 * Author: khe
 */
public class AdvancedTagMessageEntry implements MessageEntryCreator {

    private final String tag;

    public AdvancedTagMessageEntry(String tag) {
        this.tag = tag;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        String deviceMessageAttributeValue = offlineDeviceMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
        MessageTag messageTag = new MessageTag(tag + deviceMessageAttributeValue);
        messageTag.add(new MessageValue(" "));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}