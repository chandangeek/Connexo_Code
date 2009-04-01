package com.energyict.genericprotocolimpl.webrtukp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
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
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.TCPIPConnection;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.CosemObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.IPv4Setup;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.genericprotocolimpl.common.RtuMessageConstant;
import com.energyict.genericprotocolimpl.common.StoreObject;
import com.energyict.genericprotocolimpl.webrtukp.messagehandling.MessageExecutor;
import com.energyict.genericprotocolimpl.webrtukp.profiles.DailyMonthly;
import com.energyict.genericprotocolimpl.webrtukp.profiles.ElectricityProfile;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.CommunicationProfile;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.core.RtuMessage;
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
 * GNA |30032009| Added testMessage to enable overnight tests for the embedded device
 */

public class WebRTUKP implements GenericProtocol, ProtocolLink, Messaging, HHUEnabler{
	
	private boolean DEBUG = false;

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
			shadow.setIpAddress(ipAddress.toString());
//			shadow.setPhoneNumber(ipAddress.toString());
			
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
//			Array dateArray = convertUnixToDateTimeArray(strDate);
//			sas.writeExecutionTime(dateArray);
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
	
	public void forceClock(Date currentTime) throws IOException{
		try {
			getCosemObjectFactory().getClock().setTimeAttr(new DateTime(currentTime));
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not force to set the Clock object.");
		}
	}
	
	public Date getTime() throws IOException{
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
	
	public void setClock(Date time) throws IOException{
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
    public MeteringWarehouse mw() {
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
        this.forceDelay = Integer.parseInt(properties.getProperty("ForceDelay", "1"));
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
		
		MessageExecutor messageExecutor = new MessageExecutor(this);

		Iterator<RtuMessage> it = getMeter().getPendingMessages().iterator();
		RtuMessage rm = null;
		while(it.hasNext()){
			rm = it.next();
			messageExecutor.doMessage(rm);
		}
	}
	
	public int getConnectionMode(){
		return this.connectionMode;
	}
	
	public static void main(String args[]){
		WebRTUKP wkp = new WebRTUKP();
		
//		try {
////			Utilities.createEnvironment();
////			MeteringWarehouse.createBatchContext(false);
//			RtuMessageShadow rms = new RtuMessageShadow();
//			rms.setContents("<Test_Message Test_File='460'> </Test_Message>");
//			rms.setRtuId(17492);
//			
//			wkp.logger = Logger.getAnonymousLogger();
//			
////			wkp.handleMessage(wkp.mw().getRtuMessageFactory().create(rms));
//		} catch (BusinessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
//		try {
//			AXDRDateTime axdrDateTime = wkp.convertUnixToGMTDateTime("1236761593", TimeZone.getTimeZone("GMT"));
//			System.out.println(axdrDateTime.getValue().getTime());
//			System.out.println(wkp.getFirstDate(axdrDateTime.getValue().getTime(), "day", TimeZone.getTimeZone("GMT")));
//			System.out.println(axdrDateTime.getValue().get(Calendar.HOUR_OF_DAY));
//			System.out.println(axdrDateTime.getValue().getTimeZone().getRawOffset()/3600000);
//			System.out.println(axdrDateTime.getValue().getTimeZone().getOffset(Long.parseLong("1236761593")*1000)/3600000);
//			
//			axdrDateTime = wkp.convertUnixToGMTDateTime("1236761593", TimeZone.getTimeZone("Europe/Brussels"));
//			System.out.println(axdrDateTime.getValue().getTime());
//			System.out.println(wkp.getFirstDate(axdrDateTime.getValue().getTime(), "day", TimeZone.getTimeZone("Europe/Brussels")));
//			System.out.println(axdrDateTime.getValue().get(Calendar.HOUR_OF_DAY));
//			System.out.println(axdrDateTime.getValue().getTimeZone().getRawOffset()/3600000);
//			System.out.println(axdrDateTime.getValue().getTimeZone().getOffset(Long.parseLong("1236761593")*1000)/3600000);
//			
//			axdrDateTime = wkp.convertUnixToGMTDateTime("1234947193", TimeZone.getTimeZone("GMT"));
//			System.out.println(axdrDateTime.getValue().getTime());
//			System.out.println(wkp.getFirstDate(axdrDateTime.getValue().getTime(), "day", TimeZone.getTimeZone("GMT")));
//			System.out.println(axdrDateTime.getValue().get(Calendar.HOUR_OF_DAY));
//			System.out.println(axdrDateTime.getValue().getTimeZone().getRawOffset()/3600000);
//			System.out.println(axdrDateTime.getValue().getTimeZone().getOffset(Long.parseLong("1234947193")*1000)/3600000);
//			
//			axdrDateTime = wkp.convertUnixToGMTDateTime("1234947193", TimeZone.getTimeZone("Europe/Brussels"));
//			System.out.println(axdrDateTime.getValue().getTime());
//			System.out.println(wkp.getFirstDate(axdrDateTime.getValue().getTime(), "day", TimeZone.getTimeZone("Europe/Brussels")));
//			System.out.println(axdrDateTime.getValue().get(Calendar.HOUR_OF_DAY));
//			System.out.println(axdrDateTime.getValue().getTimeZone().getRawOffset()/3600000);
//			System.out.println(axdrDateTime.getValue().getTimeZone().getOffset(Long.parseLong("1234947193")*1000)/3600000);
//			
//			Date nextDate = wkp.getFirstDate(axdrDateTime.getValue().getTime(), "month", TimeZone.getTimeZone("Europe/Brussels"));
//			int days = 0;
//			while(days < 60){
//				
//				if(days == 36){
//					System.out.println("timeout");
//				}
//				
//				System.out.println(nextDate);
//				nextDate = wkp.setBeforeNextInterval(nextDate, "month", TimeZone.getTimeZone("Europe/Brussels"));
//				days++;
//			}
//			
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
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
		MessageCategorySpec catTestMessage = new MessageCategorySpec("TestMessage");
		MessageCategorySpec catGlobalDisc = new MessageCategorySpec("Global Reset");
		
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
		
		// TestMessage
		msgSpec = addTestMessage("Test Message", RtuMessageConstant.TEST_MESSAGE, true);
		catTestMessage.addMessageSpec(msgSpec);
		
		msgSpec = addNoValueMsg("Global Meter Reset", RtuMessageConstant.GLOBAL_METER_RESET, false);
		catGlobalDisc.addMessageSpec(msgSpec);
		
		categories.add(catXMLConfig);
		categories.add(catFirmware);
		categories.add(catP1Messages);
		categories.add(catDisconnect);
		categories.add(catLoadLimit);
		categories.add(catActivityCal);
		categories.add(catTime);
		categories.add(catMakeEntries);
		categories.add(catGPRSModemSetup);
		categories.add(catTestMessage);
		categories.add(catGlobalDisc);
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
	
	private MessageSpec addTestMessage(String keyId, String tagName, boolean advanced) {
    	MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.TEST_FILE, true);
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
