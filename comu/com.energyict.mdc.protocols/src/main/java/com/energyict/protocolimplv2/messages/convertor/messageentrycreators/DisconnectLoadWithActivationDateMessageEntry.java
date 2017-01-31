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

import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

public class DisconnectLoadWithActivationDateMessageEntry implements MessageEntryCreator {

    private final String activationDateAttributeName;

    /**
     * Default constructor
     *
     * @param activationDateAttributeName the name of the OfflineDeviceMessageAttribute representing the activationDate
     */
    public DisconnectLoadWithActivationDateMessageEntry(String activationDateAttributeName) {
        this.activationDateAttributeName = activationDateAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute activationDateAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, activationDateAttributeName);
        MessageTag messageTag = new MessageTag(RtuMessageConstant.DISCONNECT_LOAD);
        messageTag.add(new MessageAttribute(RtuMessageConstant.DISCONNECT_CONTROL_ACTIVATE_DATE, activationDateAttribute.getDeviceMessageAttributeValue()));
        messageTag.add(new MessageValue(" "));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
