/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.Messaging;

import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

public class SetStartOfDSTMessageEntry implements MessageEntryCreator {

    private final String monthAttributeName;
    private final String dayOfMonthAttributeName;
    private final String dayOfWeekAttributeName;
    private final String hourAttributeName;

    /**
     * Default constructor
     */
    public SetStartOfDSTMessageEntry(String monthAttributeName, String dayOfMonthAttributeName, String dayOfWeekAttributeName, String hourAttributeName) {
        this.monthAttributeName = monthAttributeName;
        this.dayOfMonthAttributeName = dayOfMonthAttributeName;
        this.dayOfWeekAttributeName = dayOfWeekAttributeName;
        this.hourAttributeName = hourAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute month = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, monthAttributeName);
        OfflineDeviceMessageAttribute dayOfMonth = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, dayOfMonthAttributeName);
        OfflineDeviceMessageAttribute dayOfWeek = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, dayOfWeekAttributeName);
        OfflineDeviceMessageAttribute hour = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, hourAttributeName);
        MessageTag messageTag = new MessageTag("StartOfDST");
        messageTag.add(new MessageAttribute("Month", month.getDeviceMessageAttributeValue()));
        messageTag.add(new MessageAttribute("Day of month", dayOfMonth.getDeviceMessageAttributeValue()));
        messageTag.add(new MessageAttribute("Day of week", dayOfWeek.getDeviceMessageAttributeValue()));
        messageTag.add(new MessageAttribute("Hour", hour.getDeviceMessageAttributeValue()));
        messageTag.add(new MessageValue(" "));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}