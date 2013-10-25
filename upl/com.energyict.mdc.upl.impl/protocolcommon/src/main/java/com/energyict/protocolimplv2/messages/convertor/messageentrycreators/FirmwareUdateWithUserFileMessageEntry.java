package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

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

    private final String userFileIdAttributeName;


    public FirmwareUdateWithUserFileMessageEntry(String userFileIdAttributeName) {
        this.userFileIdAttributeName = userFileIdAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute userFileAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, userFileIdAttributeName);
        MessageTag mainTag = new MessageTag(RtuMessageConstant.FIRMWARE_UPDATE);
        MessageTag subTag1 = new MessageTag(RtuMessageConstant.FIRMWARE_UPDATE_INCLUDED_FILE);
        subTag1.add(new MessageValue(userFileAttribute.getDeviceMessageAttributeValue()));  //The userFile bytes
        mainTag.add(subTag1);
        return new MessageEntry(messagingProtocol.writeTag(mainTag), offlineDeviceMessage.getTrackingId());
    }
}