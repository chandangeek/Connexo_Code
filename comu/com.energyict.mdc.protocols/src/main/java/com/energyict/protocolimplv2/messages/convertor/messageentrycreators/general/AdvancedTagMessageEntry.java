/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general;

import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.Messaging;

import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

public class AdvancedTagMessageEntry implements MessageEntryCreator {

    private final String tag;

    public AdvancedTagMessageEntry(String tag) {
        this.tag = tag;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        String deviceMessageAttributeValue = offlineDeviceMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
        MessageTag messageTag = new MessageTag(tag + deviceMessageAttributeValue);
        messageTag.add(new MessageValue(" "));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}