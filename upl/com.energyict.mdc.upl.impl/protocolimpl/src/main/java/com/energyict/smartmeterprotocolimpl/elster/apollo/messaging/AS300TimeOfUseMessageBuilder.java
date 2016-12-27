package com.energyict.smartmeterprotocolimpl.elster.apollo.messaging;

import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;

import com.energyict.cbo.BusinessException;
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

    public AS300TimeOfUseMessageBuilder(TariffCalendarFinder calendarFinder) {
        super(calendarFinder);
    }

    /**
     * We override this because we can't convert the CodeTable content in a proper manner ...
     */
    @Override
    protected String getMessageContent() throws BusinessException {
        if ((getCodeId().isEmpty()) && (getUserFileId() == 0)) {
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
        if (!getCodeId().isEmpty()) {
            try {
                String xmlContent = new CodeTableXmlParsing(this.getCalendarFinder(), extractor).parseActivityCalendarAndSpecialDayTable(getCodeId(), Calendar.getInstance().getTime().before(getActivationDate())?getActivationDate().getTime():1, getName());
                addChildTag(builder, getTagCode(), getCodeId());
                addChildTag(builder, RAW_CONTENT_TAG, ProtocolTools.compress(xmlContent));
            } catch (ParserConfigurationException | IOException e) {
                throw new BusinessException(e.getMessage());
            }
        }
        if (getUserFileId() > 0) {
            if (isInlineUserFiles()) {
                builder.append("<").append(INCLUDED_USERFILE_TAG).append(">");

                // This will generate a message that will make the DeviceMessageContentParser inline the file.
                builder.append("<").append(INCLUDE_USERFILE_TAG).append(" ").append(INCLUDE_USERFILE_ID_ATTRIBUTE).append("=\"").append(getUserFileId()).append("\"");
                if (isZipMessageContent()) {
                    builder.append(" ").append(CREATEZIP_ATTRIBUTE_TAG).append("=\"true\"");
                } else if (isEncodeB64()) {
                    builder.append(" ").append(ENCODEB64_ATTRIBUTE_TAG).append("=\"true\"");
                }
                builder.append("/>");

                builder.append("</").append(INCLUDED_USERFILE_TAG).append(">");
            } else {
                addChildTag(builder, getTagUserfile(), getUserFileId());
            }
        }
        builder.append("</");
        builder.append(getMessageNodeTag());
        builder.append(">");
        return builder.toString();
    }
}
