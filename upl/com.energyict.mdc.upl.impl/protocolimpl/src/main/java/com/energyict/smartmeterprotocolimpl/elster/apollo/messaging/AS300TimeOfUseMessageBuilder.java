package com.energyict.smartmeterprotocolimpl.elster.apollo.messaging;

import com.energyict.mdc.upl.messages.legacy.DateFormatter;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;

import com.energyict.messaging.TimeOfUseMessageBuilder;
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
    private final Extractor extractor;

    public AS300TimeOfUseMessageBuilder(TariffCalendarFinder calendarFinder, DeviceMessageFileFinder messageFileFinder, DateFormatter dateFormatter, Extractor extractor) {
        super(calendarFinder, messageFileFinder, dateFormatter, extractor);
        this.extractor = extractor;
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
                String xmlContent = new CodeTableXmlParsing(this.getCalendarFinder(), this.extractor).parseActivityCalendarAndSpecialDayTable(getCalendarId(), Calendar.getInstance().getTime().before(getActivationDate())?getActivationDate().getTime():1, getName());
                addChildTag(builder, getTagCode(), getCalendarId());
                addChildTag(builder, RAW_CONTENT_TAG, ProtocolTools.compress(xmlContent));
            } catch (ParserConfigurationException | IOException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        if (!getDeviceMessageFileId().isEmpty()) {
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
