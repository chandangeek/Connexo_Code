package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.a1;

import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

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
        int year = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.year).getDeviceMessageAttributeValue());
        int month = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.month).getDeviceMessageAttributeValue());
        int day = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.day).getDeviceMessageAttributeValue());
        String dayOfWeek = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.dayOfWeek).getDeviceMessageAttributeValue();

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
        return new MessageEntry(messagingProtocol.writeTag(msgTag), offlineDeviceMessage.getTrackingId());
    }
}