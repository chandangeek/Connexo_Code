/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.MessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.Messaging;

import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

public class SetMBusEncryptionKeysMessageEntry implements MessageEntryCreator {

    private final String openKeyAttributeName;
    private final String transferKeyAttributeName;

    public SetMBusEncryptionKeysMessageEntry(String openKeyAttributeName, String transferKeyAttributeName) {
        this.openKeyAttributeName = openKeyAttributeName;
        this.transferKeyAttributeName = transferKeyAttributeName;
    }

    /**
     * XML content is <Set_Encryption_keys Open_Key_Value="aaaa" Transfer_Key_Value="bbbb"> </Set_Encryption_keys>
     */
    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        String openKey = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, openKeyAttributeName).getDeviceMessageAttributeValue();
        String transferKey = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, transferKeyAttributeName).getDeviceMessageAttributeValue();

        MessageTag messageTag = new MessageTag(RtuMessageConstant.MBUS_ENCRYPTION_KEYS);
        messageTag.add(new MessageAttribute(RtuMessageConstant.MBUS_OPEN_KEY, openKey));
        messageTag.add(new MessageAttribute(RtuMessageConstant.MBUS_TRANSFER_KEY, transferKey));
        messageTag.add(new MessageValue(" "));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
