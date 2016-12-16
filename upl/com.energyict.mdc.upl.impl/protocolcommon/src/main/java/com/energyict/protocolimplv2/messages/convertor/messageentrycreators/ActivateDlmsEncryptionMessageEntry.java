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
 * Creates a MessageEntry based on the {@link com.energyict.protocolimpl.messages.RtuMessageConstant#AEE_ACTIVATE_SECURITY}
 * xml tag with an additional {@link com.energyict.protocolimpl.messages.RtuMessageConstant#AEE_SECURITYLEVEL}
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/03/13
 * Time: 16:12
 */
public class ActivateDlmsEncryptionMessageEntry implements MessageEntryCreator{

    private final String encryptionLevelAttributeName;

    public ActivateDlmsEncryptionMessageEntry(String encryptionLevelAttributeName) {
        this.encryptionLevelAttributeName = encryptionLevelAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute encryptionLevelAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, encryptionLevelAttributeName);
        MessageTag messageTag = new MessageTag(RtuMessageConstant.AEE_ACTIVATE_SECURITY);
        messageTag.add(new MessageAttribute(RtuMessageConstant.AEE_SECURITYLEVEL, encryptionLevelAttribute.getValue()));
        messageTag.add(new MessageValue(" "));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
