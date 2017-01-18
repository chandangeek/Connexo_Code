package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.Messaging;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
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
        messageTag.add(new MessageAttribute(RtuMessageConstant.AEE_SECURITYLEVEL, encryptionLevelAttribute.getDeviceMessageAttributeValue()));
        messageTag.add(new MessageValue(" "));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
