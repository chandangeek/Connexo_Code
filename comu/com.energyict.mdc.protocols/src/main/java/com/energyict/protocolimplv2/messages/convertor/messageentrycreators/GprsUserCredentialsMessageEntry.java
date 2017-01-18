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
        messageTag.add(new MessageAttribute(RtuMessageConstant.GPRS_USERNAME, userNameAttribute.getDeviceMessageAttributeValue()));
        messageTag.add(new MessageAttribute(RtuMessageConstant.GPRS_PASSWORD, passwordAttribute.getDeviceMessageAttributeValue()));
        messageTag.add(new MessageValue(" "));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
