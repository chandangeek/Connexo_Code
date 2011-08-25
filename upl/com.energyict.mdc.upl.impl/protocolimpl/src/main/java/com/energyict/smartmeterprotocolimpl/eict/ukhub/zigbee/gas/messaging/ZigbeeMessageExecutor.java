package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.messaging;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.BusinessException;
import com.energyict.dlms.DLMSUtils;
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
import com.energyict.genericprotocolimpl.webrtu.common.csvhandling.CSVParser;
import com.energyict.genericprotocolimpl.webrtu.common.csvhandling.TestObject;
import com.energyict.mdw.core.*;
import com.energyict.mdw.shadow.RtuMessageShadow;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.messaging.TimeOfUseMessageBuilder;
import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.protocolimpl.dlms.common.DlmsSession;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.elster.apollo.messaging.AS300TimeOfUseMessageBuilder;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 10-aug-2011
 * Time: 15:02:34
 */
public class ZigbeeMessageExecutor extends GenericMessageExecutor {

    private static final ObisCode ChangeOfSupplierNameObisCode = ObisCode.fromString("1.0.1.64.0.255");
    private static final ObisCode ChangeOfSupplierIdObisCode = ObisCode.fromString("1.0.1.64.1.255");

    private final AbstractSmartDlmsProtocol protocol;
    private ActivityCalendarController activityCalendarController;

    private boolean success;

    public ZigbeeMessageExecutor(final AbstractSmartDlmsProtocol protocol) {
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

                MessageHandler messageHandler = new NTAMessageHandler();
                importMessage(content, messageHandler);

                if (isChangeOfTenantMessage(messageHandler)) {
                    changeOfTenant(messageHandler);
                } else if (isChangeOfSupplierMessage(messageHandler)) {
                    changeOfSupplier(messageHandler);
                } else if (isTestMessage(messageHandler)) {
                    testMessage(messageHandler);
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
        } catch (SQLException e) {
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
        if (success) {
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

    private void changeOfTenant(final MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Received Change of Tenant message.");
        log(Level.FINEST, "Writing new Tenant Value");
        try {
            getCosemObjectFactory().getChangeOfTenantManagement().writePassiveValue(new Unsigned32(Long.valueOf(messageHandler.getTenantValue())));
        } catch (NumberFormatException e) {
            log(Level.SEVERE, "Incorrect TenantValue : " + messageHandler.getTenantValue() + " - Message will fail.");
            success = false;
        }
        if (success) { // if the previous failed, then we don't try to write the activationDate
            log(Level.FINEST, "Writing new Tenant ActivationDate");
            try {
                getCosemObjectFactory().getChangeOfTenantManagement().writeActivationDate(new DateTime(new Date(Long.valueOf(messageHandler.getTenantActivationDate()))));
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
                    log(Level.WARNING, "Length of the ActivityCalendar UserFile is not valid [" + userFileData + " bytes], failing message.");
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

    private boolean isChangeOfSupplierMessage(final MessageHandler messageHandler) {
        return (messageHandler != null) && RtuMessageConstant.CHANGE_OF_SUPPLIER.equalsIgnoreCase(messageHandler.getType());
    }

    private boolean isTestMessage(final MessageHandler messageHandler) {
        return (messageHandler != null) && (RtuMessageConstant.TEST_MESSAGE.equalsIgnoreCase(messageHandler.getType()));
    }

    private boolean isChangeOfTenantMessage(final MessageHandler messageHandler) {
        return (messageHandler != null) && RtuMessageConstant.CHANGE_OF_TENANT.equalsIgnoreCase(messageHandler.getType());
    }

    public ActivityCalendarController getActivityCalendarController() {
        if (this.activityCalendarController == null) {
            this.activityCalendarController = new ZigbeeActivityCalendarController(this.protocol);
        }
        return activityCalendarController;
    }

    private void log(Level level, String message) {
        this.protocol.getLogger().log(level, message);
    }

    @Override
    public void doMessage(final RtuMessage rtuMessage) throws BusinessException, SQLException, IOException {
        // nothing to do
    }

    @Override
    protected TimeZone getTimeZone() {
        return this.protocol.getTimeZone();
    }

    private void testMessage(MessageHandler messageHandler) throws IOException, BusinessException, SQLException {
        log(Level.INFO, "Handling message TestMessage");
        int failures = 0;
        String userFileId = messageHandler.getTestUserFileId();
        Date currentTime;
        if (!userFileId.equalsIgnoreCase("")) {
            if (com.energyict.genericprotocolimpl.common.ParseUtils.isInteger(userFileId)) {
                UserFile uf = mw().getUserFileFactory().find(Integer.parseInt(userFileId));
                if (uf != null) {
                    byte[] data = uf.loadFileInByteArray();
                    CSVParser csvParser = new CSVParser();
                    csvParser.parse(data);
                    boolean hasWritten;
                    TestObject to = new TestObject("");
                    for (int i = 0; i < csvParser.size(); i++) {
                        to = csvParser.getTestObject(i);
                        if (csvParser.isValidLine(to)) {
                            currentTime = new Date(System.currentTimeMillis());
                            hasWritten = false;
                            try {
                                switch (to.getType()) {
                                    case 0: { // GET
                                        GenericRead gr = getCosemObjectFactory().getGenericRead(to.getObisCode(), DLMSUtils.attrLN2SN(to.getAttribute()), to.getClassId());
                                        to.setResult("0x" + com.energyict.genericprotocolimpl.common.ParseUtils.decimalByteToString(gr.getResponseData()));
                                        hasWritten = true;
                                    }
                                    break;
                                    case 1: { // SET
                                        GenericWrite gw = getCosemObjectFactory().getGenericWrite(to.getObisCode(), to.getAttribute(), to.getClassId());
                                        gw.write(com.energyict.genericprotocolimpl.common.ParseUtils.hexStringToByteArray(to.getData()));
                                        to.setResult("OK");
                                        hasWritten = true;
                                    }
                                    break;
                                    case 2: { // ACTION
                                        GenericInvoke gi = getCosemObjectFactory().getGenericInvoke(to.getObisCode(), to.getClassId(), to.getMethod());
                                        if (to.getData().equalsIgnoreCase("")) {
                                            gi.invoke();
                                        } else {
                                            gi.invoke(com.energyict.genericprotocolimpl.common.ParseUtils.hexStringToByteArray(to.getData()));
                                        }
                                        to.setResult("OK");
                                        hasWritten = true;
                                    }
                                    break;
                                    case 3: { // MESSAGE
                                        RtuMessageShadow rms = new RtuMessageShadow();
                                        rms.setContents(csvParser.getTestObject(i).getData());
                                        rms.setRtuId(getRtuFromDatabaseBySerialNumber().getId());
                                        RtuMessage rm = mw().getRtuMessageFactory().create(rms);
                                        doMessage(rm);
                                        if (rm.getState().getId() == rm.getState().CONFIRMED.getId()) {
                                            to.setResult("OK");
                                        } else {
                                            to.setResult("MESSAGE failed, current state " + rm.getState().getId());
                                        }
                                        hasWritten = true;
                                    }
                                    break;
                                    case 4: { // WAIT
                                        waitCyclus(Integer.parseInt(to.getData()));
                                        to.setResult("OK");
                                        hasWritten = true;
                                    }
                                    break;
                                    case 5: {
                                        // do nothing, it's no valid line
                                    }
                                    break;
                                    default: {
                                        throw new ApplicationException("Row " + i + " of the CSV file does not contain a valid type.");
                                    }
                                }
                                to.setTime(currentTime.getTime());

                                // Check if the expected value is the same as the result
                                if ((to.getExpected() == null) || (!to.getExpected().equalsIgnoreCase(to.getResult()))) {
                                    to.setResult("Failed - " + to.getResult());
                                    failures++;
                                    log(Level.INFO, "Test " + i + " has successfully finished, but the result didn't match the expected value.");
                                } else {
                                    log(Level.INFO, "Test " + i + " has successfully finished.");
                                }

                            } catch (Exception e) {
                                if (!hasWritten) {
                                    if ((to.getExpected() != null) && (e.getMessage().indexOf(to.getExpected()) != -1)) {
                                        to.setResult(e.getMessage());
                                        log(Level.INFO, "Test " + i + " has successfully finished.");
                                        hasWritten = true;
                                    } else {
                                        log(Level.INFO, "Test " + i + " has failed.");
                                        String eMessage;
                                        if (e.getMessage().indexOf("\r\n") != -1) {
                                            eMessage = e.getMessage().substring(0, e.getMessage().indexOf("\r\n")) + "...";
                                        } else {
                                            eMessage = e.getMessage();
                                        }
                                        to.setResult("Failed. " + eMessage);
                                        hasWritten = true;
                                        failures++;
                                    }
                                    to.setTime(currentTime.getTime());
                                }
                            } finally {
                                if (!hasWritten) {
                                    to.setResult("Failed - Unknow exception ...");
                                    failures++;
                                    to.setTime(currentTime.getTime());
                                }
                            }
                        }
                    }
                    if (failures == 0) {
                        csvParser.addLine("All the tests are successfully finished.");
                    } else {
                        csvParser.addLine("" + failures + " of the " + csvParser.getValidSize() + " tests " + ((failures == 1) ? "has" : "have") + " failed.");
                    }
                    mw().getUserFileFactory().create(csvParser.convertResultToUserFile(uf, getRtuFromDatabaseBySerialNumber().getFolderId()));
                } else {
                    throw new ApplicationException("Userfile with ID " + userFileId + " does not exist.");
                }
            } else {
                throw new IOException("UserFileId is not a valid number");
            }
        } else {
            throw new IOException("No userfile id is given.");
        }
    }

    private void waitCyclus(int delay) throws IOException {
        try {
            int nrOfPolls = (delay / (20)) + (delay % (20) == 0 ? 0 : 1);
            for (int i = 0; i < nrOfPolls; i++) {
                if (i < nrOfPolls - 1) {
                    ProtocolTools.delay(20000);
                } else {
                    ProtocolTools.delay((delay - (i * (20))) * 1000);
                }
                log(Level.INFO, "Keeping connection alive");
                getCosemObjectFactory().getClock().getDateTime();
            }
        } catch (IOException e) {
            throw new IOException("Could not keep connection alive." + e.getMessage());
        }
    }

    private Rtu getRtuFromDatabaseBySerialNumber() throws IOException {
        String serial = this.protocol.getDlmsSession().getProperties().getSerialNumber();
        List<Rtu> rtus = mw().getRtuFactory().findBySerialNumber(serial);
        if(rtus.size() != 0){
            return rtus.get(0);
        } else {
            throw new IOException("No meter found, serialNumber probably not correct.");
        }
    }

    public MeteringWarehouse mw() {
        MeteringWarehouse result = MeteringWarehouse.getCurrent();
        if (result == null) {
            return new MeteringWarehouseFactory().getBatch(false);
        } else {
            return result;
        }
    }

}

