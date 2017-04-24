/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.actaris.sl7000.messaging;

import com.elster.jupiter.calendar.CalendarService;
import com.energyict.mdc.protocol.api.DeviceMessageFileService;

import com.energyict.protocolimpl.messages.codetableparsing.CodeTableXmlParsing;
import com.energyict.protocolimpl.utils.ProtocolTools;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class TimeOfUseMessageBuilder extends com.energyict.protocols.messaging.TimeOfUseMessageBuilder {

    public static final String RAW_CONTENT_TAG = "Activity_Calendar";

    public TimeOfUseMessageBuilder(CalendarService calendarService, DeviceMessageFileService deviceMessageFileService) {
        super(calendarService, deviceMessageFileService);
    }

    /**
     * We override this because we can't convert the CodeTable content in a proper manner ...
     */
    @Override
    protected String getMessageContent() throws ParserConfigurationException, IOException {
        if ((getCalendarId() == 0) && (getDeviceMessageFileId() == 0)) {
            throw new IllegalArgumentException("Calendar or device message file needed");
        }
        StringBuilder builder = new StringBuilder();
        builder.append("<");
        builder.append(getMessageNodeTag());
        if (getName() != null) {
            addAttribute(builder, getAttributeName(), getName());
        }
        if (getActivationDate() != null) {
            addAttribute(builder, getAttributeActivationDate(), getActivationDate().getTime() / 1000);
        }
        builder.append(">");
        if (getCalendarId() > 0L) {
            String xmlContent = CodeTableXmlParsing.parseActivityCalendarAndSpecialDayTable(this.getCalendar(), getActivationDate().getTime(), getName());
            addChildTag(builder, getTagCode(), getCalendarId());
            addChildTag(builder, RAW_CONTENT_TAG, ProtocolTools.compress(xmlContent));
        }
        builder.append("</");
        builder.append(getMessageNodeTag());
        builder.append(">");
        return builder.toString();
    }

}