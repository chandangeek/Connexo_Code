package com.energyict.genericprotocolimpl.webrtuz3;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.Environment;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.InvokeIdAndPriority;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.dlms.aso.XdlmsAse;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.IPv4Setup;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.genericprotocolimpl.common.CommonUtils;
import com.energyict.genericprotocolimpl.common.DLMSProtocol;
import com.energyict.genericprotocolimpl.common.LocalSecurityProvider;
import com.energyict.genericprotocolimpl.common.StoreObject;
import com.energyict.genericprotocolimpl.common.messages.RtuMessageCategoryConstants;
import com.energyict.genericprotocolimpl.common.messages.RtuMessageConstant;
import com.energyict.genericprotocolimpl.common.messages.RtuMessageKeyIdConstants;
import com.energyict.genericprotocolimpl.webrtu.common.obiscodemappers.ObisCodeMapper;
import com.energyict.genericprotocolimpl.webrtukp.WebRTUKP;
import com.energyict.genericprotocolimpl.webrtuz3.messagehandling.MessageExecutor;
import com.energyict.genericprotocolimpl.webrtuz3.profiles.DailyMonthly;
import com.energyict.genericprotocolimpl.webrtuz3.profiles.EDevice;
import com.energyict.genericprotocolimpl.webrtuz3.profiles.ElectricityProfile;
import com.energyict.genericprotocolimpl.webrtuz3.profiles.EventProfile;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.core.RtuMessage;
import com.energyict.mdw.core.RtuType;
import com.energyict.mdw.shadow.RtuShadow;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * <p>
 * Implements the WebRTUZ3 protocol. Initially it's a copy of the {@link WebRTUKP} protocol,
 * but with more extensions to it.
 * </p>
 *
 * @author gna
 *
 */
public class WebRTUZ3 extends DLMSProtocol implements EDevice {

	/** Property names */
	private static final String			PROPERTY_PASSWORD				= "Password";
	private static final String			PROPERTY_DELAY_AFTER_FAIL		= "DelayAfterFail";
	private static final String			PROPERTY_REQUEST_TIME_ZONE		= "RequestTimeZone";
	private static final String			PROPERTY_FIRMWARE_VERSION		= "FirmwareVersion";
	private static final String			PROPERTY_EXTENDED_LOGGING		= "ExtendedLogging";
	private static final String			PROPERTY_TEST_LOGGING			= "TestLogging";
	private static final String			PROPERTY_READ_DAILY_VALUES		= "ReadDailyValues";
	private static final String			PROPERTY_READ_MONTHLY_VALUES	= "ReadMonthlyValues";
	private static final String			PROPERTY_FOLDER_EXT_NAME		= "FolderExtName";
	private static final String			PROPERTY_EMETER_RTUTYPE			= "EMeterRtuType";
	private static final String			PROPERTY_MBUS_RTUTYPE			= "MBusRtuType";

	/** Property default values */
	private static final String			DEFAULT_READ_DAILY_VALUES		= "1";
	private static final String			DEFAULT_READ_MONTHLY_VALUES		= "1";
	private static final String			DEFAULT_REQUEST_TIMEZONE		= "0";
	private static final String			DEFAULT_PASSWORD				= "";

	/** Device channel mappings */
	private static final DeviceMappingRange MBUS_DEVICES					= new DeviceMappingRange(0x01, 0x20);
	private static final DeviceMappingRange EMETER_DEVICES					= new DeviceMappingRange(0x21, 0x40);

	/** The device obisCodes */
	public final static ObisCode		RF_FIRMWAREVERSION				= ObisCode.fromString("1.129.0.2.0.255");
	public final static ObisCode		RF_FIRMWARE_OBISCODE			= ObisCode.fromString("0.0.44.0.128.255");
	public static final ObisCode		SERIALNR_OBISCODE				= ObisCode.fromString("0.0.96.1.0.255");

	/** The serialNumber of the Rtu */
	private String serialNumber;

	/** The password of the Rtu */
	private String password;

	/** The prototype {@link RtuType} of the mbus devices */
	private String mbusRtuType;

	/** The prototype {@link RtuType} of the eMeter */
	private String eMeterRtuType;

	/** The external name of the folder where to place the autodiscovered MbusMeters */
	private String folderExtName;

	/** Property to indicate to read the timeZone from the device or use the one configured on the Rtu */
	private int requestTimeZone;

	/** Property to indicate the timedifference between System and device is larger then the maximum configured */
	private boolean badTime = false;

	/** Property to allow reading the daily values */
	private boolean readDaily = true;

	/** Property to allow reading the monthly values */
	private boolean readMonthly = true;

	/** An array of 'slave' MbusDevices */
	private MbusDevice[] mbusDevices;

	/** An array of 'slave' EMeters */
	private EMeter[] eMeters;

	/** The used TicDevice */
	private TicDevice ticDevice;

	/** The {@link StoreObject} used */
	private StoreObject storeObject;

	/** The {@link ObisCodeMapper} used */
	private ObisCodeMapper ocm;

	@Override
	protected void doExecute() throws BusinessException, SQLException, IOException {

		try {
			if(getMeter() != null){
				updateIPAddress();
			}

			// Check if the time is greater then allowed, if so then no data can be stored...
			// Don't do this when a forceClock is scheduled
			if(!getCommunicationScheduler().getCommunicationProfile().getForceClock() && !getCommunicationScheduler().getCommunicationProfile().getAdHoc()){
				badTime = verifyMaxTimeDifference();
			}

			// Read the loadProfile
			if (getCommunicationProfile().getReadDemandValues()) {

				ElectricityProfile ep = new ElectricityProfile(this);
				ProfileData eProfileData = ep.getProfile(EMeter.PROFILE_OBISCODE);
				if(badTime){	// if a timedifference exceeds boundary
					eProfileData.markIntervalsAsBadTime();
				}
				storeObject.add(eProfileData, getMeter());

			}

			// Read the events
			if (getCommunicationProfile().getReadMeterEvents()) {
				getLogger().log(Level.INFO, "Getting events for meter with serialnumber: " + this.serialNumber);
				EventProfile evp = new EventProfile(this);
				ProfileData pd = evp.getEvents();
				storeObject.add(pd, getMeter());
			}

			/*
			 * Here we are assuming that the daily and monthly values should be read. In future it can be that this doesn't work for all customers, then we should implement a SmartMeterProperty to
			 * indicate whether you want to read the actual registers or the daily/monthly registers ...
			 */
			if (getCommunicationProfile().getReadMeterReadings()) {
				DailyMonthly dm = new DailyMonthly(this);

//				if (readDaily) {
//                    ObisCode dailyProfileObisCode = getMeterConfig().getDailyProfileObject().getObisCode();
//                    if(doesObisCodeExistInObjectList(dailyProfileObisCode)){
//						ProfileData dailyPd = dm.getDailyValues(dailyProfileObisCode);
//						if(badTime){
//							dailyPd.markIntervalsAsBadTime();
//						}
//						storeObject.add(dailyPd, getMeter());
//					} else {
//						getLogger().log(Level.INFO, "The dailyProfile object doesn't exist in the device.");
//					}
//				}
//				if (readMonthly) {
//					if(doesObisCodeExistInObjectList(getMeterConfig().getMonthlyProfileObject().getObisCode())){
//						ProfileData monthlyPd = dm.getMonthlyValues(getMeterConfig().getMonthlyProfileObject().getObisCode());
//						if(badTime){
//							monthlyPd.markIntervalsAsBadTime();
//						}
//						storeObject.add(monthlyPd, getMeter());
//					} else {
//						getLogger().log(Level.INFO, "The monthlyProfile object doesn't exist in the device.");
//					}
//				}

				getLogger().log(Level.INFO, "Getting registers for meter with serialnumber: " + this.serialNumber);
				Map<RtuRegister, RegisterValue> registerMap = doReadRegisters();
				storeObject.addAll(registerMap);
			}

			//Send the meter messages
			if (getCommunicationProfile().getSendRtuMessage()) {
				sendMeterMessages();
			}

			// Discover and handle MbusMeters
			discoverMbusDevices();
			if (getValidMbusDevices() != 0) {
				getLogger().log(Level.INFO, "Starting to handle the MBus meters.");
				handleMbusMeters();
			}

			// Discover and handle MbusMeters
			discoverEMeters();
			if (getValidEMeters() != 0) {
				getLogger().log(Level.INFO, "Starting to handle the Emeters.");
				handleEmeters();
			}

			// Check for TIC devices and if there is one handle it
			if(hasTicDevices()){
				getLogger().log(Level.INFO, "Starting to handle the Tic device.");
				handleTicDevice();
			}

			// Set clock or Force clock... if necessary
			if (getCommunicationProfile().getForceClock()) {
				Date meterTime = getTime();
				Date currentTime = Calendar.getInstance(getTimeZone()).getTime();
				setTimeDifference(Math.abs(currentTime.getTime() - meterTime.getTime()));
				getLogger().log(Level.INFO, "Forced to set meterClock to systemTime: " + currentTime);
				forceClock(currentTime);
			} else {
				verifyAndWriteClock();
			}

			try {
				//test();
			} catch (Exception e) {
				e.printStackTrace();
			}


		} finally {
			if (storeObject != null) {
				Environment.getDefault().execute(storeObject);
			}
		}

	}

	private void test() throws IOException {

		UniversalObject[] uo = getMeterConfig().getInstantiatedObjectList();
		for (UniversalObject universalObject : uo) {
			if (SERIALNR_OBISCODE.equals(ProtocolTools.setObisCodeField(universalObject.getObisCode(), 1, (byte) 0))){
				//System.out.println(universalObject);
				try {
					DataStructure ds = getCosemObjectFactory().getGenericRead(universalObject).getDataContainer().getRoot();
					System.out.println(universalObject.getObisCode() + " = " + ds.getElement(0).toString());
				} catch (IOException e) {
					//
				}
			}
		}

		List<DLMSAttribute> dlmsAttributes = new ArrayList<DLMSAttribute>();
		for (byte i = 0; i < 16; i++) {
			dlmsAttributes.add(new DLMSAttribute(ProtocolTools.setObisCodeField(SERIALNR_OBISCODE, 1, i), 2, DLMSClassId.DATA));
		}
		getCosemObjectFactory().getGenericReadWithList(dlmsAttributes);

	}

	@Override
	protected ConformanceBlock configureConformanceBlock() {
		return new ConformanceBlock(ConformanceBlock.DEFAULT_LN_CONFORMANCE_BLOCK);
	}

	@Override
	protected InvokeIdAndPriority configureInvokeIdAndPriority() {
		try {
			return buildDefaultInvokeIdAndPriority();
		} catch (DLMSConnectionException e) {
			log(Level.FINEST, e.getMessage());
			// if we can't get it, then return null so the default should be used
			return null;
		}
	}

	@Override
	protected XdlmsAse configureXdlmsAse() {
		return  new XdlmsAse(null, true, -1, 6, configureConformanceBlock(), 1200);
	}

	@Override
	protected void doConnect() throws IOException{
		verifyMeterSerialNumber();
		log(Level.INFO, "FirmwareVersion: " + getFirmWareVersion());
		//check if RF-Firmware exists
		String rfFirmware = getRFFirmwareVersion();
		if(!rfFirmware.equalsIgnoreCase("")){
			log(Level.INFO, "RF-FirmwareVersion: " + rfFirmware);
		}
	}

	/**
	 * {@inheritDoc}
	 * @throws SQLException during smsWakeup, if we couldn't clear the IP-address in the database
	 * @throws BusinessException if a business error occurred
	 */
	@Override
	protected void doInit() throws SQLException, BusinessException, IOException{
		this.mbusDevices = new MbusDevice[MBUS_DEVICES.getNumberOfDevices()];
		this.eMeters = new EMeter[EMETER_DEVICES.getNumberOfDevices()];
		this.storeObject = new StoreObject();
		this.ocm = new ObisCodeMapper(getCosemObjectFactory());
	}

	/**
	 * Read the firmwareVersion from the device
	 * @return the firmwareVersion
	 * @throws IOException if we couldn't get the version
	 */
	private String getFirmWareVersion() throws IOException {
		try {
			return getCosemObjectFactory().getGenericRead(getMeterConfig().getVersionObject()).getString();
		} catch (IOException e) {
			String message = "Could not fetch the firmwareVersion." + e.getMessage();
			log(Level.FINEST, message);
			throw new IOException(message);
		}
	}

	/**
	 * Read the Z3/R2 RF-Firmwareversion
	 * @return the firmwareversion, if it's not available then return an empty string
	 */
	private String getRFFirmwareVersion() {
		try {
			return getCosemObjectFactory().getGenericRead(RF_FIRMWAREVERSION,DLMSUtils.attrLN2SN(2),1).getString();
		} catch (IOException e) {
			log(Level.FINEST, "Unable to read the RFFirmwareVersion: " + e.getMessage());
			return "";
		}
	}

	/**
	 * Checks if the serialnumber from the device matches the one configured in EIServer
	 * @throws IOException if it doesn't match
	 */
	private void verifyMeterSerialNumber() throws IOException {
		String serial = getSerialNumber();
		if (!(this.serialNumber.equalsIgnoreCase("")) && (!this.serialNumber.equals(serial))) {
			throw new IOException("Wrong serialnumber, EIServer settings: " + this.serialNumber + " - Meter settings: " + serial);
		}
	}

	/**
	 * Get the serialNumber from the device
	 * @return the serialnumber from the device
	 * @throws IOException we couldn't read the serialnumber
	 */
	public String getSerialNumber() throws IOException {
		try {
			return getCosemObjectFactory().getGenericRead(getMeterConfig().getSerialNumberObject()).getString();
		} catch (IOException e) {
			log(Level.FINEST, e.getMessage());
			throw new IOException("Could not retrieve the serialnumber of the meter." + e);
		}
	}

	@Override
	protected void doDisconnect() {
	}

	@Override
	protected List<String> doGetOptionalKeys() {
		List<String> result = new ArrayList<String>();
		result.add(PROPERTY_DELAY_AFTER_FAIL);
		result.add(PROPERTY_REQUEST_TIME_ZONE);
		result.add(PROPERTY_FIRMWARE_VERSION);
		result.add(PROPERTY_EXTENDED_LOGGING);
		result.add(PROPERTY_TEST_LOGGING);
		result.add(PROPERTY_READ_DAILY_VALUES);
		result.add(PROPERTY_READ_MONTHLY_VALUES);
		result.add(PROPERTY_FOLDER_EXT_NAME);
		result.add(LocalSecurityProvider.DATATRANSPORTKEY);
		result.add(LocalSecurityProvider.DATATRANSPORT_AUTHENTICATIONKEY);
		result.add(LocalSecurityProvider.MASTERKEY);
		result.add(LocalSecurityProvider.NEW_GLOBAL_KEY);
		result.add(LocalSecurityProvider.NEW_AUTHENTICATION_KEY);
		result.add(LocalSecurityProvider.NEW_HLS_SECRET);
		return result;
	}

	@Override
	protected List<String> doGetRequiredKeys() {
		List<String> result = new ArrayList<String>();
        result.add(PROPERTY_MBUS_RTUTYPE);
        result.add(PROPERTY_EMETER_RTUTYPE);
        return result;
	}

	@Override
	protected void doValidateProperties() {

		if ((getMeter() != null) && (getMeter().getSerialNumber() != "")) {
			this.serialNumber = getMeter().getSerialNumber();
		} else {
			this.serialNumber = "";
		}

		if ((getMeter() != null) && (getMeter().getPassword() != "")) {
			this.password = getMeter().getPassword();
		} else if(getMeter() == null){
			this.password = getProperties().getProperty(PROPERTY_PASSWORD,DEFAULT_PASSWORD);
		}

		this.requestTimeZone = Integer.parseInt(getProperties().getProperty(PROPERTY_REQUEST_TIME_ZONE, DEFAULT_REQUEST_TIMEZONE));
		this.readDaily = (Integer.parseInt(getProperties().getProperty(PROPERTY_READ_DAILY_VALUES, DEFAULT_READ_DAILY_VALUES)) == 1) ? true : false;
		this.readMonthly = (Integer.parseInt(getProperties().getProperty(PROPERTY_READ_MONTHLY_VALUES, DEFAULT_READ_MONTHLY_VALUES)) == 1) ? true : false;
		this.mbusRtuType = getProperties().getProperty(PROPERTY_MBUS_RTUTYPE);
		this.eMeterRtuType = getProperties().getProperty(PROPERTY_EMETER_RTUTYPE);
		this.folderExtName = getProperties().getProperty(PROPERTY_FOLDER_EXT_NAME);
	}

	/**
	 * @return true if it's allowed to read the daily values
	 */
	boolean isReadDaily(){
		return this.readDaily;
	}

	/**
	 * @return true if it's allowed to read the monthly values
	 */
	boolean isReadMonthly(){
		return this.readMonthly;
	}

	@Override
	public SecurityProvider getSecurityProvider() {
		if((getMeter() != null) && (password != null)){
			getProperties().put(MeterProtocol.PASSWORD, password);
		}
		LocalSecurityProvider lsp = new LocalSecurityProvider(getProperties());
		return lsp;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getVersion() {
		return "$Date$";
	}

	/**
	 * {@inheritDoc}
	 */
	public int getReference() {
		return ProtocolLink.LN_REFERENCE;
	}

	//TODO to complete with a property value
	public int getRoundTripCorrection() {
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public StoredValues getStoredValues() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isRequestTimeZone() {
		return (this.requestTimeZone == 1) ? true : false;
	}

	/**
	 * @return the connectionMode
	 */
	public int getConnectionMode() {
		return super.getConnectionMode();
	}

	/**
	 * @return the storeObject from the Z3
	 */
	public StoreObject getStoreObject(){
		return this.storeObject;
	}

	/**
	 * @return the badTime parameter. It's true if the timedifference exceeds the configured boundaries
	 */
	public boolean isBadTime(){
		return badTime;
	}

	/**
	 * Collect the IP address of the meter and update this value on the RTU
	 *
	 * @throws SQLException if a database exception occurred during the upgrade of the IP-address
	 * @throws BusinessException if a businessexception occurred during the upgrade of the IP-address
	 * @throws IOException caused by an invalid reference type or invalid datatype
	 */
	private void updateIPAddress() throws SQLException, BusinessException, IOException {
		StringBuffer ipAddress = new StringBuffer();
		try {
			IPv4Setup ipv4Setup = getCosemObjectFactory().getIPv4Setup();
			ipAddress.append(ipv4Setup.getIPAddress());
			ipAddress.append(":");
			ipAddress.append(getIpPortNumber());

			RtuShadow shadow = getMeter().getShadow();
			shadow.setIpAddress(ipAddress.toString());

			getMeter().update(shadow);
		} catch (IOException e) {
			log(Level.FINEST, e.getMessage());
			throw new IOException("Could not set the IP address." + e);
		} catch (SQLException e) {
			log(Level.FINEST, e.getMessage());
			throw new SQLException("Could not update the IP address." + e);
		}
	}

	@Override
	protected RegisterValue readRegister(ObisCode obisCode) throws IOException {
		if (ocm == null) {
			ocm = new ObisCodeMapper(getCosemObjectFactory());
		}
		return ocm.getRegisterValue(obisCode);
	}

    @Override
    protected void validateProperties() throws MissingPropertyException, InvalidPropertyException {
        super.validateProperties();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
	 * Messages
	 *
	 * @throws SQLException if a database access error occurs
	 * @throws BusinessException if a business error occurs
	 */
	private void sendMeterMessages() throws BusinessException, SQLException {

		MessageExecutor messageExecutor = new MessageExecutor(this);

		Iterator<RtuMessage> it = getMeter().getPendingMessages().iterator();
		RtuMessage rm = null;
		while (it.hasNext()) {
			rm = it.next();
			messageExecutor.doMessage(rm);
		}
	}

	/**
	 * Discover Mbus devices
	 * @throws SQLException
	 * @throws BusinessException
	 * @throws IOException
	 */
	public void discoverMbusDevices() throws SQLException, BusinessException, IOException{
		log(Level.FINE, "Starting discovery of MBusDevices");
		// get an MbusDeviceMap
		List<DeviceMapping> mbusMap = getMbusMapper();
		// check if the current mbus slaves are still on the meter disappeared
		checkForDisappearedMbusMeters(mbusMap);
		// check if all the mbus devices are configured in EIServer
		checkToUpdateMbusMeters(mbusMap);

        StringBuffer sb = new StringBuffer();
        sb.append("Found ").append(mbusMap.size()).append(" MBus devices: ").append("\r\n");
        for (int i = 0; i < mbusMap.size(); i++) {
            DeviceMapping deviceMapping =  mbusMap.get(i);
            sb.append(deviceMapping).append("\r\n");
        }
        getLogger().log(Level.INFO, sb.toString());

	}

	/**
	 * Discover Mbus devices
	 * @throws SQLException
	 * @throws BusinessException
	 * @throws IOException
	 */
	public void discoverEMeters() throws SQLException, BusinessException, IOException{
		log(Level.FINE, "Starting discovery of eMeters");
		// get an MbusDeviceMap
		List<DeviceMapping> eMeterMap = getEmeterMapper();
		// check if the current mbus slaves are still on the meter disappeared
		checkForDisappearedEMeters(eMeterMap);
		// check if all the mbus devices are configured in EIServer
		checkToUpdateEMeters(eMeterMap);

        StringBuffer sb = new StringBuffer();
        sb.append("Found ").append(eMeterMap.size()).append(" eMeter devices: ").append("\r\n");
        for (int i = 0; i < eMeterMap.size(); i++) {
            DeviceMapping deviceMapping =  eMeterMap.get(i);
            sb.append(deviceMapping).append("\r\n");
        }
        getLogger().log(Level.INFO, sb.toString());

	}

	/**
	 * Constructs a map containing the serialNumber and the physical address of the mbusdevice.
	 * If the serialNumber can't be retrieved from the device then we just log and try the next one.
	 * @return a map containing SerailNumber - Physical mbus address
	 * @throws ConnectionException if interframeTimeout has passed and maximum retries have been reached
	 */
	private List<DeviceMapping> getMbusMapper() throws ConnectionException{
		String mbusSerial;
		List<DeviceMapping> mbusMap = new ArrayList<DeviceMapping>();
		for (int i = MBUS_DEVICES.getFrom(); i <= MBUS_DEVICES.getTo(); i++) {
			mbusSerial = "";
			try {
				Data serialDataObject = getCosemObjectFactory().getData(ProtocolTools.setObisCodeField(SERIALNR_OBISCODE, 1, (byte) i));
				OctetString serialOctetString = serialDataObject.getAttrbAbstractDataType(2).getOctetString();
				mbusSerial = serialOctetString != null ? serialOctetString.stringValue() : null;
				if ((mbusSerial != null) && (!mbusSerial.equalsIgnoreCase(""))) {
					mbusMap.add(new DeviceMapping(mbusSerial, i));
				}
			} catch (IOException e) {
				if(e.getMessage().indexOf("com.energyict.dialer.connection.ConnectionException: receiveResponse() interframe timeout error") > -1){
					throw new ConnectionException("InterframeTimeout occurred. Meter probably not accessible anymore." + e);
				}
				//log(Level.FINE, "Could not retrieve the mbus serialNumber for channel " + i + ": " + e.getMessage());
			}
		}
		return mbusMap;
	}

	/**
	 * Constructs a map containing the serialNumber and the physical address of the e-meter.
	 * If the serialNumber can't be retrieved from the device then we just log and try the next one.
	 * @return a map containing SerailNumber - Physical address
	 * @throws ConnectionException if interframeTimeout has passed and maximum retries have been reached
	 */
	private List<DeviceMapping> getEmeterMapper() throws ConnectionException{
		String eMeterSerial;
		List<DeviceMapping> eMeterMap = new ArrayList<DeviceMapping>();
		for (int i = EMETER_DEVICES.getFrom(); i <= EMETER_DEVICES.getTo(); i++) {
			eMeterSerial = "";
			try {
				Data serialDataObject = getCosemObjectFactory().getData(ProtocolTools.setObisCodeField(SERIALNR_OBISCODE, 1, (byte) i));
				OctetString serialOctetString = serialDataObject.getAttrbAbstractDataType(2).getOctetString();
				eMeterSerial = serialOctetString != null ? serialOctetString.stringValue() : null;
				if ((eMeterSerial != null) && (!eMeterSerial.equalsIgnoreCase(""))) {
					eMeterMap.add(new DeviceMapping(eMeterSerial, i));
				}
			} catch (IOException e) {
				if(e.getMessage().indexOf("com.energyict.dialer.connection.ConnectionException: receiveResponse() interframe timeout error") > -1){
					throw new ConnectionException("InterframeTimeout occurred. Meter probably not accessible anymore." + e);
				}
				//log(Level.FINE, "Could not retrieve the eMeter serialNumber for channel " + i + ": " + e.getMessage());
			}
		}
		return eMeterMap;
	}

	/**
	 * Check to see if you find MbusDevices as slaves for the current Z3 in the DataBase, but NOT on the physical device
	 * @param mbusMap - a map of serialNumbers read from the Z3
	 */
	private void checkForDisappearedMbusMeters(List<DeviceMapping> mbusMap){

		List<Rtu> mbusSlaves = getMeter().getDownstreamRtus();
		Iterator<Rtu> it = mbusSlaves.iterator();
		while(it.hasNext()){
			Rtu mbus = it.next();
			Class device = null;
			try {
				device = Class.forName(mbus.getRtuType().getShadow().getCommunicationProtocolShadow().getJavaClassName());
				if((device != null) && (device.newInstance() instanceof MbusDevice)){		// we check to see if it's an Mbus device and no TIC device
					if(!mbusMap.contains(new DeviceMapping(mbus.getSerialNumber()))) {
						getLogger().log(Level.INFO, "MbusDevice [" + mbus + "] is not installed on the physical device.");
						mbusMap.add(new DeviceMapping(mbus.getSerialNumber(), true));
					}
				}
			} catch (ClassNotFoundException e) {
				log(Level.FINEST, e.getMessage());
				// should never come here because if the rtuType has the className, then you should be able to create a class for it...
			} catch (InstantiationException e) {
				log(Level.FINEST, e.getMessage());
				getLogger().log(Level.INFO, "Could not check if the mbusDevice " + mbus.getSerialNumber() + " exists.");
			} catch (IllegalAccessException e) {
				log(Level.FINEST, e.getMessage());
				getLogger().log(Level.INFO, "Could not check if the mbusDevice " + mbus.getSerialNumber() + " exists.");
			}
		}

	}

	/**
	 * Check to see if you find MbusDevices as slaves for the current Z3 in the DataBase, but NOT on the physical device
	 * @param eMeterMap - a map of serialNumbers read from the Z3
	 */
	private void checkForDisappearedEMeters(List<DeviceMapping> eMeterMap){

		List<Rtu> eMeterSlaves = getMeter().getDownstreamRtus();
		Iterator<Rtu> it = eMeterSlaves.iterator();
		while(it.hasNext()){
			Rtu eMeter = it.next();
			Class device = null;
			try {
				device = Class.forName(eMeter.getRtuType().getShadow().getCommunicationProtocolShadow().getJavaClassName());
                if((device != null) && (device.newInstance() instanceof EMeter)){		// we check to see if it's an Mbus device and no TIC device
					if(!eMeterMap.contains(new DeviceMapping(eMeter.getSerialNumber()))) {
						getLogger().log(Level.INFO, "EMeter [" + eMeter + "] is not installed on the physical device.");
						eMeterMap.add(new DeviceMapping(eMeter.getSerialNumber(), true));
					}
				}
			} catch (ClassNotFoundException e) {
				log(Level.FINEST, e.getMessage());
				// should never come here because if the rtuType has the className, then you should be able to create a class for it...
			} catch (InstantiationException e) {
				log(Level.FINEST, e.getMessage());
				getLogger().log(Level.INFO, "Could not check if the eMeter " + eMeter.getSerialNumber() + " exists.");
			} catch (IllegalAccessException e) {
				log(Level.FINEST, e.getMessage());
				getLogger().log(Level.INFO, "Could not check if the eMeter " + eMeter.getSerialNumber() + " exists.");
			}
		}

	}


	/**
	 * Check the ghostMbusDevices and create the mbusDevices
	 * @param mbusMap
	 * @throws BusinessException if a business error occurred
	 * @throws SQLException if database exception occurred
	 * @throws IOException if multiple meters were found in the database
	 */
    private void checkToUpdateMbusMeters(List<DeviceMapping> mbusMap) throws SQLException, BusinessException, IOException {
        int count = 0;
        for (int i = 0; i < mbusMap.size(); i++) {
            DeviceMapping deviceMapping = mbusMap.get(i);
            if (!deviceMapping.isGhostDevice()) {
                Rtu mbus = CommonUtils.findOrCreateDeviceBySerialNumber(deviceMapping.getSerialNumber(), mbusRtuType, folderExtName);
                if (mbus != null) {
                    mbus.updateGateway(getMeter());
                    this.mbusDevices[count++] = new MbusDevice(deviceMapping.getSerialNumber(), deviceMapping.getPhysicalAddress(), mbus, getLogger());
                }
            }
        }
    }

    /**
	 * Check the ghostMbusDevices and create the mbusDevices
	 * @param eMeterMap
	 * @throws BusinessException if a business error occurred
	 * @throws SQLException if database exception occurred
	 * @throws IOException if multiple meters were found in the database
	 */
	private void checkToUpdateEMeters(List<DeviceMapping> eMeterMap) throws SQLException, BusinessException, IOException{
        int count = 0;
        for (int i = 0; i < eMeterMap.size(); i++) {
            DeviceMapping deviceMapping = eMeterMap.get(i);
            if (!deviceMapping.isGhostDevice()) {
                Rtu eMeter = CommonUtils.findOrCreateDeviceBySerialNumber(deviceMapping.getSerialNumber(), eMeterRtuType, folderExtName);
                if (eMeter != null) {
                    eMeter.updateGateway(getMeter());
                    this.eMeters[count++] = new EMeter(deviceMapping.getSerialNumber(), deviceMapping.getPhysicalAddress(), eMeter, getLogger());
                }
            }
        }
	}


	/**
	 * Check to see if there are mbusDevices
	 * @return the number of MbusDevices
	 */
	private int getValidMbusDevices(){
		int count = 0;
		for(int i = 0; i < MBUS_DEVICES.getNumberOfDevices(); i++){
			if(this.mbusDevices[i] != null){
				count++;
			}
		}
		return count;
	}

	/**
	 * Check to see if there are eMeters
	 * @return the number of {@link EMeter}
	 */
	private int getValidEMeters(){
		int count = 0;
		for(int i = 0; i < EMETER_DEVICES.getNumberOfDevices(); i++){
			if(this.eMeters[i] != null){
				count++;
			}
		}
		return count;
	}

	/**
	 * Handles all the MBus devices like a separate device
	 */
	private void handleMbusMeters() {
		for (int i = 0; i < MBUS_DEVICES.getNumberOfDevices(); i++) {
			try {
				if (mbusDevices[i] != null) {
					List<CommunicationScheduler> commSchedulers = mbusDevices[i].getMbus().getCommunicationSchedulers();
					for (CommunicationScheduler commSchedule : commSchedulers) {
						mbusDevices[i].setWebRtu(this);
						mbusDevices[i].execute(commSchedule, null, null);
						getLogger().info("MbusDevice " + mbusDevices[i] + " has finished.");
					}
				}
			} catch (BusinessException e) {

				/*
				 * A single MBusMeter failed: log and try next MBusMeter.
				 */
				log(Level.FINEST, e.getMessage());
				getLogger().log(Level.SEVERE, "MBusMeter with serial: " + mbusDevices[i].getCustomerID() + " has failed.");

			} catch (SQLException e) {

				/** Close the connection after an SQL exception, connection will startup again if requested */
				Environment.getDefault().closeConnection();

				/*
				 * A single MBusMeter failed: log and try next MBusMeter.
				 */
				log(Level.FINEST, e.getMessage());
				getLogger().log(Level.SEVERE, "MBusMeter with serial: " + mbusDevices[i].getCustomerID() + " has failed.");

			} catch (IOException e) {

				/*
				 * A single MBusMeter failed: log and try next MBusMeter.
				 */
				log(Level.FINEST, e.getMessage());
				getLogger().log(Level.SEVERE, "MBusMeter with serial: " + mbusDevices[i].getCustomerID() + " has failed. [" + e.getMessage() + "]");

			}
		}
	}

	/**
	 * Checks whether there is a TicDevice configured in EIServer
	 * @return true if there is a TicDevice configured as a slave of the WebRTU
	 */
	private boolean hasTicDevices() {
		Rtu tic;
		List<Rtu> slaves = getMeter().getDownstreamRtus();
		Iterator<Rtu> it = slaves.iterator();
		while (it.hasNext()) {
			tic = it.next();
			Class ticDevice = null;

			try {
				ticDevice = Class.forName(tic.getRtuType().getShadow().getCommunicationProtocolShadow().getJavaClassName());
				if((ticDevice != null) && (ticDevice.newInstance() instanceof TicDevice)){
					this.ticDevice = new TicDevice(tic);
					return true;
				}
			} catch (ClassNotFoundException e) {
				log(Level.FINEST, e.getMessage());
//				should never come here because if the rtuType has the className, then you should be able to create a class for it...
			} catch (InstantiationException e) {
				log(Level.FINEST, e.getMessage());
				getLogger().log(Level.INFO, "Could not check for TicDevices exists.");
			} catch (IllegalAccessException e) {
				log(Level.FINEST, e.getMessage());
				getLogger().log(Level.INFO, "Could not check for TicDevices exists.");
			}
		}
		return false;
	}

	/**
	 * Handle the TIC device.
	 * Only profileData and events can be read
	 * @throws BusinessException
	 * @throws SQLException
	 * @throws IOException
	 */
	private void handleTicDevice() throws BusinessException, SQLException, IOException {
		this.ticDevice.setWebRTU(this);
		this.ticDevice.execute(getCommunicationScheduler(), null, getLogger());
	}

	/**
	 *
	 */
	private void handleEmeters() throws BusinessException, SQLException, IOException {
		for (EMeter eMeter : eMeters) {
			try {
				if (eMeter != null) {
					List<CommunicationScheduler> commSchedulers = eMeter.geteMeterRtu().getCommunicationSchedulers();
					for (CommunicationScheduler commSchedule : commSchedulers) {
						eMeter.setWebRtu(this);
						eMeter.execute(commSchedule, null, null);
						getLogger().info("Emeter " + eMeter + " has finished.");
					}
				}
			} catch (BusinessException e) {

				/*
				 * A single MBusMeter failed: log and try next MBusMeter.
				 */
				log(Level.FINEST, e.getMessage());
				getLogger().log(Level.SEVERE, "Emeter with serial: " + eMeter.getSerialNumber() + " has failed.");

			} catch (SQLException e) {

				/** Close the connection after an SQL exception, connection will startup again if requested */
				Environment.getDefault().closeConnection();

				/*
				 * A single MBusMeter failed: log and try next MBusMeter.
				 */
				log(Level.FINEST, e.getMessage());
				getLogger().log(Level.SEVERE, "Emeter with serial: " + eMeter.getSerialNumber() + " has failed.");

			} catch (IOException e) {

				/*
				 * A single MBusMeter failed: log and try next MBusMeter.
				 */
				log(Level.FINEST, e.getMessage());
				getLogger().log(Level.SEVERE, "Emeter with serial: " + eMeter.getSerialNumber() + " has failed. [" + e.getMessage() + "]");

			}

		}
	}

	@Override
	public List getMessageCategories() {
		List<MessageCategorySpec> categories = new ArrayList();
		MessageCategorySpec catXMLConfig = getXmlConfigCategory();
		MessageCategorySpec catFirmware = getFirmwareCategory();
		MessageCategorySpec catP1Messages = getP1Category();
		MessageCategorySpec catDisconnect = getConnectControlCategory();
		MessageCategorySpec catLoadLimit = getLoadLimitCategory();
		MessageCategorySpec catActivityCal = getActivityCalendarCategory();
		MessageCategorySpec catTime = getTimeCategory();
		MessageCategorySpec catMakeEntries = getDataBaseEntriesCategory();
		MessageCategorySpec catTestMessage = getTestCategory();
		MessageCategorySpec catGlobalDisc = getGlobalResetCategory();
		MessageCategorySpec catAuthEncrypt = getAuthEncryptCategory();
		MessageCategorySpec catConnectivity = getConnectivityCategory();

		categories.add(catXMLConfig);
		categories.add(catFirmware);
		categories.add(catP1Messages);
		categories.add(catDisconnect);
		categories.add(catLoadLimit);
		categories.add(catActivityCal);
		categories.add(catTime);
		categories.add(catMakeEntries);
		categories.add(catTestMessage);
		categories.add(catGlobalDisc);
		categories.add(catConnectivity);

		categories.add(catAuthEncrypt);

		return categories;
	}

	/**
	 * This messageCategory let's you upgrade two types of firmware.
	 * One is the normal meter firmware, the other is the RF-firmware
	 * Both are imported with a userfile
	 * @return the messages for the FirmwareUpgrade
	 */
	@Override
	public MessageCategorySpec getFirmwareCategory() {
		MessageCategorySpec catFirmware = new MessageCategorySpec(
				RtuMessageCategoryConstants.FIRMWARE);
		MessageSpec msgSpec = addFirmwareMsg(RtuMessageKeyIdConstants.FIRMWARE,
				RtuMessageConstant.FIRMWARE_UPGRADE, false);
		catFirmware.addMessageSpec(msgSpec);
		msgSpec = addFirmwareMsg(RtuMessageKeyIdConstants.RFFIRMWARE,
				RtuMessageConstant.RF_FIRMWARE_UPGRADE, false);
		catFirmware.addMessageSpec(msgSpec);
		return catFirmware;
	}

	/**
	 * @return the messages for the ConnectivityCategory
	 */
	private MessageCategorySpec getConnectivityCategory() {
		MessageCategorySpec catGPRSModemSetup = new MessageCategorySpec(
				RtuMessageCategoryConstants.CHANGECONNECTIVITY);
		MessageSpec msgSpec = addChangeGPRSSetup(
				RtuMessageKeyIdConstants.GPRSMODEMSETUP,
				RtuMessageConstant.GPRS_MODEM_SETUP, false);
		catGPRSModemSetup.addMessageSpec(msgSpec);
		msgSpec = addPhoneListMsg(RtuMessageKeyIdConstants.SETWHITELIST,
				RtuMessageConstant.WAKEUP_ADD_WHITELIST, false);
		catGPRSModemSetup.addMessageSpec(msgSpec);
		msgSpec = addNoValueMsg(RtuMessageKeyIdConstants.ACTIVATESMSWAKEUP,
				RtuMessageConstant.WAKEUP_ACTIVATE, false);
		catGPRSModemSetup.addMessageSpec(msgSpec);
		msgSpec = addNoValueMsg(RtuMessageKeyIdConstants.DEACTIVATESMSWAKEUP,
				RtuMessageConstant.WAKEUP_DEACTIVATE, false);
		catGPRSModemSetup.addMessageSpec(msgSpec);
		return catGPRSModemSetup;
	}

	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.webrtuz3.profiles.EDevice#getPhysicalAddress()
	 */
	public int getPhysicalAddress() {
		return 0;
	}
}
