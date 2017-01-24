package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.Messaging;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

/**
 * Creates a MessageEntry for the GlobalMeterReset message
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/04/13
 * Time: 9:28
 */
public class GlobalMeterReset implements MessageEntryCreator {

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        MessageTag messageTag = new MessageTag(RtuMessageConstant.GLOBAL_METER_RESET);
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
