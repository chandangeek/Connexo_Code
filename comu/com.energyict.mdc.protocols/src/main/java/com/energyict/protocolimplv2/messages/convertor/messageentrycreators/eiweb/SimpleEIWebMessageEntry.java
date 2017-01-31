/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb;

import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.Messaging;

public class SimpleEIWebMessageEntry extends AbstractEIWebMessageEntry {


    /**
     * Default constructor
     */
    public SimpleEIWebMessageEntry() {
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        String messageName = getMessageName(offlineDeviceMessage);
        MessageTag messageTag = new MessageTag(messageName);
        messageTag.add(new MessageValue(offlineDeviceMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue()));
        return createMessageEntry(messageTag, offlineDeviceMessage.getTrackingId());
    }
}