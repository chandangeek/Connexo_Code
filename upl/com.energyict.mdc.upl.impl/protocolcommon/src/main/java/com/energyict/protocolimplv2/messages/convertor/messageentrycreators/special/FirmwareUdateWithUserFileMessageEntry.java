package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special;

import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineDeviceMessageAttribute;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

/**
 * Creates a MessageEntry for the protocols that implement the FirmwareUpdateMessaging interface.
 * The contents of the message are the bytes of the userFile!
 * The message is parsed by the FirmwareUpdateMessageBuilder.
 * <p/>
 * E.g.: <FirmwareUpdate><IncludedFile> (bytes as string) </IncludedFile></FirmwareUpdate>
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 16:54
 */
public class FirmwareUdateWithUserFileMessageEntry implements MessageEntryCreator {

    private final String userFileBytesAttributeName;
    private final String resumeAttributeName;

    public FirmwareUdateWithUserFileMessageEntry(String userFileBytesAttributeName) {
        this.userFileBytesAttributeName = userFileBytesAttributeName;
        this.resumeAttributeName = null;
    }

    public FirmwareUdateWithUserFileMessageEntry(String userFileIdAttributeName, String resumeAttributeName) {
        this.userFileBytesAttributeName = userFileIdAttributeName;
        this.resumeAttributeName = resumeAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute userFileBytesAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, userFileBytesAttributeName);
        String extraTrackingId = "";
        if (resumeAttributeName != null) {
            boolean resume = Boolean.valueOf(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, resumeAttributeName).getDeviceMessageAttributeValue());
            if (!resume) {
                extraTrackingId = "noresume ";
            } else {
                // todo; other protocols might use "resume" in the trackingId ?
            }
        }

        MessageTag mainTag = new MessageTag(RtuMessageConstant.FIRMWARE_UPDATE);
        MessageTag subTag1 = new MessageTag(RtuMessageConstant.FIRMWARE_UPDATE_INCLUDED_FILE);
        subTag1.add(new MessageValue(userFileBytesAttribute.getDeviceMessageAttributeValue()));  //The userFile bytes
        mainTag.add(subTag1);
        return new MessageEntry(messagingProtocol.writeTag(mainTag), extraTrackingId + offlineDeviceMessage.getTrackingId());
    }
}