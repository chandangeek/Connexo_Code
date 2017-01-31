/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.Messaging;

import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

public class AddPhoneNumbersToWhiteListMessageEntry implements MessageEntryCreator {

    private static final String PHONENUMBER = "Phonenumber";
    private static final String PHONE_NUMBER_SEPARATOR = ";";

    private final String phoneNumbersAttributeName;

    public AddPhoneNumbersToWhiteListMessageEntry(String phoneNumbersAttributeName) {
        this.phoneNumbersAttributeName = phoneNumbersAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute phoneNumbers = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, phoneNumbersAttributeName);
        MessageTag messageTag = new MessageTag(RtuMessageConstant.WAKEUP_ADD_WHITELIST);
        final String[] allPhoneNumbers = phoneNumbers.getDeviceMessageAttributeValue().split(PHONE_NUMBER_SEPARATOR);
        int counter = 1;
        for (String number : allPhoneNumbers) {
            messageTag.add(new MessageAttribute(PHONENUMBER + counter++, number.trim()));
        }
        messageTag.add(new MessageValue(" "));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
