package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineDeviceMessageAttribute;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.MessageValueSpec;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

import java.util.Arrays;
import java.util.List;

/**
 * Creates a MessageEntry based on the
 * {@link com.energyict.protocolimpl.messages.RtuMessageConstant#FIRMWARE}
 * xml tag with an additional userFile
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 16:54
 */
public class FirmwareUpgradeWithUserFileMessageEntry implements MessageEntryCreator {

    private final String userFileIdAttributeName;


    public FirmwareUpgradeWithUserFileMessageEntry(String userFileIdAttributeName) {
        this.userFileIdAttributeName = userFileIdAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute userFileAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, userFileIdAttributeName);
        MessageTag messageTag = new MessageTag(RtuMessageConstant.FIRMWARE_UPGRADE);
        messageTag.add(new MessageAttribute(RtuMessageConstant.FIRMWARE, userFileAttribute.getDeviceMessageAttributeValue()));
        messageTag.add(new MessageValue(" "));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
