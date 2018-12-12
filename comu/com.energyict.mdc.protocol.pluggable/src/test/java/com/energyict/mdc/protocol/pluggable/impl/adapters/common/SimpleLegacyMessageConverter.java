/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.elster.jupiter.calendar.Calendar;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.properties.PropertySpec;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class SimpleLegacyMessageConverter implements LegacyMessageConverter {

    public static final String calendarFormattingResult = "ThisIsTheCalendarFormattingResult";
    public static final String dateFormattingResult = "ThisIsTheDateFormattingResult";

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Collections.emptyList();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (Calendar.class.isAssignableFrom(messageAttribute.getClass())) {
            return calendarFormattingResult;
        } else if (Date.class.isAssignableFrom(messageAttribute.getClass())) {
            return dateFormattingResult;
        }
        return messageAttribute.toString();
    }

    @Override
    public MessageEntry toMessageEntry(OfflineDeviceMessage offlineDeviceMessage) {
        return MessageEntry.fromContent(offlineDeviceMessage.toString()).trackingId("").finish();
    }

    @Override
    public void setMessagingProtocol(Messaging messagingProtocol) {
        // nothing to do
    }

}