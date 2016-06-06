package com.energyict.smartmeterprotocolimpl.elster.apollo.messaging;

import com.elster.jupiter.calendar.CalendarService;
import com.energyict.mdc.protocol.api.DeviceMessageFileService;
import com.energyict.protocols.messaging.TimeOfUseMessageBuilder;

import com.energyict.protocolimpl.messages.codetableparsing.CodeTableXmlParsing;
import com.energyict.protocolimpl.utils.ProtocolTools;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Calendar;

/**
 * MessageBuilder for the TimeOfUse Message.
 */
public class AS300TimeOfUseMessageBuilder extends TimeOfUseMessageBuilder {

    public static final String RAW_CONTENT_TAG = "Activity_Calendar";

    public AS300TimeOfUseMessageBuilder(CalendarService calendarService, DeviceMessageFileService deviceMessageFileService) {
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
            String xmlContent = CodeTableXmlParsing.parseActivityCalendarAndSpecialDayTable(this.getCalendar(), Calendar.getInstance().getTime().before(getActivationDate())?getActivationDate().getTime():1, getName());
            addChildTag(builder, getTagCode(), getCalendarId());
            addChildTag(builder, RAW_CONTENT_TAG, ProtocolTools.compress(xmlContent));
        }
        if (getDeviceMessageFileId() > 0) {
            if (isInlineUserFiles()) {
                builder.append("<").append(INCLUDED_USERFILE_TAG).append(">");

                // This will generate a message that will make the DeviceMessageContentParser inline the file.
                builder.append("<").append(INCLUDE_USERFILE_TAG).append(" ").append(INCLUDE_USERFILE_ID_ATTRIBUTE).append("=\"").append(getDeviceMessageFileId()).append("\"");
                if (isZipMessageContent()) {
                    builder.append(" ").append(CREATEZIP_ATTRIBUTE_TAG).append("=\"true\"");
                } else if (isEncodeB64()) {
                    builder.append(" ").append(ENCODEB64_ATTRIBUTE_TAG).append("=\"true\"");
                }
                builder.append("/>");

                builder.append("</").append(INCLUDED_USERFILE_TAG).append(">");
            } else {
                addChildTag(builder, getTagUserfile(), getDeviceMessageFileId());
            }
        }
        builder.append("</");
        builder.append(getMessageNodeTag());
        builder.append(">");
        return builder.toString();
    }

}