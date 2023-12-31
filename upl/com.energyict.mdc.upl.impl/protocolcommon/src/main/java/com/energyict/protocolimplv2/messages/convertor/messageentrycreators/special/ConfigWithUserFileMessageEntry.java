package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.Messaging;

import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagWriter;

/**
 * Generates a message with the contents of an user file.
 * <p/>
 * E.g.: <tag><IncludedFile> (bytes as string) </IncludedFile></tag>
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 16:54
 */
public class ConfigWithUserFileMessageEntry implements MessageEntryCreator {

    private final String tag;
    private final String userFileIdAttributeName;


    public ConfigWithUserFileMessageEntry(String userFileIdAttributeName, String tag) {
        this.userFileIdAttributeName = userFileIdAttributeName;
        this.tag = tag;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute userFileAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, userFileIdAttributeName);
        MessageTag mainTag = new MessageTag(tag);
        MessageTag subTag1 = new MessageTag(RtuMessageConstant.FIRMWARE_UPDATE_INCLUDED_FILE);
        subTag1.add(new MessageValue(userFileAttribute.getValue()));  //The userFile bytes
        mainTag.add(subTag1);
        return MessageEntry.fromContent(SimpleTagWriter.writeTag(mainTag)).andMessage(offlineDeviceMessage).finish();
    }
}