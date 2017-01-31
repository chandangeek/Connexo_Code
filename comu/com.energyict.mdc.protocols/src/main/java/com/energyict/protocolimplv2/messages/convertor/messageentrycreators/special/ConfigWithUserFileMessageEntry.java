/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special;

import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.Messaging;

import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagWriter;

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
        subTag1.add(new MessageValue(userFileAttribute.getDeviceMessageAttributeValue()));  //The userFile bytes
        mainTag.add(subTag1);
        return new MessageEntry(SimpleTagWriter.writeTag(mainTag), offlineDeviceMessage.getTrackingId());
    }
}