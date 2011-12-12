package com.energyict.smartmeterprotocolimpl.elster.apollo.messaging;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.BusinessException;
import com.energyict.dlms.ParseUtils;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.xmlparsing.GenericDataToWrite;
import com.energyict.dlms.xmlparsing.XmlToDlms;
import com.energyict.genericprotocolimpl.common.GenericMessageExecutor;
import com.energyict.genericprotocolimpl.common.messages.GenericMessaging;
import com.energyict.genericprotocolimpl.common.messages.MessageHandler;
import com.energyict.genericprotocolimpl.nta.messagehandling.NTAMessageHandler;
import com.energyict.mdw.core.*;
import com.energyict.mdw.shadow.UserFileShadow;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    private static final String STANDING_CHARGE = "Standing charge";
    private static final String READ_PRICE_PER_UNIT = "ReadPricePerUnit";
    private static final String SET_STANDING_CHARGE = "SetStandingCharge";
    private static final String READ_ACTIVITY_CALENDAR = "ReadActivityCalendar";
    private static final String SET_PRICE_PER_UNIT = "SetPricePerUnit";
    private static final String COMMA_SEPARATED_PRICES = "CommaSeparatedPrices";
    private static final String ACTIVATION_DATE_TAG = "ActivationDate";
    private static final String ACTIVATION_DATE = "Activation date (dd/mm/yyyy hh:mm:ss) (optional)";
    private static final ObisCode PRICE_MATRIX_OBISCODE = ObisCode.fromString("0.0.1.61.0.255");
    private static final ObisCode ACTIVITY_CALENDAR_OBISCODE = ObisCode.fromString("0.0.13.0.1.255");
    private static final ObisCode STANDING_CHARGE_OBISCODE = ObisCode.fromString("0.0.0.61.2.255");
    private static final ObisCode METER_MESSAGE_CONTROL = ObisCode.fromString("1.0.35.3.8.255");
    private static final ObisCode DISCONNECTOR = ObisCode.fromString("0.0.96.3.10.255");

    protected final AbstractSmartDlmsProtocol protocol;

    protected boolean success;

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
            } else if (isConnectControlMessage(content)) {
                doConnect(content);
            } else if (isDisconnectControlMessage(content)) {
                doDisconnect(content);
            } else if (isTextToDisplayMessage(content)) {
                sendTextToDisplay(content);
            } else if (isSetPricePerUnit(content)) {
                setPricePerUnit(content);
            } else if (isSetStandingCharge(content)) {
                setStandingCharge(content);
            } else if (isReadPricePerUnit(content)) {
                readPricePerUnit();
            } else if (isReadActivityCalendar(content)) {
                readActivityCalendar();
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
        } catch (SQLException e) {
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

    private void readActivityCalendar() throws IOException, BusinessException, SQLException {
        ActivityCalendar activityCalendar = getCosemObjectFactory().getActivityCalendar(ACTIVITY_CALENDAR_OBISCODE);

        StringBuilder sb = new StringBuilder();
        String name = activityCalendar.readCalendarNameActive().getOctetString().stringValue();
        sb.append("Activity calendar: ").append(name).append('\n');

        Array seasonProfile = activityCalendar.readSeasonProfileActive();
        sb.append("    Active season profile:").append('\n');
        for (int i = 0; i < seasonProfile.nrOfDataTypes(); i++) {
            AbstractDataType dataType = seasonProfile.getDataType(i);
            if (dataType == null) {
                break;
            }
            Structure season = dataType.getStructure();
            sb.append("        Season ").append(i).append(":").append('\n');
            sb.append("          Name: ").append(season.getDataType(0));
            sb.append("          Start: ").append(season.getDataType(1));
            sb.append("          Week name: ").append(season.getDataType(2));
        }

        Array weekProfile = activityCalendar.readWeekProfileTableActive();
        sb.append("    Active week profile:").append('\n');
        for (int i = 0; i < weekProfile.nrOfDataTypes(); i++) {
            AbstractDataType dataType = weekProfile.getDataType(i);
            if (dataType == null) {
                break;
            }
            Structure week = dataType.getStructure();
            sb.append("        Week ").append(i).append(":").append('\n');
            sb.append("          Name: ").append(week.getDataType(0));
            sb.append("          Monday: day ID = ").append(week.getDataType(1));
            sb.append("          Tuesday: day ID = ").append(week.getDataType(2));
            sb.append("          Wednesday: day ID = ").append(week.getDataType(3));
            sb.append("          Thursday: day ID = ").append(week.getDataType(4));
            sb.append("          Friday: day ID = ").append(week.getDataType(5));
            sb.append("          Saturday: day ID = ").append(week.getDataType(6));
            sb.append("          Sunday: day ID = ").append(week.getDataType(7));
        }

        Array dayProfile = activityCalendar.readDayProfileTableActive();
        sb.append("    Active day profile:").append('\n');
        for (int i = 0; i < dayProfile.nrOfDataTypes(); i++) {
            AbstractDataType dataType = dayProfile.getDataType(i);
            if (dataType == null) {
                break;
            }
            Structure day = dataType.getStructure();
            sb.append("        Day ").append(i).append(":").append('\n');
            sb.append("          Day ID: ").append(day.getDataType(0));

            Array dayProfileActions = day.getDataType(1).getArray();
            if (dayProfileActions != null) {
                sb.append("          Day schedule: ").append('\n');
                for (int j = 0; j < dayProfileActions.nrOfDataTypes(); j++) {
                    Structure action = dayProfileActions.getDataType(j).getStructure();
                    if (action == null) {
                        break;
                    }
                    sb.append("              Day profile action ").append(j).append(":").append('\n');
                    sb.append("                Start time: ").append(action.getDataType(0));
                    sb.append("                Script logical name: ").append(action.getDataType(1));
                    sb.append("                Script selector: ").append(action.getDataType(2));
                }
            }
        }

        String fileName = "ActivityCalendar_" + protocol.getDlmsSession().getProperties().getSerialNumber() + "_" + ProtocolTools.getFormattedDate("yyyy-MM-dd_HH.mm.ss");
        UserFileShadow ufs = ProtocolTools.createUserFileShadow(fileName, sb.toString().getBytes("UTF-8"), getFolderIdFromHub(), "txt");
        mw().getUserFileFactory().create(ufs);

        log(Level.INFO, "Stored price information in userFile: " + fileName);
    }

    /**
     * Short notation for MeteringWarehouse.getCurrent()
     */
    public MeteringWarehouse mw() {
        MeteringWarehouse result = MeteringWarehouse.getCurrent();
        if (result == null) {
            return new MeteringWarehouseFactory().getBatch(false);
        } else {
            return result;
        }
    }

    private int getFolderIdFromHub() {
        return getRtuFromDatabaseBySerialNumber().getFolderId();
    }

    private Rtu getRtuFromDatabaseBySerialNumber() {
        String serial = this.protocol.getDlmsSession().getProperties().getSerialNumber();
        return mw().getRtuFactory().findBySerialNumber(serial).get(0);
    }

    private void setStandingCharge(String content) throws IOException {
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

        ActivePassive standingCharge = getCosemObjectFactory().getActivePassive(STANDING_CHARGE_OBISCODE);
        standingCharge.writePassiveValue(new Unsigned32(standingChargeValue));         //Double long, signed
        if (activationDate != null) {
            Calendar cal = Calendar.getInstance(protocol.getTimeZone());
            cal.setTime(activationDate);
            standingCharge.writeActivationDate(new DateTime(cal));
        } else {
            standingCharge.activate();
        }
    }

    private String getValueFromXML(String tag, String content) {
        int startIndex = content.indexOf("<" + tag);
        int endIndex = content.indexOf("</" + tag);
        return content.substring(startIndex + tag.length() + 2, endIndex);
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

    private void setPricePerUnit(String content) throws IOException {
        ActivePassive priceInformation = getCosemObjectFactory().getActivePassive(PRICE_MATRIX_OBISCODE);
        String[] prices = getValueFromXML(COMMA_SEPARATED_PRICES, content).split(",");
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

        priceInformation.writePassiveValue(priceArray);

        if (activationDate != null) {
            Calendar cal = Calendar.getInstance(protocol.getTimeZone());
            cal.setTime(activationDate);
            priceInformation.writeActivationDate(new DateTime(cal));
        } else {
            priceInformation.activate();
        }
    }

    private void updateFirmware(String content) throws IOException {
        getLogger().info("Executing firmware update message");
        try {
            String base64Encoded = getIncludedContent(content);
            byte[] imageData = new BASE64Decoder().decodeBuffer(base64Encoded);
            final ImageTransfer it = getCosemObjectFactory().getImageTransfer();
            it.upgrade(imageData, false);
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

    private void changeOfTenantMessage(final MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Received Change of Tenant message.");
        log(Level.FINEST, "Writing new Tenant Value");
        ChangeOfTenantManagement changeOfTenant = getCosemObjectFactory().getChangeOfTenantManagement(ChangeOfTennantObisCode);
        try {
            changeOfTenant.writePassiveValue(new Unsigned32(Long.valueOf(messageHandler.getTenantValue())));
        } catch (NumberFormatException e) {
            log(Level.SEVERE, "Incorrect TenantValue : " + messageHandler.getTenantValue() + " - Message will fail.");
            success = false;
        } catch (IOException e) {
            if (e.getMessage().indexOf("Cosem Data-Access-Result exception R/W denied") >= 0) {
                log(Level.SEVERE, "Could not write the new tenant value, still try to update the activationDate");
            } else {
                throw e;
            }
        }
        if (success) { // if the previous failed, then we don't try to write the activationDate
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

    private void doConnect(final String content) throws IOException {
        log(Level.INFO, "Received Remote Connect message.");
        Disconnector connector = getCosemObjectFactory().getDisconnector(DISCONNECTOR);
        connector.remoteReconnect();
    }

    private void doDisconnect(final String content) throws IOException {
        log(Level.INFO, "Received Disconnect Control - Disonnect message.");
        Disconnector connector = getCosemObjectFactory().getDisconnector(DISCONNECTOR);
        connector.remoteDisconnect();
    }

    private void sendTextToDisplay(final String content) throws IOException {
        log(Level.INFO, "Send text message to display message received.");
        ActivePassive meterMessageControl = getCosemObjectFactory().getActivePassive(METER_MESSAGE_CONTROL);

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
        OctetString octetString = OctetString.fromString((message.length() > 128 ? message.substring(0, 127) : message), 128);
        structure.addDataType(octetString);
        structure.addDataType(new Unsigned16(duration));
        structure.addDataType(new BitString(0x0F, 8));
        structure.addDataType(new Unsigned16(0));

        meterMessageControl.writePassiveValue(structure);
        if (date != null) {
            Calendar cal = Calendar.getInstance(protocol.getTimeZone());
            cal.setTime(date);
            meterMessageControl.writeActivationDate(new DateTime(cal));
        } else {
            meterMessageControl.activate();
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

    protected void log(Level level, String message) {
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

    private boolean isSetPricePerUnit(final String messageContent) {
        return (messageContent != null) && messageContent.contains(SET_PRICE_PER_UNIT);
    }

    private boolean isSetStandingCharge(final String messageContent) {
        return (messageContent != null) && messageContent.contains(SET_STANDING_CHARGE);
    }

    private boolean isReadPricePerUnit(final String messageContent) {
        return (messageContent != null) && messageContent.contains(READ_PRICE_PER_UNIT);
    }

    private boolean isReadActivityCalendar(final String messageContent) {
        return (messageContent != null) && messageContent.contains(READ_ACTIVITY_CALENDAR);
    }

    private boolean isChangeOfTenantMessage(final MessageHandler messageHandler) {
        return (messageHandler != null) && RtuMessageConstant.CHANGE_OF_TENANT.equalsIgnoreCase(messageHandler.getType());
    }

    private boolean isConnectControlMessage(final String messageContent) {
        return (messageContent != null) && messageContent.contains(AS300Messaging.DISCONNECT_CONTROL_RECONNECT);
    }

    private boolean isDisconnectControlMessage(final String messageContent) {
        return (messageContent != null) && messageContent.contains(AS300Messaging.DISCONNECT_CONTROL_DISCONNECT);
    }

    private boolean isTextToDisplayMessage(final String messageContent) {
        return (messageContent != null) && messageContent.contains(AS300Messaging.TEXT_TO_DISPLAY);
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
