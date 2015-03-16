package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessageAttribute;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.Messaging;

import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

/**
 * Creates a MessageEntry consisting of a tag with the content of a UserFile included as value, as is used for the IEC1107 ABBA230 protocol
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 16:54
 */
public class ABBA230UserFileMessageEntry implements MessageEntryCreator {

    private final String tag;

    public ABBA230UserFileMessageEntry(String tag) {
        this.tag = tag;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        String userFileAttributeName;
        if (offlineDeviceMessage.getDeviceMessageId().equals(DeviceMessageId.CONFIGURATION_CHANGE_UPLOAD_METER_SCHEME)) {
            userFileAttributeName = DeviceMessageConstants.MeterScheme;
        }
        else {
            userFileAttributeName = DeviceMessageConstants.firmwareUpdateFileAttributeName;
        }

        OfflineDeviceMessageAttribute userFileAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, userFileAttributeName);
        String fileContent = userFileAttribute.getDeviceMessageAttributeValue();

        MessageTag messageTag = new MessageTag(tag);
        messageTag.add(new MessageValue(fileContent));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }

}
