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

import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagWriter;

import java.util.TimeZone;

public class ContactorControlWithActivationDateAndTimezoneMessageEntry implements MessageEntryCreator {

    private final String tag;

    public ContactorControlWithActivationDateAndTimezoneMessageEntry(String tag) {
        this.tag = tag;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        MessageTag messageTag = new MessageTag(tag);
        messageTag.add(new MessageAttribute("Date (dd/mm/yyyy hh:mm)", offlineDeviceMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue()));
        messageTag.add(new MessageAttribute("TimeZone", TimeZone.getDefault().getID()));
        messageTag.add(new MessageValue(" "));
        return new MessageEntry(SimpleTagWriter.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
