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
 * Copyrights EnergyICT
 * Date: 2/04/13
 * Time: 12:14
 */
public class AddManagedPhoneNumbersToWhiteListMessageEntry implements MessageEntryCreator {

    private static final String MANAGED_PHONENUMBER = "ManagedPhonenumber";
    private static final String PHONE_NUMBER_SEPARATOR = ";";

    private final String phoneNumbersAttributeName;

    public AddManagedPhoneNumbersToWhiteListMessageEntry(String phoneNumbersAttributeName) {
        this.phoneNumbersAttributeName = phoneNumbersAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute phoneNumbers = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, phoneNumbersAttributeName);
        MessageTag messageTag = new MessageTag(RtuMessageConstant.WAKEUP_ADD_WHITELIST);
        final String[] allPhoneNumbers = phoneNumbers.getValue().split(PHONE_NUMBER_SEPARATOR);
        int counter = 1;
        for (String number : allPhoneNumbers) {
            messageTag.add(new MessageAttribute(MANAGED_PHONENUMBER + counter++, number.trim()));
        }
        messageTag.add(new MessageValue(" "));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
