package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineDeviceMessageAttribute;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

/**
 * Creates a MessageEntry based on the
 * {@link com.energyict.protocolimpl.messages.RtuMessageConstant#FIRMWARE}
 * xml tag with an additional userFile and activationDate
 *
 * This is NOT for the message that is created using the FirmwareUpdateMessaging interface!
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 17:13
 */
public class WebRTUFirmwareUpgradeWithUserFileActivationDateMessageEntry implements MessageEntryCreator {

    private final String userFileIdAttributeName;
    private final String activationDateAttributeName;


    public WebRTUFirmwareUpgradeWithUserFileActivationDateMessageEntry(String userFileIdAttributeName, String activationDateAttributeName) {
        this.userFileIdAttributeName = userFileIdAttributeName;
        this.activationDateAttributeName = activationDateAttributeName;

    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute userFileAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, userFileIdAttributeName);
        OfflineDeviceMessageAttribute activationDateAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, activationDateAttributeName);
        MessageTag messageTag = new MessageTag(RtuMessageConstant.FIRMWARE_UPGRADE);
        messageTag.add(new MessageAttribute(RtuMessageConstant.FIRMWARE, userFileAttribute.getDeviceMessageAttributeValue()));
        messageTag.add(new MessageAttribute(RtuMessageConstant.FIRMWARE_ACTIVATE_DATE, activationDateAttribute.getDeviceMessageAttributeValue()));
        messageTag.add(new MessageValue(" "));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
