package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.messaging;

import com.energyict.cbo.*;
import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.xmlparsing.GenericDataToWrite;
import com.energyict.dlms.xmlparsing.XmlToDlms;
import com.energyict.protocolimpl.generic.MessageParser;
import com.energyict.protocolimpl.generic.messages.GenericMessaging;
import com.energyict.protocolimpl.generic.messages.MessageHandler;
import com.energyict.protocolimpl.generic.csvhandling.CSVParser;
import com.energyict.protocolimpl.generic.csvhandling.TestObject;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.smartmeterprotocolimpl.eict.NTAMessageHandler;
import com.energyict.mdw.core.*;
import com.energyict.mdw.shadow.UserFileShadow;
import com.energyict.messaging.TimeOfUseMessageBuilder;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimpl.base.Base64EncoderDecoder;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.ObisCodeProvider;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.ZigbeeGas;
import com.energyict.smartmeterprotocolimpl.elster.apollo.messaging.AS300TimeOfUseMessageBuilder;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 10-aug-2011
 * Time: 15:02:34
 */
public class ZigbeeMessageExecutor extends MessageParser {

    private static final ObisCode ChangeOfSupplierNameObisCode = ObisCode.fromString("7.0.1.64.0.255");
    private static final ObisCode ChangeOfSupplierIdObisCode = ObisCode.fromString("7.0.1.64.1.255");
    private static final String STANDING_CHARGE = "Standing charge";
    private static final String SET_STANDING_CHARGE = "SetStandingChargeAndActivationDate";
    private static final String SET_CALORIFIC_VALUE = "SetCalorificValueAndActivationDate";
    private static final String READ_PRICE_PER_UNIT = "ReadPricePerUnit";
    private static final String SET_CONVERSION_FACTOR = "SetConversionFactorAndActivationDate";
    private static final String SET_PRICE_PER_UNIT = "SetPricePerUnit";
    private static final String ACTIVATION_DATE_TAG = "ActivationDate";
    private static final String TARIFF_LABEL = "TariffLabel";
    private static final String ACTIVATION_DATE = "Activation date (dd/mm/yyyy hh:mm:ss) (optional)";
    private static final ObisCode PRICE_MATRIX_OBISCODE = ObisCode.fromString("0.0.1.61.0.255");   //TODO C field, 1 or 2? (A+ or A-)
    private static final ObisCode STANDING_CHARGE_OBISCODE = ObisCode.fromString("0.0.0.61.2.255");
    private static final ObisCode CALORIFIC_VALUE_OBISCODE = ObisCode.fromString("7.0.54.0.0.255");
    private static final ObisCode CONVERSION_FACTOR_OBISCODE = ObisCode.fromString("7.0.52.0.0.255");
    private static final ObisCode TARIFF_LABEL_OBISCODE = ObisCode.fromString("0.0.1.63.1.255");
    private static final String CALORIFIC_VALUE = "Calorific value";
    private static final String CONVERSION_FACTOR = "Conversion factor";
    private static final ObisCode DISCONNECTOR = ObisCode.fromString("0.0.96.3.10.255");
    private static final ObisCode IPD_MESSAGE_CONTROL = ObisCode.fromString("7.0.35.3.7.255");
    private static final String RESUME = "resume";

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
        String trackingId = messageEntry.getTrackingId();
        success = true;

        try {
            if (isTimeOfUseMessage(content)) {
                updateTimeOfUse(content);
            } else if (isUpdatePricingInformationMessage(content)) {
                updatePricingInformation(content);
            } else if (isSetPricePerUnit(content)) {
                setPricePerUnit(content);
            } else if (isSetStandingCharge(content)) {
                setStandingCharge(content);
            } else if (isSetCF(content)) {
                setCF(content);
            } else if (isSetCV(content)) {
                setCV(content);
            } else if (isReadPricePerUnit(content)) {
                readPricePerUnit();
            } else if (isTextToDisplayMessage(content)) {
                sendTextToDisplay(content);
            } else if (isConnectControlMessage(content)) {
                doConnect(content);
            } else if (isDisconnectControlMessage(content)) {
                doDisconnect(content);
            } else {

                MessageHandler messageHandler = new NTAMessageHandler();
                importMessage(content, messageHandler);

                if (isChangeOfTenantMessage(messageHandler)) {
                    changeOfTenant(messageHandler);
                } else if (isChangeOfSupplierMessage(messageHandler)) {
                    changeOfSupplier(messageHandler);
                } else if (isTestMessage(messageHandler)) {
                    testMessage(messageHandler);
                } else if (isFirmwareUpgradeMessage(content)) {
                    doFirmwareUpgrade(messageHandler, content, trackingId);
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
        } catch (InterruptedException e) {
            log(Level.SEVERE, "Message failed : " + e.getMessage());
            success = false;
            Thread.currentThread().interrupt();
            throw MdcManager.getComServerExceptionFactory().communicationInterruptedException(e);
        }

        if (success) {
            log(Level.INFO, "Message has finished.");
            return MessageResult.createSuccess(messageEntry);
        } else {
            return MessageResult.createFailed(messageEntry);
        }
    }

    private String getValueFromXMLAttribute(String tag, String content) throws IOException {
        int startIndex = content.indexOf(tag + "=\"");
        int endIndex = content.indexOf("\"", startIndex + tag.length() + 2);
        try {
            return content.substring(startIndex + tag.length() + 2, endIndex);
        } catch (IndexOutOfBoundsException e) {
            return "";  //optional value is empty
        }
    }

    private String getValueFromXML(String tag, String content) throws IOException {
        int startIndex = content.indexOf(tag + ">");
        int endIndex = content.indexOf("</" + tag);
        try {
            return content.substring(startIndex + tag.length() + 1, endIndex);
        } catch (IndexOutOfBoundsException e) {
            throw new IOException(e.getMessage());
        }
    }

    private void sendTextToDisplay(final String content) throws IOException {
        log(Level.INFO, "Send text message to display message received.");
        ActivePassive meterMessageControl = getCosemObjectFactory().getActivePassive(IPD_MESSAGE_CONTROL);

        String[] parts = content.split("=");
        String message = parts[1].substring(1).split("\"")[0];
        int duration = 0;
        Date date = null;

        try {
            duration = Integer.parseInt(parts[2].substring(1).split("\"")[0]);
            if (parts.length > 3) {
                String dateString = parts[3].substring(1).split("\"")[0];

                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                date = formatter.parse(dateString);
            }
        } catch (ParseException e) {
            log(Level.SEVERE, "Error while parsing the activation date: " + e.getMessage());
        } catch (NumberFormatException e) {
            log(Level.SEVERE, "Error while parsing the time duration: " + e.getMessage());
        }

        Structure structure = new Structure();
        structure.addDataType(new Unsigned32((int) Calendar.getInstance().getTimeInMillis()));
        OctetString octetString = OctetString.fromString((message.length() > 128 ? message.substring(0, 127) : message));
        structure.addDataType(octetString);
        structure.addDataType(new Unsigned16(duration));
        structure.addDataType(new BitString(0x0F, 8));
        structure.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString("FFFFFFFFFFFFFFFF", ""), 8));

        meterMessageControl.writePassiveValue(structure);
        if (date != null) {
            Calendar cal = Calendar.getInstance(protocol.getTimeZone());
            cal.setTime(date);
            meterMessageControl.writeActivationDate(new DateTime(cal));
        } else {
            meterMessageControl.activate();
        }
    }

    private void setPricePerUnit(String content) throws IOException {
        ActivePassive priceInformation = getCosemObjectFactory().getActivePassive(PRICE_MATRIX_OBISCODE);
        ActivePassive tariffLabel = getCosemObjectFactory().getActivePassive(TARIFF_LABEL_OBISCODE);

        String[] prices = getIncludedContent(content).trim().split("\r\n");
        String activationDateString = getValueFromXML(ACTIVATION_DATE_TAG, content);

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date activationDate = null;
        try {
            if (!activationDateString.equalsIgnoreCase("0") && !activationDateString.equals("")) {
                activationDate = formatter.parse(activationDateString);
            }
        } catch (ParseException e) {
            protocol.getLogger().log(Level.SEVERE, "Error parsing the given date: " + e.getMessage());
            throw new IOException(e.getMessage());
        }

        Array priceArray = new Array(64);
        for (int index = 0; index < 64; index++) {
            if (index < prices.length) {
                try {
                    priceArray.setDataType(index, new Unsigned32(Integer.valueOf(prices[index])));    //Double long unsigned
                } catch (NumberFormatException e) {
                    throw new IOException("Invalid price integer: " + prices[index]);
                }
            } else {
                priceArray.setDataType(index, new Unsigned32(0));
            }
        }

        String randomString = Double.toString(Math.random());
        tariffLabel.writePassiveValue(OctetString.fromString(randomString, 6));
        priceInformation.writePassiveValue(priceArray);

        if (activationDate != null) {
            Calendar cal = Calendar.getInstance(protocol.getTimeZone());
            cal.setTime(activationDate);
            priceInformation.writeActivationDate(new DateTime(cal));
            tariffLabel.writeActivationDate(new DateTime(cal));
        } else {
            priceInformation.activate();
            tariffLabel.activate();
        }
    }

    private void setCF(String content) throws IOException {
        int conversionFactorValue;
        try {
            conversionFactorValue = Integer.parseInt(getValueFromXMLAttribute(CONVERSION_FACTOR, content));
        } catch (NumberFormatException e) {
            throw new IOException(e.getMessage());
        }

        Date activationDate = null;
        String activationDateString = getValueFromXMLAttribute(ACTIVATION_DATE, content);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            if (!activationDateString.equalsIgnoreCase("")) {
                activationDate = formatter.parse(activationDateString);
            }
        } catch (ParseException e) {
            protocol.getLogger().log(Level.SEVERE, "Error parsing the given date: " + e.getMessage());
            throw new IOException(e.getMessage());
        }

        ActivePassive conversionFactor = getCosemObjectFactory().getActivePassive(CONVERSION_FACTOR_OBISCODE);
        conversionFactor.writePassiveValue(new Unsigned32(conversionFactorValue));         //Double long, signed
        if (activationDate != null) {
            Calendar cal = Calendar.getInstance(protocol.getTimeZone());
            cal.setTime(activationDate);
            conversionFactor.writeActivationDate(new DateTime(cal));
        } else {
            conversionFactor.activate();
        }
    }

    private void readPricePerUnit() throws IOException, BusinessException, SQLException {
        ActivePassive priceInformation = getCosemObjectFactory().getActivePassive(PRICE_MATRIX_OBISCODE);
        Array array = priceInformation.getValue().getArray();
        String priceInfo = "Pricing information unavailable: empty array";
        String fileName = "PriceInformation_" + protocol.getDlmsSession().getProperties().getSerialNumber() + "_" + ProtocolTools.getFormattedDate("yyyy-MM-dd_HH.mm.ss");
        if (array != null && array.nrOfDataTypes() > 0) {
            StringBuilder sb = new StringBuilder();

            String unit;
            try {
                ScalerUnit scalerUnit = priceInformation.getScalerUnit();
                unit = scalerUnit.toString();
            } catch (IOException e) {
                unit = "Error reading unit_scaler: " + e.getMessage();
            } catch (ApplicationException e) {
                unit = "(no valid unit specified)";
            }
            sb.append(unit).append("\n");
            for (int i = 0; i < array.nrOfDataTypes(); i++) {
                sb.append("Value ").append(i + 1).append(": ").append(array.getDataType(i));
            }
            priceInfo = sb.toString();
        }

        UserFileShadow ufs = ProtocolTools.createUserFileShadow(fileName, priceInfo.getBytes("UTF-8"), getFolderIdFromHub(), "txt");
        mw().getUserFileFactory().create(ufs);

        log(Level.INFO, "Stored price information in userFile: " + fileName);
    }

    private int getFolderIdFromHub() throws IOException {
        return getRtuFromDatabaseBySerialNumberAndClientMac().getFolderId();
    }

    private void setCV(String content) throws IOException {
        int calorificValue;
        try {
            calorificValue = Integer.parseInt(getValueFromXMLAttribute(CALORIFIC_VALUE, content));
        } catch (NumberFormatException e) {
            throw new IOException(e.getMessage());
        }

        Date activationDate = null;
        String activationDateString = getValueFromXMLAttribute(ACTIVATION_DATE, content);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            if (!activationDateString.equalsIgnoreCase("")) {
                activationDate = formatter.parse(activationDateString);
            }
        } catch (ParseException e) {
            protocol.getLogger().log(Level.SEVERE, "Error parsing the given date: " + e.getMessage());
            throw new IOException(e.getMessage());
        }

        ActivePassive calorificValueObject = getCosemObjectFactory().getActivePassive(CALORIFIC_VALUE_OBISCODE);
        calorificValueObject.writePassiveValue(new Unsigned32(calorificValue));         //Double long, signed
        if (activationDate != null) {
            Calendar cal = Calendar.getInstance(protocol.getTimeZone());
            cal.setTime(activationDate);
            calorificValueObject.writeActivationDate(new DateTime(cal));
        } else {
            calorificValueObject.activate();
        }
    }

    private void setStandingCharge(String content) throws IOException {
        ActivePassive standingCharge = getCosemObjectFactory().getActivePassive(STANDING_CHARGE_OBISCODE);
        ActivePassive tariffLabel = getCosemObjectFactory().getActivePassive(TARIFF_LABEL_OBISCODE);

        int standingChargeValue;
        try {
            standingChargeValue = Integer.parseInt(getValueFromXMLAttribute(STANDING_CHARGE, content));
        } catch (NumberFormatException e) {
            throw new IOException(e.getMessage());
        }

        Date activationDate = null;
        String activationDateString = getValueFromXMLAttribute(ACTIVATION_DATE, content);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            if (!activationDateString.equalsIgnoreCase("")) {
                activationDate = formatter.parse(activationDateString);
            }
        } catch (ParseException e) {
            protocol.getLogger().log(Level.SEVERE, "Error parsing the given date: " + e.getMessage());
            throw new IOException(e.getMessage());
        }

        standingCharge.writePassiveValue(new Unsigned32(standingChargeValue));         //Double long, signed
        String randomString = Double.toString(Math.random());
        tariffLabel.writePassiveValue(OctetString.fromString(randomString, 6));
        if (activationDate != null) {
            Calendar cal = Calendar.getInstance(protocol.getTimeZone());
            cal.setTime(activationDate);
            standingCharge.writeActivationDate(new DateTime(cal));
            tariffLabel.writeActivationDate(new DateTime(cal));
        } else {
            standingCharge.activate();
            tariffLabel.activate();
        }
    }

    private void changeOfSupplier(final MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Received Change of Supplier message.");
        log(Level.FINEST, "Writing new SupplierName Value");
        ChangeOfSupplierManagement changeOfSupplier = getCosemObjectFactory().getChangeOfSupplierManagement();
        getCosemObjectFactory().getSupplierName(ChangeOfSupplierNameObisCode).writePassiveValue(OctetString.fromString(messageHandler.getSupplierName()));

        try {
            log(Level.FINEST, "Writing new SupplierId Value");
            getCosemObjectFactory().getSupplierId(ChangeOfSupplierIdObisCode).writePassiveValue(new Unsigned32(Long.valueOf(messageHandler.getSupplierId())));
        } catch (NumberFormatException e) {
            log(Level.SEVERE, "Incorrect SupplierID : " + messageHandler.getTenantValue() + " - Message will fail.");
            success = false;
        }
        if (success) {
            try {
                Calendar cal = Calendar.getInstance(protocol.getTimeZone());
                if (messageHandler.getSupplierActivationDate() != null && !messageHandler.getSupplierActivationDate().equals("")) {
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    Date date = formatter.parse(messageHandler.getSupplierActivationDate());
                    cal.setTime(date);

                    log(Level.FINEST, "Writing new Supplier ActivationDates");
                    changeOfSupplier.writePassiveValue(new DateTime(cal));
                    changeOfSupplier.writeActivationDate(new DateTime(cal));
                    getCosemObjectFactory().getSupplierName(ChangeOfSupplierNameObisCode).writeActivationDate(new DateTime(cal));
                    getCosemObjectFactory().getSupplierId(ChangeOfSupplierIdObisCode).writeActivationDate(new DateTime(cal));
                } else {
                    log(Level.FINEST, "No activation date specified, the changes will be activated immediately.");
                    cal.setTime(new Date());
                    changeOfSupplier.writePassiveValue(new DateTime(cal));
                    changeOfSupplier.activate();
                    getCosemObjectFactory().getSupplierName(ChangeOfSupplierNameObisCode).activate();
                    getCosemObjectFactory().getSupplierId(ChangeOfSupplierIdObisCode).activate();
                }
            } catch (ParseException e) {
                log(Level.SEVERE, "Error while parsing the activation date: " + e.getMessage());
                success = false;
            }
        }
    }

    private void changeOfTenant(final MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Received Change of Tenant message.");
        ChangeOfTenantManagement changeOfTenant = getCosemObjectFactory().getChangeOfTenantManagement();

        try {
            Calendar cal = Calendar.getInstance(protocol.getTimeZone());
            if (messageHandler.getTenantActivationDate() != null && !messageHandler.getTenantActivationDate().equals("")) {
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Date date = formatter.parse(messageHandler.getTenantActivationDate());
                cal.setTime(date);
                log(Level.FINEST, "Writing new Tenant ActivationDates");

                changeOfTenant.writePassiveValue(new DateTime(cal));
                changeOfTenant.writeActivationDate(new DateTime(cal));
            } else {
                log(Level.FINEST, "No activation date specified, the changes will be activated immediately.");
                cal.setTime(new Date());
                changeOfTenant.writePassiveValue(new DateTime(cal));
                changeOfTenant.activate();
            }
        } catch (ParseException e) {
            log(Level.SEVERE, "Error while parsing the activation date: " + e.getMessage());
            success = false;
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

    private void doConnect(final String content) throws IOException {
        log(Level.INFO, "Received Remote Connect message.");
        Disconnector connector = getCosemObjectFactory().getDisconnector(DISCONNECTOR);
        connector.remoteReconnect();
    }

    private void doDisconnect(final String content) throws IOException {
        log(Level.INFO, "Received Remote Disconnect message.");
        Disconnector connector = getCosemObjectFactory().getDisconnector(DISCONNECTOR);
        connector.remoteDisconnect();
    }

    private void doFirmwareUpgrade(MessageHandler messageHandler, final String content, final String trackingId) throws IOException, InterruptedException {
        log(Level.INFO, "Handling message Firmware upgrade");

        String userFileID = messageHandler.getUserFileId();
         boolean resume = false;
        if ((trackingId != null) && trackingId.toLowerCase().contains(RESUME)) {
            resume = true;
        }

        if (!com.energyict.protocolimpl.generic.ParseUtils.isInteger(userFileID)) {
            String str = "Not a valid entry for the userFile.";
            throw new IOException(str);
        }
        UserFile uf = mw().getUserFileFactory().find(Integer.parseInt(userFileID));
        if (!(uf instanceof UserFile)) {
            String str = "Not a valid entry for the userfileID " + userFileID;
            throw new IOException(str);
        }

        String[] parts = content.split("=");
        Date date = null;
        try {
            if (parts.length > 2) {
                String dateString = parts[2].substring(1).split("\"")[0];

                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                date = formatter.parse(dateString);
            }
        } catch (ParseException e) {
            log(Level.SEVERE, "Error while parsing the activation date: " + e.getMessage());
            throw new NestedIOException(e);
        } catch (NumberFormatException e) {
            log(Level.SEVERE, "Error while parsing the time duration: " + e.getMessage());
            throw new NestedIOException(e);
        }

        byte[] imageData = new Base64EncoderDecoder().decode(uf.loadFileInByteArray());
        ImageTransfer it = getCosemObjectFactory().getImageTransfer(ObisCodeProvider.FIRMWARE_UPDATE);
        if (resume) {
            int lastTransferredBlockNumber = it.readFirstNotTransferedBlockNumber().intValue();
            if (lastTransferredBlockNumber > 0) {
                it.setStartIndex(lastTransferredBlockNumber);
            }
        }
        it.upgrade(imageData);
        if (date != null) {
            SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(ObisCodeProvider.IMAGE_ACTIVATION_SCHEDULER);
            Array dateArray = convertUnixToDateTimeArray(String.valueOf(date.getTime() / 1000));
            sas.writeExecutionTime(dateArray);
        } else {
             it.imageActivation();
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

    private boolean isSetPricePerUnit(final String messageContent) {
        return (messageContent != null) && messageContent.contains(SET_PRICE_PER_UNIT);
    }

    private boolean isSetStandingCharge(final String messageContent) {
        return (messageContent != null) && messageContent.contains(SET_STANDING_CHARGE);
    }

    private boolean isSetCF(final String messageContent) {
        return (messageContent != null) && messageContent.contains(SET_CONVERSION_FACTOR);
    }

    private boolean isSetCV(final String messageContent) {
        return (messageContent != null) && messageContent.contains(SET_CALORIFIC_VALUE);
    }

    private boolean isReadPricePerUnit(final String messageContent) {
        return (messageContent != null) && messageContent.contains(READ_PRICE_PER_UNIT);
    }

    private boolean isTextToDisplayMessage(final String messageContent) {
        return (messageContent != null) && messageContent.contains(ZigbeeGasMessaging.TEXT_TO_DISPLAY);
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

    private boolean isConnectControlMessage(final String messageContent) {
        return (messageContent != null) && messageContent.contains(ZigbeeGasMessaging.REMOTECONNECT);
    }

    private boolean isDisconnectControlMessage(final String messageContent) {
        return (messageContent != null) && messageContent.contains(ZigbeeGasMessaging.REMOTEDISCONNECT);
    }

    private boolean isFirmwareUpgradeMessage(final String messageContent) {
        return (messageContent != null) &&  messageContent.contains(RtuMessageConstant.FIRMWARE_UPGRADE);
    }

    public ActivityCalendarController getActivityCalendarController() {
        if (this.activityCalendarController == null) {
            this.activityCalendarController = new ZigbeeActivityCalendarController((ZigbeeGas) this.protocol);
        }
        return activityCalendarController;
    }

    private void log(Level level, String message) {
        this.protocol.getLogger().log(level, message);
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
            if (com.energyict.protocolimpl.generic.ParseUtils.isInteger(userFileId)) {
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
                                        to.setResult("0x" + com.energyict.protocolimpl.generic.ParseUtils.decimalByteToString(gr.getResponseData()));
                                        hasWritten = true;
                                    }
                                    break;
                                    case 1: { // SET
                                        GenericWrite gw = getCosemObjectFactory().getGenericWrite(to.getObisCode(), to.getAttribute(), to.getClassId());
                                        gw.write(com.energyict.protocolimpl.generic.ParseUtils.hexStringToByteArray(to.getData()));
                                        to.setResult("OK");
                                        hasWritten = true;
                                    }
                                    break;
                                    case 2: { // ACTION
                                        GenericInvoke gi = getCosemObjectFactory().getGenericInvoke(to.getObisCode(), to.getClassId(), to.getMethod());
                                        if (to.getData().equalsIgnoreCase("")) {
                                            gi.invoke();
                                        } else {
                                            gi.invoke(com.energyict.protocolimpl.generic.ParseUtils.hexStringToByteArray(to.getData()));
                                        }
                                        to.setResult("OK");
                                        hasWritten = true;
                                    }
                                    break;
                                    case 3: { // MESSAGE
                                        //Do nothing, no longer supported

                                        //OldDeviceMessageShadow rms = new OldDeviceMessageShadow();
                                        //rms.setContents(csvParser.getTestObject(i).getData());
                                        //rms.setRtuId(getRtuFromDatabaseBySerialNumberAndClientMac().getId());
                                        //OldDeviceMessage rm = mw().getRtuMessageFactory().create(rms);
                                        //doMessage(rm);
                                        //if (rm.getState().getId() == rm.getState().CONFIRMED.getId()) {
                                        //    to.setResult("OK");
                                        //} else {
                                        //    to.setResult("MESSAGE failed, current state " + rm.getState().getId());
                                        //}
                                        //hasWritten = true;
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
                    mw().getUserFileFactory().create(csvParser.convertResultToUserFile(uf, getFolderIdFromHub()));
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

    private Device getRtuFromDatabaseBySerialNumberAndClientMac() throws IOException {
        String serial = this.protocol.getDlmsSession().getProperties().getSerialNumber();
        List<Device> rtusWithSameSerialNumber = mw().getDeviceFactory().findBySerialNumber(serial);
        for (Device each : rtusWithSameSerialNumber) {
            if (((String) each.getProtocolProperties().getProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS)).equalsIgnoreCase("" + this.protocol.getDlmsSession().getProperties().getClientMacAddress())) {
                return each;
            }
        }
        throw new IOException("Could not find the EiServer rtu.");
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