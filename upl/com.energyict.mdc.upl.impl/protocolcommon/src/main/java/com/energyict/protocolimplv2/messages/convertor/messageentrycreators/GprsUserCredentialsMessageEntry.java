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
 * Copyrights EnergyICT
 * Date: 2/04/13
 * Time: 10:33
 */
public class GprsUserCredentialsMessageEntry implements MessageEntryCreator {

    private final String userNameAttributeName;
    private final String passwordAttributeName;

    public GprsUserCredentialsMessageEntry(String userNameAttributeName, String passwordAttributeName) {
        this.userNameAttributeName = userNameAttributeName;
        this.passwordAttributeName = passwordAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute userNameAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, userNameAttributeName);
        OfflineDeviceMessageAttribute passwordAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, passwordAttributeName);
        MessageTag messageTag = new MessageTag(RtuMessageConstant.GPRS_MODEM_CREDENTIALS);
        messageTag.add(new MessageAttribute(RtuMessageConstant.GPRS_USERNAME, userNameAttribute.getValue()));
        messageTag.add(new MessageAttribute(RtuMessageConstant.GPRS_PASSWORD, passwordAttribute.getValue()));
        messageTag.add(new MessageValue(" "));
        return MessageEntry.fromContent(messagingProtocol.writeTag(messageTag)).andMessage(offlineDeviceMessage).finish();
    }
}
