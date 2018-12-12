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
public class A1SpecialDaysMessageEntry implements MessageEntryCreator {

    private final String tag = "SetSpecialDaysTable";
    private final String attributeTag = "SPECIAL_DAYS_TABLE_FILE";

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        String specialDaysUserFile = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.XmlUserFileAttributeName).getValue();
        specialDaysUserFile = specialDaysUserFile.replaceAll("\"", "''");

        return createMessageEntry(offlineDeviceMessage, specialDaysUserFile);
    }

    public MessageEntry createMessageEntry(OfflineDeviceMessage offlineDeviceMessage, String userFileContent) {
        MessageTag messageTag = new MessageTag(tag);
        messageTag.add(new MessageValue(" "));
        messageTag.add(new MessageAttribute(attributeTag, userFileContent));

        return MessageEntry
                    .fromContent(writeTag(messageTag))
                    .andMessage(offlineDeviceMessage)
                    .finish();
    }

    private String writeTag(MessageTag messageTag) {
        return SimpleTagWriter.writeTag(messageTag);

    }
}