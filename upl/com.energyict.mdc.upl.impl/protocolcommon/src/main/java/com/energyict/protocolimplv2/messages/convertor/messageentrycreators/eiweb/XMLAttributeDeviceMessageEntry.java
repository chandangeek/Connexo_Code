package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;

/**
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 14:59
 */
public class XMLAttributeDeviceMessageEntry extends AbstractEIWebMessageEntry {

    /**
     * Default constructor
     */
    public XMLAttributeDeviceMessageEntry() {
    }

    /**
     * Return a message entry where the XML content is the attribute value of the OfflineDeviceMessage
     */
    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        return MessageEntry
                .fromContent(offlineDeviceMessage.getDeviceMessageAttributes().get(0).getValue())
                .andMessage(offlineDeviceMessage)
                .finish();
    }
}