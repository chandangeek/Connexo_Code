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

public class ConnectControlModeMessageEntry implements MessageEntryCreator {

    private final String connectModeAttributeName;

    /**
     * Default constructor
     *
     * @param connectModeAttributeName the name of the OfflineDeviceMessageAttribute representing the connect mode
     */
    public ConnectControlModeMessageEntry(String connectModeAttributeName) {
        this.connectModeAttributeName = connectModeAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute connectModeAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, connectModeAttributeName);
        MessageTag messageTag = new MessageTag(RtuMessageConstant.CONNECT_CONTROL_MODE);
        messageTag.add(new MessageAttribute(RtuMessageConstant.CONNECT_MODE, connectModeAttribute.getDeviceMessageAttributeValue()));
        messageTag.add(new MessageValue(" "));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
