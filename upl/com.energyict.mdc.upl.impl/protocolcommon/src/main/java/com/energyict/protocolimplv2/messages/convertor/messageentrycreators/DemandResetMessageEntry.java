package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

/**
 * Creates a MessageEntry based on the "DemandReset" xml tag with no additional parameters or values
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 14:33
 */
public class DemandResetMessageEntry implements MessageEntryCreator {

    public DemandResetMessageEntry() {
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        MessageTag messageTag = new MessageTag("DemandReset");
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
