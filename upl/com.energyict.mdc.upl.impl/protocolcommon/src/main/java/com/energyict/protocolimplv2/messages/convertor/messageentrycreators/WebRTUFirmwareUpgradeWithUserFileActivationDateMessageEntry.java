package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

/**
 * Creates a MessageEntry based on the
 * {@link com.energyict.protocolimpl.messages.RtuMessageConstant#FIRMWARE_PATH}
 * xml tag with an additional userFile and activationDate
 * <p/>
 * This is NOT for the message that is created using the FirmwareUpdateMessaging interface!
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 17:13
 */
public class WebRTUFirmwareUpgradeWithUserFileActivationDateMessageEntry implements MessageEntryCreator {

    private final String userFileIdAttributeName;
    private final String activationDateAttributeName;
    private final String firmwareUpdateImageIdentifierAttributeName;


    public WebRTUFirmwareUpgradeWithUserFileActivationDateMessageEntry(String userFileIdAttributeName, String activationDateAttributeName) {
        this.userFileIdAttributeName = userFileIdAttributeName;
        this.activationDateAttributeName = activationDateAttributeName;
        this.firmwareUpdateImageIdentifierAttributeName = null;
    }

    public WebRTUFirmwareUpgradeWithUserFileActivationDateMessageEntry(String userFileIdAttributeName, String activationDateAttributeName, String firmwareUpdateImageIdentifierAttributeName) {
        this.userFileIdAttributeName = userFileIdAttributeName;
        this.activationDateAttributeName = activationDateAttributeName;
        this.firmwareUpdateImageIdentifierAttributeName = firmwareUpdateImageIdentifierAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute userFileAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, userFileIdAttributeName);
        OfflineDeviceMessageAttribute activationDateAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, activationDateAttributeName);
        OfflineDeviceMessageAttribute firmwareUpdateImageIdentifierAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, firmwareUpdateImageIdentifierAttributeName);
        MessageTag messageTag = new MessageTag(RtuMessageConstant.FIRMWARE_UPGRADE);
        messageTag.add(new MessageAttribute(RtuMessageConstant.FIRMWARE_PATH, userFileAttribute.getValue()));

        String activationDateAttributeValue = activationDateAttribute.getValue();
        if (activationDateAttributeName != null && activationDateAttributeValue != null && !activationDateAttributeValue.isEmpty()) {
            messageTag.add(new MessageAttribute(RtuMessageConstant.FIRMWARE_ACTIVATE_DATE, activationDateAttributeValue));
        }

        String imageIdentifierAttributeValue = firmwareUpdateImageIdentifierAttribute.getValue();
        if (firmwareUpdateImageIdentifierAttributeName != null && imageIdentifierAttributeValue != null && !imageIdentifierAttributeValue.isEmpty()) {
            messageTag.add(new MessageAttribute(RtuMessageConstant.FIRMWARE_IMAGE_IDENTIFIER, imageIdentifierAttributeValue));
        }
        messageTag.add(new MessageValue(" "));
        return MessageEntry.fromContent(messagingProtocol.writeTag(messageTag)).andMessage(offlineDeviceMessage).finish();
    }
}
