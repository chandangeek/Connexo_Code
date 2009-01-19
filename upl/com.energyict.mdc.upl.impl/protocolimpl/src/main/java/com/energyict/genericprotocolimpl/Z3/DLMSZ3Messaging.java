package com.energyict.genericprotocolimpl.Z3;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.core.Link;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.TCPIPConnection;
import com.energyict.dlms.axrdencoding.Integer16;
import com.energyict.dlms.axrdencoding.Integer32;
import com.energyict.dlms.axrdencoding.Integer64;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.axrdencoding.util.AXDRBoolean;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.Register;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.genericprotocolimpl.common.RtuMessageConstant;
import com.energyict.genericprotocolimpl.webrtukp.AARQ;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.core.CommunicationProfile;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.core.RtuMessage;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MeterReadingData;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageAttributeSpec;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageElement;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.MessageValueSpec;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimpl.dlms.HDLCConnection;

public class DLMSZ3Messaging implements GenericProtocol, Messaging, ProtocolLink, RegisterProtocol, Constant{
	
	/**
	 * Properties
	 */
	private int securityLevel;	// 0: No Authentication - 1: Low Level - 2: High Level
	private int connectionMode; // 0: DLMS/HDLC - 1: DLMS/TCPIP
	private int clientMacAddress;
	private int serverLowerMacAddress;
	private int serverUpperMacAddress;
	private int timeout;
	private int forceDelay;
	private int retries;
	private int addressingMode;
	private String password;
	
	private CosemObjectFactory 		cosemObjectFactory;
	private CommunicationProfile 	commProfile;
	private DLMSConnection 			dlmsConnection;
	private DLMSMeterConfig			dlmsMeterConfig;
	private AARQ					aarq;
	private Logger					logger;
	private Rtu						rtu;
	

	public void execute(CommunicationScheduler scheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {
		
		this.commProfile = scheduler.getCommunicationProfile();
		this.rtu = scheduler.getRtu();
		
		try {
			init(link.getInputStream(), link.getOutputStream(), scheduler.getRtu().getDeviceTimeZone(), logger);
			connect();
			
			if(commProfile.getSendRtuMessage()){
				List messageEntries = rtu.getPendingMessages();
				applyMessages(messageEntries);
			}
			
			if(commProfile.getReadDemandValues()){
				log(Level.INFO, "Reading demand values is not supported.");
			}
			
			if(commProfile.getReadMeterReadings()){
				log(Level.INFO, "Reading meter readings is not supported.");
			}
			
		} catch (IOException e){
			e.printStackTrace();
			throw new IOException(e.getMessage());
		} finally {
			// means we got a connection
			if(aarq != null){
				disconnect();
			}
		}
	}
	
	public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
		
		try {
			
			this.logger = logger;
			
			this.cosemObjectFactory	= new CosemObjectFactory((ProtocolLink)this);
			
			this.dlmsConnection = (this.connectionMode == 0)?
					new HDLCConnection(inputStream, outputStream, this.timeout, this.forceDelay, this.retries, this.clientMacAddress, this.serverLowerMacAddress, this.serverUpperMacAddress, this.addressingMode):
					new TCPIPConnection(inputStream, outputStream, this.timeout, this.forceDelay, this.retries, this.clientMacAddress, this.serverLowerMacAddress);
					
			this.dlmsMeterConfig = DLMSMeterConfig.getInstance("EZ3");
			
		} catch (DLMSConnectionException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	
	}
	
	public void log(Level level, String tekst){
		this.logger.log(level, tekst);
	}
	
	public Rtu getMeter(){
		return this.rtu;
	}
	
	public void connect() throws IOException {
		try {
			
			getDLMSConnection().connectMAC();
			getDLMSConnection().setIskraWrapper(1);
			aarq = new AARQ(this.securityLevel, this.password, getDLMSConnection());
			
			
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

	public DLMSConnection getDLMSConnection(){
		return this.dlmsConnection;
	}
	
	public CosemObjectFactory getCosemObjectFactory(){
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
        Iterator<String> iterator= getRequiredKeys().iterator();
        while (iterator.hasNext())
        {
            String key = iterator.next();
            if (properties.getProperty(key) == null)
                throw new MissingPropertyException (key + " key missing");
        }
        
        this.password = properties.getProperty(MeterProtocol.PASSWORD, "");
        this.securityLevel = Integer.parseInt(properties.getProperty("SecurityLevel", "0"));
        this.connectionMode = Integer.parseInt(properties.getProperty("ConnectionMode", "1"));
        this.clientMacAddress = Integer.parseInt(properties.getProperty("ClientMacAddress", "16"));
        this.serverLowerMacAddress = Integer.parseInt(properties.getProperty("ServerLowerMacAddress", "1"));
        this.serverUpperMacAddress = Integer.parseInt(properties.getProperty("ServerUpperMacAddress", "17"));
        // if HDLC set default timeout to 10s, if TCPIP set default timeout to 60s
        this.timeout = Integer.parseInt(properties.getProperty("Timeout", (this.connectionMode==0)?"10000":"60000"));
        this.forceDelay = Integer.parseInt(properties.getProperty("ForceDelay", "100"));
        this.retries = Integer.parseInt(properties.getProperty("Retries", "3"));
        this.addressingMode = Integer.parseInt(properties.getProperty("AddressingMode", "2"));
	}

	// not supported
	public void setRegister(String name, String value) throws IOException, NoSuchRegisterException, UnsupportedException {
//		throw new UnsupportedException();
		String type = value.substring(0, value.indexOf(" "));
		String dataStr = value.substring(value.indexOf(" ")+1, value.length());
		
		int typeInt = 0;
		
		try {
			typeInt = Integer.parseInt(type);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new IOException("Type is not correct.");
		}
		
		if(typeInt != DLMSCOSEMGlobals.TYPEDESC_NULL){
			
			switch(typeInt){
			
//			case DLMSCOSEMGlobals.TYPEDESC_ARRAY:{
//				Array array = new Array();
////				array.
//				
//			};break;
			
			case DLMSCOSEMGlobals.TYPEDESC_BOOLEAN:{
				byte[] data = new byte[]{DLMSCOSEMGlobals.TYPEDESC_BOOLEAN, (byte)Integer.parseInt(dataStr)};
				getCosemObjectFactory().getGenericWrite(ObisCode.fromString(name), 2).write(data);
			};break;
			
			case DLMSCOSEMGlobals.TYPEDESC_INTEGER:{
				Integer8 integer = new Integer8(Integer.parseInt(dataStr));
				getCosemObjectFactory().getGenericWrite(ObisCode.fromString(name), 2).write(integer.getBEREncodedByteArray());
			};break;
			
			case DLMSCOSEMGlobals.TYPEDESC_LONG:{
				Integer16 integer = new Integer16(Integer.parseInt(dataStr));
				getCosemObjectFactory().getGenericWrite(ObisCode.fromString(name), 2).write(integer.getBEREncodedByteArray());
			};break;
			
			case DLMSCOSEMGlobals.TYPEDESC_DOUBLE_LONG:{
				Integer32 integer = new Integer32(Integer.parseInt(dataStr));
				getCosemObjectFactory().getGenericWrite(ObisCode.fromString(name), 2).write(integer.getBEREncodedByteArray());
			};break;
			
			case DLMSCOSEMGlobals.TYPEDESC_LONG64:{
				Integer64 integer = new Integer64(Integer.parseInt(dataStr));
				getCosemObjectFactory().getGenericWrite(ObisCode.fromString(name), 2).write(integer.getBEREncodedByteArray());
			};break;
			
			case DLMSCOSEMGlobals.TYPEDESC_LONG_UNSIGNED:{
				Unsigned16 integer = new Unsigned16(Integer.parseInt(dataStr));
				getCosemObjectFactory().getGenericWrite(ObisCode.fromString(name), 2).write(integer.getBEREncodedByteArray());
			};break;
			
			case DLMSCOSEMGlobals.TYPEDESC_UNSIGNED:{
				Unsigned8 integer = new Unsigned8(Integer.parseInt(dataStr));
				getCosemObjectFactory().getGenericWrite(ObisCode.fromString(name), 2).write(integer.getBEREncodedByteArray());
			};break;
			
			case DLMSCOSEMGlobals.TYPEDESC_DOUBLE_LONG_UNSIGNED:{
				Unsigned32 integer = new Unsigned32(Integer.parseInt(dataStr));
				getCosemObjectFactory().getGenericWrite(ObisCode.fromString(name), 2).write(integer.getBEREncodedByteArray());
			};break;
			
			case DLMSCOSEMGlobals.TYPEDESC_OCTET_STRING:{
				OctetString  octString = OctetString.fromString(dataStr);
				getCosemObjectFactory().getGenericWrite(ObisCode.fromString(name), 2).write(octString.getBEREncodedByteArray());
			};break;
			
			case DLMSCOSEMGlobals.TYPEDESC_TIME:{
				
			};break;
				
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
        msgSpec = addNoValueMsg("Disable load limiting", RtuMessageConstant.LOAD_LIMIT_DISALBE, false);
        catLoadLimit.addMessageSpec(msgSpec);
        msgSpec = addParametersLoadLimit("Configure load limiting", RtuMessageConstant.LOAD_LIMIT_CONFIGURE, false);
        catLoadLimit.addMessageSpec(msgSpec);
        
        theCategories.add(catPrePaid);
        theCategories.add(catConnectControl);
        theCategories.add(catLoadLimit);
        return theCategories;
	}
	
	private MessageSpec addBudgetMsg(String keyId, String tagName, boolean advanced){
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
	
	private MessageSpec addNoValueMsg(String keyId, String tagName, boolean advanced){
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
	}
	
	private MessageSpec addConfigurePrepaid(String keyId, String tagName, boolean advanced){
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
        for(int i = 1; i < 9; i++){
        	msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.PREPAID_MULTIPLIER+i, true);
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
        for (Iterator it = msgTag.getAttributes().iterator(); it.hasNext();) {
            MessageAttribute att = (MessageAttribute) it.next();
            if (att.getValue() == null || att.getValue().length() == 0)
                continue;
            buf.append(" ").append(att.getSpec().getName());
            buf.append("=").append('"').append(att.getValue()).append('"');
        }
        if (msgTag.getSubElements().isEmpty()) {
            buf.append("/>");
            return buf.toString();
        }
        buf.append(">");
        // c. sub elements
        for (Iterator it = msgTag.getSubElements().iterator(); it.hasNext();) {
            MessageElement elt = (MessageElement) it.next();
            if (elt.isTag())
                buf.append(writeTag((MessageTag) elt));
            else if (elt.isValue()) {
                String value = writeValue((MessageValue) elt);
                if (value == null || value.length() == 0)
                    return "";
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

	private byte[] getAXDRBooleanByte(boolean state){
        byte[] data = new byte[2];
        data[0] = DLMSCOSEMGlobals.TYPEDESC_LONG;
        data[1] = (byte)(state?0xff:0x00);
        return data;
	}
	
	public void applyMessages(List rtuMessages) throws IOException, BusinessException, SQLException {
		MessageHandler messageHandler = new MessageHandler();
		
		Iterator it = rtuMessages.iterator();
		RtuMessage rm = null;
		boolean success = false;
		while(it.hasNext()){
			try {
				rm = (RtuMessage)it.next();
				String content = rm.getContents();
				importMessage(content, messageHandler);
				
				boolean disConnect 				= messageHandler.getType().equals(RtuMessageConstant.DISCONNECT_LOAD);
				boolean connect 				= messageHandler.getType().equals(RtuMessageConstant.CONNECT_LOAD);
				boolean prepaidConfiguration 	= messageHandler.getType().equals(RtuMessageConstant.PREPAID_CONFIGURED);
				boolean prepaidAdd				= messageHandler.getType().equals(RtuMessageConstant.PREPAID_ADD);
				boolean prepaidEnable			= messageHandler.getType().equals(RtuMessageConstant.PREPAID_ENABLE);
				boolean prepaidDisable			= messageHandler.getType().equals(RtuMessageConstant.PREPAID_DISABLE);
				boolean prepaidRead				= messageHandler.getType().equals(RtuMessageConstant.PREPAID_READ);
				boolean loadLimitConfiguration	= messageHandler.getType().equals(RtuMessageConstant.LOAD_LIMIT_CONFIGURE);
				boolean loadLimitEnable			= messageHandler.getType().equals(RtuMessageConstant.LOAD_LIMIT_ENABLE);
				boolean loadLimitDisable		= messageHandler.getType().equals(RtuMessageConstant.LOAD_LIMIT_DISALBE);
				
				success = false;
				
				if(disConnect){
					
					// Execute the message
					String digOut = messageHandler.getResult();
					if(digOut.equals("1") || digOut.equals("2")){
						//TODO TEST THIS
						getCosemObjectFactory().getGenericWrite(digitalOutputObisCode[Integer.parseInt(digOut) - 1], 2).write(getAXDRBooleanByte(false));
						success = true;
						
					} else {
						String error = "Disonnect message does not contain a valid digital output: " + digOut + ".";
						log(Level.INFO, error);
						rm.setFailed();
					}
					
				} else if(connect){
					
					// Execute the message
					String digOut = messageHandler.getResult();
					if(digOut.equals("1") || digOut.equals("2")){
						//TODO TEST THIS
						getCosemObjectFactory().getGenericWrite(digitalOutputObisCode[Integer.parseInt(digOut) - 1], 2).write(getAXDRBooleanByte(true));
						success = true;
						
					} else {
						String error = "Disonnect message does not contain a valid digital output: " + digOut + ".";
						log(Level.INFO, error);
						rm.setFailed();
					}
					
				} else if(prepaidEnable){
					
					// TODO TEST THIS
					getCosemObjectFactory().getGenericWrite(prepaidStateObisCode, 2).write(getAXDRBooleanByte(true));
					success = true;
					
				} else if(prepaidDisable){
					
					// TODO TEST THIS
					getCosemObjectFactory().getGenericWrite(prepaidStateObisCode, 2).write(getAXDRBooleanByte(false));
					success = true;
					
				} else if(prepaidConfiguration){
					
					/**
					 * Note: after the configuration setting we also enable the prepaid configuration!
					 */
					
					// TODO TEST THIS 
					
					// The Budget register
//					writeRegisterStructure(rm, prepaidSetBudgetObisCode, prepaidBudgetScalerUnit, messageHandler.getBudget());
					getCosemObjectFactory().getGenericWrite(prepaidSetBudgetObisCode, 2).write(new Integer32(Integer.valueOf(messageHandler.getBudget())).getBEREncodedByteArray());
					
					// The Threshold register
//					writeRegisterStructure(rm, prepaidThresholdObisCode, prepaidThresholdScalerUnit, messageHandler.getThreshold());
					getCosemObjectFactory().getGenericWrite(prepaidThresholdObisCode, 2).write(new Unsigned32(Long.valueOf(messageHandler.getThreshold())).getBEREncodedByteArray());
					
					// The ReadFrequency register
//					writeRegisterStructure(rm, prepaidReadFrequencyObisCode, prepaidReadFrequencyScalerUnit, messageHandler.getReadFrequency());
					getCosemObjectFactory().getGenericWrite(prepaidReadFrequencyObisCode, 2).write(new Unsigned32(Long.valueOf(messageHandler.getReadFrequency())).getBEREncodedByteArray());
					
					
					// The Multiplier registers
					for(int i = 0; i < 8; i++){
//						writeRegisterStructure(rm, prepaidMultiplierObisCode[i], prepaidMultiplierScalerUnit, messageHandler.getMultiplier(i));
						getCosemObjectFactory().getGenericWrite(prepaidMultiplierObisCode[i], 2).write(new Integer32(Integer.valueOf(messageHandler.getMultiplier(i))).getBEREncodedByteArray());

					}
					
					// Enabling the prepaid configuration
					getCosemObjectFactory().getGenericWrite(prepaidStateObisCode, 2).write(getAXDRBooleanByte(true));
					
					success = true;
					
				} else if(prepaidAdd){
					
					/**
					 * Note: after the configuration setting we also enable the prepaid configuration!
					 */
					
					// TODO TEST THIS
//					writeRegisterStructure(rm, prepaidAddBudgetObisCode, prepaidBudgetScalerUnit, messageHandler.getBudget());
					getCosemObjectFactory().getGenericWrite(prepaidAddBudgetObisCode, 2).write(new Unsigned32(Long.valueOf(messageHandler.getBudget())).getBEREncodedByteArray());

					
					// Enabling the prepaid configuration					
					getCosemObjectFactory().getGenericWrite(prepaidStateObisCode, 2).write(getAXDRBooleanByte(true));
					
					success = true;
					
				} else if(prepaidRead){
					
					try {
						// Read the complete register (value and scalerUnit)
						RtuRegister rr = getMeter().getRegister(prepaidSetBudgetObisCode);
						if(rr != null){
							
							DataContainer dc = getCosemObjectFactory().getGenericRead(prepaidSetBudgetObisCode, 0).getDataContainer();
							if(dc.getRoot().getElements().length > 0){
								for(int i = 0; i < dc.getRoot().getElements().length; i++){
									if(dc.getRoot().getStructure(i).getOctetString(0).toString().equals(prepaidSetBudgetObisCode.toString())){

											Register register = getCosemObjectFactory().getRegister(prepaidSetBudgetObisCode);
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
							throw new IOException("No RtuRegister with the prepaidBudgetObisCode (" + prepaidSetBudgetObisCode + ")");
						}
						
					} catch (SQLException e) {
						e.printStackTrace();
						throw new IOException("Could not store the budget register." + e.getMessage());
					}
					
				} else if(loadLimitConfiguration){
				
					/**
					 * Note: after the configuration setting we also enable the prepaid configuration!
					 */
					
					// TODO TEST THIS
					
					// The Threshold register
//					writeRegisterStructure(rm, loadLimitThresholdObisCode, loadLimitThresholdScalerUnit, messageHandler.getLLThreshold());
					getCosemObjectFactory().getGenericWrite(loadLimitThresholdObisCode, 2).write(new Unsigned32(Long.valueOf(messageHandler.getLLThreshold())).getBEREncodedByteArray());
					
					// The ReadFrequency register
//					writeRegisterStructure(rm, loadLimitReadFrequencyObisCode, loadLimitReadFrequencyScalerUnit, messageHandler.getLLReadFrequency());
					getCosemObjectFactory().getGenericWrite(loadLimitReadFrequencyObisCode, 2).write(new Unsigned32(Long.valueOf(messageHandler.getLLReadFrequency())).getBEREncodedByteArray());

					
					// The Duration register
//					writeRegisterStructure(rm, loadLimitDurationObisCode, loadLimitDurationScalerUnit, messageHandler.getLLDuration());
					getCosemObjectFactory().getGenericWrite(loadLimitDurationObisCode, 2).write(new Unsigned32(Long.valueOf(messageHandler.getLLDuration())).getBEREncodedByteArray());


					getCosemObjectFactory().getGenericWrite(loadLimitOutputLogicObisCode[0],2).write(new Integer8(Integer.parseInt(messageHandler.getLLD1Invert())).getBEREncodedByteArray());
					if(!messageHandler.getLLD1Invert().equalsIgnoreCase("")){
					}
					
					if(!messageHandler.getLLD1Invert().equalsIgnoreCase("")){
						getCosemObjectFactory().getGenericWrite(loadLimitOutputLogicObisCode[1],2).write(new Integer8(Integer.parseInt(messageHandler.getLLD2Invert())).getBEREncodedByteArray());
					}
					
					// Enabling the loadLimit configuration					
					getCosemObjectFactory().getGenericWrite(loadLimitStateObisCode, 2).write(getAXDRBooleanByte(true));
					
					success = true;
					
				} else if(loadLimitEnable){
				
					// TODO TEST THIS
					getCosemObjectFactory().getGenericWrite(loadLimitStateObisCode, 2).write(getAXDRBooleanByte(true));
					success = true;
					
				} else if(loadLimitDisable){
					
					// TODO TEST THIS
					getCosemObjectFactory().getGenericWrite(loadLimitStateObisCode, 2).write(getAXDRBooleanByte(false));
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
			} catch (IOException e){
				e.printStackTrace();
				log(Level.INFO, "Message " + rm.displayString() + " hase failed. " + e.getMessage());
			} catch (NumberFormatException e){
				e.printStackTrace();
				log(Level.INFO, "Message " + rm.displayString() + " hase failed. " + e.getMessage());
			} finally {
				if(success){
					rm.confirm();
				} else {
					rm.setFailed();
				}
			}
		}
	}
	
//	private void writeRegisterStructure(RtuMessage rm, ObisCode oc, ScalerUnit su, String value) throws IOException, BusinessException, SQLException{
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
//			log(Level.INFO, "RtuMessage " + rm + " has a non numeric value.");
//			throw new NumberFormatException(e.getMessage());
//		}
//	}
	
	private void importMessage(String message, DefaultHandler handler) throws BusinessException{
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
		if(obisCode.getC() == 96){
			value = Long.toString(getCosemObjectFactory().getRegister(obisCode).getValue());
		} else if(obisCode.getB() == 129){
			value = Long.toString(getCosemObjectFactory().getData(obisCode).getValue());
		}
		
//		String value = getRegister(obisCode.toString());
		RegisterValue rv = new RegisterValue(obisCode, value);
//		getRegister("0.129.0.0.1.255");
		
		return rv;
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
}
