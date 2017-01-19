package com.energyict.smartmeterprotocolimpl.actaris.sl7000.messaging;

import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.Formatter;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;

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
public class TimeOfUseMessageBuilder extends com.energyict.messaging.TimeOfUseMessageBuilder {

    public static final String RAW_CONTENT_TAG = "Activity_Calendar";

    public TimeOfUseMessageBuilder(TariffCalendarFinder calendarFinder, DeviceMessageFileFinder messageFileFinder, Formatter formatter, DeviceMessageFileExtractor deviceMessageFileExtractor, TariffCalendarExtractor tariffCalendarExtractor) {
        super(calendarFinder, tariffCalendarExtractor, messageFileFinder, deviceMessageFileExtractor, formatter);
    }

    /**
     * We override this because we can't convert the CodeTable content in a proper manner ...
     */
    @Override
    protected String getMessageContent() {
        if ((getCalendarId().isEmpty()) && (getDeviceMessageFileId().isEmpty())) {
            throw new IllegalArgumentException("Code or userFile needed");
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
        if (!getCalendarId().isEmpty()) {
            try {
                String xmlContent = new CodeTableXmlParsing(this.getCalendarFinder(), this.getCalendarExtractor()).parseActivityCalendarAndSpecialDayTable(getCalendarId(), getActivationDate().getTime(), getName());
                addChildTag(builder, getTagCode(), getCalendarId());
                addChildTag(builder, RAW_CONTENT_TAG, ProtocolTools.compress(xmlContent));
            } catch (ParserConfigurationException | IOException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        builder.append("</");
        builder.append(getMessageNodeTag());
        builder.append(">");
        return builder.toString();
    }
}