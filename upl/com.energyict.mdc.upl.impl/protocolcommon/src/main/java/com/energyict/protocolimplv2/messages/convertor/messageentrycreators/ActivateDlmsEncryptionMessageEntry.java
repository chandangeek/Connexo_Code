package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.Messaging;

import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

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
        return MessageEntry.fromContent(messagingProtocol.writeTag(messageTag)).andMessage(offlineDeviceMessage).finish();
    }
}
