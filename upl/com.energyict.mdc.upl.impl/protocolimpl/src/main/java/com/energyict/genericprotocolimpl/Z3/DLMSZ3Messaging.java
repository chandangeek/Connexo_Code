package com.energyict.genericprotocolimpl.Z3;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.*;
import com.energyict.dialer.core.Link;
import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.*;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.amr.Register;
import com.energyict.mdw.core.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.*;
import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DLMSZ3Messaging implements GenericProtocol, Messaging, ProtocolLink, RegisterProtocol, Constant {

    /**
     * Properties
     */
    private int securityLevel;    // 0: No Authentication - 1: Low Level - 2: High Level
    private int connectionMode; // 0: DLMS/HDLC - 1: DLMS/TCPIP
    private int clientMacAddress;
    private int serverLowerMacAddress;
    private int serverUpperMacAddress;
    private int timeout;
    private int forceDelay;
    private int retries;
    private int addressingMode;
    private String password;

    private CosemObjectFactory cosemObjectFactory;
    private CommunicationProfile commProfile;
    private DLMSConnection dlmsConnection;
    private DLMSMeterConfig dlmsMeterConfig;
    private AARQ aarq;
    private Logger logger;
    private Device rtu;


    public void execute(CommunicationScheduler scheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {

        this.commProfile = scheduler.getCommunicationProfile();
        this.rtu = scheduler.getRtu();

        try {
            init(link.getInputStream(), link.getOutputStream(), scheduler.getRtu().getDeviceTimeZone(), logger);
            connect();

            if (commProfile.getSendRtuMessage()) {
                List messageEntries = rtu.getPendingMessages();
                applyMessages(messageEntries);
            }

            if (commProfile.getReadDemandValues()) {
                log(Level.INFO, "Reading demand values is not supported.");
            }

            if (commProfile.getReadMeterReadings()) {
                log(Level.INFO, "Reading meter readings is not supported.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        } finally {
            // means we got a connection
            if (aarq != null) {
                disconnect();
            }
        }
    }

    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {

        try {

            this.logger = logger;

            this.cosemObjectFactory = new CosemObjectFactory((ProtocolLink) this);

//			this.dlmsConnection = (this.connectionMode == 0)?
//					new HDLCConnection(inputStream, outputStream, this.timeout, this.forceDelay, this.retries, this.clientMacAddress, this.serverLowerMacAddress, this.serverUpperMacAddress, this.addressingMode):
//					new TCPIPConnection(inputStream, outputStream, this.timeout, this.forceDelay, this.retries, this.clientMacAddress, this.serverLowerMacAddress);

            if (this.connectionMode == 0) {
                this.dlmsConnection = new HDLCConnection(inputStream, outputStream, this.timeout, this.forceDelay, this.retries, this.clientMacAddress, this.serverLowerMacAddress, this.serverUpperMacAddress, this.addressingMode);
            } else {
                this.dlmsConnection = new TCPIPConnection(inputStream, outputStream, this.timeout, this.forceDelay, this.retries, this.clientMacAddress, this.serverLowerMacAddress);
            }

            this.dlmsMeterConfig = DLMSMeterConfig.getInstance("EZ3");

        } catch (DLMSConnectionException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }

    }

    public void log(Level level, String tekst) {
        this.logger.log(level, tekst);
    }

    public Device getMeter() {
        return this.rtu;
    }

    public void connect() throws IOException {
        try {

            getDLMSConnection().connectMAC();
            getDLMSConnection().setIskraWrapper(1);
            aarq = new AARQ(this.password, getDLMSConnection());
            aarq.requestApplicationAssociation(this.securityLevel);


        } catch (DLMSConnectionException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    public void disconnect() throws IOException {
        try {
            getDLMSConnection().disconnectMAC();
        } catch (DLMSConnectionException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    public DLMSConnection getDLMSConnection() {
        return this.dlmsConnection;
    }

    public CosemObjectFactory getCosemObjectFactory() {
        return this.cosemObjectFactory;
    }

    public String getFirmwareVersion() throws IOException, UnsupportedException {
        // TODO Auto-generated method stub
        return null;
    }

    public String getRegister(String name) throws IOException, UnsupportedException, NoSuchRegisterException {
//		throw new UnsupportedException();
        return Long.toString(getCosemObjectFactory().getRegister(ObisCode.fromString(name)).getValue());
//		getCosemObjectFactory().getData(ObisCode.fromString(name)).getString();
//		return getCosemObjectFactory().getGenericRead(ObisCode.fromString(name), 2).getString();
    }

    public Date getTime() throws IOException {
        // TODO get the date from the device
        return new Date();
    }

    public void setProperties(Properties properties)
            throws InvalidPropertyException, MissingPropertyException {
        Iterator iterator = getRequiredKeys().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            if (properties.getProperty(key) == null) {
                throw new MissingPropertyException(key + " key missing");
            }
        }

        this.password = properties.getProperty(MeterProtocol.PASSWORD, "");
        this.securityLevel = Integer.parseInt(properties.getProperty("SecurityLevel", "0"));
        this.connectionMode = Integer.parseInt(properties.getProperty("ConnectionMode", "1"));
        this.clientMacAddress = Integer.parseInt(properties.getProperty("ClientMacAddress", "16"));
        this.serverLowerMacAddress = Integer.parseInt(properties.getProperty("ServerLowerMacAddress", "1"));
        this.serverUpperMacAddress = Integer.parseInt(properties.getProperty("ServerUpperMacAddress", "17"));
        // if HDLC set default timeout to 10s, if TCPIP set default timeout to 60s
        this.timeout = Integer.parseInt(properties.getProperty("Timeout", (this.connectionMode == 0) ? "10000" : "60000"));
        this.forceDelay = Integer.parseInt(properties.getProperty("ForceDelay", "100"));
        this.retries = Integer.parseInt(properties.getProperty("Retries", "3"));
        this.addressingMode = Integer.parseInt(properties.getProperty("AddressingMode", "2"));
    }

    // not supported
    public void setRegister(String name, String value) throws IOException, NoSuchRegisterException, UnsupportedException {
//		throw new UnsupportedException();
        String type = value.substring(0, value.indexOf(" "));
        String dataStr = value.substring(value.indexOf(" ") + 1, value.length());

        int typeInt = 0;

        try {
            typeInt = Integer.parseInt(type);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new IOException("Type is not correct.");
        }

        if (typeInt != AxdrType.NULL.getTag()) {

            final AxdrType axdrType = AxdrType.fromTag((byte) (typeInt & 0x0FF));
            switch (axdrType) {

//			case DLMSCOSEMGlobals.TYPEDESC_ARRAY:{
//				Array array = new Array();
////				array.
//				
//			};break;

                case BOOLEAN: {
                    byte[] data = new byte[]{axdrType.getTag(), (byte) Integer.parseInt(dataStr)};
                    getCosemObjectFactory().getGenericWrite(ObisCode.fromString(name), 2).write(data);
                }
                ;
                break;

                case INTEGER: {
                    Integer8 integer = new Integer8(Integer.parseInt(dataStr));
                    getCosemObjectFactory().getGenericWrite(ObisCode.fromString(name), 2).write(integer.getBEREncodedByteArray());
                }
                ;
                break;

                case LONG: {
                    Integer16 integer = new Integer16(Integer.parseInt(dataStr));
                    getCosemObjectFactory().getGenericWrite(ObisCode.fromString(name), 2).write(integer.getBEREncodedByteArray());
                }
                ;
                break;

                case DOUBLE_LONG: {
                    Integer32 integer = new Integer32(Integer.parseInt(dataStr));
                    getCosemObjectFactory().getGenericWrite(ObisCode.fromString(name), 2).write(integer.getBEREncodedByteArray());
                }
                ;
                break;

                case LONG64: {
                    Integer64 integer = new Integer64(Integer.parseInt(dataStr));
                    getCosemObjectFactory().getGenericWrite(ObisCode.fromString(name), 2).write(integer.getBEREncodedByteArray());
                }
                ;
                break;

                case LONG_UNSIGNED: {
                    Unsigned16 integer = new Unsigned16(Integer.parseInt(dataStr));
                    getCosemObjectFactory().getGenericWrite(ObisCode.fromString(name), 2).write(integer.getBEREncodedByteArray());
                }
                ;
                break;

                case UNSIGNED: {
                    Unsigned8 integer = new Unsigned8(Integer.parseInt(dataStr));
                    getCosemObjectFactory().getGenericWrite(ObisCode.fromString(name), 2).write(integer.getBEREncodedByteArray());
                }
                ;
                break;

                case DOUBLE_LONG_UNSIGNED: {
                    Unsigned32 integer = new Unsigned32(Integer.parseInt(dataStr));
                    getCosemObjectFactory().getGenericWrite(ObisCode.fromString(name), 2).write(integer.getBEREncodedByteArray());
                }
                ;
                break;

                case OCTET_STRING: {
                    OctetString octString = OctetString.fromString(dataStr);
                    getCosemObjectFactory().getGenericWrite(ObisCode.fromString(name), 2).write(octString.getBEREncodedByteArray());
                }
                ;
                break;

                case TIME: {

                }
                ;
                break;

            }

        }

    }

    public void setTime() throws IOException {

    }

    public List getOptionalKeys() {

        List result = new ArrayList(9);
        result.add("Timeout");
        result.add("Retries");
        result.add("DelayAfterFail");
        result.add("RequestTimeZone");
        result.add("FirmwareVersion");
        result.add("SecurityLevel");
        result.add("ClientMacAddress");
        result.add("ServerUpperMacAddress");
        result.add("ServerLowerMacAddress");
        result.add("ExtendedLogging");
        result.add("LoadProfileId");
        result.add("AddressingMode");
        result.add("Connection");
        return result;

    }

    public List getRequiredKeys() {
        List result = new ArrayList();
        return result;
    }

    public String getVersion() {
        return getProtocolVersion();
    }

    public String getProtocolVersion() {
        return "$Date$";
    }

    public Logger getLogger() {
        return this.logger;
    }

    public DLMSMeterConfig getMeterConfig() {
        return this.dlmsMeterConfig;
    }

    /**
     * Messages
     */

    public List getMessageCategories() {
        List theCategories = new ArrayList();
        MessageCategorySpec catPrePaid = new MessageCategorySpec("PrePaid");
        MessageCategorySpec catConnectControl = new MessageCategorySpec("Connect/Disconnect");
        MessageCategorySpec catLoadLimit = new MessageCategorySpec("Load Limiting");

        // Prepaid related messages
        MessageSpec msgSpec = addConfigurePrepaid("Configure prepaid functionality", RtuMessageConstant.PREPAID_CONFIGURED, false);
        catPrePaid.addMessageSpec(msgSpec);
        msgSpec = addBudgetMsg("Add prepaid credit", RtuMessageConstant.PREPAID_ADD, false);
        catPrePaid.addMessageSpec(msgSpec);
        msgSpec = addNoValueMsg("Read prepaid credit", RtuMessageConstant.PREPAID_READ, false);
        catPrePaid.addMessageSpec(msgSpec);
        msgSpec = addNoValueMsg("Enable prepaid functionality", RtuMessageConstant.PREPAID_ENABLE, false);
        catPrePaid.addMessageSpec(msgSpec);
        msgSpec = addNoValueMsg("Disable prepaid functionality", RtuMessageConstant.PREPAID_DISABLE, false);
        catPrePaid.addMessageSpec(msgSpec);

        // Disconnect related messages
        msgSpec = addConnectControlMsg("Disconnect", RtuMessageConstant.DISCONNECT_LOAD, false);
        catConnectControl.addMessageSpec(msgSpec);
        msgSpec = addConnectControlMsg("Connect", RtuMessageConstant.CONNECT_LOAD, false);
        catConnectControl.addMessageSpec(msgSpec);

        // Load Limiting related messages
        msgSpec = addNoValueMsg("Enable load limiting", RtuMessageConstant.LOAD_LIMIT_ENABLE, false);
        catLoadLimit.addMessageSpec(msgSpec);
        msgSpec = addNoValueMsg("Disable load limiting", RtuMessageConstant.LOAD_LIMIT_DISABLE, false);
        catLoadLimit.addMessageSpec(msgSpec);
        msgSpec = addParametersLoadLimit("Configure load limiting", RtuMessageConstant.LOAD_LIMIT_CONFIGURE, false);
        catLoadLimit.addMessageSpec(msgSpec);

        theCategories.add(catPrePaid);
        theCategories.add(catConnectControl);
        theCategories.add(catLoadLimit);
        return theCategories;
    }

    private MessageSpec addBudgetMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.PREPAID_BUDGET, true);
        tagSpec.add(msgVal);
        tagSpec.add(msgAttrSpec);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private MessageSpec addNoValueMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private MessageSpec addConfigurePrepaid(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        tagSpec.add(msgVal);
        MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.PREPAID_BUDGET, true);
        tagSpec.add(msgAttrSpec);
        msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.PREPAID_THRESHOLD, true);
        tagSpec.add(msgAttrSpec);
        msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.PREPAID_READ_FREQUENCY, true);
        tagSpec.add(msgAttrSpec);
        for (int i = 1; i < 9; i++) {
            msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.PREPAID_MULTIPLIER + i, true);
            tagSpec.add(msgAttrSpec);
        }
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private MessageSpec addParametersLoadLimit(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        tagSpec.add(msgVal);
        MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.LOAD_LIMIT_READ_FREQUENCY, true);
        tagSpec.add(msgAttrSpec);
        msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.LOAD_LIMIT_THRESHOLD, true);
        tagSpec.add(msgAttrSpec);
        msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.LOAD_LIMIT_DURATION, true);
        tagSpec.add(msgAttrSpec);
        msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.LOAD_LIMIT_D1_INVERT, false);
        tagSpec.add(msgAttrSpec);
        msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.LOAD_LIMIT_D2_INVERT, false);
        tagSpec.add(msgAttrSpec);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private MessageSpec addConnectControlMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.DIGITAL_OUTPUT, true);
        tagSpec.add(msgVal);
        tagSpec.add(msgAttrSpec);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    public String writeTag(MessageTag msgTag) {
        StringBuffer buf = new StringBuffer();

        // a. Opening tag
        buf.append("<");
        buf.append(msgTag.getName());

        // b. Attributes
        for (Iterator it = msgTag.getAttributes().iterator(); it.hasNext(); ) {
            MessageAttribute att = (MessageAttribute) it.next();
            if (att.getValue() == null || att.getValue().length() == 0) {
                continue;
            }
            buf.append(" ").append(att.getSpec().getName());
            buf.append("=").append('"').append(att.getValue()).append('"');
        }
        if (msgTag.getSubElements().isEmpty()) {
            buf.append("/>");
            return buf.toString();
        }
        buf.append(">");
        // c. sub elements
        for (Iterator it = msgTag.getSubElements().iterator(); it.hasNext(); ) {
            MessageElement elt = (MessageElement) it.next();
            if (elt.isTag()) {
                buf.append(writeTag((MessageTag) elt));
            } else if (elt.isValue()) {
                String value = writeValue((MessageValue) elt);
                if (value == null || value.length() == 0) {
                    return "";
                }
                buf.append(value);
            }
        }

        // d. Closing tag
        buf.append("</");
        buf.append(msgTag.getName());
        buf.append(">");

        return buf.toString();
    }

    public String writeValue(MessageValue msgValue) {
        return msgValue.getValue();
    }

    private byte[] getAXDRBooleanByte(boolean state) {
        byte[] data = new byte[2];
        data[0] = AxdrType.BOOLEAN.getTag();
        data[1] = (byte) (state ? 0xff : 0x00);
        return data;
    }

    public void applyMessages(List rtuMessages) throws IOException, BusinessException, SQLException {
        MessageHandler messageHandler = new MessageHandler();

        Iterator it = rtuMessages.iterator();
        DeviceMessage rm = null;
        boolean success = false;
        while (it.hasNext()) {
            try {
                rm = (DeviceMessage) it.next();
                String content = rm.getContents();
                importMessage(content, messageHandler);

                boolean disConnect = messageHandler.getType().equals(RtuMessageConstant.DISCONNECT_LOAD);
                boolean connect = messageHandler.getType().equals(RtuMessageConstant.CONNECT_LOAD);
                boolean prepaidConfiguration = messageHandler.getType().equals(RtuMessageConstant.PREPAID_CONFIGURED);
                boolean prepaidAdd = messageHandler.getType().equals(RtuMessageConstant.PREPAID_ADD);
                boolean prepaidEnable = messageHandler.getType().equals(RtuMessageConstant.PREPAID_ENABLE);
                boolean prepaidDisable = messageHandler.getType().equals(RtuMessageConstant.PREPAID_DISABLE);
                boolean prepaidRead = messageHandler.getType().equals(RtuMessageConstant.PREPAID_READ);
                boolean loadLimitConfiguration = messageHandler.getType().equals(RtuMessageConstant.LOAD_LIMIT_CONFIGURE);
                boolean loadLimitEnable = messageHandler.getType().equals(RtuMessageConstant.LOAD_LIMIT_ENABLE);
                boolean loadLimitDisable = messageHandler.getType().equals(RtuMessageConstant.LOAD_LIMIT_DISABLE);

                success = false;

                if (disConnect) {

                    // Execute the message
                    String digOut = messageHandler.getResult();
                    if (digOut.equals("1") || digOut.equals("2")) {
                        //TODO TEST THIS
                        getCosemObjectFactory().getData(digitalOutputObisCode[Integer.parseInt(digOut) - 1]).setValueAttr(new BooleanObject(false));
                        success = true;

                    } else {
                        String error = "Disonnect message does not contain a valid digital output: " + digOut + ".";
                        log(Level.INFO, error);
                        rm.setFailed();
                    }

                } else if (connect) {

                    // Execute the message
                    String digOut = messageHandler.getResult();
                    if (digOut.equals("1") || digOut.equals("2")) {
                        //TODO TEST THIS
                        getCosemObjectFactory().getData(digitalOutputObisCode[Integer.parseInt(digOut) - 1]).setValueAttr(new BooleanObject(true));
                        success = true;

                    } else {
                        String error = "Disonnect message does not contain a valid digital output: " + digOut + ".";
                        log(Level.INFO, error);
                        rm.setFailed();
                    }

                } else if (prepaidEnable) {

                    // TODO TEST THIS

                    getCosemObjectFactory().getRegister(prepaidStateObisCode).setValueAttr(new BooleanObject(true));
                    success = true;

                } else if (prepaidDisable) {

                    // TODO TEST THIS
                    getCosemObjectFactory().getRegister(prepaidStateObisCode).setValueAttr(new BooleanObject(false));
                    success = true;

                } else if (prepaidConfiguration) {

                    /**
                     * Note: after the configuration setting we also enable the prepaid configuration!
                     */

                    // TODO TEST THIS

                    // The Budget register
//					getCosemObjectFactory().getGenericWrite(prepaidSetBudgetObisCode, 2).write(new Integer32(Integer.valueOf(messageHandler.getBudget())).getBEREncodedByteArray());
                    getCosemObjectFactory().getRegister(prepaidSetBudgetObisCode).setValueAttr(new Integer32(Integer.valueOf(messageHandler.getBudget()).intValue()));

                    // The Threshold register
//					writeRegisterStructure(rm, prepaidThresholdObisCode, prepaidThresholdScalerUnit, messageHandler.getThreshold());
//					getCosemObjectFactory().getGenericWrite(prepaidThresholdObisCode, 2).write(new Unsigned32(Long.valueOf(messageHandler.getThreshold())).getBEREncodedByteArray());
                    getCosemObjectFactory().getRegister(prepaidThresholdObisCode).setValueAttr(new Unsigned32(Long.valueOf(messageHandler.getThreshold()).longValue()));

                    // The ReadFrequency register
//					writeRegisterStructure(rm, prepaidReadFrequencyObisCode, prepaidReadFrequencyScalerUnit, messageHandler.getReadFrequency());
//					getCosemObjectFactory().getGenericWrite(prepaidReadFrequencyObisCode, 2).write(new Unsigned32(Long.valueOf(messageHandler.getReadFrequency())).getBEREncodedByteArray());
                    getCosemObjectFactory().getRegister(prepaidReadFrequencyObisCode).setValueAttr(new Unsigned32(Long.valueOf(messageHandler.getReadFrequency()).longValue()));


                    // The Multiplier registers
                    for (int i = 0; i < 8; i++) {
//						writeRegisterStructure(rm, prepaidMultiplierObisCode[i], prepaidMultiplierScalerUnit, messageHandler.getMultiplier(i));
//						getCosemObjectFactory().getGenericWrite(prepaidMultiplierObisCode[i], 2).write(new Integer32(Integer.valueOf(messageHandler.getMultiplier(i))).getBEREncodedByteArray());
                        getCosemObjectFactory().getRegister(prepaidMultiplierObisCode[i]).setValueAttr(new Integer32(Integer.valueOf(messageHandler.getMultiplier(i)).intValue()));
                    }

                    // Enabling the prepaid configuration
                    getCosemObjectFactory().getRegister(prepaidStateObisCode).setValueAttr(new BooleanObject(true));

                    success = true;

                } else if (prepaidAdd) {

                    /**
                     * Note: after the configuration setting we also enable the prepaid configuration!
                     */

                    // TODO TEST THIS
//					writeRegisterStructure(rm, prepaidAddBudgetObisCode, prepaidBudgetScalerUnit, messageHandler.getBudget());
//					getCosemObjectFactory().getGenericWrite(prepaidAddBudgetObisCode, 2).write(new Unsigned32(Long.valueOf(messageHandler.getBudget())).getBEREncodedByteArray());
                    getCosemObjectFactory().getRegister(prepaidAddBudgetObisCode).setValueAttr(new Unsigned32(Long.valueOf(messageHandler.getBudget()).longValue()));

                    // Enabling the prepaid configuration
                    getCosemObjectFactory().getRegister(prepaidStateObisCode).setValueAttr(new BooleanObject(true));

                    success = true;

                } else if (prepaidRead) {

                    try {
                        // Read the complete register (value and scalerUnit)
                        Register rr = getMeter().getRegister(prepaidSetBudgetObisCode);
                        if (rr != null) {

                            DataContainer dc = getCosemObjectFactory().getGenericRead(prepaidSetBudgetObisCode, 0).getDataContainer();
                            if (dc.getRoot().getElements().length > 0) {
                                for (int i = 0; i < dc.getRoot().getElements().length; i++) {
                                    if (dc.getRoot().getStructure(i).getOctetString(0).toString().equals(prepaidSetBudgetObisCode.toString())) {

                                        com.energyict.dlms.cosem.Register register = getCosemObjectFactory().getRegister(prepaidSetBudgetObisCode);
                                        RegisterValue rv = new RegisterValue(prepaidSetBudgetObisCode, ParseUtils.registerToQuantity(register));
                                        MeterReadingData mrd = new MeterReadingData();
                                        mrd.add(rv);
                                        getMeter().store(mrd);

                                    }
                                }
                            } else {
                                throw new IOException("Could not read the budget registers.");
                            }

                            success = true;

                        } else {
                            throw new IOException("No Register with the prepaidBudgetObisCode (" + prepaidSetBudgetObisCode + ")");
                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                        throw new IOException("Could not store the budget register." + e.getMessage());
                    }

                } else if (loadLimitConfiguration) {

                    /**
                     * Note: after the configuration setting we also enable the prepaid configuration!
                     */

                    // TODO TEST THIS

                    // The Threshold register
//					writeRegisterStructure(rm, loadLimitThresholdObisCode, loadLimitThresholdScalerUnit, messageHandler.getLLThreshold());
//					getCosemObjectFactory().getGenericWrite(loadLimitThresholdObisCode, 2).write(new Unsigned32(Long.valueOf(messageHandler.getLLThreshold())).getBEREncodedByteArray());
                    getCosemObjectFactory().getRegister(loadLimitThresholdObisCode).setValueAttr(new Unsigned32(Long.valueOf(messageHandler.getLLThreshold()).longValue()));

                    // The ReadFrequency register
//					writeRegisterStructure(rm, loadLimitReadFrequencyObisCode, loadLimitReadFrequencyScalerUnit, messageHandler.getLLReadFrequency());
//					getCosemObjectFactory().getGenericWrite(loadLimitReadFrequencyObisCode, 2).write(new Unsigned32(Long.valueOf(messageHandler.getLLReadFrequency())).getBEREncodedByteArray());
                    getCosemObjectFactory().getRegister(loadLimitReadFrequencyObisCode).setValueAttr(new Unsigned32(Long.valueOf(messageHandler.getLLReadFrequency()).longValue()));


                    // The Duration register
//					writeRegisterStructure(rm, loadLimitDurationObisCode, loadLimitDurationScalerUnit, messageHandler.getLLDuration());
//					getCosemObjectFactory().getGenericWrite(loadLimitDurationObisCode, 2).write(new Unsigned32(Long.valueOf(messageHandler.getLLDuration())).getBEREncodedByteArray());
                    getCosemObjectFactory().getRegister(loadLimitDurationObisCode).setValueAttr(new Unsigned32(Long.valueOf(messageHandler.getLLDuration()).longValue()));


                    if (!messageHandler.getLLD1Invert().equalsIgnoreCase("")) {
//						getCosemObjectFactory().getGenericWrite(loadLimitOutputLogicObisCode[0],2).write(new Integer8(Integer.parseInt(messageHandler.getLLD1Invert())).getBEREncodedByteArray());
//						getCosemObjectFactory().getRegister(loadLimitOutputLogicObisCode[0]).setValueAttr(new Integer8(Integer.parseInt(messageHandler.getLLD1Invert())));
                        getCosemObjectFactory().getRegister(loadLimitOutputLogicObisCode[0]).setValueAttr(new BooleanObject(messageHandler.getLLD1Invert().equals(Integer.toString(1))));
                    }

                    if (!messageHandler.getLLD1Invert().equalsIgnoreCase("")) {
//						getCosemObjectFactory().getGenericWrite(loadLimitOutputLogicObisCode[1],2).write(new Integer8(Integer.parseInt(messageHandler.getLLD2Invert())).getBEREncodedByteArray());
//						getCosemObjectFactory().getRegister(loadLimitOutputLogicObisCode[1]).setValueAttr(new Integer8(Integer.parseInt(messageHandler.getLLD2Invert())));
                        getCosemObjectFactory().getRegister(loadLimitOutputLogicObisCode[1]).setValueAttr(new BooleanObject(messageHandler.getLLD1Invert().equals(Integer.toString(1))));
                    }

                    // Enabling the loadLimit configuration
                    getCosemObjectFactory().getRegister(loadLimitStateObisCode).setValueAttr(new BooleanObject(true));

                    success = true;

                } else if (loadLimitEnable) {

                    // TODO TEST THIS
                    getCosemObjectFactory().getRegister(loadLimitStateObisCode).setValueAttr(new BooleanObject(true));
                    success = true;

                } else if (loadLimitDisable) {

                    // TODO TEST THIS
                    getCosemObjectFactory().getRegister(loadLimitStateObisCode).setValueAttr(new BooleanObject(false));
                    success = true;

                } else {
                    success = false;
                }

            } catch (BusinessException e) {
                e.printStackTrace();
                log(Level.INFO, "Message " + rm.displayString() + " hase failed. " + e.getMessage());
            } catch (SQLException e) {
                e.printStackTrace();
                log(Level.INFO, "Message " + rm.displayString() + " hase failed. " + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                log(Level.INFO, "Message " + rm.displayString() + " hase failed. " + e.getMessage());
            } catch (NumberFormatException e) {
                e.printStackTrace();
                log(Level.INFO, "Message " + rm.displayString() + " hase failed. " + e.getMessage());
            } finally {
                if (success) {
                    rm.confirm();
                } else {
                    rm.setFailed();
                }
            }
        }
    }

//	private void writeRegisterStructure(DeviceMessage rm, ObisCode oc, ScalerUnit su, String value) throws IOException, BusinessException, SQLException{
//		try {
//			Structure struct = new Structure();
//			struct.addDataType(OctetString.fromString(oc.toString()));	
//			struct.addDataType(new Integer32(Integer.parseInt(value)));
//			struct.addDataType(new Integer8(su.getScaler()));
//			struct.addDataType(new TypeEnum(su.getUnit().getDlmsCode()));
//			getCosemObjectFactory().getGenericWrite(oc, 0).write(struct.getBEREncodedByteArray());
//		} catch (NumberFormatException e) {
//			e.printStackTrace();
////			rm.setFailed();
//			log(Level.INFO, "DeviceMessage " + rm + " has a non numeric value.");
//			throw new NumberFormatException(e.getMessage());
//		}
//	}

    private void importMessage(String message, DefaultHandler handler) throws BusinessException {
        try {

            byte[] bai = message.getBytes();
            InputStream i = (InputStream) new ByteArrayInputStream(bai);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(i, handler);

        } catch (ParserConfigurationException thrown) {
            thrown.printStackTrace();
            throw new BusinessException(thrown);
        } catch (SAXException thrown) {
            thrown.printStackTrace();
            throw new BusinessException(thrown);
        } catch (IOException thrown) {
            thrown.printStackTrace();
            throw new BusinessException(thrown);
        }
    }

    public String writeMessage(Message msg) {
        return msg.write(this);
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        // TODO Auto-generated method stub
        String value = "";
        if (obisCode.getC() == 96) {
            value = Long.toString(getCosemObjectFactory().getRegister(obisCode).getValue());
        } else if (obisCode.getB() == 129) {
            value = Long.toString(getCosemObjectFactory().getData(obisCode).getValue());
        }

//		String value = getRegister(obisCode.toString());
        RegisterValue rv = new RegisterValue(obisCode, value);
//		getRegister("0.129.0.0.1.255");

        return rv;
    }

    @Override
    public void addProperties(TypedProperties properties) {
        addProperties(properties.toStringProperties());
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return PropertySpecFactory.toPropertySpecs(getRequiredKeys());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return PropertySpecFactory.toPropertySpecs(getOptionalKeys());
    }

    public void addProperties(Properties properties) {
        try {
            setProperties(properties);
        } catch (InvalidPropertyException e) {
            e.printStackTrace();
        } catch (MissingPropertyException e) {
            e.printStackTrace();
        }
    }

    public int getReference() {
        return 0;
    }

    public int getRoundTripCorrection() {
        return 0;
    }

    public StoredValues getStoredValues() {
        return null;
    }

    public TimeZone getTimeZone() {
        return null;
    }

    public boolean isRequestTimeZone() {
        return false;
    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo("RegisterInfo");
    }

    public long getTimeDifference() {
        // TODO Auto-generated method stub
        return 0;
    }
}
