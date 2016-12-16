package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;

import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
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
        String userFileAttributeName = getMessageName(offlineDeviceMessage).equals(ConfigurationChangeDeviceMessage.UploadSwitchPointClockSettings.name())
                ? DeviceMessageConstants.SwitchPointClockSettings
                : DeviceMessageConstants.SwitchPointClockUpdateSettings;

        OfflineDeviceMessageAttribute userFileAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, userFileAttributeName);
        String fileContent = userFileAttribute.getValue();

        MessageTag messageTag = new MessageTag(tag);
        messageTag.add(new MessageValue(fileContent));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }

    /**
     * Creates the message parent tag based on the name of the given deviceMessage spec enum.
     */
    protected String getMessageName(OfflineDeviceMessage offlineDeviceMessage) {
        String messageName = ((Enum) offlineDeviceMessage.getSpecification()).name();
        return messageName;
    }
}
