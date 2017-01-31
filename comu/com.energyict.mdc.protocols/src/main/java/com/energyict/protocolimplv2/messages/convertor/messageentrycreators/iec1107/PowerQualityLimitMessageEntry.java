/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107;

import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.Messaging;

import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

public class PowerQualityLimitMessageEntry implements MessageEntryCreator {

    private final String powerQualityThresholdAttributeName;

    /**
     * Default constructor
     *
     * @param powerQualityThresholdAttributeName the name of the OfflineDeviceMessageAttribute representing the threshold
     */
    public PowerQualityLimitMessageEntry(String powerQualityThresholdAttributeName) {
        this.powerQualityThresholdAttributeName = powerQualityThresholdAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute thresholdAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, powerQualityThresholdAttributeName);
        MessageTag messageTag = new MessageTag("CLASS_37_UPDATE");
        messageTag.add(new MessageValue(thresholdAttribute.getDeviceMessageAttributeValue()));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
