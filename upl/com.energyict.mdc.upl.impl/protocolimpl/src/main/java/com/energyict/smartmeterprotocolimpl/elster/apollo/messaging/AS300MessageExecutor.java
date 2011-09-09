package com.energyict.smartmeterprotocolimpl.elster.apollo.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.dlms.ParseUtils;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.xmlparsing.GenericDataToWrite;
import com.energyict.dlms.xmlparsing.XmlToDlms;
import com.energyict.genericprotocolimpl.common.GenericMessageExecutor;
import com.energyict.genericprotocolimpl.common.messages.GenericMessaging;
import com.energyict.genericprotocolimpl.common.messages.MessageHandler;
import com.energyict.genericprotocolimpl.nta.messagehandling.NTAMessageHandler;
import com.energyict.mdw.core.RtuMessage;
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
import sun.misc.BASE64Decoder;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 8-aug-2011
 * Time: 15:02:14
 */
public class AS300MessageExecutor extends GenericMessageExecutor {

    private static final ObisCode ChangeOfSupplierNameObisCode = ObisCode.fromString("1.0.1.64.0.255");
    private static final ObisCode ChangeOfSupplierIdObisCode = ObisCode.fromString("1.0.1.64.1.255");
    private static final ObisCode ChangeOfTennantObisCode = ObisCode.fromString("0.128.128.0.0.255");

    private final AbstractSmartDlmsProtocol protocol;

    private boolean success;

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
            if (isFirmwareUpdateMessage(content)) {
                updateFirmware(content);
            } else if (isTimeOfUseMessage(content)) {
                updateTimeOfUse(content);
            } else if (isUpdatePricingInformationMessage(content)) {
                updatePricingInformation(content);
            } else {

                MessageHandler messageHandler = new NTAMessageHandler();
                importMessage(content, messageHandler);

                if (isChangeOfTenantMessage(messageHandler)) {
                    changeOfTenantMessage(messageHandler);
                } else if (isChangeOfSupplierMessage(messageHandler)) {
                    changeOfSupplier(messageHandler);
                } else {
                    log(Level.INFO, "Message not supported : " + content);
                    success = false;
                }
            }
        } catch (IOException e) {
            log(Level.SEVERE, "Message failed : " + e.getMessage());
            success = false;
        } catch (BusinessException e) {
            log(Level.SEVERE, "Message failed : " + e.getMessage());
            success = false;
        }

        if (success) {
            log(Level.INFO, "Message has FINISHED.");
            return MessageResult.createSuccess(messageEntry);
        } else {
            log(Level.INFO, "Message has FAILED.");
            return MessageResult.createFailed(messageEntry);
        }
    }

    private void updateFirmware(String content) throws IOException {
        getLogger().info("Executing firmware update message");
        try {
            String base64Encoded = getIncludedContent(content);
            byte[] imageData = new BASE64Decoder().decodeBuffer(base64Encoded);
            final ImageTransfer it = getCosemObjectFactory().getImageTransfer();
            it.upgrade(imageData);
            it.imageActivation();
        } catch (InterruptedException e) {
            String msg = "Firmware upgrade failed! " + e.getClass().getName() + " : " + e.getMessage();
            getLogger().severe(msg);
            throw new IOException(msg);
        }
        getLogger().info("Received a firmware upgrade message.");
    }

    private boolean isFirmwareUpdateMessage(String messageContent) {
        return (messageContent != null) && messageContent.contains(AS300FirmwareUpdateMessageBuilder.getMessageNodeTag());
    }

    private void changeOfSupplier(final MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Received Change of Supplier message.");
        log(Level.FINEST, "Writing new SupplierName Value");
        getCosemObjectFactory().getSupplierName(ChangeOfSupplierNameObisCode).writePassiveValue(OctetString.fromString(messageHandler.getSupplierName()));
        try {
            log(Level.FINEST, "Writing new SupplierId Value");
            getCosemObjectFactory().getSupplierId(ChangeOfSupplierIdObisCode).writePassiveValue(new Unsigned32(Long.valueOf(messageHandler.getSupplierId())));
        } catch (NumberFormatException e) {
            log(Level.SEVERE, "Incorrect SupplierID : " + messageHandler.getTenantValue() + " - Message will fail.");
            success = false;
        }
        if(success) {
            log(Level.FINEST, "Writing new Supplier ActivationDates");
            try {
                getCosemObjectFactory().getSupplierName(ChangeOfSupplierNameObisCode).writeActivationDate(new DateTime(new Date(Long.valueOf(messageHandler.getSupplierActivationDate()))));
                getCosemObjectFactory().getSupplierId(ChangeOfSupplierIdObisCode).writeActivationDate(new DateTime(new Date(Long.valueOf(messageHandler.getSupplierActivationDate()))));
            } catch (NumberFormatException e) {
                log(Level.SEVERE, "Incorrect ActivationDate : " + messageHandler.getSupplierActivationDate() + " - Message will fail.");
                success = false;
            }
        }
    }

    private void changeOfTenantMessage(final MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Received Change of Tenant message.");
        log(Level.FINEST, "Writing new Tenant Value");
        ChangeOfTenantManagement changeOfTenant = getCosemObjectFactory().getChangeOfTenantManagement(ChangeOfTennantObisCode);
        try {
            changeOfTenant.writePassiveValue(new Unsigned32(Long.valueOf(messageHandler.getTenantValue())));
        } catch (NumberFormatException e) {
            log(Level.SEVERE, "Incorrect TenantValue : " + messageHandler.getTenantValue() + " - Message will fail.");
            success = false;
        } catch (IOException e){
            if(e.getMessage().indexOf("Cosem Data-Access-Result exception R/W denied") >= 0){
                log(Level.SEVERE, "Could not write the new tenant value, still try to update the activationDate");
            } else {
                throw e;
            }
        }
        if(success){ // if the previous failed, then we don't try to write the activationDate
            log(Level.FINEST, "Writing new Tenant ActivationDate");
            try {
                changeOfTenant.writeActivationDate(new DateTime(new Date(Long.valueOf(messageHandler.getTenantActivationDate()))));
            } catch (NumberFormatException e) {
                log(Level.SEVERE, "Incorrect ActivationDate : " + messageHandler.getTenantActivationDate() + " - Message will fail.");
                success = false;
            }
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
        ActivityCalendarController activityCalendarController = new AS300ActivityCalendarController((AS300) this.protocol);
        try {
            builder.initFromXml(content);

            if (builder.getCodeId() > 0) { // codeTable implementation
                log(Level.FINEST, "Parsing the content of the CodeTable.");
                activityCalendarController.parseContent(content);
                log(Level.FINEST, "Setting the new Passive Calendar Name.");
                activityCalendarController.writeCalendarName("");
                log(Level.FINEST, "Sending out the new Passive Calendar objects.");
                activityCalendarController.writeCalendar();
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

    private void log(Level level, String message) {
        getLogger().log(level, message);
    }

    private Logger getLogger() {
        return this.protocol.getLogger();
    }

    private boolean isTimeOfUseMessage(final String messageContent) {
        return (messageContent != null) && messageContent.contains(TimeOfUseMessageBuilder.getMessageNodeTag());
    }

    private boolean isUpdatePricingInformationMessage(final String messageContent) {
        return (messageContent != null) && messageContent.contains(RtuMessageConstant.UPDATE_PRICING_INFORMATION);
    }

    private boolean isChangeOfSupplierMessage(final MessageHandler messageHandler) {
        return (messageHandler != null) && RtuMessageConstant.CHANGE_OF_SUPPLIER.equalsIgnoreCase(messageHandler.getType());
    }

    private boolean isChangeOfTenantMessage(final MessageHandler messageHandler) {
        return (messageHandler != null) && RtuMessageConstant.CHANGE_OF_TENANT.equalsIgnoreCase(messageHandler.getType());
    }

    @Override
    public void doMessage(final RtuMessage rtuMessage) throws BusinessException, SQLException, IOException {
        // nothing to do
    }

    @Override
    protected TimeZone getTimeZone() {
        return this.protocol.getTimeZone();
    }
}
