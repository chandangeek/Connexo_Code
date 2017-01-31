/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special;

import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessageAttribute;
import com.energyict.mdc.protocol.api.messaging.Messaging;

import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;
import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;

public class PartialLoadProfileMessageEntry implements MessageEntryCreator {

    private static final String MESSAGETAG = "PartialLoadProfile";

    private final String loadProfileAttributeName;
    private final String fromDateAttributeName;
    private final String toDateAttributeName;

    public PartialLoadProfileMessageEntry(String loadProfileAttributeName, String fromDateAttributeName, String toDateAttributeName) {
        this.loadProfileAttributeName = loadProfileAttributeName;
        this.fromDateAttributeName = fromDateAttributeName;
        this.toDateAttributeName = toDateAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute loadProfileAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, loadProfileAttributeName);
        OfflineDeviceMessageAttribute fromDateAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, fromDateAttributeName);
        OfflineDeviceMessageAttribute toDateAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, toDateAttributeName);
        final String partialLoadProfileMessage = LoadProfileMessageUtils.createPartialLoadProfileMessage(
                MESSAGETAG, fromDateAttribute.getDeviceMessageAttributeValue(),
                toDateAttribute.getDeviceMessageAttributeValue(),
                loadProfileAttribute.getDeviceMessageAttributeValue());
        return new MessageEntry(partialLoadProfileMessage, offlineDeviceMessage.getTrackingId());
    }
}
