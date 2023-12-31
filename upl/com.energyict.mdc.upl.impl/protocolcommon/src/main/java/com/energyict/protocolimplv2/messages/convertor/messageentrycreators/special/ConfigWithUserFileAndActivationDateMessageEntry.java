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
 * E.g.: <tag><IncludedFile> (bytes as string) </IncludedFile><ActivationDate> date </ActivationDate></tag>
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 16:54
 */
public class ConfigWithUserFileAndActivationDateMessageEntry implements MessageEntryCreator {

    private final String tag;
    private final String userFileIdAttributeName;
    private final String activationDateAttributeName;


    public ConfigWithUserFileAndActivationDateMessageEntry(String userFileIdAttributeName, String activationDateAttributeName,  String tag) {
        this.tag = tag;
        this.userFileIdAttributeName = userFileIdAttributeName;
        this.activationDateAttributeName = activationDateAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute userFileAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, userFileIdAttributeName);
        OfflineDeviceMessageAttribute activationDateAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, activationDateAttributeName);
        MessageTag mainTag = new MessageTag(tag);

        MessageTag subTag1 = new MessageTag(RtuMessageConstant.FIRMWARE_UPDATE_INCLUDED_FILE);
        subTag1.add(new MessageValue(userFileAttribute.getValue()));  //The userFile bytes
        mainTag.add(subTag1);

        MessageTag subTag2 = new MessageTag(RtuMessageConstant.FIRMWARE_UPDATE_ACTIVATION_DATE);
        subTag2.add(new MessageValue(activationDateAttribute.getValue())); // The activation date
        mainTag.add(subTag2);
        return MessageEntry.fromContent(SimpleTagWriter.writeTag(mainTag)).andMessage(offlineDeviceMessage).finish();
    }
}