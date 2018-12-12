package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.a1;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.Messaging;

import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

/**
 * @author sva
 * @since 14/08/2015 - 11:27
 */
public class ConfigureBillingPeriodStartDate implements MessageEntryCreator {

    private final String messageTag;
    private final String messageAttribute;

    public ConfigureBillingPeriodStartDate(String messageTag, String messageAttribute) {
        this.messageTag = messageTag;
        this.messageAttribute = messageAttribute;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        int year = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.year).getValue());
        int month = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.month).getValue());
        int day = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.day).getValue());
        String dayOfWeek = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.dayOfWeek).getValue();

        MessageTag msgTag = new MessageTag(messageTag);

        StringBuilder startDate = new StringBuilder();
        if (year != 0) {
            startDate.append(year);
            startDate.append("-");
        }
        if (month != 0) {
            startDate.append(String.format("%02d", month));
            startDate.append("-");
        }
        startDate.append(String.format("%02d", day));

        if(!dayOfWeek.equals("--")) {
            startDate.append(" ");
            startDate.append(dayOfWeek);
        }

        msgTag.add(new MessageAttribute(messageAttribute, startDate.toString()));
        msgTag.add(new MessageValue(" "));
        return MessageEntry
                    .fromContent(messagingProtocol.writeTag(msgTag))
                    .andMessage(offlineDeviceMessage)
                    .finish();
    }
}