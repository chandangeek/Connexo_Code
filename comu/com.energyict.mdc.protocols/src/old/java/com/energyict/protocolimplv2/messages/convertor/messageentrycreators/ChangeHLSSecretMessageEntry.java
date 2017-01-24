package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.Messaging;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

/**
 * Creates a MessageEntry based on the {@link com.energyict.protocolimpl.messages.RtuMessageConstant#AEE_CHANGE_HLS_SECRET}
 * xml tag with NO an additional values.
 * <p/>
 * Copyrights EnergyICT
 * Date: 2/04/13
 * Time: 9:26
 */
public class ChangeHLSSecretMessageEntry implements MessageEntryCreator {

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        MessageTag messageTag = new MessageTag(RtuMessageConstant.AEE_CHANGE_HLS_SECRET);
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
