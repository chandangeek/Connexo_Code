package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.Messaging;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

/**
 * Creates a MessageEntry based on the
 * {@link com.energyict.protocolimpl.messages.RtuMessageConstant#DISCONNECT_LOAD}
 * xml tag with no additional parameters
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 14:47
 */
public class DisconnectLoadMessageEntry implements MessageEntryCreator {

    public DisconnectLoadMessageEntry() {
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        MessageTag messageTag = new MessageTag(RtuMessageConstant.DISCONNECT_LOAD);
        messageTag.add(new MessageValue(" "));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
