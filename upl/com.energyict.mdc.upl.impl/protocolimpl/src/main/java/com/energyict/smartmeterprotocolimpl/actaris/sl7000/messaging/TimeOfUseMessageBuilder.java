package com.energyict.smartmeterprotocolimpl.actaris.sl7000.messaging;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 23/04/12
 * Time: 11:59
 */

import com.energyict.cbo.BusinessException;
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
public class TimeOfUseMessageBuilder extends com.energyict.protocol.messaging.TimeOfUseMessageBuilder {

      public static final String RAW_CONTENT_TAG = "Activity_Calendar";

    /**
     * We override this because we can't convert the CodeTable content in a proper manner ...
     */
    @Override
    protected String getMessageContent() throws BusinessException {
        if ((getCodeId() == 0) && (getUserFileId() == 0)) {
            throw new BusinessException("Code or userFile needed");
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
        if (getCodeId() > 0l) {
            try {
                String xmlContent = CodeTableXmlParsing.parseActivityCalendarAndSpecialDayTable(getCodeId(), getActivationDate().getTime(), getName());
                addChildTag(builder, getTagCode(), getCodeId());
                addChildTag(builder, RAW_CONTENT_TAG, ProtocolTools.compress(xmlContent));
            } catch (ParserConfigurationException e) {
                throw new BusinessException(e.getMessage());
            } catch (IOException e) {
                throw new BusinessException(e.getMessage());
            }
        }
        builder.append("</");
        builder.append(getMessageNodeTag());
        builder.append(">");
        return builder.toString();
    }
}