package com.energyict.genericprotocolimpl.webrtukp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
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
import com.energyict.dialer.core.Link;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.TCPIPConnection;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.CosemObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.IPv4Setup;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.genericprotocolimpl.common.GenericCache;
import com.energyict.genericprotocolimpl.common.RtuMessageConstant;
import com.energyict.genericprotocolimpl.common.StoreObject;
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
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageElement;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.MessageValueSpec;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimpl.dlms.DLMSCache;
import com.energyict.protocolimpl.dlms.HDLCConnection;
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
 */

public class WebRTUKP implements GenericProtocol, ProtocolLink, Messaging{

	private CosemObjectFactory 		cosemObjectFactory;
	private DLMSConnection 			dlmsConnection;
	private DLMSMeterConfig			dlmsMeterConfig;
	private AARQ					aarq;
	private CommunicationProfile	commProfile;
	private Logger					logger;
	private CommunicationScheduler	scheduler;
	private Rtu						webRtuKP;
//	private Cache					dlmsCache;
	
	// this cache object is supported by 7.5
	private DLMSCache 				dlmsCache=new DLMSCache();	     
	
	private MbusDevice[]			mbusDevices;
	private Clock					deviceClock;
	private StoreObject				storeObject;
	private ObisCodeMapper			ocm;
	
	
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
	private String password;
	private String serialNumber;
	
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
		
		validateProperties();
		
		try {
			
			init(link.getInputStream(), link.getOutputStream());
			connect();
			
//			readFromMeter("1.0.1.7.0.255");
			
//			hasMBusMeters();
//			handleMbusMeters();
			
			verifyAndWriteClock();
			
			if(this.commProfile.getReadDemandValues()){
				getLogger().log(Level.INFO, "Getting loadProfile for meter with serialnumber: " + webRtuKP.getSerialNumber());
				ElectricityProfile ep = new ElectricityProfile(this);
				
				
//				TODO ep.getProfile(Constant.loadProfileObisCode, this.commProfile.getReadMeterEvents());
				ep.getProfile(Constant.loadProfileObisCode, false);
			}
			
    		/**
    		 * Here we are assuming that the daily and monthly values should be read.
    		 * In future it can be that this doesn't work for all customers, then we should implement a SmartMeterProperty to indicate whether you
    		 * want to read the actual registers or the daily/monthly registers ...
    		 */
			if(this.commProfile.getReadMeterReadings()){
				
				// TODO read the daily/Monthly values
//				getLogger().log(Level.INFO, "Getting daily and monthly values for meter with serialnumber: " + webRtuKP.getSerialNumber());
//				DailyMonthly dm = new DailyMonthly(this);
//				dm.getDailyValues(Constant.dailyObisCode);
//				dm.getMonthlyValues(Constant.monthlyObisCode);
				
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
					new HDLCConnection(is, os, this.timeout, this.forceDelay, this.retries, this.clientMacAddress, this.serverLowerMacAddress, this.serverUpperMacAddress, this.addressingMode):
					new TCPIPConnection(is, os, this.timeout, this.forceDelay, this.retries, this.clientMacAddress, this.serverLowerMacAddress);
		
		this.dlmsMeterConfig = DLMSMeterConfig.getInstance(Constant.MANUFACTURER);
		
		// if we get a serialVersion mix-up we should set the dlmsCache to NULL so the cache is read from the meter
//		Object tempCache = GenericCache.startCacheMechanism(getMeter());
//		this.dlmsCache = (tempCache == null)?new Cache():(Cache)tempCache;
		
		// this cacheobject is supported by the 7.5
//		this.dlmsCache = (DLMSCache)fetchCache(getMeter().getId());
		setCache(fetchCache(getMeter().getId()));
		
		this.mbusDevices = new MbusDevice[Constant.MaxMbusMeters];
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
		String ipAddress = "";
		try {
			IPv4Setup ipv4Setup = getCosemObjectFactory().getIPv4Setup();
			ipAddress = ipv4Setup.getIPAddress();
			
			RtuShadow shadow = getMeter().getShadow();
			shadow.setIpAddress(ipAddress);
			
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
	 * Just to test some objects
	 */
	private void doSomeTestCalls(){
//		try {
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	/**
	 * Handles all the MBus devices like a separate device
	 */
	private void handleMbusMeters(){
		for(int i = 0; i < Constant.MaxMbusMeters; i++){
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
		// TODO complete the method
		
		int configNumber;
		if(dlmsCache.getObjectList() != null){		// the dlmsCache exists
			setCachedObjects();
			
			try {
				log(Level.INFO, "Checking the configuration parameters.");
				configNumber = requestConfigurationChanges();
//				dlmsCache.setConfProfChange(configNumber);
//				dlmsCache.setConfProgChange(configNumber);
			} catch (IOException e) {
				e.printStackTrace();
				configNumber = -1;
				log(Level.SEVERE, "Config change parameter could not be retrieved, configuration is forced to be read.");
				requestConfiguration();
				dlmsCache.saveObjectList(getMeterConfig().getInstantiatedObjectList());
//				dlmsCache.setConfProfChange(configNumber);
				dlmsCache.setConfProgChange(configNumber);
			}
			
			if(dlmsCache.getConfProgChange() != configNumber){
				log(Level.INFO,"Meter configuration has changed, configuration is forced to be read.");
				requestConfiguration();
				dlmsCache.saveObjectList(getMeterConfig().getInstantiatedObjectList());
//				dlmsCache.setConfProfChange(configNumber);
				dlmsCache.setConfProgChange(configNumber);
			}
			
		} else {		// cache does not exist
			log(Level.INFO,"Cache does not exist, configuration is forced to be read.");
			requestConfiguration();
			try {
				configNumber = requestConfigurationChanges();
				dlmsCache.saveObjectList(getMeterConfig().getInstantiatedObjectList());
//				dlmsCache.setConfProfChange(configNumber);
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
		//TODO complete the method
		getMeterConfig().setInstantiatedObjectList(this.dlmsCache.getObjectList());
//		this.genericProfiles = this.dlmsCache.getGenericProfiles();
	}
	
//	/**
//	 * Set variable objects in the cached object
//	 */
//	private void prepareForCacheSaving(){
//		//TODO complete the method if there are other objects to save
//		this.dlmsCache.setGenericProfiles(this.genericProfiles);
//	}
	
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
			
			long diff = Math.abs(now.getTime()-meterTime.getTime())/1000;
			
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
		while(it.hasNext()){
			try {
				mbus = it.next();
				serialMbus = mbus.getSerialNumber();
				this.mbusDevices[count++] = new MbusDevice(serialMbus, mbus, getLogger());
			} catch (ApplicationException e) {
				// catch and go to next slave
				e.printStackTrace();
			}
		}
		
    	for(int i = 0; i < Constant.MaxMbusMeters; i++){
			if ( mbusDevice(i) != null ){
				if(isValidMbusMeter(i)){
					return true;
				}
			}
    	}
		
    	return false;
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
        
        this.password = properties.getProperty(MeterProtocol.PASSWORD, "");
        this.serialNumber = getMeter().getSerialNumber();
        this.securityLevel = Integer.parseInt(properties.getProperty("SecurityLevel", "0"));
        this.connectionMode = Integer.parseInt(properties.getProperty("ConnectionMode", "1"));
        this.clientMacAddress = Integer.parseInt(properties.getProperty("ClientMacAddress", "16"));
        this.serverLowerMacAddress = Integer.parseInt(properties.getProperty("ServerLowerMacAddress", "1"));
        this.serverUpperMacAddress = Integer.parseInt(properties.getProperty("ServerUpperMacAddress", "17"));
        this.requestTimeZone = Integer.parseInt(properties.getProperty("RequestTimeZone", "0"));
        // if HDLC set default timeout to 10s, if TCPIP set default timeout to 60s
        this.timeout = Integer.parseInt(properties.getProperty("Timeout", (this.connectionMode==0)?"10000":"60000"));
        this.forceDelay = Integer.parseInt(properties.getProperty("ForceDelay", "100"));
        this.retries = Integer.parseInt(properties.getProperty("Retries", "3"));
        this.addressingMode = Integer.parseInt(properties.getProperty("AddressingMode", "2"));
        this.extendedLogging = Integer.parseInt(properties.getProperty("ExtendedLogging", "0"));
	}
	
	public void addProperties(Properties properties) {
		this.properties = properties;
	}

	public String getVersion() {
		return "$Date$";
	}

	public List<String> getOptionalKeys() {
        List<String> result = new ArrayList<String>(16);
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
        result.add("RtuType");
        result.add("TestLogging");
        result.add("ForceDelay");
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
		while(it.hasNext()){
			
			try {
				rm = (RtuMessage)it.next();
				String content = rm.getContents();
				importMessage(content, messageHandler);
				
				boolean xmlConfig		= messageHandler.getType().equals(RtuMessageConstant.XMLCONFIG);
				
				if(xmlConfig){
					
					//TODO TEST THIS
					String xmlConfigStr = getMessageValue(content, RtuMessageConstant.XMLCONFIG);
					
					getCosemObjectFactory().getData(getMeterConfig().getXMLConfig().getObisCode()).setValueAttr(OctetString.fromString(xmlConfigStr));
					
					success = true;
					
				} else {
					success = false;
				}
				
			} catch (BusinessException e) {
				e.printStackTrace();
				log(Level.INFO, "Message " + rm.displayString() + " hase failed. " + e.getMessage());
			} catch (IOException e) {
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
	
	private String getMessageValue(String msgStr, String str) {
		try {
			return msgStr.substring(msgStr.indexOf(str + ">") + str.length()
					+ 1, msgStr.indexOf("</" + str));
		} catch (Exception e) {
			return "";
		}
	}
	
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

	public List getMessageCategories() {
		List categories = new ArrayList();
		MessageCategorySpec catXMLConfig = new MessageCategorySpec("XMLConfig");
		
		// XMLConfig releated messages
		MessageSpec msgSpec = addDefaultValueMsg("XMLConfig", RtuMessageConstant.XMLCONFIG, false);
		catXMLConfig.addMessageSpec(msgSpec);
		
		categories.add(catXMLConfig);
		return categories;
	}

	private MessageSpec addDefaultValueMsg(String keyId, String tagName, boolean advanced){
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
        tagSpec.add(msgVal);
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
	
	
	/** EIServer 7.5 Cache mechanism, only the DLMSCache is in that database, the 8.x has a DEVICECACHE ... */
	
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
