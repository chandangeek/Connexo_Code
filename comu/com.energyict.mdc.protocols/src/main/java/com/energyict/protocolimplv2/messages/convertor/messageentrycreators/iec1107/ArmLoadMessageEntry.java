package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107;

import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

/**
 * Creates a MessageEntry based on the "CONTACTOR_ARM" xml tag with no additional parameters
 * This is used by the IEC1107 protocols (as1440, as220)
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 14:33
 */
public class ArmLoadMessageEntry implements MessageEntryCreator {

    public ArmLoadMessageEntry() {
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        MessageTag messageTag = new MessageTag("CONTACTOR_ARM");
        messageTag.add(new MessageValue(" "));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
