package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessageAttribute;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.Messaging;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

/**
 * Creates a MessageEntry consisting of a tag with the content of a UserFile included as value, as is used for the IEC1107 ABBA1350 protocol
 *
 * @author sva
 * @since 25/10/13 - 9:42
 */
public class ABBA1350UserFileMessageEntry implements MessageEntryCreator {

    private final String tag;


    public ABBA1350UserFileMessageEntry(String tag) {
        this.tag = tag;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        String userFileAttributeName;
        if (offlineDeviceMessage.getDeviceMessageId().equals(DeviceMessageId.CONFIGURATION_CHANGE_UPLOAD_SWITCH_POINT_CLOCK_SETTINGS)) {
            userFileAttributeName = DeviceMessageConstants.SwitchPointClockSettings;
        }
        else {
            userFileAttributeName = DeviceMessageConstants.SwitchPointClockUpdateSettings;
        }

        OfflineDeviceMessageAttribute userFileAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, userFileAttributeName);
        String fileContent = userFileAttribute.getDeviceMessageAttributeValue();

        MessageTag messageTag = new MessageTag(tag);
        messageTag.add(new MessageValue(fileContent));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }

}
