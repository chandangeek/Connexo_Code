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
        String activationDate = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.activityCalendarActivationDateAttributeName).getDeviceMessageAttributeValue();
        String tariffCalendarsUserFile = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.XmlUserFileAttributeName).getDeviceMessageAttributeValue();
        tariffCalendarsUserFile = tariffCalendarsUserFile.replaceAll("\"", "''");

        return createMessageEntry(offlineDeviceMessage, activationDate, tariffCalendarsUserFile);
    }

    public MessageEntry createMessageEntry(OfflineDeviceMessage offlineDeviceMessage, String activationDate, String userFileContent) {
        MessageTag messageTag = new MessageTag(tag);
        messageTag.add(new MessageValue(" "));
        messageTag.add(new MessageAttribute(activationDateAttributeTag, activationDate));
        messageTag.add(new MessageAttribute(calendarFileAttributeTag, userFileContent));

        return new MessageEntry(writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }

    private String writeTag(MessageTag messageTag) {
        return SimpleTagWriter.writeTag(messageTag);
    }
}