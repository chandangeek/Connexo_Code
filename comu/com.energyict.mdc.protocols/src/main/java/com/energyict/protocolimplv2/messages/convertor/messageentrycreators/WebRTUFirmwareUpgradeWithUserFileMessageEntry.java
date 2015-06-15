package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.Messaging;

import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

/**
 * Creates a MessageEntry based on the
 * {@link com.energyict.protocolimpl.messages.RtuMessageConstant#FIRMWARE_CONTENT}
 * xml tag with an additional userFile
 * <p/>
 * This is NOT for the message that is created using the FirmwareUpdateMessaging interface!
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 16:54
 */
public class WebRTUFirmwareUpgradeWithUserFileMessageEntry implements MessageEntryCreator {

    private final String userFileIdAttributeName;


    public WebRTUFirmwareUpgradeWithUserFileMessageEntry(String userFileIdAttributeName) {
        this.userFileIdAttributeName = userFileIdAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute userFileAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, userFileIdAttributeName);
        MessageTag messageTag = new MessageTag(RtuMessageConstant.FIRMWARE_UPGRADE);
        messageTag.add(new MessageAttribute(RtuMessageConstant.FIRMWARE_CONTENT, userFileAttribute.getDeviceMessageAttributeValue()));
        messageTag.add(new MessageValue(" "));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
