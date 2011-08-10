package com.energyict.smartmeterprotocolimpl.elster.apollo.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.TimePeriod;
import com.energyict.cpo.*;
import com.energyict.cuo.core.DesktopDecorator;
import com.energyict.dynamicattributes.AttributeType;
import com.energyict.mdw.core.*;
import com.energyict.mdw.relation.*;
import com.energyict.mdw.shadow.UserFileShadow;
import com.energyict.metadata.TypeId;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.dlms.as220.parsing.CodeTableXml;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.xml.sax.SAXException;
import sun.misc.BASE64Encoder;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 8-aug-2011
 * Time: 15:27:28
 */
public class AS300TimeOfUseMessageBuilder extends TimeOfUseMessageBuilder {

    public static final String RAW_CONTENT_TAG = "Activity_Calendar";

    /**
     * {@inheritDoc}
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
                String xmlContent = CodeTableXml.parseActivityCalendarAndSpecialDayTable(getCodeId(), getActivationDate().getTime());
                addChildTag(builder, getTagCode(), getCodeId());
                addChildTag(builder, RAW_CONTENT_TAG, ProtocolTools.compress(xmlContent));
            } catch (ParserConfigurationException e) {
                throw new BusinessException(e.getMessage());
            } catch (IOException e) {
                throw new BusinessException(e.getMessage());
            }
        }
        if (getUserFileId() > 0) {
            if (isInlineUserFiles()) {
                builder.append("<").append(INCLUDED_USERFILE_TAG).append(">");

                // This will generate a message that will make the RtuMessageContentParser inline the file.
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
