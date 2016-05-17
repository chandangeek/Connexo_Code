package com.energyict.smartmeterprotocolimpl.actaris.sl7000.messaging;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 23/04/12
 * Time: 11:59
 */

import com.elster.jupiter.calendar.CalendarService;
import com.energyict.mdc.protocol.api.DeviceMessageFileService;

import com.energyict.protocolimpl.messages.codetableparsing.CodeTableXmlParsing;
import com.energyict.protocolimpl.utils.ProtocolTools;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 3/04/12
 * Time: 13:40
 */
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
        if (getCalendarId() > 0l) {
            String xmlContent = CodeTableXmlParsing.parseActivityCalendarAndSpecialDayTable(getCalendarId(), getActivationDate().getTime(), getName());
            addChildTag(builder, getTagCode(), getCalendarId());
            addChildTag(builder, RAW_CONTENT_TAG, ProtocolTools.compress(xmlContent));
        }
        builder.append("</");
        builder.append(getMessageNodeTag());
        builder.append(">");
        return builder.toString();
    }

}