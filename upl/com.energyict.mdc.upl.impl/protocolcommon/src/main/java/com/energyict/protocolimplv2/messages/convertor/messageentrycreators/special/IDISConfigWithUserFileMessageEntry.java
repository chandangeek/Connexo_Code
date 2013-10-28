package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special;

import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineDeviceMessageAttribute;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagWriter;

/**
 * Generates a message with the contents of an XML user file. This file contains a full configuration for an IDIS meter.
 * <p/>
 * E.g.: <Configuration download><IncludedFile> (bytes as string) </IncludedFile></Configuration download>
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 16:54
 */
public class IDISConfigWithUserFileMessageEntry implements MessageEntryCreator {

    private final String userFileIdAttributeName;


    public IDISConfigWithUserFileMessageEntry(String userFileIdAttributeName) {
        this.userFileIdAttributeName = userFileIdAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute userFileAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, userFileIdAttributeName);
        MessageTag mainTag = new MessageTag("Configuration download");
        MessageTag subTag1 = new MessageTag(RtuMessageConstant.FIRMWARE_UPDATE_INCLUDED_FILE);
        subTag1.add(new MessageValue(userFileAttribute.getDeviceMessageAttributeValue()));  //The userFile bytes
        mainTag.add(subTag1);
        return new MessageEntry(SimpleTagWriter.writeTag(mainTag), offlineDeviceMessage.getTrackingId());
    }
}