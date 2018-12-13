package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;
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
 * Time: 12:14
 */
public class IskraMx372AddPhoneNumbersToWhiteListMessageEntry implements MessageEntryCreator {

    private static final String PHONENUMBER = "Phonenumber";
    private static final String PHONE_NUMBER_SEPARATOR = ";";

    private final String phoneNumbersAttributeName;

    public IskraMx372AddPhoneNumbersToWhiteListMessageEntry(String phoneNumbersAttributeName) {
        this.phoneNumbersAttributeName = phoneNumbersAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute phoneNumbers = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, phoneNumbersAttributeName);
        MessageTag messageTag = new MessageTag(RtuMessageConstant.WAKEUP_ADD_WHITELIST);
        final String[] allPhoneNumbers = phoneNumbers.getValue().split(PHONE_NUMBER_SEPARATOR);
        int counter = 1;
        for (String number : allPhoneNumbers) {
            MessageTag innerTag = new MessageTag(PHONENUMBER + counter++);
            innerTag.add(new MessageValue(number.trim()));
            messageTag.add(innerTag);
        }
        return MessageEntry.fromContent(messagingProtocol.writeTag(messageTag)).andMessage(offlineDeviceMessage).finish();
    }
}
