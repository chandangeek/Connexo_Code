package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special;

import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.Messaging;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;
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
        subTag1.add(new MessageValue(userFileAttribute.getDeviceMessageAttributeValue()));  //The userFile bytes
        mainTag.add(subTag1);

        MessageTag subTag2 = new MessageTag(RtuMessageConstant.FIRMWARE_UPDATE_ACTIVATION_DATE);
        subTag2.add(new MessageValue(activationDateAttribute.getDeviceMessageAttributeValue())); // The activation date
        mainTag.add(subTag2);
        return new MessageEntry(SimpleTagWriter.writeTag(mainTag), offlineDeviceMessage.getTrackingId());
    }
}