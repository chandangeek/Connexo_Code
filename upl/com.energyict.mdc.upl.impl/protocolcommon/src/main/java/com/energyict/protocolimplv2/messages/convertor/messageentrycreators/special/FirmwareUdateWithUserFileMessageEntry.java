package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;

import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagWriter;

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
    private final String typeAttributeName;

    public FirmwareUdateWithUserFileMessageEntry(String userFileBytesAttributeName) {
        this(userFileBytesAttributeName, null);
    }

    public FirmwareUdateWithUserFileMessageEntry(String userFileBytesAttributeName, String resumeAttributeName) {
        this(userFileBytesAttributeName, resumeAttributeName, null);
    }

    public FirmwareUdateWithUserFileMessageEntry(String userFileBytesAttributeName, String resumeAttributeName, String typeAttributeName) {
        this.userFileBytesAttributeName = userFileBytesAttributeName;
        this.resumeAttributeName = resumeAttributeName;
        this.typeAttributeName = typeAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute userFileBytesAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, userFileBytesAttributeName);
        String extraTrackingId = "";
        String extraTrackingId2 = "";
        if (resumeAttributeName != null) {
            boolean resume = Boolean.valueOf(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, resumeAttributeName).getValue());
            if (!resume) {
                extraTrackingId = "noresume ";
            } else {
                // todo; other protocols might use "resume" in the trackingId ?
            }
        }
        if (typeAttributeName != null) {   //Attribute that indicates the firmware upgrade type (true: PLC, false: normal)
            boolean plc = Boolean.valueOf(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, typeAttributeName).getValue());
            if (plc) {
                extraTrackingId2 = "plc ";   //Add "plc" to the trackingId in case of PLC firmware upgrade
            }
        }

        MessageTag mainTag = new MessageTag(RtuMessageConstant.FIRMWARE_UPDATE);
        MessageTag subTag = new MessageTag(RtuMessageConstant.FIRMWARE_UPDATE_INCLUDED_FILE);
        subTag.add(new MessageValue(userFileBytesAttribute.getValue()));  //The userFile bytes
        mainTag.add(subTag);
        return new MessageEntry(SimpleTagWriter.writeTag(mainTag), extraTrackingId + extraTrackingId2 + offlineDeviceMessage.getTrackingId());
    }
}