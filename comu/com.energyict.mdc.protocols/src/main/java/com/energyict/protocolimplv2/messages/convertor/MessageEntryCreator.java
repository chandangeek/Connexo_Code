package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.Messaging;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;

/**
 * Defines functionality to create {@link MessageEntry MessageEntries}
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 14:29
 */
public interface MessageEntryCreator {

    /**
     * Creates a <i>legacy</i> MessageEntry from the MessagingProtocol and the OfflineDeviceMessage.
     *
     * @param messagingProtocol    the protocol which contains the <i>old</i> message-formatting
     * @param offlineDeviceMessage the new DeviceMessage
     * @return the old MessageEntry which is mapped from the offlineDeviceMessage
     */
    MessageEntry createMessageEntry(final Messaging messagingProtocol, final OfflineDeviceMessage offlineDeviceMessage);

}