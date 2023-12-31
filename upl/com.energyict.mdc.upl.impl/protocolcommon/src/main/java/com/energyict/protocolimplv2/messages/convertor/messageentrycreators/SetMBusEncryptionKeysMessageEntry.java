package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.Messaging;

import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

/**
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 14:33
 */
public class SetMBusEncryptionKeysMessageEntry implements MessageEntryCreator {

    private final String openKeyAttributeName;
    private final String transferKeyAttributeName;
    private final String tag;

    public SetMBusEncryptionKeysMessageEntry(String openKeyAttributeName, String transferKeyAttributeName) {
        this(openKeyAttributeName, transferKeyAttributeName, RtuMessageConstant.MBUS_ENCRYPTION_KEYS);
    }

    public SetMBusEncryptionKeysMessageEntry(String openKeyAttributeName, String transferKeyAttributeName, String tag) {
        this.openKeyAttributeName = openKeyAttributeName;
        this.transferKeyAttributeName = transferKeyAttributeName;
        this.tag = tag;
    }

    /**
     * XML content is <tag Open_Key_Value="aaaa" Transfer_Key_Value="bbbb"> </tag>
     */
    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        String openKey = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, openKeyAttributeName).getValue();
        String transferKey = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, transferKeyAttributeName).getValue();

        MessageTag messageTag = new MessageTag(tag);
        messageTag.add(new MessageAttribute(RtuMessageConstant.MBUS_OPEN_KEY, openKey));
        messageTag.add(new MessageAttribute(RtuMessageConstant.MBUS_TRANSFER_KEY, transferKey));
        messageTag.add(new MessageValue(" "));
        return MessageEntry.fromContent(messagingProtocol.writeTag(messageTag)).andMessage(offlineDeviceMessage).finish();
    }
}
