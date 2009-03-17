package com.energyict.genericprotocolimpl.webrtukp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.BusinessException;
import com.energyict.cbo.NotFoundException;
import com.energyict.cpo.Environment;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dialer.core.DialerMarker;
import com.energyict.dialer.core.Link;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.TCPIPConnection;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.BitString;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.Integer16;
import com.energyict.dlms.axrdencoding.Integer32;
import com.energyict.dlms.axrdencoding.Integer64;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.NullData;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.axrdencoding.VisibleString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.ActivityCalendar;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.CosemObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.DemandRegister;
import com.energyict.dlms.cosem.Disconnector;
import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.dlms.cosem.IPv4Setup;
import com.energyict.dlms.cosem.Limiter;
import com.energyict.dlms.cosem.P3ImageTransfer;
import com.energyict.dlms.cosem.PPPSetup;
import com.energyict.dlms.cosem.Register;
import com.energyict.dlms.cosem.ScriptTable;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.dlms.cosem.SpecialDaysTable;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.dlms.cosem.Limiter.ValueDefinitionType;
import com.energyict.dlms.cosem.PPPSetup.PPPAuthenticationType;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.genericprotocolimpl.common.RtuMessageConstant;
import com.energyict.genericprotocolimpl.common.StoreObject;
import com.energyict.genericprotocolimpl.webrtukp.profiles.DailyMonthly;
import com.energyict.genericprotocolimpl.webrtukp.profiles.ElectricityProfile;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.CodeCalendar;
import com.energyict.mdw.core.CodeDayType;
import com.energyict.mdw.core.CodeDayTypeDef;
import com.energyict.mdw.core.CommunicationProfile;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.Lookup;
import com.energyict.mdw.core.LookupEntry;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.core.RtuMessage;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdw.shadow.RtuShadow;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterValue;
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
import com.energyict.protocolimpl.dlms.DLMSCache;
import com.energyict.protocolimpl.dlms.RtuDLMS;
import com.energyict.protocolimpl.dlms.RtuDLMSCache;

/**
 * 
 * @author gna
 * |08012009| First complete draft containing:
 * 					- LoadProfile E-meter
 * 					- Registers E-meter
 * 					- LoadProfile Mbus-meter
 * 					- Registers Mbus-meter
 * Changes:
 * GNA |20012009| Added the imageTransfer message, here we use the P3ImageTransfer object
 * GNA |22012009| Added the Consumer messages over the P1 port 
 * GNA |27012009| Added the Disconnect Control message
 * GNA |28012009| Implemented the Loadlimit messages - Enabled the daily/Monthly code
 * GNA |02022009| Added the forceClock functionality
 * GNA |12022009| Added ActivityCalendar and SpecialDays as rtu message
 * GNA |17022009| Bug in hasMbusMeters(), if serialnumber is not found -> log and go next
 * GNA |19022009| Changed all messageEntrys in date-form to a UnixTime entry; 
 * 					Added a message to change to connectMode of the disconnectorObject;
 * 					Fixed bugs in the ActivityCalendar object; Added an entry delete of the specialDays
 * GNA |09032009| Added the informationFieldSize to the HDLCConnection so the max send/received length is customizable
 * GNA |16032009| Added the getTimeDifference method so timedifferences are shown in the AMR logging. 
 * 					Added properties to disable the reading of the daily/monthly values
 * 					Added ipPortNumber property for updating the phone number with inbound communications
 */

public class WebRTUKP implements GenericProtocol, ProtocolLink, Messaging, HHUEnabler{
	
	private boolean DEBUG = true; // TODO set it to false if you release

	private CosemObjectFactory 		cosemObjectFactory;
	private DLMSConnection 			dlmsConnection;
	private DLMSMeterConfig			dlmsMeterConfig;
	private AARQ					aarq;
	private CommunicationProfile	commProfile;
	private Logger					logger;
	private CommunicationScheduler	scheduler;
	private Link					link;
	private Rtu						webRtuKP;
	
	// this cache object is supported by 7.5
	private DLMSCache 				dlmsCache=new DLMSCache();	     
	
	private MbusDevice[]			mbusDevices;
	private Clock					deviceClock;
	private StoreObject				storeObject;
	private ObisCodeMapper			ocm;
	
	private long 					timeDifference = 0;
	/**
	 * Properties
	 */
	private Properties properties;
	private int securityLevel;	// 0: No Authentication - 1: Low Level - 2: High Level
	private int connectionMode; // 0: DLMS/HDLC - 1: DLMS/TCPIP
	private int clientMacAddress;
	private int serverLowerMacAddress;
	private int serverUpperMacAddress;
	private int requestTimeZone;
	private int timeout;
	private int forceDelay;
	private int retries;
	private int addressingMode;
	private int extendedLogging;
	private int maxMbusDevices;
	private int informationFieldSize;
	private String password;
	private String serialNumber;
	private String manufacturer;
	private String deviceId;
	private boolean readDaily;
	private boolean readMonthly;
	
	/**
	 * This method handles the complete WebRTU. The Rtu acts as an Electricity meter. The E-meter itself can have several MBus meters
	 * - First he handles his own data collection:
	 * 		_Profiles
	 * 		_Daily/Monthly readings
	 * 		_Registers
	 * 		_Messages
	 * - Then all the MBus meters are handled in the same way as the E-meter
	 */
	public void execute(CommunicationScheduler scheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {
		
		boolean success = false;
		
		this.scheduler = scheduler;
		this.logger = logger;
		this.commProfile = this.scheduler.getCommunicationProfile();
		this.webRtuKP = this.scheduler.getRtu();
		this.link = link;
		
		validateProperties();
		
		
		try {
			
			init(link.getInputStream(), link.getOutputStream());
			connect();
			
//			doSomeTestCalls();
			
//			readFromMeter("1.0.1.7.0.255");
			
//			hasMBusMeters();
//			handleMbusMeters();
        	// Set clock or Force clock... if necessary
        	if( this.commProfile.getForceClock() ){
        		
        		// TODO get the timedifference here!
        		// TODO			
        		Date meterTime = getTime();
        		Date currentTime = Calendar.getInstance(getTimeZone()).getTime();
        		this.timeDifference = Math.abs(currentTime.getTime()-meterTime.getTime());
        		getLogger().log(Level.INFO, "Forced to set meterClock to systemTime: " + currentTime);
        		forceClock(currentTime);
        	}else {
        		verifyAndWriteClock();
        	}
			
			if(this.commProfile.getReadDemandValues()){
				getLogger().log(Level.INFO, "Getting loadProfile for meter with serialnumber: " + webRtuKP.getSerialNumber());
				ElectricityProfile ep = new ElectricityProfile(this);
				
				ep.getProfile(getMeterConfig().getProfileObject().getObisCode(), this.commProfile.getReadMeterEvents());
			} 
			
    		/**
    		 * Here we are assuming that the daily and monthly values should be read.
    		 * In future it can be that this doesn't work for all customers, then we should implement a SmartMeterProperty to indicate whether you
    		 * want to read the actual registers or the daily/monthly registers ...
    		 */
			if(this.commProfile.getReadMeterReadings()){
				
				DailyMonthly dm = new DailyMonthly(this);
				if(readDaily){
					getLogger().log(Level.INFO, "Getting daily values for meter with serialnumber: " + webRtuKP.getSerialNumber());
					dm.getDailyValues(getMeterConfig().getDailyProfileObject().getObisCode());
				}
				if(readMonthly){
					getLogger().log(Level.INFO, "Getting monthly values for meter with serialnumber: " + webRtuKP.getSerialNumber());
					dm.getMonthlyValues(getMeterConfig().getMonthlyProfileObject().getObisCode());
				}
				
				getLogger().log(Level.INFO, "Getting registers for meter with serialnumber: " + webRtuKP.getSerialNumber());
				doReadRegisters();
			}
			
			if(this.commProfile.getSendRtuMessage()){
				sendMeterMessages();
			}
			
			if(hasMBusMeters()){
				getLogger().log(Level.INFO, "Starting to handle the MBus meters.");
				handleMbusMeters();
			}
			
			success = true;
			
		} catch (DLMSConnectionException e) {
			e.printStackTrace();
			disConnect();
		} catch (ClassCastException e){
			// Mostly programmers fault if you get here ...
			e.printStackTrace();
			disConnect();
		} catch (SQLException e){
			e.printStackTrace();
			disConnect();
			
			/** Close the connection after an SQL exception, connection will startup again if requested */
        	Environment.getDefault().closeConnection();
			
			throw new BusinessException(e);
		} finally{
			
//			GenericCache.stopCacheMechanism(getMeter(), dlmsCache);

			// This cacheobject is supported by the 7.5
			updateCache(getMeter().getId(), dlmsCache);
			
			Environment.getDefault().execute(getStoreObject());
			if(success){
				disConnect();
				getLogger().info("Meter " + this.serialNumber + " has completely finished.");
			}
		}
	}
	

	public long getTimeDifference() {
		return this.timeDifference;
	}
	
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel,false);
    }
    /**
     * Used by the framework
     * @param commChannel communication channel object
     * @param datareadout enable or disable data readout
     * @throws com.energyict.dialer.connection.ConnectionException thrown when a connection exception happens
     */
    public void enableHHUSignOn(SerialCommunicationChannel commChannel,boolean datareadout) throws ConnectionException {
        HHUSignOn hhuSignOn =
        (HHUSignOn)new IEC1107HHUConnection(commChannel, this.timeout, this.retries, 300, 0);
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(datareadout);
        getDLMSConnection().setHHUSignOn(hhuSignOn, this.deviceId);
    }
    /**
     * Getter for the data readout
     * @return byte[] with data readout
     */
    public byte[] getHHUDataReadout() {
        return getDLMSConnection().getHhuSignOn().getDataReadout();
    }
	
	/**
	 * Initializing global objects
	 * @param is - the inputStream to work with
	 * @param os - the outputStream to work with
	 * @throws IOException - can be cause by the TCPIPConnection
	 * @throws DLMSConnectionException - could not create a dlmsconnection
	 * @throws BusinessException 
	 * @throws SQLException 
	 */
	private void init(InputStream is, OutputStream os) throws IOException, DLMSConnectionException, SQLException, BusinessException{
		this.cosemObjectFactory	= new CosemObjectFactory((ProtocolLink)this);
		
		this.dlmsConnection = (this.connectionMode == 0)?
//					new TempHDLCConnection(is, os, this.timeout, this.forceDelay, this.retries, this.clientMacAddress, this.serverLowerMacAddress, this.serverUpperMacAddress, this.addressingMode):
					new KPHDLCConnection(is, os, this.timeout, this.forceDelay, this.retries, this.clientMacAddress, this.serverLowerMacAddress, this.serverUpperMacAddress, this.addressingMode,this.informationFieldSize):
					new TCPIPConnection(is, os, this.timeout, this.forceDelay, this.retries, this.clientMacAddress, this.serverLowerMacAddress);
		
					
		if (DialerMarker.hasOpticalMarker(this.link)){
			((HHUEnabler)this).enableHHUSignOn(this.link.getSerialCommunicationChannel());
		}			
					
		this.dlmsMeterConfig = DLMSMeterConfig.getInstance(this.manufacturer);
		
		// this cacheobject is supported by the 7.5
		setCache(fetchCache(getMeter().getId()));
		
		this.mbusDevices = new MbusDevice[this.maxMbusDevices];
		this.storeObject = new StoreObject();
	}

	/**
	 * Makes a connection to the server, if the socket is not available then an error is thrown.
	 * After a successful connection, we initiate an authentication request.
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws BusinessException 
	 */
	private void connect() throws IOException, SQLException, BusinessException{
		try {
			getDLMSConnection().connectMAC();
			if(this.connectionMode == 0){
					log(Level.INFO, "Sign On procedure done.");
			}
			getDLMSConnection().setIskraWrapper(1);
			aarq = new AARQ(this.securityLevel, this.password, getDLMSConnection());
			
			// objectList
			checkCacheObjects();
			
			if(getMeterConfig().getInstantiatedObjectList() == null){	// should never do this
				getMeterConfig().setInstantiatedObjectList(this.dlmsCache.getObjectList());
			}
			
			// do some checks to know you are connected to the correct meter
			verifyMeterSerialNumber();
			log(Level.INFO, "FirmwareVersion: " + getFirmWareVersion());
			
			// for incoming IP-calls
			updateIPAddress();
			
			if(this.extendedLogging >= 1){
				log(Level.INFO, getRegistersInfo());
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		} catch (DLMSConnectionException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		} catch (BusinessException e) {
			e.printStackTrace();
			throw new BusinessException(e);
		}
	}
	
	/**
	 * Collect the IP address of the meter and update this value on the RTU
	 * @throws SQLException
	 * @throws BusinessException
	 * @throws IOException
	 */
	private void updateIPAddress() throws SQLException, BusinessException, IOException{
		StringBuffer ipAddress = new StringBuffer();
		try {
			IPv4Setup ipv4Setup = getCosemObjectFactory().getIPv4Setup();
			ipAddress.append(ipv4Setup.getIPAddress());
			ipAddress.append(":");
			ipAddress.append(getPortNumber());
			
			RtuShadow shadow = getMeter().getShadow();
//			shadow.setIpAddress(ipAddress.toString());
			shadow.setPhoneNumber(ipAddress.toString());
			
			getMeter().update(shadow);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not set the IP address.");
		} catch (SQLException e){
			e.printStackTrace();
			throw new SQLException("Could not update the IP address.");
		}
	}
	
    /**
     * Look if there is a portnumber given with the property IpPortNumber, else use the default 2048
     * @return
     */
    private String getPortNumber(){
    	String port = getMeter().getProperties().getProperty("IpPortNumber");
    	if(port != null){
    		return port; 
    	} else {
    		return "4059";	// default port number
    	}
    }
	
	/**
	 * Just to test some objects
	 */
	private void doSomeTestCalls(){
		try {
			SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getImageActivationSchedule().getObisCode());
			String strDate = "27/01/2009 07:45:00";
			Array dateArray = convertUnixToDateTimeArray(strDate);
			sas.writeExecutionTime(dateArray);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Handles all the MBus devices like a separate device
	 */
	private void handleMbusMeters(){
		for(int i = 0; i < this.maxMbusDevices; i++){
			try {
				if(mbusDevices[i] != null){
					mbusDevices[i].setWebRtu(this);
					mbusDevices[i].execute(scheduler, null, null);
					getLogger().info("MbusDevice " + i + " has finished." );
				}
			} catch (BusinessException e) {
				
	            /*
	             * A single MBusMeter failed: log and try next MBusMeter.
	             */
				e.printStackTrace();
				getLogger().log(Level.SEVERE, "MBusMeter with serial: " + mbusDevices[i].getCustomerID() + " has failed.");
				
			} catch (SQLException e) {
				
	        	/** Close the connection after an SQL exception, connection will startup again if requested */
	        	Environment.getDefault().closeConnection();
				
	            /*
	             * A single MBusMeter failed: log and try next MBusMeter.
	             */
				e.printStackTrace();
				getLogger().log(Level.SEVERE, "MBusMeter with serial: " + mbusDevices[i].getCustomerID() + " has failed.");
				
			} catch (IOException e) {
				
	            /*
	             * A single MBusMeter failed: log and try next MBusMeter.
	             */
				e.printStackTrace();
				getLogger().log(Level.SEVERE, "MBusMeter with serial: " + mbusDevices[i].getCustomerID() + " has failed. [" + e.getMessage() + "]" );
				
			}
		}
	}
	
	/**
	 * Reading all the registers configured on the RTU
	 * @throws IOException
	 */
	private void doReadRegisters() throws IOException{
		Iterator<RtuRegister> it = getMeter().getRegisters().iterator();
		ObisCode oc = null;
		RegisterValue rv;
		RtuRegister rr;
		try {
			while(it.hasNext()){
				rr = it.next();
				oc = rr.getRtuRegisterSpec().getObisCode();
				rv = readRegister(oc);
				rv.setRtuRegisterId(rr.getId());
				
				if(rr.getReadingAt(rv.getReadTime()) == null){
					getStoreObject().add(rr, rv);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}
	
	/**
	 * TestMethod to read a certain obisCode from the meter
	 * @param name - the Obiscode in String format
	 * @throws IOException
	 */
	private void readFromMeter(String name) throws IOException{
		try {
			CosemObject cobj = getCosemObjectFactory().getCosemObject(ObisCode.fromString(name));
			cobj.getText();
			long value = cobj.getValue();
			
//			String value = "";
//			getCosemObjectFactory().getGenericRead(getMeterConfig().getMbusSerialNumber(0)).getString();
//			System.out.println("Value: " + value);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Reading of object has failed!");
		}
	}
	
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
    	if(ocm == null){
    		ocm = new ObisCodeMapper(getCosemObjectFactory());
    	}
    	return ocm.getRegisterValue(obisCode);
    }
	
	private String getFirmWareVersion() throws IOException{
		try {
			return getCosemObjectFactory().getGenericRead(getMeterConfig().getVersionObject()).getString();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not fetch the firmwareVersion.");
		}
	}
	
	private void verifyMeterSerialNumber() throws IOException{
		String serial = getSerialNumber();
		if(!this.serialNumber.equals(serial)){
			throw new IOException("Wrong serialnumber, EIServer settings: " + this.serialNumber + " - Meter settings: " + serial);
		}
	}

	public String getSerialNumber() throws IOException{
		try {
			return getCosemObjectFactory().getGenericRead(getMeterConfig().getSerialNumberObject()).getString();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not retrieve the serialnumber of the meter." + e);
		}
	}

	private String getRegistersInfo() throws IOException {
		try {
			StringBuilder strBuilder = new StringBuilder();
			
			strBuilder.append("********************* All instantiated objects in the meter *********************\n");
			for (int i=0;i<getMeterConfig().getInstantiatedObjectList().length;i++) {
			    UniversalObject uo = getMeterConfig().getInstantiatedObjectList()[i];
			    strBuilder.append(uo.getObisCode().toString()+" "+uo.getObisCode().getDescription()+"\n");
			}
			
			strBuilder.append("********************* Objects captured into load profile *********************\n");
			Iterator<CapturedObject> it = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getProfileObject().getObisCode()).getCaptureObjects().iterator();
			while(it.hasNext()) {
			    CapturedObject capturedObject = it.next();
			    strBuilder.append(capturedObject.getLogicalName().getObisCode().toString()+" "+capturedObject.getLogicalName().getObisCode().getDescription()+" (load profile)\n");
			}
			
			strBuilder.append("********************* Objects captured into daily load profile *********************\n");
			Iterator<CapturedObject> it2 = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getDailyProfileObject().getObisCode()).getCaptureObjects().iterator();
			while(it2.hasNext()) {
			    CapturedObject capturedObject = it2.next();
			    strBuilder.append(capturedObject.getLogicalName().getObisCode().toString()+" "+capturedObject.getLogicalName().getObisCode().getDescription()+" (load profile)\n");
			}
			
//			strBuilder.append("********************* Objects captured into monthly load profile *********************\n");
//			Iterator<CapturedObject> it3 = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getMonthlyProfileObject().getObisCode()).getCaptureObjects().iterator();
//			while(it3.hasNext()) {
//			    CapturedObject capturedObject = it3.next();
//			    strBuilder.append(capturedObject.getLogicalName().getObisCode().toString()+" "+capturedObject.getLogicalName().getObisCode().getDescription()+" (load profile)\n");
//			}
			
			return strBuilder.toString();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not generate the extended loggings." + e);
		}
	}
	
	/**
	 * After every communication, we close the connection to the meter.
	 * @throws IOException
	 * @throws DLMSConnectionException 
	 */
	private void disConnect() throws IOException{
		try {
			aarq.disConnect();
			getDLMSConnection().disconnectMAC();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException();
		} catch (DLMSConnectionException e) {
			e.printStackTrace();
			throw new IOException("Failed to access the dlmsConnection");
		}
	}
	
	/**
	 * Method to check whether the cache needs to be read out or not, if so the read will be forced
	 * @throws IOException 
	 */
	private void checkCacheObjects() throws IOException{
		
		int configNumber;
		if(dlmsCache.getObjectList() != null){		// the dlmsCache exists
			setCachedObjects();
			
			try {
				log(Level.INFO, "Checking the configuration parameters.");
				configNumber = requestConfigurationChanges();
			} catch (IOException e) {
				e.printStackTrace();
				configNumber = -1;
				log(Level.SEVERE, "Config change parameter could not be retrieved, configuration is forced to be read.");
				requestConfiguration();
				dlmsCache.saveObjectList(getMeterConfig().getInstantiatedObjectList());
				dlmsCache.setConfProgChange(configNumber);
			}
			
			if(dlmsCache.getConfProgChange() != configNumber){
				log(Level.INFO,"Meter configuration has changed, configuration is forced to be read.");
				requestConfiguration();
				dlmsCache.saveObjectList(getMeterConfig().getInstantiatedObjectList());
				dlmsCache.setConfProgChange(configNumber);
			}
			
		} else {		// cache does not exist
			log(Level.INFO,"Cache does not exist, configuration is forced to be read.");
			requestConfiguration();
			try {
				configNumber = requestConfigurationChanges();
				dlmsCache.saveObjectList(getMeterConfig().getInstantiatedObjectList());
				dlmsCache.setConfProgChange(configNumber);
			} catch (IOException e) {
				e.printStackTrace();
				configNumber=-1;
			}
		}
	}
	
	/**
	 * Fill in all the parameters from the cached object.
	 * NOTE: do NOT mix this with the CAPTURED_OBJECTS
	 */
	protected void setCachedObjects(){
		getMeterConfig().setInstantiatedObjectList(this.dlmsCache.getObjectList());
	}
	
	/**
	 * Read the number of configuration changes in the meter
	 * @return
	 * @throws IOException
	 */
	private int requestConfigurationChanges() throws IOException{
		try {
			return (int)getCosemObjectFactory().getCosemObject(getMeterConfig().getConfigObject().getObisCode()).getValue();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not retrieve the configuration change parameter" + e);
		}
	}
	
	/**
	 * Request all the configuration parameters out of the meter.
	 */
	private void requestConfiguration(){
		
		dlmsCache = new DLMSCache();   
		// get the complete objectlist from the meter
		try {
			getMeterConfig().setInstantiatedObjectList(getCosemObjectFactory().getAssociationLN().getBuffer());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param channel
	 * @return a Calendar object from the lastReading of the given channel, if the date is NULL,
	 * a date from one month ago is created at midnight.
	 */
	public Calendar getFromCalendar(Channel channel){
		Date lastReading = channel.getLastReading();
		if(lastReading == null){
			lastReading = com.energyict.genericprotocolimpl.common.ParseUtils.getClearLastMonthDate(channel.getRtu());
		}
		Calendar cal = ProtocolUtils.getCleanCalendar(getTimeZone());
		cal.setTime(lastReading);
		return cal;
	}
	
	/**
	 * @param rtu
	 * @return a Calendar object from the lastLogReading of the given channel, if the date is NULL,
	 * a date from one month ago is created at midnight
	 */
	public Calendar getFromLogCalendar(Rtu rtu){
		Date lastLogReading = rtu.getLastLogbook();
		if(lastLogReading == null){
			lastLogReading = com.energyict.genericprotocolimpl.common.ParseUtils.getClearLastMonthDate(rtu);
		}
		Calendar cal = ProtocolUtils.getCleanCalendar(getTimeZone());
		cal.setTime(lastLogReading);
		return cal;
	}
	
	public Calendar getToCalendar(){
		return ProtocolUtils.getCalendar(getTimeZone());
	}
	
	private void verifyAndWriteClock() throws IOException{
		try {
			Date meterTime = getTime();
			Date now = Calendar.getInstance(getTimeZone()).getTime();
			
			this.timeDifference = Math.abs(now.getTime()-meterTime.getTime());
			long diff = this.timeDifference / 1000;
			
			log(Level.INFO, "Difference between metertime(" + meterTime + ") and systemtime(" + now + ") is " + diff + "s.");
			if(this.commProfile.getWriteClock()){
				if( (diff < this.commProfile.getMaximumClockDifference()) && (diff > this.commProfile.getMinimumClockDifference()) ){
					log(Level.INFO, "Metertime will be set to systemtime: " + now);
					setClock(now);
				}
			} else {
				log(Level.INFO, "WriteClock is disabled, metertime will not be set.");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
		
	}
	
	private void forceClock(Date currentTime) throws IOException{
		try {
			getCosemObjectFactory().getClock().setTimeAttr(new DateTime(currentTime));
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not force to set the Clock object.");
		}
	}
	
	private Date getTime() throws IOException{
		try {
			Date meterTime;
			this.deviceClock = getCosemObjectFactory().getClock();
			meterTime = deviceClock.getDateTime();
			return meterTime;
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not retrieve the Clock object.");
		}
	}
	
	private void setClock(Date time) throws IOException{
		try {
			getCosemObjectFactory().getClock().setTimeAttr(new DateTime(time));
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not set the Clock object.");
		}
	}
	
	private Clock getDeviceClock(){
		return this.deviceClock;
	}
	
	/**
	 * @return the current webRtu
	 */
	public Rtu getMeter(){
		return this.webRtuKP;
	}
	
	private boolean hasMBusMeters() throws SQLException, BusinessException, IOException{

		String serialMbus = "";
		Rtu mbus;
		List<Rtu> slaves = getMeter().getDownstreamRtus();
		Iterator<Rtu> it = slaves.iterator();
		int count = 0;
		int mbusChannel;
		while(it.hasNext()){
			mbusChannel = -1;
			try {
				mbus = it.next();
				serialMbus = mbus.getSerialNumber();
				mbusChannel = checkSerialForMbusChannel(serialMbus);
//				this.mbusDevices[count++] = new MbusDevice(serialMbus, mbus, getLogger());
				if(mbusChannel != -1){
					this.mbusDevices[count++] = new MbusDevice(serialMbus, mbusChannel, mbus, getLogger());
				} else {
					getLogger().log(Level.INFO, "Mbusmeter with serialnumber " + serialMbus + " is not found on E-meter " + this.serialNumber);
				}
			} catch (ApplicationException e) {
				// catch and go to next slave
				e.printStackTrace();
			}
		}
		
    	for(int i = 0; i < this.maxMbusDevices; i++){
			if ( mbusDevice(i) != null ){
				if(isValidMbusMeter(i)){
					return true;
				}
			}
    	}
		
    	return false;
	}
	
	/**
	 * Method to check which mbusSerialnumber is on which channel ...
	 * @param serialMbus
	 * @return the channel corresponding with the serialnumber
	 */
	private int checkSerialForMbusChannel(String serialMbus) {
		String slaveSerial = "";
		for(int i = 0; i < this.maxMbusDevices; i++){
			try {
				slaveSerial = getCosemObjectFactory().getGenericRead(getMeterConfig().getMbusSerialNumber(i)).getString();
				if(slaveSerial.equalsIgnoreCase(serialMbus)){
					return i;
				}
			} catch (IOException e) {
				e.printStackTrace();	// catch and go to next
				log(Level.INFO, "Could not retrieve the mbusSerialNumber for channel " + (i+1));
			}
		}
		return -1;
	}

	/** Short notation for MeteringWarehouse.getCurrent() */
    private MeteringWarehouse mw() {
        return MeteringWarehouse.getCurrent();
    }
	
	private boolean isValidMbusMeter(int i){
		return mbusDevice(i).isValid();
	}
	
	private MbusDevice mbusDevice(int i){
		return this.mbusDevices[i];
	}
	
	private void validateProperties() throws MissingPropertyException{
        Iterator<String> iterator= getRequiredKeys().iterator();
        while (iterator.hasNext())
        {
            String key = iterator.next();
            if (properties.getProperty(key) == null)
                throw new MissingPropertyException (key + " key missing");
        }
        
		if(getMeter().getDeviceId() != ""){
			this.deviceId = getMeter().getDeviceId();
		} else { 
			this.deviceId = "!"; 
		}
		if(getMeter().getPassword() != ""){
			this.password = getMeter().getPassword();
		} else {
			this.password = "";
		}
		if(getMeter().getSerialNumber() != ""){
			this.serialNumber = getMeter().getSerialNumber();
		} else {
			this.serialNumber = "";
		}
        
//        this.serialNumber = properties.getProperty(MeterProtocol.SERIALNUMBER, "");
        this.securityLevel = Integer.parseInt(properties.getProperty("SecurityLevel", "0"));
        this.connectionMode = Integer.parseInt(properties.getProperty("Connection", "1"));
        this.clientMacAddress = Integer.parseInt(properties.getProperty("ClientMacAddress", "16"));
        this.serverLowerMacAddress = Integer.parseInt(properties.getProperty("ServerLowerMacAddress", "1"));
        this.serverUpperMacAddress = Integer.parseInt(properties.getProperty("ServerUpperMacAddress", "17"));
        this.requestTimeZone = Integer.parseInt(properties.getProperty("RequestTimeZone", "0"));
        // if HDLC set default timeout to 5s, if TCPIP set default timeout to 60s
        this.timeout = Integer.parseInt(properties.getProperty("Timeout", (this.connectionMode==0)?"5000":"60000"));	// set the HDLC timeout to 5000 for the WebRTU KP
        this.forceDelay = Integer.parseInt(properties.getProperty("ForceDelay", "100"));
        this.retries = Integer.parseInt(properties.getProperty("Retries", "3"));	
        this.addressingMode = Integer.parseInt(properties.getProperty("AddressingMode", "2"));
        this.extendedLogging = Integer.parseInt(properties.getProperty("ExtendedLogging", "0"));
        this.manufacturer = properties.getProperty("Manufacturer", "WKP");
        this.maxMbusDevices = Integer.parseInt(properties.getProperty("MaxMbusDevices", "4"));
        this.informationFieldSize = Integer.parseInt(properties.getProperty("InformationFieldSize","-1"));
        this.readDaily = !properties.getProperty("ReadDailyValues", "1").equalsIgnoreCase("0");
        this.readMonthly = !properties.getProperty("ReadMonthlyValues", "1").equalsIgnoreCase("0");
	}
	
	public void addProperties(Properties properties) {
		this.properties = properties;
	}

	public String getVersion() {
		return "$Date$";
	}

	public List<String> getOptionalKeys() {
        List<String> result = new ArrayList<String>(20);
        result.add("Timeout");
        result.add("Retries");
        result.add("DelayAfterFail");
        result.add("RequestTimeZone");
        result.add("FirmwareVersion");
        result.add("SecurityLevel");
        result.add("ClientMacAddress");
        result.add("ServerUpperMacAddress");
        result.add("ServerLowerMacAddress");
        result.add("InformationFieldSize");
        result.add("ExtendedLogging");
        result.add("LoadProfileId");
        result.add("AddressingMode");
        result.add("Connection");
        result.add("RtuType");
        result.add("TestLogging");
        result.add("ForceDelay");
        result.add("Manufacturer");
        result.add("MaxMbusDevices");
        result.add("ReadDailyValues");
        result.add("ReadMonthlyValues");
        result.add("IpPortNumber");	
		return result;
	}

	public List<String> getRequiredKeys() {
		List<String> result = new ArrayList<String>();
		return result;
	}

	public DLMSConnection getDLMSConnection() {
		return dlmsConnection;
	}

	public Logger getLogger() {
		return this.logger;
	}
	
	public void log(Level level, String msg){
		getLogger().log(level, msg);
	}

	public DLMSMeterConfig getMeterConfig() {
		return this.dlmsMeterConfig;
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
		return getMeter().getDeviceTimeZone();
	}

	public TimeZone getMeterTimeZone() throws IOException{
		if(getDeviceClock() != null){
			return TimeZone.getTimeZone(Integer.toString(getDeviceClock().getTimeZone()));
		}
		else {
			getTime();		//dummy to get the device DLMS clock
			return TimeZone.getTimeZone(Integer.toString(getDeviceClock().getTimeZone()));
		}
	}
	
	public boolean isRequestTimeZone() {
		return (this.requestTimeZone==1)?true:false;
	}
	
	public CosemObjectFactory getCosemObjectFactory() {
		return cosemObjectFactory;
	}
	
	public StoreObject getStoreObject(){
		return this.storeObject;
	}
	
	/**
	 * Messages
	 * @throws SQLException 
	 * @throws BusinessException 
	 */
	private void sendMeterMessages() throws BusinessException, SQLException {

		MessageHandler messageHandler = new MessageHandler();
		
		Iterator<RtuMessage> it = getMeter().getPendingMessages().iterator();
		RtuMessage rm = null;
		boolean success = false;
		byte theMonitoredAttributeType = -1;
		while(it.hasNext()){
			
			try {
				rm = (RtuMessage)it.next();
				String content = rm.getContents();
				importMessage(content, messageHandler);
				
				boolean xmlConfig		= messageHandler.getType().equals(RtuMessageConstant.XMLCONFIG);
				boolean firmware		= messageHandler.getType().equals(RtuMessageConstant.FIRMWARE_UPGRADE);
				boolean p1Text 			= messageHandler.getType().equals(RtuMessageConstant.P1TEXTMESSAGE);
				boolean p1Code 			= messageHandler.getType().equals(RtuMessageConstant.P1CODEMESSAGE);
				boolean connect			= messageHandler.getType().equals(RtuMessageConstant.CONNECT_LOAD);
				boolean disconnect		= messageHandler.getType().equals(RtuMessageConstant.DISCONNECT_LOAD);
				boolean connectMode		= messageHandler.getType().equals(RtuMessageConstant.CONNECT_CONTROL_MODE);
				boolean llConfig		= messageHandler.getType().equals(RtuMessageConstant.LOAD_LIMIT_CONFIGURE);
				boolean llClear			= messageHandler.getType().equals(RtuMessageConstant.LOAD_LIMIT_DISABLE);
				boolean llSetGrId		= messageHandler.getType().equals(RtuMessageConstant.LOAD_LIMIT_EMERGENCY_PROFILE_GROUP_ID_LIST);
				boolean touCalendar		= messageHandler.getType().equals(RtuMessageConstant.TOU_ACTIVITY_CAL);
				boolean touSpecialDays 	= messageHandler.getType().equals(RtuMessageConstant.TOU_SPECIAL_DAYS);
				boolean specialDelEntry	= messageHandler.getType().equals(RtuMessageConstant.TOU_SPECIAL_DAYS_DELETE);
				boolean setTime			= messageHandler.getType().equals(RtuMessageConstant.SET_TIME);
				boolean fillUpDB		= messageHandler.getType().equals(RtuMessageConstant.ME_MAKING_ENTRIES);
				boolean gprsParameters 	= messageHandler.getType().equals(RtuMessageConstant.GPRS_MODEM_SETUP);
				
				if(xmlConfig){
					
					log(Level.INFO, "Handling message " + rm.displayString() + ": XmlConfig");
					
					String xmlConfigStr = getMessageValue(content, RtuMessageConstant.XMLCONFIG);
					
					getCosemObjectFactory().getData(getMeterConfig().getXMLConfig().getObisCode()).setValueAttr(OctetString.fromString(xmlConfigStr));
					
					success = true;
					
				} else if(firmware){
					
					log(Level.INFO, "Handling message " + rm.displayString() + ": Firmware upgrade");
					
					String userFileID = messageHandler.getUserFileId();
					if(DEBUG)System.out.println("UserFileID: " + userFileID);
					
					if(!ParseUtils.isInteger(userFileID)){
						String str = "Not a valid entry for the current meter message (" + content + ").";
	            		throw new IOException(str);
					} 
					UserFile uf = mw().getUserFileFactory().find(Integer.parseInt(userFileID));
					if(!(uf instanceof UserFile )){
						String str = "Not a valid entry for the userfileID " + userFileID;
						throw new IOException(str);
					}
					
					byte[] imageData = uf.loadFileInByteArray();
					P3ImageTransfer p3it = getCosemObjectFactory().getP3ImageTransfer();
					p3it.upgrade(imageData);
					if(DEBUG)System.out.println("UserFile is send to the device.");
					if(messageHandler.activateNow()){
						if(DEBUG)System.out.println("Start the activateNow.");
						p3it.activateAndRetryImage();
						if(DEBUG)System.out.println("ActivateNow complete.");
					} else if(!messageHandler.getActivationDate().equalsIgnoreCase("")){
						SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getImageActivationSchedule().getObisCode());
						String strDate = messageHandler.getActivationDate();
						Array dateArray = convertUnixToDateTimeArray(strDate);
						if(DEBUG)System.out.println("Write the executionTime");
						sas.writeExecutionTime(dateArray);
						if(DEBUG)System.out.println("ExecutionTime sent...");
					}
					
					success = true;
					
				} else if(p1Code){
					
					log(Level.INFO, "Handling message " + rm.displayString() + ": Consumer message Code");
					
					Data dataCode = getCosemObjectFactory().getData(getMeterConfig().getConsumerMessageCode().getObisCode());
					dataCode.setValueAttr(OctetString.fromString(messageHandler.getP1Code()));
					
					success = true;
					
					
				} else if(p1Text){
					
					log(Level.INFO, "Handling message " + rm.displayString() + ": Consumer message Text");
					
					Data dataCode = getCosemObjectFactory().getData(getMeterConfig().getConsumerMessageText().getObisCode());
					dataCode.setValueAttr(OctetString.fromString(messageHandler.getP1Text()));
					
					success = true;
					
				} else if(connect){
					
					log(Level.INFO, "Handling message " + rm.displayString() + ": Connect");
					
					if(!messageHandler.getConnectDate().equals("")){	// use the disconnectControlScheduler
						
						Array executionTimeArray = convertUnixToDateTimeArray(messageHandler.getConnectDate());
						SingleActionSchedule sasConnect = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getDisconnectControlSchedule().getObisCode());
						
						ScriptTable disconnectorScriptTable = getCosemObjectFactory().getScriptTable(getMeterConfig().getDisconnectorScriptTable().getObisCode());
						byte[] scriptLogicalName = disconnectorScriptTable.getObjectReference().getLn(); 
						Structure scriptStruct = new Structure();
						scriptStruct.addDataType(new OctetString(scriptLogicalName));
						scriptStruct.addDataType(new Unsigned16(2)); 	// method '2' is the 'remote_connect' method
						
						sasConnect.writeExecutedScript(scriptStruct);
						sasConnect.writeExecutionTime(executionTimeArray);
						
					} else {	// immediate connect
						Disconnector connector = getCosemObjectFactory().getDisconnector();
						connector.remoteReconnect();
					}
					
					success = true;
				} else if(disconnect){
					
					log(Level.INFO, "Handling message " + rm.displayString() + ": Disconnect");
					
					if(!messageHandler.getDisconnectDate().equals("")){ // use the disconnectControlScheduler
						
						Array executionTimeArray = convertUnixToDateTimeArray(messageHandler.getDisconnectDate());
						SingleActionSchedule sasDisconnect = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getDisconnectControlSchedule().getObisCode());
						
						ScriptTable disconnectorScriptTable = getCosemObjectFactory().getScriptTable(getMeterConfig().getDisconnectorScriptTable().getObisCode());
						byte[] scriptLogicalName = disconnectorScriptTable.getObjectReference().getLn(); 
						Structure scriptStruct = new Structure();
						scriptStruct.addDataType(new OctetString(scriptLogicalName));
						scriptStruct.addDataType(new Unsigned16(1));	// method '1' is the 'remote_disconnect' method
						
						sasDisconnect.writeExecutedScript(scriptStruct);
						sasDisconnect.writeExecutionTime(executionTimeArray);
						
					} else { 	// immediate disconnect
						Disconnector disconnector = getCosemObjectFactory().getDisconnector();
						disconnector.remoteDisconnect();
					}
					
					success = true;
				} else if(connectMode){
					
					log(Level.INFO, "Handling message " + rm.displayString() + ": ConnectControl mode");
					String mode = messageHandler.getConnectControlMode();
					
					if(mode != null){
						try {
							int modeInt = Integer.parseInt(mode);
							
							if((modeInt >=0) && (modeInt <=6)){
								Disconnector connectorMode = getCosemObjectFactory().getDisconnector();
								connectorMode.writeControlMode(new TypeEnum(modeInt));
								
							} else {
								throw new IOException("Mode is not a valid entry for message " + rm.displayString() + ", value must be between 0 and 6");
							}
							
						} catch (NumberFormatException e) {
							e.printStackTrace();
							throw new IOException("Mode is not a valid entry for message " + rm.displayString());
						}
					} else {
						// should never get to the else, can't leave message empty 
						throw new IOException("Message " + rm.displayString() + " can not be empty");
					}
					
					success = true;
				} else if (llClear){
					
					log(Level.INFO, "Handling message " + rm.displayString() + ": Clear LoadLimit configuration");
					
					Limiter clearLLimiter = getCosemObjectFactory().getLimiter();
					
					// set the normal threshold duration to null
					clearLLimiter.writeThresholdNormal(new NullData());
					// set the emergency threshold duration to null
					clearLLimiter.writeThresholdEmergency(new NullData());
					// erase the emergency profile
					Structure emptyStruct = new Structure();
					emptyStruct.addDataType(new NullData());
					emptyStruct.addDataType(new NullData());
					emptyStruct.addDataType(new NullData());
					clearLLimiter.writeEmergencyProfile(clearLLimiter.new EmergencyProfile(emptyStruct.getBEREncodedByteArray(), 0, 0));
					
					success = true;
				} else if (llConfig){
					
					log(Level.INFO, "Handling message " + rm.displayString() + ": Set LoadLimit configuration");
					
					Limiter loadLimiter = getCosemObjectFactory().getLimiter();
					
					if(theMonitoredAttributeType == -1){	// check for the type of the monitored value
						ValueDefinitionType valueDefinitionType = loadLimiter.getMonitoredValue();
						theMonitoredAttributeType = getMonitoredAttributeType(valueDefinitionType);
					}
					
					// Write the normalThreshold
					if(messageHandler.getNormalThreshold() != null){
						try {
							loadLimiter.writeThresholdNormal(convertToMonitoredType(theMonitoredAttributeType, messageHandler.getNormalThreshold()));
						} catch (NumberFormatException e) {
							e.printStackTrace();
							log(Level.INFO, "Could not pars the normalThreshold value to an integer.");
							throw new IOException("Could not pars the normalThreshold value to an integer." + e.getMessage());
						}
					}
					
					// Write the emergencyThreshold
					if(messageHandler.getEmergencyThreshold() != null){
						try{
							loadLimiter.writeThresholdEmergency(convertToMonitoredType(theMonitoredAttributeType, messageHandler.getEmergencyThreshold()));
						} catch (NumberFormatException e) {
							e.printStackTrace();
							log(Level.INFO, "Could not pars the emergencyThreshold value to an integer.");
							throw new IOException("Could not pars the emergencyThreshold value to an integer." + e.getMessage());
						}
					}
					
					// Write the minimumOverThresholdDuration
					if(messageHandler.getOverThresholdDurtion() != null){
						try{
							loadLimiter.writeMinOverThresholdDuration(new Unsigned32(Integer.parseInt(messageHandler.getOverThresholdDurtion())));
						} catch (NumberFormatException e) {
							e.printStackTrace();
							log(Level.INFO, "Could not pars the minimum over threshold duration value to an integer.");
							throw new IOException("Could not pars the minimum over threshold duration value to an integer." + e.getMessage());
						}
					}
					
					// Construct the emergencyProfile
					Structure emergencyProfile = new Structure();
					if(messageHandler.getEpProfileId() != null){	// The EmergencyProfileID
						try {
							emergencyProfile.addDataType(new Unsigned16(Integer.parseInt(messageHandler.getEpProfileId())));
						} catch (NumberFormatException e) {
							e.printStackTrace();
							log(Level.INFO, "Could not pars the emergency profile id value to an integer.");
							throw new IOException("Could not pars the emergency profile id value to an integer." + e.getMessage());
						}
					}
					if(messageHandler.getEpActivationTime() != null){	// The EmergencyProfileActivationTime
						try{
//							emergencyProfile.addDataType(new OctetString(convertStringToDateTimeOctetString(messageHandler.getEpActivationTime()).getBEREncodedByteArray(), 0, true));
							emergencyProfile.addDataType(new OctetString(convertUnixToGMTDateTime(messageHandler.getEpActivationTime(), getTimeZone()).getBEREncodedByteArray(), 0, true));
						} catch (NumberFormatException e) {
							e.printStackTrace();
							log(Level.INFO, "Could not pars the emergency profile activationTime value to a valid date.");
							throw new IOException("Could not pars the emergency profile activationTime value to a valid date." + e.getMessage());
						}
					}
					if(messageHandler.getEpDuration() != null){		// The EmergencyProfileDuration
						try{
							emergencyProfile.addDataType(new Unsigned32(Integer.parseInt(messageHandler.getEpDuration())));
						} catch (NumberFormatException e) {
							e.printStackTrace();
							log(Level.INFO, "Could not pars the emergency profile duration value to an integer.");
							throw new IOException("Could not pars the emergency profile duration value to an integer." + e.getMessage());
						}
					}
					if((emergencyProfile.nrOfDataTypes() > 0) && (emergencyProfile.nrOfDataTypes() != 3)){	// If all three elements are correct, then send it, otherwise throw error
						throw new IOException("The complete emergecy profile must be filled in before sending it to the meter.");
					} else {
						if(emergencyProfile.nrOfDataTypes() > 0){
							loadLimiter.writeEmergencyProfile(emergencyProfile.getBEREncodedByteArray());
						}
					}
					
					success = true;
				} else if (llSetGrId){
					
					log(Level.INFO, "Handling message " + rm.displayString() + ": Set LoadLimit EmergencyProfile group ID's");
					
					Limiter epdiLimiter = getCosemObjectFactory().getLimiter();
					try {
						Lookup lut = mw().getLookupFactory().find(Integer.parseInt(messageHandler.getEpGroupIdListLookupTableId()));
						if(lut == null){
							throw new IOException("No lookuptable defined with id '" + messageHandler.getEpGroupIdListLookupTableId() + "'");
						} else {
							Iterator entriesIt = lut.getEntries().iterator();
							Array idArray = new Array();
							while(entriesIt.hasNext()){
								LookupEntry lue = (LookupEntry)entriesIt.next();
								idArray.addDataType(new Unsigned16(lue.getKey()));
							}
							epdiLimiter.writeEmergencyProfileGroupIdList(idArray);
						}
					} catch (NumberFormatException e) {
						e.printStackTrace();
						throw new IOException("The given lookupTable id is not a valid entry.");
					}
					
					success = true;
					
				} else if(touCalendar){
					
					log(Level.INFO, "Handling message " + rm.displayString() + ": Set Activity calendar");
					
					String name = messageHandler.getTOUCalendarName();
					String activateDate = messageHandler.getTOUActivationDate();
					String codeTable = messageHandler.getTOUCodeTable();
					String userFile = messageHandler.getTOUUserFile();
					
					if((codeTable == null) &&(userFile == null)){
						throw new IOException("CodeTable-ID AND UserFile-ID can not be both empty.");
					} else if((codeTable != null) &&(userFile != null)){
						throw new IOException("CodeTable-ID AND UserFile-ID can not be both filled in.");
					}
					
					if(codeTable != null){
						
						Code ct = mw().getCodeFactory().find(Integer.parseInt(codeTable));
						if(ct == null){
							throw new IOException("No CodeTable defined with id '" + codeTable + "'");
						} else {
							
							List calendars = ct.getCalendars();
							Array seasonArray = new Array();
							Array weekArray = new Array();
							HashMap seasonsProfile = new HashMap();
							ArrayList seasonsP = new ArrayList();
							
							Iterator itr = calendars.iterator();
							while(itr.hasNext()){ 
								CodeCalendar cc = (CodeCalendar)itr.next();
								int seasonId = cc.getSeason();
								if(seasonId != 0){
									OctetString os = new OctetString(new byte[]{(byte) ((cc.getYear()==-1)?0xff:((cc.getYear()>>8)&0xFF)), (byte) ((cc.getYear()==-1)?0xff:(cc.getYear())&0xFF), 
											(byte) ((cc.getMonth()==-1)?0xFF:cc.getMonth()), (byte) ((cc.getDay()==-1)?0xFF:cc.getDay()), (byte) 0xFF, 0, 0, 0, 0, (byte) 0x80, 0, 0});
									seasonsProfile.put(os, seasonId);
								}
							}

							seasonsP = getSortedList(seasonsProfile);
							
							int weekCount = 0;
							Iterator seasonsPIt = seasonsP.iterator();
							while(seasonsPIt.hasNext()){
								Structure entry = (Structure)seasonsPIt.next();
								OctetString dateTime = (OctetString)entry.getDataType(0);
								Structure seasonStruct = new Structure();
								int seasonProfileNameId = ((Unsigned8)entry.getDataType(1)).getValue();
								if(!seasonArrayExists(seasonProfileNameId, seasonArray)){
									
									String weekProfileName = "Week" + weekCount++;
									seasonStruct.addDataType(OctetString.fromString(Integer.toString(seasonProfileNameId)));	// the seasonProfileName is the DB id of the season
									seasonStruct.addDataType(dateTime);
									seasonStruct.addDataType(OctetString.fromString(weekProfileName));
									seasonArray.addDataType(seasonStruct);
									if(!weekArrayExists(weekProfileName, weekArray)){
										Structure weekStruct = new Structure();
										Iterator sIt = calendars.iterator();
										CodeDayType dayTypes[] = {null, null, null, null, null, null, null};
										CodeDayType any = null;
										while(sIt.hasNext()){
											CodeCalendar codeCal = (CodeCalendar)sIt.next();
											if(codeCal.getSeason() == seasonProfileNameId){
												switch(codeCal.getDayOfWeek()){
												case 1: {
													if(dayTypes[0] != null){
														if(dayTypes[0] != codeCal.getDayType()){throw new IOException("Season profiles are not correctly configured.");}
													}else{dayTypes[0] = codeCal.getDayType();}}break;
												case 2: {
													if(dayTypes[1] != null){
														if(dayTypes[1] != codeCal.getDayType()){throw new IOException("Season profiles are not correctly configured.");}
													}else{dayTypes[1] = codeCal.getDayType();}}break;
												case 3: {
													if(dayTypes[2] != null){
														if(dayTypes[2] != codeCal.getDayType()){throw new IOException("Season profiles are not correctly configured.");}
													}else{dayTypes[2] = codeCal.getDayType();}}break;
												case 4: {
													if(dayTypes[3] != null){
														if(dayTypes[3] != codeCal.getDayType()){throw new IOException("Season profiles are not correctly configured.");}
													}else{dayTypes[3] = codeCal.getDayType();}}break;
												case 5: {
													if(dayTypes[4] != null){
														if(dayTypes[4] != codeCal.getDayType()){throw new IOException("Season profiles are not correctly configured.");}
													}else{dayTypes[4] = codeCal.getDayType();}}break;
												case 6: {
													if(dayTypes[5] != null){
														if(dayTypes[5] != codeCal.getDayType()){throw new IOException("Season profiles are not correctly configured.");}
													}else{dayTypes[5] = codeCal.getDayType();}}break;
												case 7: {
													if(dayTypes[6] != null){
														if(dayTypes[6] != codeCal.getDayType()){throw new IOException("Season profiles are not correctly configured.");}
													}else{dayTypes[6] = codeCal.getDayType();}}break;
												case -1: {
													if(any != null){
														if(any != codeCal.getDayType()){throw new IOException("Season profiles are not correctly configured.");}
													}else{any = codeCal.getDayType();}}break;
												default: throw new IOException("Undefined daytype code received.");
												}
											}
										}
										
										weekStruct.addDataType(OctetString.fromString(weekProfileName));
										for(int i = 0; i < dayTypes.length; i++){
											if(dayTypes[i] != null){
												weekStruct.addDataType(new Unsigned8(dayTypes[i].getId()));
											} else if(any != null){
												weekStruct.addDataType(new Unsigned8(any.getId()));
											} else {
												throw new IOException("Not all dayId's are correctly filled in.");
											}
										}
										weekArray.addDataType(weekStruct);
										
									}
								}
							}
							Array dayArray = new Array();
							List dayProfiles = ct.getDayTypesOfCalendar();
							Iterator dayIt = dayProfiles.iterator();
							while(dayIt.hasNext()){
								CodeDayType cdt = (CodeDayType)dayIt.next();
								Structure schedule = new Structure();
								List definitions = cdt.getDefinitions();
								Array daySchedules = new Array();
								for(int i = 0; i < definitions.size(); i++){
									Structure def = new Structure();
									CodeDayTypeDef cdtd = (CodeDayTypeDef)definitions.get(i);
									int tStamp = cdtd.getTstampFrom();
									int hour = tStamp/10000;
									int min = (tStamp-hour*10000)/100;
									int sec = tStamp-(hour*10000)-(min*100);
									OctetString tstampOs = new OctetString(new byte[]{(byte)hour, (byte)min, (byte)sec, 0});
									Unsigned16 selector = new Unsigned16(cdtd.getCodeValue());
									def.addDataType(tstampOs);
									def.addDataType(new OctetString(getMeterConfig().getTariffScriptTable().getLNArray()));
//									def.addDataType(new OctetString(new byte[]{0,0,10,0,(byte)100,(byte)255}));
									def.addDataType(selector);
									daySchedules.addDataType(def);
								}
								schedule.addDataType(new Unsigned8(cdt.getId()));
								schedule.addDataType(daySchedules);
								dayArray.addDataType(schedule);
							}
							
							ActivityCalendar ac = getCosemObjectFactory().getActivityCalendar(getMeterConfig().getActivityCalendar().getObisCode());
							
							if(DEBUG)System.out.println(seasonArray);
							if(DEBUG)System.out.println(weekArray);
							if(DEBUG)System.out.println(dayArray);

							ac.writeSeasonProfilePassive(seasonArray);
							ac.writeWeekProfileTablePassive(weekArray);
							ac.writeDayProfileTablePassive(dayArray);
							
							if(name != null){
								if(name.length() > 8){
									name = name.substring(0, 8);
								}
								ac.writeCalendarNamePassive(OctetString.fromString(name));
							} 
							if(activateDate != null){
//								ac.writeActivatePassiveCalendarTime(new OctetString(convertStringToDateTimeOctetString(activateDate).getBEREncodedByteArray(), 0, true));
								ac.writeActivatePassiveCalendarTime(new OctetString(convertUnixToGMTDateTime(activateDate, getTimeZone()).getBEREncodedByteArray(), 0));
							}
							
						}
						
					} else if(userFile != null){
						throw new IOException("ActivityCalendar by userfile is not supported yet.");
					} else {
						// should never get here 
						throw new IOException("CodeTable-ID AND UserFile-ID can not be both empty.");
					}
					
					success = true;
					
				} else if(touSpecialDays){
					
					log(Level.INFO, "Handling message " + rm.displayString() + ": Set Special Days table");
					
					String codeTable = messageHandler.getSpecialDaysCodeTable();
					
					if(codeTable == null){
						throw new IOException("CodeTalbe-ID can not be empty.");
					} else {
						
						Code ct = mw().getCodeFactory().find(Integer.parseInt(codeTable));
						if(ct == null){
							throw new IOException("No CodeTable defined with id '" + codeTable + "'");
						} else {

							List calendars = ct.getCalendars();
							Array sdArray = new Array();

							SpecialDaysTable sdt = getCosemObjectFactory().getSpecialDaysTable(getMeterConfig().getSpecialDaysTable().getObisCode());
							
							for(int i = 0; i < calendars.size(); i++){
								CodeCalendar cc = (CodeCalendar)calendars.get(i);
								if(cc.getSeason() == 0){
									OctetString os = new OctetString(new byte[]{(byte) ((cc.getYear()==-1)?0xff:((cc.getYear()>>8)&0xFF)), (byte) ((cc.getYear()==-1)?0xff:(cc.getYear())&0xFF), 
											(byte) ((cc.getMonth()==-1)?0xFF:cc.getMonth()), (byte) ((cc.getDay()==-1)?0xFF:cc.getDay()),
											(byte) ((cc.getDayOfWeek()==-1)?0xFF:cc.getDayOfWeek())});
									Unsigned8 dayType = new Unsigned8(cc.getDayType().getId());
									Structure struct = new Structure();
									AXDRDateTime dt = new AXDRDateTime(new byte[]{(byte)0x09, (byte) ((cc.getYear()==-1)?0x07:((cc.getYear()>>8)&0xFF)), (byte) ((cc.getYear()==-1)?0xB2:(cc.getYear())&0xFF), 
											(byte) ((cc.getMonth()==-1)?0xFF:cc.getMonth()), (byte) ((cc.getDay()==-1)?0xFF:cc.getDay()),
											(byte) ((cc.getDayOfWeek()==-1)?0xFF:cc.getDayOfWeek()), 0, 0, 0, 0, 0, 0, 0});	
									long days = dt.getValue().getTimeInMillis()/1000/60/60/24;
									struct.addDataType(new Unsigned16((int)days));
									struct.addDataType(os);
									struct.addDataType(dayType);
//									sdt.insert(struct);
									sdArray.addDataType(struct);
								}
							}
							
							if(sdArray.nrOfDataTypes() != 0){
								sdt.writeSpecialDays(sdArray);
							}
							
							success = true;
						}
					}
				} else if(specialDelEntry){
					try {
						SpecialDaysTable sdt = getCosemObjectFactory().getSpecialDaysTable(getMeterConfig().getSpecialDaysTable().getObisCode());
						sdt.delete(Integer.parseInt(messageHandler.getSpecialDayDeleteEntry()));
					} catch (NumberFormatException e) {
						e.printStackTrace();
						throw new IOException("Delete index is not a valid entry");
					}
					
					success = true;
				} else if(setTime){
					
					String epochTime = messageHandler.getEpochTime();
					log(Level.INFO, "Handling message " + rm.displayString() + ": Setting the device time to: " + convertUnixToGMTDateTime(epochTime, getTimeZone()).getValue().getTime());
					forceClock(convertUnixToGMTDateTime(epochTime, getTimeZone()).getValue().getTime());
					success = true;
					
				} else if(fillUpDB){
					
					log(Level.INFO, "Handling message " + rm.displayString() + ": Making database entries.");
					log(Level.INFO, "(This can take several minutes/houres, depending on the number of entries you want to simulate)");
					
					if(messageHandler.getMEEntries() > 0){
						// Start the entry making ...
						
						int entries = messageHandler.getMEEntries();
						String type = messageHandler.getMEInterval();
						Long millis = Long.parseLong(messageHandler.getMEStartDate())*1000;
						Date startTime = new Date(Long.parseLong(messageHandler.getMEStartDate())*1000);
						startTime = getFirstDate(startTime, type, getMeter().getTimeZone());
						while(entries > 0){
							log(Level.INFO, "Setting meterTime to: " + startTime );
							setClock(startTime);
							waitForCrossingBoundry();
							startTime = setBeforeNextInterval(startTime, type);
							entries--;
						}
					}
					
					if(messageHandler.getMESyncAtEnd()){
		        		Date currentTime = Calendar.getInstance(getTimeZone()).getTime();
		        		getLogger().log(Level.INFO, "Synced clock to: " + currentTime);
		        		forceClock(currentTime);
					}
					
					success = true;
					
				} else if(gprsParameters){
					
					log(Level.INFO, "Handling message " + rm.displayString() + ": Changing gprs modem parameters");
					
					PPPAuthenticationType pppat = getCosemObjectFactory().getPPPSetup().new PPPAuthenticationType();
					pppat.setAuthenticationType(PPPSetup.LCPOptionsType.AUTH_PAP);
					if(messageHandler.getGprsUsername() != null){
						pppat.setUserName(messageHandler.getGprsUsername());
					}
					if(messageHandler.getGprsPassword() != null){
						pppat.setPassWord(messageHandler.getGprsPassword());
					}
					if((messageHandler.getGprsUsername() != null) || (messageHandler.getGprsPassword() != null)){
						getCosemObjectFactory().getPPPSetup().writePPPAuthenticationType(pppat);
						
						// TODO change this back to using the DLMS object instead of the raw data
//						byte[] b = new byte[pppat.getBEREncodedByteArray().length + 2];
//						b[0] = (byte)0x16;
//						b[1] = (byte)0x01;
//						System.arraycopy(pppat.getBEREncodedByteArray(), 0, b, 2, b.length-2);
//						getCosemObjectFactory().getGenericWrite(ObisCode.fromString("0.0.25.3.0.255"), 5, 44).write(b);
					}
					
					if(messageHandler.getGprsApn() != null){
						getCosemObjectFactory().getGPRSModemSetup().writeAPN(messageHandler.getGprsApn());
					}
					
					success = true;
				}
				else {
					success = false;
				}
				
			} catch (BusinessException e) {
				e.printStackTrace();
				log(Level.INFO, "Message " + rm.displayString() + " has failed. " + e.getMessage());
			} catch (ConnectionException e){
				e.printStackTrace();
				log(Level.INFO, "Message " + rm.displayString() + " has failed. " + e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
				log(Level.INFO, "Message " + rm.displayString() + " has failed. " + e.getMessage());
			} catch (InterruptedException e) {
				e.printStackTrace();
				log(Level.INFO, "Message " + rm.displayString() + " has failed. " + e.getMessage());
			} finally {
				if(success){
					rm.confirm();
					log(Level.INFO, "Message " + rm.displayString() + " has finished.");
				} else {
					rm.setFailed();
				}
			}
		}
	}

	private void waitForCrossingBoundry() throws IOException{
		try {
			for(int i = 0; i < 3; i++){
				Thread.sleep(15000);
				log(Level.INFO, "Keeping connection alive");
				getTime();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new IOException("Interrupted while waiting." + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not keep connection alive." + e.getMessage());
		}
	}
	
	public static void main(String args[]){
		WebRTUKP wkp = new WebRTUKP();
		
		try {
			AXDRDateTime axdrDateTime = wkp.convertUnixToGMTDateTime("1236761593", TimeZone.getTimeZone("GMT"));
			System.out.println(axdrDateTime.getValue().getTime());
			System.out.println(wkp.getFirstDate(axdrDateTime.getValue().getTime(), "day", TimeZone.getTimeZone("GMT")));
			System.out.println(axdrDateTime.getValue().get(Calendar.HOUR_OF_DAY));
			System.out.println(axdrDateTime.getValue().getTimeZone().getRawOffset()/3600000);
			System.out.println(axdrDateTime.getValue().getTimeZone().getOffset(Long.parseLong("1236761593")*1000)/3600000);
			
			axdrDateTime = wkp.convertUnixToGMTDateTime("1236761593", TimeZone.getTimeZone("Europe/Brussels"));
			System.out.println(axdrDateTime.getValue().getTime());
			System.out.println(wkp.getFirstDate(axdrDateTime.getValue().getTime(), "day", TimeZone.getTimeZone("Europe/Brussels")));
			System.out.println(axdrDateTime.getValue().get(Calendar.HOUR_OF_DAY));
			System.out.println(axdrDateTime.getValue().getTimeZone().getRawOffset()/3600000);
			System.out.println(axdrDateTime.getValue().getTimeZone().getOffset(Long.parseLong("1236761593")*1000)/3600000);
			
			axdrDateTime = wkp.convertUnixToGMTDateTime("1234947193", TimeZone.getTimeZone("GMT"));
			System.out.println(axdrDateTime.getValue().getTime());
			System.out.println(wkp.getFirstDate(axdrDateTime.getValue().getTime(), "day", TimeZone.getTimeZone("GMT")));
			System.out.println(axdrDateTime.getValue().get(Calendar.HOUR_OF_DAY));
			System.out.println(axdrDateTime.getValue().getTimeZone().getRawOffset()/3600000);
			System.out.println(axdrDateTime.getValue().getTimeZone().getOffset(Long.parseLong("1234947193")*1000)/3600000);
			
			axdrDateTime = wkp.convertUnixToGMTDateTime("1234947193", TimeZone.getTimeZone("Europe/Brussels"));
			System.out.println(axdrDateTime.getValue().getTime());
			System.out.println(wkp.getFirstDate(axdrDateTime.getValue().getTime(), "day", TimeZone.getTimeZone("Europe/Brussels")));
			System.out.println(axdrDateTime.getValue().get(Calendar.HOUR_OF_DAY));
			System.out.println(axdrDateTime.getValue().getTimeZone().getRawOffset()/3600000);
			System.out.println(axdrDateTime.getValue().getTimeZone().getOffset(Long.parseLong("1234947193")*1000)/3600000);
			
			Date nextDate = wkp.getFirstDate(axdrDateTime.getValue().getTime(), "month", TimeZone.getTimeZone("Europe/Brussels"));
			int days = 0;
			while(days < 60){
				
				if(days == 36){
					System.out.println("timeout");
				}
				
				System.out.println(nextDate);
				nextDate = wkp.setBeforeNextInterval(nextDate, "month", TimeZone.getTimeZone("Europe/Brussels"));
				days++;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		}
	
	private Date getFirstDate(Date startTime, String type) throws IOException{
		return getFirstDate(startTime, type, getTimeZone());
		}
	
	private Date getFirstDate(Date startTime, String type, TimeZone timeZone) throws IOException{
		Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		cal1.setTime(startTime);
		if(type.equalsIgnoreCase("15")){
			if(cal1.get(Calendar.MINUTE) < 15){
				cal1.set(Calendar.MINUTE, 14);
				cal1.set(Calendar.SECOND, 40);
			} else if(cal1.get(Calendar.MINUTE) < 30){
				cal1.set(Calendar.MINUTE, 29);
				cal1.set(Calendar.SECOND, 40);
			} else if(cal1.get(Calendar.MINUTE) < 45){
				cal1.set(Calendar.MINUTE, 44);
				cal1.set(Calendar.SECOND, 40);
			} else {
				cal1.set(Calendar.MINUTE, 59);
				cal1.set(Calendar.SECOND, 40);
			}
			return cal1.getTime();
		} else if(type.equalsIgnoreCase("day")){
			cal1.set(Calendar.HOUR_OF_DAY, (23 - (timeZone.getOffset(startTime.getTime())/3600000)));
			cal1.set(Calendar.MINUTE, 59);
			cal1.set(Calendar.SECOND, 40);
			return cal1.getTime();
		} else if(type.equalsIgnoreCase("month")){
			cal1.set(Calendar.DATE, cal1.getActualMaximum(Calendar.DAY_OF_MONTH));
			cal1.set(Calendar.HOUR_OF_DAY, (23 - (timeZone.getOffset(startTime.getTime())/3600000)));
			cal1.set(Calendar.MINUTE, 59);
			cal1.set(Calendar.SECOND, 40);
			return cal1.getTime();
		}
		
		throw new IOException("Invalid intervaltype.");
	}

	private Date setBeforeNextInterval(Date startTime, String type) throws IOException {
		return setBeforeNextInterval(startTime, type, getMeter().getTimeZone());
	}
	
	private Date setBeforeNextInterval(Date startTime, String type, TimeZone timeZone) throws IOException{
		Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		cal1.setTime(startTime);
		int zoneOffset = 0;
		if(type.equalsIgnoreCase("15")){
			cal1.add(Calendar.MINUTE, 15);
			return cal1.getTime();
		} else if(type.equalsIgnoreCase("day")){
			zoneOffset = timeZone.getOffset(cal1.getTimeInMillis()) / 3600000;
			cal1.add(Calendar.DAY_OF_MONTH, 1);
			zoneOffset = zoneOffset - (timeZone.getOffset(cal1.getTimeInMillis()) / 3600000);
			cal1.add(Calendar.HOUR_OF_DAY, zoneOffset);
			return cal1.getTime();
		} else if(type.equalsIgnoreCase("month")){
			zoneOffset = timeZone.getOffset(cal1.getTimeInMillis()) / 3600000;
			cal1.add(Calendar.MONTH, 1);
			cal1.set(Calendar.DATE, cal1.getActualMaximum(Calendar.DAY_OF_MONTH));
			zoneOffset = zoneOffset - (timeZone.getOffset(cal1.getTimeInMillis()) / 3600000);
			cal1.add(Calendar.HOUR_OF_DAY, zoneOffset);
			return cal1.getTime();
		}
		
		throw new IOException("Invalid intervaltype.");
	}

	private ArrayList getSortedList(HashMap seasonsProfile) throws IOException {
		LinkedList list = new LinkedList();
		Structure struct;
		Iterator it = seasonsProfile.entrySet().iterator();
		boolean check;
		while(it.hasNext()){
			Map.Entry entry = (Map.Entry)it.next();
			AXDRDateTime dt = new AXDRDateTime((OctetString)entry.getKey());
			check = false;
			for(int i = 0; i < list.size(); i++){
				if(dt.getValue().getTime().before((new AXDRDateTime((OctetString)((Structure)list.get(i)).getDataType(0))).getValue().getTime())){
					struct = new Structure();
					struct.addDataType((OctetString)entry.getKey());
					struct.addDataType(new Unsigned8((Integer)entry.getValue()));
					list.add(i, struct);
					check = true;
					break;
				}
			}
			if(!check){
				struct = new Structure();
				struct.addDataType((OctetString)entry.getKey());
				struct.addDataType(new Unsigned8((Integer)entry.getValue()));
				list.add(struct);
			}
		}
		
		return new ArrayList(list);
	}

	private boolean seasonArrayExists(int seasonProfileNameId, Array seasonArray) {
		for(int i = 0; i < seasonArray.nrOfDataTypes(); i++){
			Structure struct = (Structure)seasonArray.getDataType(i);
			if(new String(((OctetString)struct.getDataType(0)).getOctetStr()).equalsIgnoreCase(Integer.toString(seasonProfileNameId))){
				return true;
			}
		}
		return false;
	}

	private boolean weekArrayExists(String weekProfileName, Array weekArray) {
		for(int i = 0; i < weekArray.nrOfDataTypes(); i++){
			Structure struct = (Structure)weekArray.getDataType(i);
			if(new String(((OctetString)struct.getDataType(0)).getOctetStr()).equalsIgnoreCase(weekProfileName)){
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the monitoredAttributeType
	 * @param vdt
	 * @return the abstractDataType of the monitored attribute
	 * @throws IOException
	 */
	private byte getMonitoredAttributeType(ValueDefinitionType vdt) throws IOException{ 
		
      if (getMeterConfig().getClassId(vdt.getObisCode()) == Register.CLASSID){
    	  return getCosemObjectFactory().getRegister(vdt.getObisCode()).getAttrbAbstractDataType(vdt.getAttributeIndex().getValue()).getBEREncodedByteArray()[0];
      } else if (getMeterConfig().getClassId(vdt.getObisCode()) == ExtendedRegister.CLASSID){
    	  return getCosemObjectFactory().getExtendedRegister(vdt.getObisCode()).getAttrbAbstractDataType(vdt.getAttributeIndex().getValue()).getBEREncodedByteArray()[0];
      }else if (getMeterConfig().getClassId(vdt.getObisCode()) == DemandRegister.CLASSID){
    	  return getCosemObjectFactory().getDemandRegister(vdt.getObisCode()).getAttrbAbstractDataType(vdt.getAttributeIndex().getValue()).getBEREncodedByteArray()[0];
      }else if (getMeterConfig().getClassId(vdt.getObisCode()) == Data.CLASSID){
    	  return getCosemObjectFactory().getData(vdt.getObisCode()).getAttrbAbstractDataType(vdt.getAttributeIndex().getValue()).getBEREncodedByteArray()[0];
      } else{
    	  throw new IOException("WebRtuKP, getMonitoredAttributeType, invalid classID " + getMeterConfig().getClassId(vdt.getObisCode())+" for obisCode "+vdt.getObisCode().toString()) ;
      }
	}
	
	/**
	 * Convert the value to write to the Limiter object to the correct monitored value type ...
	 * @param theMonitoredAttributeType
	 * @param value
	 * @return
	 * @throws IOException
	 */
	private AbstractDataType convertToMonitoredType(byte theMonitoredAttributeType, String value) throws IOException {
		try {
			switch(theMonitoredAttributeType){
			case DLMSCOSEMGlobals.TYPEDESC_NULL:{return new NullData();}
			case DLMSCOSEMGlobals.TYPEDESC_BOOLEAN:{return new BooleanObject(value.equalsIgnoreCase("1"));}
			case DLMSCOSEMGlobals.TYPEDESC_BITSTRING:{return new BitString(Integer.parseInt(value));}              
			case DLMSCOSEMGlobals.TYPEDESC_DOUBLE_LONG:{return new Integer32(Integer.parseInt(value));}
			case DLMSCOSEMGlobals.TYPEDESC_DOUBLE_LONG_UNSIGNED:{return new Unsigned32(Integer.parseInt(value));}
			case DLMSCOSEMGlobals.TYPEDESC_OCTET_STRING:{return OctetString.fromString(value);}
			case DLMSCOSEMGlobals.TYPEDESC_VISIBLE_STRING:{return new VisibleString(value);}
			case DLMSCOSEMGlobals.TYPEDESC_INTEGER:{return new Integer8(Integer.parseInt(value));}
			case DLMSCOSEMGlobals.TYPEDESC_LONG:{return new Integer16(Integer.parseInt(value));}
			case DLMSCOSEMGlobals.TYPEDESC_UNSIGNED:{return new Unsigned8(Integer.parseInt(value));}
			case DLMSCOSEMGlobals.TYPEDESC_LONG_UNSIGNED:{return new Unsigned16(Integer.parseInt(value));}
			case DLMSCOSEMGlobals.TYPEDESC_LONG64:{return new Integer64(Integer.parseInt(value));}
			case DLMSCOSEMGlobals.TYPEDESC_ENUM:{return new TypeEnum(Integer.parseInt(value));}
			default:    
			    throw new IOException("convertToMonitoredtype error, unknown type.");
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new NumberFormatException();
		}
	}
	@Deprecated
	private AXDRDateTime convertStringToDateTimeOctetString(String strDate) throws IOException{
		AXDRDateTime dateTime = null;
		Calendar cal = Calendar.getInstance(getMeter().getTimeZone());
		cal.set(Integer.parseInt(strDate.substring(strDate.lastIndexOf("/") + 1, strDate.indexOf(" ")))&0xFFFF,
				(Integer.parseInt(strDate.substring(strDate.indexOf("/") + 1, strDate.lastIndexOf("/")))&0xFF) -1,
				Integer.parseInt(strDate.substring(0, strDate.indexOf("/")))&0xFF,
				Integer.parseInt(strDate.substring(strDate.indexOf(" ") + 1, strDate.indexOf(":")))&0xFF,
				Integer.parseInt(strDate.substring(strDate.indexOf(":") + 1, strDate.lastIndexOf(":")))&0xFF,
				Integer.parseInt(strDate.substring(strDate.lastIndexOf(":") + 1, strDate.length()))&0xFF);
		dateTime = new AXDRDateTime(cal);
		return dateTime;
	}
	
	private AXDRDateTime convertUnixToGMTDateTime(String time, TimeZone timeZone) throws IOException{
		try {
			AXDRDateTime dateTime = null;
			Calendar cal = Calendar.getInstance(timeZone);
			cal.setTimeInMillis(Long.parseLong(time)*1000);
			dateTime = new AXDRDateTime(cal);
			return dateTime;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new IOException("Could not parse " + time + " to a long value");
		}
	}
	
	public Array convertUnixToDateTimeArray(String strDate) throws IOException {
		try {
			Calendar cal = Calendar.getInstance(getTimeZone());
			cal.setTimeInMillis(Long.parseLong(strDate)*1000);
			byte[] dateBytes = new byte[5];
			dateBytes[0] = (byte) ((cal.get(Calendar.YEAR) >> 8)&0xFF);
			dateBytes[1] = (byte) (cal.get(Calendar.YEAR) &0xFF);
			dateBytes[2] = (byte) ((cal.get(Calendar.MONTH)&0xFF) +1 );	
			dateBytes[3] = (byte) (cal.get(Calendar.DAY_OF_MONTH)&0xFF);
			dateBytes[4] = (byte)0xFF;
			OctetString date = new OctetString(dateBytes);
			byte[] timeBytes = new byte[4];
			timeBytes[0] = (byte) cal.get(Calendar.HOUR_OF_DAY);
			timeBytes[1] = (byte) cal.get(Calendar.MINUTE);
			timeBytes[2] = (byte) 0x00;
			timeBytes[3] = (byte) 0x00;
			OctetString time = new OctetString(timeBytes);
			
			Array dateTimeArray = new Array();
			dateTimeArray.addDataType(time);
			dateTimeArray.addDataType(date);
			return dateTimeArray;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new IOException("Could not parse " + strDate + " to a long value");
		}
	}
	
	@Deprecated
	public Array convertStringToDateTimeArray(String strDate) throws IOException {
		OctetString date = null;
		byte[] dateBytes = new byte[5];
		dateBytes[0] = (byte) ((Integer.parseInt(strDate.substring(strDate.lastIndexOf("/") + 1, strDate.indexOf(" "))) >> 8)&0xFF);
		dateBytes[1] = (byte) (Integer.parseInt(strDate.substring(strDate.lastIndexOf("/") + 1, strDate.indexOf(" ")))&0xFF);
		dateBytes[2] = (byte) ((Integer.parseInt(strDate.substring(strDate.indexOf("/") + 1, strDate.lastIndexOf("/"))))&0xFF);
		dateBytes[3] = (byte) (Integer.parseInt(strDate.substring(0, strDate.indexOf("/")))&0xFF);
		dateBytes[4] = (byte)0xFF;
		date = new OctetString(dateBytes, true);
		int deviation = getMeter().getTimeZone().getOffset(Calendar.getInstance(getMeterTimeZone()).getTimeInMillis())/3600000;
		Calendar cal = Calendar.getInstance(getMeterTimeZone());
		cal.set((dateBytes[0]<<8)+(dateBytes[1]&0xff), dateBytes[2], dateBytes[3],
				(Integer.parseInt(strDate.substring(strDate.indexOf(" ") + 1, strDate.indexOf(":")))&0xFF) - deviation,
				(Integer.parseInt(strDate.substring(strDate.indexOf(":") + 1, strDate.lastIndexOf(":")))&0xFF), 0);
		OctetString time = null;
		byte[] timeBytes = new byte[4];
//		timeBytes[0] = (byte) (Integer.parseInt(strDate.substring(strDate.indexOf(" ") + 1, strDate.indexOf(":")))&0xFF);
		timeBytes[0] = (byte) cal.get(Calendar.HOUR_OF_DAY);
//		timeBytes[1] = (byte) (Integer.parseInt(strDate.substring(strDate.indexOf(":") + 1, strDate.lastIndexOf(":")))&0xFF);
		timeBytes[1] = (byte) cal.get(Calendar.MINUTE);
		timeBytes[2] = (byte) 0x00;
		timeBytes[3] = (byte) 0x00;
		time = new OctetString(timeBytes, true);
		
		Array dateTimeArray = new Array();
		dateTimeArray.addDataType(time);
		dateTimeArray.addDataType(date);
		return dateTimeArray;
	}
	
	private String getMessageValue(String msgStr, String str) {
		try {
			return msgStr.substring(msgStr.indexOf(str + ">") + str.length()
					+ 1, msgStr.indexOf("</" + str));
		} catch (Exception e) {
			return "";
		}
	}
	
	public void importMessage(String message, DefaultHandler handler) throws BusinessException{
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

	public List getMessageCategories() {
		List categories = new ArrayList();
		MessageCategorySpec catXMLConfig = new MessageCategorySpec("XMLConfig");
		MessageCategorySpec catFirmware = new MessageCategorySpec("Firmware");
		MessageCategorySpec catP1Messages = new MessageCategorySpec("Consumer messages to P1");
		MessageCategorySpec catDisconnect = new MessageCategorySpec("Disconnect Control");
		MessageCategorySpec catLoadLimit = new MessageCategorySpec("LoadLimit");
		MessageCategorySpec catActivityCal = new MessageCategorySpec("ActivityCalendar");
		MessageCategorySpec catTime = new MessageCategorySpec("Time");
		MessageCategorySpec catMakeEntries = new MessageCategorySpec("Create database entries");
		MessageCategorySpec catGPRSModemSetup = new MessageCategorySpec("Change GPRS modem setup");
		
		// XMLConfig related messages
		MessageSpec msgSpec = addDefaultValueMsg("XMLConfig", RtuMessageConstant.XMLCONFIG, false);
		catXMLConfig.addMessageSpec(msgSpec);
		
		// Firmware related messages
		msgSpec = addFirmwareMsg("Upgrade Firmware", RtuMessageConstant.FIRMWARE_UPGRADE, false);
		catFirmware.addMessageSpec(msgSpec);
		
		// Consumer messages to P1 related messages
		msgSpec = addP1Text("Consumer message Text to port P1", RtuMessageConstant.P1TEXTMESSAGE, false);
		catP1Messages.addMessageSpec(msgSpec);
		msgSpec = addP1Code("Consumer message Code to port P1", RtuMessageConstant.P1CODEMESSAGE, false);
		catP1Messages.addMessageSpec(msgSpec);
		
		// Disconnect control related messages
		msgSpec = addConnectControl("Disconnect", RtuMessageConstant.DISCONNECT_LOAD, false);
		catDisconnect.addMessageSpec(msgSpec);
		msgSpec = addConnectControl("Connect", RtuMessageConstant.CONNECT_LOAD, false);
		catDisconnect.addMessageSpec(msgSpec);
		msgSpec = addConnectControlMode("ConnectControl mode", RtuMessageConstant.CONNECT_CONTROL_MODE, false);
		catDisconnect.addMessageSpec(msgSpec);
		
		// LoadLimit related messages
		msgSpec = addConfigureLL("Configure Loadlimiting parameters", RtuMessageConstant.LOAD_LIMIT_CONFIGURE, false);
		catLoadLimit.addMessageSpec(msgSpec);
		msgSpec = addNoValueMsg("Clear the Loadlimit configuration", RtuMessageConstant.LOAD_LIMIT_DISABLE, false);
		catLoadLimit.addMessageSpec(msgSpec);
		msgSpec = addGroupIdsLL("Set emergency profile group id's", RtuMessageConstant.LOAD_LIMIT_EMERGENCY_PROFILE_GROUP_ID_LIST, false);
		catLoadLimit.addMessageSpec(msgSpec);
		
		// Activity Calendar related messages
		msgSpec = addTimeOfUse("Select the Activity Calendar", RtuMessageConstant.TOU_ACTIVITY_CAL, false);
		catActivityCal.addMessageSpec(msgSpec);
		msgSpec = addSpecialDays("Select the Special days Calendar", RtuMessageConstant.TOU_SPECIAL_DAYS, false);
		catActivityCal.addMessageSpec(msgSpec);
		msgSpec = addSpecialDaysDelete("Delete Special Day entry", RtuMessageConstant.TOU_SPECIAL_DAYS_DELETE, false);
		catActivityCal.addMessageSpec(msgSpec);
		
		// Time related messages
		msgSpec = addTimeMessage("Set the meterTime to a specific time", RtuMessageConstant.SET_TIME, false);
		catTime.addMessageSpec(msgSpec);
		
		// Create database entries
		msgSpec = addCreateDBEntries("Create entries in the meters database", RtuMessageConstant.ME_MAKING_ENTRIES, false);
		catMakeEntries.addMessageSpec(msgSpec);
		
		// Change GPRS modem setup
		msgSpec = addChangeGPRSSetup("Change GPRS modem setup parameters", RtuMessageConstant.GPRS_MODEM_SETUP, false);
		catGPRSModemSetup.addMessageSpec(msgSpec);
		
		
		categories.add(catXMLConfig);
		categories.add(catFirmware);
		categories.add(catP1Messages);
		categories.add(catDisconnect);
		categories.add(catLoadLimit);
		categories.add(catActivityCal);
		categories.add(catTime);
		categories.add(catMakeEntries);
		categories.add(catGPRSModemSetup);
		return categories;
	}
	
	private MessageSpec addSpecialDays(String keyId, String tagName, boolean advanced) {
    	MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        tagSpec.add(msgVal);
        MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.TOU_SPECIAL_DAYS_CODE_TABLE, false);
        tagSpec.add(msgAttrSpec);
        msgSpec.add(tagSpec);
        return msgSpec;
	}
	
	private MessageSpec addSpecialDaysDelete(String keyId, String tagName, boolean advanced) {
    	MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        tagSpec.add(msgVal);
        MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.TOU_SPECIAL_DAYS_DELETE_ENTRY, true);
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
	
    private MessageSpec addGroupIdsLL(String keyId, String tagName, boolean advanced) {
    	MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.LOAD_LIMIT_EP_GRID_LOOKUP_ID, true);
        tagSpec.add(msgVal);
        tagSpec.add(msgAttrSpec);
        msgSpec.add(tagSpec);
        return msgSpec;
	}

	private MessageSpec addConfigureLL(String keyId, String tagName, boolean advanced) {
    	MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.LOAD_LIMIT_NORMAL_THRESHOLD, false);
        tagSpec.add(msgAttrSpec);
        msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.LOAD_LIMIT_EMERGENCY_THRESHOLD, false);
        tagSpec.add(msgAttrSpec);
        msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.LOAD_LIMIT_MIN_OVER_THRESHOLD_DURATION, false);
        tagSpec.add(msgAttrSpec);
        MessageTagSpec profileTagSpec = new MessageTagSpec("Emergency_Profile");
        profileTagSpec.add(msgVal);
        msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.LOAD_LIMIT_EP_PROFILE_ID, false);
        profileTagSpec.add(msgAttrSpec);
        msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.LOAD_LIMIT_EP_ACTIVATION_TIME, false);
        profileTagSpec.add(msgAttrSpec);
        msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.LOAD_LIMIT_EP_DURATION, false);
        profileTagSpec.add(msgAttrSpec);
        tagSpec.add(msgVal);
        tagSpec.add(profileTagSpec);
        msgSpec.add(tagSpec);
        return msgSpec;
	}
	
	private MessageSpec addChangeGPRSSetup(String keyId, String tagName, boolean advanced){
    	MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.GPRS_APN, false);
        tagSpec.add(msgAttrSpec);
        msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.GPRS_USERNAME, false);
        tagSpec.add(msgAttrSpec);
        msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.GPRS_PASSWORD, false);
        tagSpec.add(msgAttrSpec);
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
	}
	
	private MessageSpec addCreateDBEntries(String keyId, String tagName, boolean advanced){
    	MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.ME_START_DATE, true);
        tagSpec.add(msgAttrSpec);
        msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.ME_NUMBER_OF_ENTRIES, true);
        tagSpec.add(msgAttrSpec);
        msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.ME_INTERVAL, true);
        tagSpec.add(msgAttrSpec);
        msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.ME_SET_CLOCK_BACK, false);
        tagSpec.add(msgAttrSpec);
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
	}
	
	private MessageSpec addTimeMessage(String keyId, String tagName, boolean advanced) {
    	MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.SET_TIME_VALUE, true);
        tagSpec.add(msgVal);
        tagSpec.add(msgAttrSpec);
        msgSpec.add(tagSpec);
        return msgSpec;
	}

	private MessageSpec addConnectControl(String keyId, String tagName, boolean advanced) {
    	MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.DISCONNECT_CONTROL_ACTIVATE_DATE, false);
        tagSpec.add(msgVal);
        tagSpec.add(msgAttrSpec);
        msgSpec.add(tagSpec);
        return msgSpec;
	}
	
	private MessageSpec addConnectControlMode(String keyId, String tagName, boolean advanced) {
    	MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.CONNECT_MODE, true);
        tagSpec.add(msgVal);
        tagSpec.add(msgAttrSpec);
        msgSpec.add(tagSpec);
        return msgSpec;
	}

	private MessageSpec addP1Code(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        tagSpec.add(msgVal);
        MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.P1CODE, false);
        tagSpec.add(msgAttrSpec);
        msgSpec.add(tagSpec);
    	return msgSpec;
	}

	private MessageSpec addP1Text(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        tagSpec.add(msgVal);
        MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.P1TEXT, false);
        tagSpec.add(msgAttrSpec);
        msgSpec.add(tagSpec);
    	return msgSpec;
	}

	private MessageSpec addFirmwareMsg(String keyId, String tagName, boolean advanced){
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        tagSpec.add(msgVal);
        MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.FIRMWARE, true);
        tagSpec.add(msgAttrSpec);
        msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.FIRMWARE_ACTIVATE_NOW, false);
        tagSpec.add(msgAttrSpec);
        msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.FIRMWARE_ACTIVATE_DATE, false);
        tagSpec.add(msgAttrSpec);
        msgSpec.add(tagSpec);
    	return msgSpec;
    }

	private MessageSpec addDefaultValueMsg(String keyId, String tagName, boolean advanced){
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
	}

	private MessageSpec addTimeOfUse(String keyId, String tagName, boolean advanced) {
    	MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        tagSpec.add(msgVal);
        MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.TOU_ACTIVITY_NAME, false);
        tagSpec.add(msgAttrSpec);
        msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.TOU_ACTIVITY_DATE, false);
        tagSpec.add(msgAttrSpec);
        msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.TOU_ACTIVITY_CODE_TABLE, false);
        tagSpec.add(msgAttrSpec);
        msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.TOU_ACTIVITY_USER_FILE, false);
        tagSpec.add(msgAttrSpec);
        msgSpec.add(tagSpec);
        return msgSpec;
	}
	public String writeMessage(Message msg) {
		return msg.write(this);
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
	
	public boolean isReadDaily() {
		return readDaily;
	}


	public boolean isReadMonthly() {
		return readMonthly;
	}
	
	/** EIServer 7.5 Cache mechanism, only the DLMSCache is in that database, the 8.x has a EISDEVICECACHE ... */
	
    public void setCache(Object cacheObject) {
        this.dlmsCache=(DLMSCache)cacheObject;
    }
    public Object getCache() {
        return dlmsCache;
    }
    public Object fetchCache(int rtuid) throws java.sql.SQLException, com.energyict.cbo.BusinessException {
        if (rtuid != 0) {
            RtuDLMSCache rtuCache = new RtuDLMSCache(rtuid);
            RtuDLMS rtu = new RtuDLMS(rtuid);
            try {
               return new DLMSCache(rtuCache.getObjectList(), rtu.getConfProgChange());
            }
            catch(NotFoundException e) {
               return new DLMSCache(null,-1);  
            }
        }
        else throw new com.energyict.cbo.BusinessException("invalid RtuId!");
    } 
    public void updateCache(int rtuid, Object cacheObject) throws java.sql.SQLException,com.energyict.cbo.BusinessException {
        if (rtuid != 0) {
            DLMSCache dc = (DLMSCache)cacheObject;
            if (dc.isChanged()) {
                RtuDLMSCache rtuCache = new RtuDLMSCache(rtuid);
                RtuDLMS rtu = new RtuDLMS(rtuid);
                rtuCache.saveObjectList(dc.getObjectList());
                rtu.setConfProgChange(dc.getConfProgChange());
            }
        }
        else throw new com.energyict.cbo.BusinessException("invalid RtuId!");
    }
}
