package com.energyict.smartmeterprotocolimpl.elster.apollo.messaging;

import com.energyict.dlms.ParseUtils;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.xmlparsing.GenericDataToWrite;
import com.energyict.dlms.xmlparsing.XmlToDlms;
import com.energyict.genericprotocolimpl.common.messages.GenericMessaging;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.messaging.TimeOfUseMessageBuilder;
import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.protocolimpl.dlms.common.DlmsSession;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.elster.apollo.AS300;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 8-aug-2011
 * Time: 15:02:14
 */
public class AS300MessageExecutor {

    private final AbstractSmartDlmsProtocol protocol;

    private boolean success;
    private ActivityCalendarController activityCalendarController;

    public AS300MessageExecutor(final AbstractSmartDlmsProtocol protocol) {
        this.protocol = protocol;
    }

    private CosemObjectFactory getCosemObjectFactory() {
        return getDlmsSession().getCosemObjectFactory();
    }

    private DlmsSession getDlmsSession() {
        return getProtocol().getDlmsSession();
    }

    public AbstractSmartDlmsProtocol getProtocol() {
        return this.protocol;
    }

    public MessageResult executeMessageEntry(final MessageEntry messageEntry) {
        String content = messageEntry.getContent();
        success = true;

        try {
            if (isTimeOfUseMessage(content)) {
                updateTimeOfUse(content);
            } else if (isUpdatePricingInformationMessage(content)) {
                updatePricingInformation(content);
            } else {
                log(Level.INFO, "Message not supported : " + content);
                success = false;
            }
        } catch (IOException e) {
            log(Level.SEVERE, "Message failed : " + e.getMessage());
            success = false;
        }

        if (success) {
            log(Level.INFO, "Message has finished.");
            return MessageResult.createSuccess(messageEntry);
        } else {
            return MessageResult.createFailed(messageEntry);
        }
    }

    private void updatePricingInformation(final String content) throws IOException {
        log(Level.INFO, "Received update Update Pricing Information message.");
        log(Level.FINEST, "Getting UserFile from message");
        String includedFile = getIncludedContent(content);
        try {
            if (includedFile.length() > 0) {
                log(Level.FINEST, "Sending out the PricingInformation objects.");
                handleXmlToDlms(includedFile);
            } else {
                log(Level.WARNING, "Length of the PricingInformation UserFile is not valid [" + includedFile.length() + " bytes], failing message.");
                success = false;
            }
        } catch (SAXException e) {
            log(Level.SEVERE, "Cannot process ActivityCalendar upgrade message due to an XML parsing error [" + e.getMessage() + "]");
            success = false;
        }
    }

    private String getIncludedContent(final String content) {
        int begin = content.indexOf(GenericMessaging.INCLUDED_USERFILE_TAG) + GenericMessaging.INCLUDED_USERFILE_TAG.length() + 1;
        int end = content.indexOf(GenericMessaging.INCLUDED_USERFILE_TAG, begin) - 2;
        return content.substring(begin, end);
    }

    private void updateTimeOfUse(final String content) throws IOException {
        log(Level.INFO, "Received update ActivityCalendar message.");
        final AS300TimeOfUseMessageBuilder builder = new AS300TimeOfUseMessageBuilder();

        try {
            builder.initFromXml(content);

            if (builder.getCodeId() > 0) { // codeTable implementation
                log(Level.FINEST, "Parsing the content of the CodeTable.");
                getActivityCalendarController().parseContent(content);
                log(Level.FINEST, "Setting the new Passive Calendar Name.");
                getActivityCalendarController().writeCalendarName("");
                log(Level.FINEST, "Sending out the new Passive Calendar objects.");
                getActivityCalendarController().writeCalendar();
            } else if (builder.getUserFile() != null) { // userFile implementation
                log(Level.FINEST, "Getting UserFile from message");
                final byte[] userFileData = builder.getUserFile().loadFileInByteArray();
                if (userFileData.length > 0) {
                    log(Level.FINEST, "Sending out the new Passive Calendar objects.");
                    handleXmlToDlms(new String(userFileData, "US-ASCII"));
                } else {
                    log(Level.WARNING, "Length of the ActivityCalendar UserFile is not valid [" + userFileData.length + " bytes], failing message.");
                    success = false;
                }
            }

        } catch (SAXException e) {
            log(Level.SEVERE, "Cannot process ActivityCalendar upgrade message due to an XML parsing error [" + e.getMessage() + "]");
            success = false;
        }
    }

    /**
     * Parse to given xmlContent to GenericDataToWrite objects and send them over to the device
     *
     * @param xmlContent the xml DLMS content
     * @throws IOException  if writing to the device failed
     * @throws SAXException if parsing the xmlContent failed
     */
    private void handleXmlToDlms(String xmlContent) throws IOException, SAXException {
        XmlToDlms x2d = new XmlToDlms(getDlmsSession());
        List<GenericDataToWrite> gdtwList = x2d.parseSetRequests(new String(ProtocolTools.decompressBytes(xmlContent), "US-ASCII"));
        log(Level.FINEST, "Parsed message XML content, staring to write...");
        for (GenericDataToWrite gdtw : gdtwList) {
            try {
                gdtw.writeData();
            } catch (IOException e) {
                throw new IOException("Could not write [" + ParseUtils.decimalByteToString(gdtw.getDataToWrite()) + "] to object " + ObisCode.fromByteArray(gdtw.getGenericWrite().getObjectReference().getLn()) + ", message will fail.");
            }
        }
    }

    private boolean isTimeOfUseMessage(final String messageContent) {
        return (messageContent != null) && messageContent.contains(TimeOfUseMessageBuilder.getMessageNodeTag());
    }

    private boolean isUpdatePricingInformationMessage(final String messageContent) {
        return (messageContent != null) && messageContent.contains(RtuMessageConstant.UPDATE_PRICING_INFORMATION);
    }

    public ActivityCalendarController getActivityCalendarController() {
        if (this.activityCalendarController == null) {
            this.activityCalendarController = new AS300ActivityCalendarController((AS300) this.protocol);
        }
        return activityCalendarController;
    }

    private void log(Level level, String message) {
        this.protocol.getLogger().log(level, message);
    }
}
