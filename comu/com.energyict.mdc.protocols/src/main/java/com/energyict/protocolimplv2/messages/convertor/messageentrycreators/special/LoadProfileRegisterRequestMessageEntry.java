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

public class LoadProfileRegisterRequestMessageEntry implements MessageEntryCreator {

    private static final String MESSAGETAG = "LoadProfileRegister";

    private final String loadProfileAttributeName;
    private final String fromDateAttributeName;

    public LoadProfileRegisterRequestMessageEntry(String loadProfileAttributeName, String fromDateAttributeName) {
        this.loadProfileAttributeName = loadProfileAttributeName;
        this.fromDateAttributeName = fromDateAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute loadProfileAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, loadProfileAttributeName);
        OfflineDeviceMessageAttribute fromDateAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, fromDateAttributeName);
        final String loadProfileRegisterMessage = LoadProfileMessageUtils.createLoadProfileRegisterMessage(
                MESSAGETAG,
                fromDateAttribute.getDeviceMessageAttributeValue(),
                loadProfileAttribute.getDeviceMessageAttributeValue());

        return new MessageEntry(loadProfileRegisterMessage, offlineDeviceMessage.getTrackingId());
    }
}
