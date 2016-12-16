package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;

import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

/**
 * Creates a MessageEntry based on the {@link com.energyict.protocolimpl.messages.RtuMessageConstant#NTA_AEE_CHANGE_DATATRANSPORT_AUTHENTICATION_KEY}
 * xml tag with NO an additional values.
 * <p/>
 * Copyrights EnergyICT
 * Date: 2/04/13
 * Time: 9:17
 */
public class ChangeNTADataTransportAuthenticationKeyMessageEntry implements MessageEntryCreator {

    private final String newAuthenticationKeyAttributeName;

    public ChangeNTADataTransportAuthenticationKeyMessageEntry(String newAuthenticationKeyAttributeName) {
        this.newAuthenticationKeyAttributeName = newAuthenticationKeyAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute msgAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, newAuthenticationKeyAttributeName);
        MessageTag messageTag = new MessageTag(RtuMessageConstant.NTA_AEE_CHANGE_DATATRANSPORT_AUTHENTICATION_KEY);
        messageTag.add(new MessageAttribute(RtuMessageConstant.AEE_NEW_AUTHENTICATION_KEY, msgAttribute.getValue()));
        messageTag.add(new MessageValue(" "));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
