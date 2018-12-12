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
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagWriter;

/**
 * @author sva
 * @since 14/08/2015 - 16:15
 */
public class A1ActivityCalendarMessageEntry implements MessageEntryCreator {

    private final String tag = "SetPassiveCalendar";
    private final String activationDateAttributeTag = "TARIFF_ACTIVATION_DATE";
    private final String calendarFileAttributeTag = "TARIFF_CALENDAR_FILE";

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        String activationDate = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.activityCalendarActivationDateAttributeName).getValue();
        String tariffCalendarsUserFile = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.XmlUserFileAttributeName).getValue();
        tariffCalendarsUserFile = tariffCalendarsUserFile.replaceAll("\"", "''");

        return createMessageEntry(offlineDeviceMessage, activationDate, tariffCalendarsUserFile);
    }

    public MessageEntry createMessageEntry(OfflineDeviceMessage offlineDeviceMessage, String activationDate, String userFileContent) {
        MessageTag messageTag = new MessageTag(tag);
        messageTag.add(new MessageValue(" "));
        messageTag.add(new MessageAttribute(activationDateAttributeTag, activationDate));
        messageTag.add(new MessageAttribute(calendarFileAttributeTag, userFileContent));

        return MessageEntry
                    .fromContent(writeTag(messageTag))
                    .andMessage(offlineDeviceMessage)
                    .finish();
    }

    private String writeTag(MessageTag messageTag) {
        return SimpleTagWriter.writeTag(messageTag);
    }
}