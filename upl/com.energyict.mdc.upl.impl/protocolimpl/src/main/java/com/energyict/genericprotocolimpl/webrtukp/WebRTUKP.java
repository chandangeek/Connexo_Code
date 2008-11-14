package com.energyict.genericprotocolimpl.webrtukp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
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

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.Environment;
import com.energyict.dialer.core.Link;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.OctetString;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.TCPIPConnection;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.client.ParseUtils;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.CosemObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.genericprotocolimpl.common.GenericCache;
import com.energyict.genericprotocolimpl.common.StatusCodeProfile;
import com.energyict.genericprotocolimpl.common.StoreObject;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.CommunicationProfile;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.Rtu;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimpl.dlms.HDLCConnection;

public class WebRTUKP implements GenericProtocol, ProtocolLink, Messaging{

	private CosemObjectFactory 		cosemObjectFactory;
	private DLMSConnection 			dlmsConnection;
	private DLMSMeterConfig			dlmsMeterConfig;
	private AARQ					aarq;
	private CommunicationProfile	commProfile;
	private Logger					logger;
	private CommunicationScheduler	scheduler;
	private Rtu						webRtuKP;
	private Cache					dlmsCache; 
	private MbusDevice[]			mbusDevices;
	private Clock					deviceClock;
	private StoreObject				storeObject;
	
	private HashMap<ObisCode, ProfileGeneric> 				genericProfiles;
	
	/**
	 * Properties
	 */
	private Properties properties;
	private int securityLevel;	// 0: No Authentication - 1: Low Level - 2: High Level
	private int connectionMode; // 0: DLMS/HDLS - 1: DLMS/TCPIP
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
	 * - Then all the MBus meters are handled
	 */
	public void execute(CommunicationScheduler scheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {
		
		boolean success = false;
		
		this.scheduler = scheduler;
		this.logger = logger;
		this.commProfile = this.scheduler.getCommunicationProfile();
		
		validateProperties();
		
		try {
			
			init(link.getInputStream(), link.getOutputStream());
			connect();
			
			verifyAndWriteClock();
			
			if(this.commProfile.getReadDemandValues()){
				// TODO read the meterProfile
				getProfileData();
			}
			
			if(this.commProfile.getReadMeterReadings()){
				// TODO read the daily/Monthly values
				
				// TODO Just first Test method
				// TODO fill in an obiscode
				readFromMeter("");
			}
			
			if(this.commProfile.getSendRtuMessage()){
				// TODO send the meter messages
			}
			
			if(hasMBusMeters()){
				// TODO handle the MBus Slave meters
			}
			
			success = true;
			
		} catch (DLMSConnectionException e) {
			try {
				disConnect();
			} catch (DLMSConnectionException e1) {
				e1.printStackTrace();
				new BusinessException(e1);
			}
			e.printStackTrace();
		} finally{
			prepareForCacheSaving();
			GenericCache.stopCacheMechanism(getWebRtu(), dlmsCache);
			if(success){
				try {
					disConnect();
					Environment.getDefault().execute(getStoreObject());
				} catch (DLMSConnectionException e) {
					e.printStackTrace();
					new BusinessException(e);
				}
			}
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
			System.out.println(cobj);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Reading of object has failed!");
		}
	}
	
	/**
	 * Initializing global objects
	 * @param is - the inputStream to work with
	 * @param os - the outputStream to work with
	 * @throws IOException - can be cause by the TCPIPConnection
	 * @throws DLMSConnectionException - could not create a dlmsconnection
	 */
	private void init(InputStream is, OutputStream os) throws IOException, DLMSConnectionException{
		this.cosemObjectFactory	= new CosemObjectFactory((ProtocolLink)this);
		
		this.dlmsConnection = (this.connectionMode == 0)?
					new HDLCConnection(is, os, this.timeout, this.forceDelay, this.retries, this.clientMacAddress, this.serverLowerMacAddress, this.serverUpperMacAddress, this.addressingMode):
					new TCPIPConnection(is, os, this.timeout, this.forceDelay, this.retries, this.clientMacAddress, this.serverLowerMacAddress);
		
		this.dlmsMeterConfig = DLMSMeterConfig.getInstance(Constant.MANUFACTURER);
		this.webRtuKP = this.scheduler.getRtu();
		// if we get a serialVersion mix-up we should set the dlmsCache to NULL so the cache is read from the meter
		// TODO need to catch this.
		this.dlmsCache = (Cache)GenericCache.startCacheMechanism(getWebRtu());
		this.mbusDevices = new MbusDevice[Constant.MaxMbusMeters];
		this.genericProfiles = new HashMap<ObisCode, ProfileGeneric>();
		this.storeObject = new StoreObject();
	}

	/**
	 * Makes a connection to the server, if the socket is not available then an error is thrown.
	 * After a successful connection, we initiate an authentication request.
	 * @throws IOException 
	 */
	private void connect() throws IOException{
		try {
			getDLMSConnection().connectMAC();
			aarq = new AARQ(this.securityLevel, this.password, getDLMSConnection());
			
			checkCacheObjects();
			
			if(getMeterConfig().getInstantiatedObjectList() == null){	// should never do this
				getMeterConfig().setInstantiatedObjectList(this.dlmsCache.getObjectList());
			}
			
			// do some checks to know you are connected to the correct meter
			verifyMeterSerialNumber();
			log(Level.INFO, "FirmwareVersion: " + getFirmWareVersion());
			
			if(this.extendedLogging >= 1){
				log(Level.INFO, getRegistersInfo());
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		} catch (DLMSConnectionException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
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

	private String getSerialNumber() throws IOException{
		try {
			return getCosemObjectFactory().getGenericRead(getMeterConfig().getSerialNumberObject()).toString();
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
	private void disConnect() throws IOException, DLMSConnectionException{
		try {
			aarq.disConnect();
			getDLMSConnection().disconnectMAC();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException();
		} catch (DLMSConnectionException e) {
			e.printStackTrace();
			throw new DLMSConnectionException("Failed to access the dlmsConnection");
		}
	}
	
	/**
	 * Method to check whether the cache needs to be read out or not, if so the read will be forced
	 */
	private void checkCacheObjects(){
		// TODO complete the method
		
		int configNumber;
		if(dlmsCache != null){		// the dlmsCache exists
			setCachedObjects();
			
			try {
				log(Level.INFO, "Checking the configuration parameters.");
				configNumber = requestConfigurationChanges();
				dlmsCache.setConfProfChange(configNumber);
			} catch (IOException e) {
				e.printStackTrace();
				configNumber = -1;
				log(Level.SEVERE, "Config change parameter could not be retrieved, configuration is forced to be read.");
				requestConfiguration();
				dlmsCache.saveObjectList(getMeterConfig().getInstantiatedObjectList());
				dlmsCache.setConfProfChange(configNumber);
			}
			
			if(dlmsCache.isChanged()){
				log(Level.INFO,"Meter configuration has changed, configuration is forced to be read.");
				requestConfiguration();
				dlmsCache.saveObjectList(getMeterConfig().getInstantiatedObjectList());
				dlmsCache.setConfProfChange(configNumber);
			}
			
		} else {		// cache does not exist
			log(Level.INFO,"Cache does not exist, configuration is forced to be read.");
			requestConfiguration();
			try {
				configNumber = requestConfigurationChanges();
				dlmsCache.saveObjectList(getMeterConfig().getInstantiatedObjectList());
				dlmsCache.setConfProfChange(configNumber);
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
		this.genericProfiles = this.dlmsCache.getGenericProfiles();
	}
	
	/**
	 * Set variable objects in the cached object
	 */
	private void prepareForCacheSaving(){
		//TODO complete the method if there are other objects to save
		this.dlmsCache.setGenericProfiles(this.genericProfiles);
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
		//TODO complete the method with everything you need
		dlmsCache = new Cache();	// delete all possible data in the cache object
		// get the complete objectlist from the meter
		try {
			getMeterConfig().setInstantiatedObjectList(getCosemObjectFactory().getAssociationLN().getBuffer());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void getProfileData(){
		// NOTE: use the from data from the channels of the meter and not the meter itself
		
		ProfileData profileData = new ProfileData();
		try {
			ProfileGeneric genericProfile = getProfileGeneric(getMeterConfig().getProfileObject().getObisCode());
			List<ChannelInfo> channelInfos = getChannelInfos(genericProfile);
			profileData.setChannelInfos(channelInfos);
			
			//TODO Here we make a choice to use the first channel as a reference for the profile fromDate
			Calendar from = getFromCalendar(getWebRtu().getChannel(0));
			Calendar to = getToCalendar();
//			UniversalObject[] intervals = genericProfile.getBufferAsUniversalObjects(from, to);
			DataContainer dc = genericProfile.getBuffer(from, to);
			buildProfileData(dc, profileData, genericProfile);
			if(this.commProfile.getReadMeterEvents()){
				Calendar fromLog = getFromLogCalendar(getWebRtu());
				DataContainer dcEvent = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getEventLogObject().getObisCode()).getBuffer(fromLog);
				Events events = new Events(getTimeZone(), fromLog, dcEvent);
				profileData.getMeterEvents().addAll(events.getMeterEvents());
				profileData.applyEvents(getProfileInterval()/60);
			}
			
			getStoreObject().add(getWebRtu(), profileData);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private int getProfileInterval() throws IOException{
		try {
			return getCosemObjectFactory().getProfileGeneric(getMeterConfig().getProfileObject().getObisCode()).getCapturePeriod();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not retrieve the profileInterval." + e);
		}
	}
	private void buildProfileData(DataContainer dc, ProfileData pd, ProfileGeneric pg) throws IOException{
		
		//TODO check how this reacts with the profile.
		
		Calendar cal = null;
		IntervalData currentInterval = null;
		int profileStatus = 0;
		if(dc.getRoot().getElements().length == 0){
			throw new IOException("No entries in loadprofile datacontainer.");
		}
		
		for(int i = 0; i < dc.getRoot().getElements().length; i++){
			cal = dc.getRoot().getStructure(i).getOctetString(getProfileClockChannelIndex(pg)).toCalendar(getTimeZone());
			if(cal != null){
				if(getProfileStatusChannelIndex(pg) != -1){
					profileStatus = dc.getRoot().getStructure(i).getInteger(getProfileStatusChannelIndex(pg));
				} else {
					profileStatus = 0;
				}
				
				currentInterval = getIntervalData(dc.getRoot().getStructure(i), cal, profileStatus, pg);
				if(currentInterval != null){
					pd.addInterval(currentInterval);
				}
			}
		}
	}
	
	private IntervalData getIntervalData(DataStructure ds, Calendar cal, int status, ProfileGeneric pg)throws IOException{
		
		IntervalData id = new IntervalData(cal.getTime(), StatusCodeProfile.intervalStateBits(status));
		
		try {
			for(int i = 0; i < pg.getNumberOfProfileChannels(); i++){
				if(pg.getCaptureObjectsAsUniversalObjects()[i].isCapturedObjectElectricity()){
					id.addValue(new Integer(ds.getInteger(i)));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Failed to parse the intervalData objects form the datacontainer.");
		}
		
		return id;
	}
	
	private int getProfileClockChannelIndex(ProfileGeneric pg) throws IOException{
		try {
			for(int i = 0; i < pg.getCaptureObjectsAsUniversalObjects().length; i++){
				if(pg.getCaptureObjectsAsUniversalObjects()[i].equals(getMeterConfig().getClockObject().getObisCode())){
					return i;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not retrieve the index of the profileData's clock attribute.");
		}
		return -1;
	}
	
	private int getProfileStatusChannelIndex(ProfileGeneric pg) throws IOException{
		try {
			for(int i = 0; i < pg.getCaptureObjectsAsUniversalObjects().length; i++){
				if(pg.getCaptureObjectsAsUniversalObjects()[i].equals(getMeterConfig().getStatusObject().getObisCode())){
					return i;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not retrieve the index of the profileData's status attribute.");
		}
		return -1;
	}
	
	protected ProfileGeneric getProfileGeneric(ObisCode oc) throws IOException{
		if(!this.genericProfiles.containsKey(oc)){
			try {
				this.genericProfiles.put(oc, getCosemObjectFactory().getProfileGeneric(oc));
				this.dlmsCache.setGenericProfiles(this.genericProfiles);
			} catch (IOException e) {
				e.printStackTrace();
				throw new IOException("Failed to read the genericProfile " + oc);
			}
		}
		return this.genericProfiles.get(oc);
	}
	
	/**
	 * Create a list with channelInfos starting from the capture_objects
	 * @param profile
	 * @return
	 * @throws IOException
	 */
	private List<ChannelInfo> getChannelInfos(ProfileGeneric profile) throws IOException{
		List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
		try {
			for(int i = 0; i < profile.getCaptureObjectsAsUniversalObjects().length; i++){
				if(profile.getCaptureObjectsAsUniversalObjects()[i].isCapturedObjectNotAbstract()){	// make a channel out of it
					CapturedObject co = ((CapturedObject)profile.getCaptureObjects().get(i));
					ScalerUnit su = getMeterDemandRegisterScalerUnit(co.getLogicalName().getObisCode());
					ChannelInfo ci = new ChannelInfo(i, "WebRTU-Channel_"+i, su.getUnit());
					if(ParseUtils.isObisCodeCumulative(co.getLogicalName().getObisCode())){
						//TODO need to check the wrapValue
						ci.setCumulativeWrapValue(BigDecimal.valueOf(1).movePointRight(9));
					}
					channelInfos.add(ci);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Failed to build the channelInfos." + e);
		}
		return channelInfos;
	}
	
	/**
	 * Read the given object and return the scalerUnit
	 * @param oc
	 * @return
	 * @throws IOException
	 */
	private ScalerUnit getMeterDemandRegisterScalerUnit(ObisCode oc) throws IOException{
		try {
			return getCosemObjectFactory().getCosemObject(oc).getScalerUnit();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not get the scalerunit from object '" + oc + "'.");
		}
	}
	
	/**
	 * @param channel
	 * @return a Calendar object from the lastReading of the given channel, if the date is NULL,
	 * a date from one month ago is created at midnight.
	 */
	private Calendar getFromCalendar(Channel channel){
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
	private Calendar getFromLogCalendar(Rtu rtu){
		Date lastLogReading = rtu.getLastLogbook();
		if(lastLogReading == null){
			lastLogReading = com.energyict.genericprotocolimpl.common.ParseUtils.getClearLastMonthDate(rtu);
		}
		Calendar cal = ProtocolUtils.getCleanCalendar(getTimeZone());
		cal.setTime(lastLogReading);
		return cal;
	}
	
	private Calendar getToCalendar(){
		return ProtocolUtils.getCalendar(getTimeZone());
	}
	
	private void verifyAndWriteClock() throws IOException{
		try {
			Date meterTime = getTime();
			Date now = new Date();
			
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
			Clock clock = getCosemObjectFactory().getClock();
			meterTime = clock.getDateTime();
			return meterTime;
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not retrieve the Clock object.");
		}
	}
	
	private void setClock(Date time) throws IOException{
		try {
			getCosemObjectFactory().getClock().setDateTime(new OctetString(time.toString().getBytes()));
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not set the Clock object.");
		}
	}
	
	/**
	 * @return the current webRtu
	 */
	private Rtu getWebRtu(){
		return this.webRtuKP;
	}
	
	private boolean hasMBusMeters(){
    	for(int i = 0; i < Constant.MaxMbusMeters; i++){
    		if ( mbusDevice(i) != null ){
    			if(isValidMbusMeter(i)){
    				return true;
    			}
    		}
    	}
    	return false;
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
        this.serialNumber = properties.getProperty(MeterProtocol.SERIALNUMBER, "");
        this.securityLevel = Integer.parseInt(properties.getProperty("SecurityLevel", "0"));
        this.connectionMode = Integer.parseInt(properties.getProperty("ConnectionMode", "0"));
        this.clientMacAddress = Integer.parseInt(properties.getProperty("ClientMacAddress", "100"));
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
		// TODO Auto-generated method stub
		return 0;
	}

	public int getRoundTripCorrection() {
		// TODO Auto-generated method stub
		return 0;
	}

	public StoredValues getStoredValues() {
		// TODO Auto-generated method stub
		return null;
	}

	public TimeZone getTimeZone() {
		return getWebRtu().getTimeZone();
	}

	public boolean isRequestTimeZone() {
		return (this.requestTimeZone==1)?true:false;
	}
	
	protected CosemObjectFactory getCosemObjectFactory() {
		return cosemObjectFactory;
	}
	
	public StoreObject getStoreObject(){
		return this.storeObject;
	}
	
	/**
	 * Messages
	 */
	public List getMessageCategories() {
		// TODO Auto-generated method stub
		return null;
	}

	public String writeMessage(Message msg) {
		// TODO Auto-generated method stub
		return null;
	}

	public String writeTag(MessageTag tag) {
		// TODO Auto-generated method stub
		return null;
	}

	public String writeValue(MessageValue value) {
		// TODO Auto-generated method stub
		return null;
	}
}
