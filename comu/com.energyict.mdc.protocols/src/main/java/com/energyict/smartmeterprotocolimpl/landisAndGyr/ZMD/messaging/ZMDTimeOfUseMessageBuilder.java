package com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD.messaging;

import com.energyict.mdc.protocol.api.DeviceMessageFileService;
import com.energyict.mdc.protocol.api.codetables.CodeFactory;
import com.energyict.protocols.messaging.TimeOfUseMessageBuilder;

import com.energyict.protocolimpl.messages.codetableparsing.CodeTableXmlParsing;
import com.energyict.protocolimpl.utils.ProtocolTools;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * MessageBuilder for the TimeOfUse Message.
 * Copyrights EnergyICT
 * User: sva
 * Date: 20/12/11
 * Time: 15:09
 */
public class ZMDTimeOfUseMessageBuilder extends TimeOfUseMessageBuilder {

    public static final String RAW_CONTENT_TAG = "Activity_Calendar";

    public ZMDTimeOfUseMessageBuilder(CalendarService calendarService, DeviceMessageFileService deviceMessageFileService) {
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