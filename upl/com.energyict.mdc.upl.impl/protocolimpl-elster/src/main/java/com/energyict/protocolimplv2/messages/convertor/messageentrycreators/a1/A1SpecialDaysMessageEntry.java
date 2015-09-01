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
public class A1SpecialDaysMessageEntry implements MessageEntryCreator {

    private final String tag = "SetSpecialDaysTable";
    private final String attributeTag = "SPECIAL_DAYS_TABLE_FILE";

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        String specialDaysUserFile = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.XmlUserFileAttributeName).getDeviceMessageAttributeValue();
        specialDaysUserFile = specialDaysUserFile.replaceAll("\"", "''");

        return createMessageEntry(offlineDeviceMessage, specialDaysUserFile);
    }

    public MessageEntry createMessageEntry(OfflineDeviceMessage offlineDeviceMessage, String userFileContent) {
        MessageTag messageTag = new MessageTag(tag);
        messageTag.add(new MessageValue(" "));
        messageTag.add(new MessageAttribute(attributeTag, userFileContent));

        return new MessageEntry(writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }

    private String writeTag(MessageTag messageTag) {
        return SimpleTagWriter.writeTag(messageTag);

    }
}