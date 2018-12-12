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
 * Creates a MessageEntry based on the {@link com.energyict.protocolimpl.messages.RtuMessageConstant#AEE_CHANGE_AUTHENTICATION_LEVEL}
 * xml tag with an additional {@link com.energyict.protocolimpl.messages.RtuMessageConstant#AEE_AUTHENTICATIONLEVEL}
 * <p/>
 * Copyrights EnergyICT
 * Date: 19/03/13
 * Time: 9:09
 */
public class ChangeDlmsAuthenticationLevelMessageEntry implements MessageEntryCreator {

    private final String authenticationLevelAttributeName;

    public ChangeDlmsAuthenticationLevelMessageEntry(String authenticationLevelAttributeName) {
        this.authenticationLevelAttributeName = authenticationLevelAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute authenticationLevelAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, authenticationLevelAttributeName);
        MessageTag messageTag = new MessageTag(RtuMessageConstant.AEE_CHANGE_AUTHENTICATION_LEVEL);
        messageTag.add(new MessageAttribute(RtuMessageConstant.AEE_AUTHENTICATIONLEVEL, authenticationLevelAttribute.getValue()));
        messageTag.add(new MessageValue(" "));
        return MessageEntry.fromContent(messagingProtocol.writeTag(messageTag)).andMessage(offlineDeviceMessage).finish();
    }
}
