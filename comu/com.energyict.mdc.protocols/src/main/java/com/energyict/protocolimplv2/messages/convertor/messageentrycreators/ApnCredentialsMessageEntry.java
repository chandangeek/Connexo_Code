package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineDeviceMessageAttribute;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

/**
 * Copyrights EnergyICT
 * Date: 2/04/13
 * Time: 10:33
 */
public class ApnCredentialsMessageEntry implements MessageEntryCreator {

    private final String gprsApn;
    private final String gprsUserName;
    private final String gprsPassword;

    public ApnCredentialsMessageEntry(String gprsApn, String gprsUserName, String gprsPassword) {
        this.gprsApn = gprsApn;
        this.gprsUserName = gprsUserName;
        this.gprsPassword = gprsPassword;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute userNameAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, gprsUserName);
        OfflineDeviceMessageAttribute passwordAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, gprsPassword);
        OfflineDeviceMessageAttribute apnAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, gprsApn);
        MessageTag messageTag = new MessageTag(RtuMessageConstant.GPRS_MODEM_SETUP);
        messageTag.add(new MessageAttribute(RtuMessageConstant.GPRS_APN, apnAttribute.getDeviceMessageAttributeValue()));
        messageTag.add(new MessageAttribute(RtuMessageConstant.GPRS_USERNAME, userNameAttribute.getDeviceMessageAttributeValue()));
        messageTag.add(new MessageAttribute(RtuMessageConstant.GPRS_PASSWORD, passwordAttribute.getDeviceMessageAttributeValue()));
        messageTag.add(new MessageValue(" "));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
