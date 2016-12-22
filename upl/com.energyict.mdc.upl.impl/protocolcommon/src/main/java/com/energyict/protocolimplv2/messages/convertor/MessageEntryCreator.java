package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.Messaging;

/**
 * Defines functionality to create {@link com.energyict.protocol.MessageEntry MessageEntries}
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 14:29
 */
public interface MessageEntryCreator {

    /**
     * Based on the given MessagingProtocol and the OfflineDeviceMessage,
     * create a <i>legacy</i> MessageEntry
     *
     * @param messagingProtocol    the protocol which contains the <i>old</i> message-formatting
     * @param offlineDeviceMessage the new DeviceMessage
     * @return the old MessageEntry which is mapped from the offlineDeviceMessage
     */
    MessageEntry createMessageEntry(final Messaging messagingProtocol, final OfflineDeviceMessage offlineDeviceMessage);

}
