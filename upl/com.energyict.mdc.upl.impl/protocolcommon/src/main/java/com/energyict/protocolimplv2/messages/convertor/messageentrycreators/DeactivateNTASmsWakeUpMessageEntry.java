package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

/**
 * Creates a MessageEntry based on the {@link com.energyict.protocolimpl.messages.RtuMessageConstant#WAKEUP_DEACTIVATE}
 * xml tag with NO an additional values.
 * <p/>
 * Copyrights EnergyICT
 * Date: 2/04/13
 * Time: 10:23
 */
public class DeactivateNTASmsWakeUpMessageEntry implements MessageEntryCreator {

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        MessageTag messageTag = new MessageTag(RtuMessageConstant.WAKEUP_DEACTIVATE);
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
