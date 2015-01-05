package com.energyict.protocolimpl.dlms.siemenszmd;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 11/01/12
 * Time: 15:32
 */

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.protocol.api.UserFileFactory;
import com.energyict.mdc.protocol.api.codetables.CodeFactory;

import com.energyict.protocols.messaging.TimeOfUseMessageBuilder;
import com.energyict.protocolimpl.messages.codetableparsing.CodeTableXmlParsing;
import com.energyict.protocolimpl.utils.ProtocolTools;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * MessageBuilder for the TimeOfUse Message.
 */
public class ZMDTimeOfUseMessageBuilder extends TimeOfUseMessageBuilder {

    public static final String RAW_CONTENT_TAG = "Activity_Calendar";

    public ZMDTimeOfUseMessageBuilder(CodeFactory codeFactory, UserFileFactory userFileFactory) {
        super(codeFactory, userFileFactory);
    }

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