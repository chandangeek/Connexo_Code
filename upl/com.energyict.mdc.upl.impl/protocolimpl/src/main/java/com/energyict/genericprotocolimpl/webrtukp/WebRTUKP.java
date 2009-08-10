package com.energyict.genericprotocolimpl.webrtukp;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.NotFoundException;
import com.energyict.cbo.Utils;
import com.energyict.cpo.Environment;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dialer.core.DialerMarker;
import com.energyict.dialer.core.Link;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dialer.coreimpl.SocketStreamConnection;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.InvokeIdAndPriority;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.SecureConnection;
import com.energyict.dlms.TCPIPConnection;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.aso.AssociationControlServiceElement;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.dlms.aso.XdlmsAse;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.CosemObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.IPv4Setup;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.genericprotocolimpl.common.StoreObject;
import com.energyict.genericprotocolimpl.webrtukp.messagehandling.MessageExecutor;
import com.energyict.genericprotocolimpl.webrtukp.messagehandling.MeterMessages;
import com.energyict.genericprotocolimpl.webrtukp.profiles.DailyMonthly;
import com.energyict.genericprotocolimpl.webrtukp.profiles.ElectricityProfile;
import com.energyict.genericprotocolimpl.webrtukp.profiles.EventProfile;
import com.energyict.genericprotocolimpl.webrtukp.wakeup.SmsWakeup;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.amr.RtuRegisterGroup;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.CommunicationProfile;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.Folder;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.core.RtuMessage;
import com.energyict.mdw.core.RtuType;
import com.energyict.mdw.shadow.RtuShadow;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.DLMSCache;
import com.energyict.protocolimpl.dlms.HDLC2Connection;
import com.energyict.protocolimpl.dlms.RtuDLMS;
import com.energyict.protocolimpl.dlms.RtuDLMSCache;

/**
 * 
 * @author gna |08012009| First complete draft containing: - LoadProfile E-meter - Registers E-meter - LoadProfile Mbus-meter - Registers Mbus-meter Changes: GNA |20012009| Added the imageTransfer
 *         message, here we use the P3ImageTransfer object GNA |22012009| Added the Consumer messages over the P1 port GNA |27012009| Added the Disconnect Control message GNA |28012009| Implemented
 *         the Loadlimit messages - Enabled the daily/Monthly code GNA |02022009| Added the forceClock functionality GNA |12022009| Added ActivityCalendar and SpecialDays as rtu message GNA |17022009|
 *         Bug in hasMbusMeters(), if serialnumber is not found -> log and go next GNA |19022009| Changed all messageEntrys in date-form to a UnixTime entry; Added a message to change to connectMode
 *         of the disconnectorObject; Fixed bugs in the ActivityCalendar object; Added an entry delete of the specialDays GNA |09032009| Added the informationFieldSize to the HDLCConnection so the max
 *         send/received length is customizable GNA |16032009| Added the getTimeDifference method so timedifferences are shown in the AMR logging. Added properties to disable the reading of the
 *         daily/monthly values Added ipPortNumber property for updating the phone number with inbound communications GNA |30032009| Added testMessage to enable overnight tests for the embedded device
 *         GNA |May 2009| Added Sms wakeup support GNA |03062009| Added registerGroup support GNA |05062009| Changed writeClock support, split meterEvents and meterProfile
 */

public class WebRTUKP extends MeterMessages implements GenericProtocol, ProtocolLink, HHUEnabler, MeterToolProtocol {

	private boolean DEBUG = false;
	private boolean connected = false;
	private boolean badTime = false;
	private boolean enforceSerialNumber = true;

	private CosemObjectFactory cosemObjectFactory;
	private DLMSConnection dlmsConnection;
	private DLMSMeterConfig dlmsMeterConfig;
	private CommunicationProfile commProfile;
	private Logger logger;
	private CommunicationScheduler scheduler;
	private Link link;

	private Rtu webRtuKP;
	private ApplicationServiceObject aso;

	// this cache object is supported by 7.5
	private DLMSCache dlmsCache = new DLMSCache();

	private MbusDevice[] mbusDevices;
	/** GhostMbusDevices are Mbus meters that are connected with their gateway in EIServer, but not on the physical device anymore*/
	private HashMap<String, Integer> ghostMbusDevices = new HashMap<String, Integer>();
	private TicDevice ticDevice;
	private Clock deviceClock;
	private StoreObject storeObject;
	private ObisCodeMapper ocm;

	private long timeDifference = -1;
	private long roundTripCorrection = 0;

	/**
	 * Properties
	 */
	private Properties properties;
	private int authenticationSecurityLevel;
	private int datatransportSecurityLevel; 	
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
	private int iiapPriority;
	private int iiapServiceClass;
	private int iiapInvokeId;
	private int wakeup;

	/**
	 * <pre>
	 * This method handles the complete WebRTU. The Rtu acts as an Electricity meter. The E-meter itself can have several MBus meters 
	 * - First he handles his own data collection: 
	 * 	_Profiles
	 * 	_Daily/Monthly readings 
	 * 	_Registers 
	 * 	_Messages 
	 * - Then all the MBus meters are handled in the same way as the E-meter
	 * </pre>
	 */
	public void execute(CommunicationScheduler scheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {

		boolean success = false;
		String ipAddress = "";

		this.scheduler = scheduler;
		this.logger = logger;
		this.commProfile = this.scheduler.getCommunicationProfile();
		this.webRtuKP = this.scheduler.getRtu();
		this.link = link;

		validateProperties();
		
		try {

			if (this.wakeup == 1) {
				this.logger.info("In Wakeup");
				SmsWakeup smsWakeup = new SmsWakeup(this.scheduler, this.logger);
				smsWakeup.doWakeUp();

				this.webRtuKP = getUpdatedMeter();

				ipAddress = checkIPAddressForPortNumber(smsWakeup.getIpAddress());

				this.link.setStreamConnection(new SocketStreamConnection(ipAddress));
				this.link.getStreamConnection().open();
				getLogger().log(Level.INFO, "Connected to " + ipAddress);
			} else if((this.scheduler.getDialerFactory().getName() != null)&&(this.scheduler.getDialerFactory().getName().equalsIgnoreCase("nulldialer"))){
				throw new ConnectionException("The NullDialer type is only allowed for the wakeup meter.");
			}

			init();
			connect();
			connected = true;

			/*****************************************************************************
			 * T E S T M E T H O D S
			 */
			// doSomeTestCalls();
			// readFromMeter("1.0.90.7.0.255");
			// hasMBusMeters();
			// handleMbusMeters();
			/*****************************************************************************/

			// Check if the time is greater then allowed, if so then no data can be stored...
			// Don't do this when a forceClock is scheduled
			if(!this.scheduler.getCommunicationProfile().getForceClock() && !this.scheduler.getCommunicationProfile().getAdHoc()){
				badTime = verifyMaxTimeDifference();
			}
			
			/**
			 * After 03/06/09 the events are read apart from the intervalData
			 */
			if (this.commProfile.getReadDemandValues()) {
				ElectricityProfile ep = new ElectricityProfile(this);
				ep.getProfile(getMeterConfig().getProfileObject().getObisCode());
			}

			if (this.commProfile.getReadMeterEvents()) {
				getLogger().log(Level.INFO, "Getting events for meter with serialnumber: " + webRtuKP.getSerialNumber());
				EventProfile evp = new EventProfile(this);
				evp.getEvents();
			}

			/**
			 * Here we are assuming that the daily and monthly values should be read. In future it can be that this doesn't work for all customers, then we should implement a SmartMeterProperty to
			 * indicate whether you want to read the actual registers or the daily/monthly registers ...
			 */
			if (this.commProfile.getReadMeterReadings()) {

				DailyMonthly dm = new DailyMonthly(this);
				
				if (readDaily) {
					if(doesObisCodeExistInObjectList(getMeterConfig().getDailyProfileObject().getObisCode())){
						dm.getDailyValues(getMeterConfig().getDailyProfileObject().getObisCode());
					} else {
						getLogger().log(Level.INFO, "The dailyProfile object doesn't exist in the device.");
					}
				}
				if (readMonthly) {
					if(doesObisCodeExistInObjectList(getMeterConfig().getMonthlyProfileObject().getObisCode())){
						dm.getMonthlyValues(getMeterConfig().getMonthlyProfileObject().getObisCode());
					} else {
						getLogger().log(Level.INFO, "The monthlyProfile object doesn't exist in the device.");
					}
				}

				getLogger().log(Level.INFO, "Getting registers for meter with serialnumber: " + getSerialNumberValue());
				doReadRegisters();
			}

			if (this.commProfile.getSendRtuMessage()) {
				sendMeterMessages();
			}

			discoverMbusDevices();
			if (getValidMbusDevices() != 0) {
				getLogger().log(Level.INFO, "Starting to handle the MBus meters.");
				handleMbusMeters();
			}

			if(hasTicDevices()){
				getLogger().log(Level.INFO, "Starting to handle the Tic device.");
				handleTicDevice();
			}
			
			// Set clock or Force clock... if necessary
			if (this.commProfile.getForceClock()) {
				Date meterTime = getTime();
				Date currentTime = Calendar.getInstance(getTimeZone()).getTime();
				this.timeDifference = (this.timeDifference == -1) ? Math.abs(currentTime.getTime() - meterTime.getTime()) : this.timeDifference;
				getLogger().log(Level.INFO, "Forced to set meterClock to systemTime: " + currentTime);
				forceClock(currentTime);
			} else {
				verifyAndWriteClock();
			}

			success = true;

		} catch (DLMSConnectionException e) {
			e.printStackTrace();
			disConnect();
		} catch (ClassCastException e) {
			// Mostly programmers fault if you get here ...
			e.printStackTrace();
			disConnect();
		} catch (SQLException e) {
			e.printStackTrace();
			disConnect();

			/** Close the connection after an SQL exception, connection will startup again if requested */
			Environment.getDefault().closeConnection();

			throw new BusinessException(e);
		} finally {

			// GenericCache.stopCacheMechanism(getMeter(), dlmsCache);

			if (success) {
				disConnect();
				getLogger().info("Meter " + this.serialNumber + " has completely finished.");
			}

			if (getMeter() != null) {
				// This cacheobject is supported by the 7.5
				updateCache(getMeter().getId(), dlmsCache);
			}

			if (getStoreObject() != null) {
				Environment.getDefault().execute(getStoreObject());
			}
		}
	}
	
	/**
	 * Check if a given ObisCode is in the objectList
	 * @param obisCode to check
	 * @return true if the list is null, or when the object is found. False if it's not found
	 */
	private boolean doesObisCodeExistInObjectList(ObisCode obisCode) {
		UniversalObject[] objectList = getMeterConfig().getInstantiatedObjectList();
		if(objectList == null){
			return true;	// we don't have the objectList so try to read it 
		} else {
			for (int i=0;i<objectList.length;i++) {
				if (objectList[i].getObisCode().equals(obisCode)){
					return true;
				}
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
		this.ticDevice.execute(this.scheduler, null, getLogger());
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
				e.printStackTrace();
//				should never come here because if the rtuType has the className, then you should be able to create a class for it...
			} catch (InstantiationException e) {
				e.printStackTrace();
				getLogger().log(Level.INFO, "Could not check for TicDevices exists.");
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				getLogger().log(Level.INFO, "Could not check for TicDevices exists.");
			}
		}
		return false;
	}

	private Rtu getUpdatedMeter() {
		return mw().getRtuFactory().find(this.webRtuKP.getId());
	}

	protected boolean verifyMaxTimeDifference() throws IOException {
		Date systemTime = Calendar.getInstance().getTime();
		Date meterTime = getTime();

		this.timeDifference = Math.abs(meterTime.getTime() - systemTime.getTime());
		long diff = this.timeDifference; // in milliseconds
		if ((diff / 1000 > this.commProfile.getMaximumClockDifference())) {

			String msg = "Time difference exceeds configured maximum: (" + (diff / 1000) + " s > " + this.commProfile.getMaximumClockDifference() + " s )";

			getLogger().log(Level.SEVERE, msg);

			if (this.commProfile.getCollectOutsideBoundary()) {
				// TODO should set the completion code to TIMEERROR, but that's not possible without changing the interface ...
				return true;
			} else {
				throw new IOException(msg);
			}
		}
		return false;
	}

	/**
	 * If the received IP address doesn't contain a portnumber, then put one in it
	 * 
	 * @param ipAddress
	 * @return
	 */
	private String checkIPAddressForPortNumber(String ipAddress) {
		if (!ipAddress.contains(":")) {
			StringBuffer strBuff = new StringBuffer();
			strBuff.append(ipAddress);
			strBuff.append(":");
			strBuff.append(getPortNumber());
			return strBuff.toString();
		}
		return ipAddress;
	}

	public long getTimeDifference() {
		return this.timeDifference;
	}

	public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
		enableHHUSignOn(commChannel, false);
	}

	/**
	 * Used by the framework
	 * 
	 * @param commChannel communication channel object
	 * @param datareadout enable or disable data readout
	 * @throws com.energyict.dialer.connection.ConnectionException thrown when a connection exception happens
	 */
	public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
		HHUSignOn hhuSignOn = (HHUSignOn) new IEC1107HHUConnection(commChannel, this.timeout, this.retries, 300, 0);
		hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
		hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
		hhuSignOn.enableDataReadout(datareadout);
		getDLMSConnection().setHHUSignOn(hhuSignOn, this.deviceId);
	}

	/**
	 * Getter for the data readout
	 * 
	 * @return byte[] with data readout
	 */
	public byte[] getHHUDataReadout() {
		return getDLMSConnection().getHhuSignOn().getDataReadout();
	}

	/**
	 * Initializing global objects
	 * 
	 * @throws IOException - can be cause by the TCPIPConnection
	 * @throws DLMSConnectionException - could not create a dlmsConnection
	 * @throws BusinessException
	 * @throws SQLException when a database exception occurred
	 */
	public void init() throws IOException, DLMSConnectionException, SQLException, BusinessException{
		
		this.cosemObjectFactory	= new CosemObjectFactory((ProtocolLink)this);
		
		ConformanceBlock cb = new ConformanceBlock(ConformanceBlock.DEFAULT_LN_CONFORMANCE_BLOCK);
		XdlmsAse xDlmsAse = new XdlmsAse(null, true, -1, 6, cb, 1200);
		//TODO the dataTransport encryptionType should be a property (although currently only 0 is described by DLMS)
		SecurityContext sc = new SecurityContext(this.datatransportSecurityLevel, this.authenticationSecurityLevel, 0, getSecurityProvider());
		
		this.aso = new ApplicationServiceObject(xDlmsAse, this, sc, 
					(this.datatransportSecurityLevel == 0)?AssociationControlServiceElement.LOGICAL_NAME_REFERENCING_NO_CIPHERING:
					AssociationControlServiceElement.LOGICAL_NAME_REFERENCING_WITH_CIPHERING);

		this.dlmsConnection = new SecureConnection(this.aso, getTransportDLMSConnection());

		InvokeIdAndPriority iiap = buildInvokeIdAndPriority();
		this.dlmsConnection.setInvokeIdAndPriority(iiap);

		if (DialerMarker.hasOpticalMarker(this.link)) {
			((HHUEnabler) this).enableHHUSignOn(this.link.getSerialCommunicationChannel());
		}

		this.dlmsMeterConfig = DLMSMeterConfig.getInstance(this.manufacturer);

		if (getMeter() != null) {
			// this cacheobject is supported by the 7.5
			setCache(fetchCache(getMeter().getId()));
		}
		this.mbusDevices = new MbusDevice[this.maxMbusDevices];
		this.storeObject = new StoreObject();
	}

	/**
	 * @return the current securityProvider (currently only LocalSecurityProvider is availeable) 
	 */
	public SecurityProvider getSecurityProvider(){
		
		Properties meterProperties = getMeter().getProperties();
		meterProperties.put(MeterProtocol.PASSWORD, getMeter().getPassword());
		LocalSecurityProvider lsp = new LocalSecurityProvider(meterProperties);
		
		return lsp;
	}
	
	/**
	 * @param is - The inputStream from the Link
	 * @param os - The outputStream from the Link
	 * @return the DLMSConnection to use
	 * @throws DLMSConnectionException if unknown addressingMode has been selected
	 * @throws IOException when Connection couldn't be instantiated or when the connectionMode isn't correct
	 */
	private DLMSConnection getTransportDLMSConnection() throws DLMSConnectionException, IOException {
		DLMSConnection transportConnection;
		if (this.connectionMode == 0) {
			transportConnection = new HDLC2Connection(this.link.getInputStream(), this.link.getOutputStream(), this.timeout, this.forceDelay, this.retries, this.clientMacAddress,
					this.serverLowerMacAddress, this.serverUpperMacAddress, this.addressingMode, this.informationFieldSize, 5);
		} else if (this.connectionMode == 1) {
			transportConnection = new TCPIPConnection(this.link.getInputStream(), this.link.getOutputStream(), this.timeout, this.forceDelay, this.retries, this.clientMacAddress,
					this.serverLowerMacAddress);
		} else {
			throw new IOException("Unknown connectionMode: " + this.connectionMode + " - Only 0(HDLC) and 1(TCP) are allowed");
		}
		return transportConnection;
	}

	private InvokeIdAndPriority buildInvokeIdAndPriority() throws DLMSConnectionException {
		InvokeIdAndPriority iiap = new InvokeIdAndPriority();
		iiap.setPriority(this.iiapPriority);
		iiap.setServiceClass(this.iiapServiceClass);
		iiap.setTheInvokeId(this.iiapInvokeId);
		return iiap;
	}

	/**
	 * Makes a connection to the server, if the socket is not available then an error is thrown. After a successful connection, we initiate an authentication request.
	 * 
	 * @throws IOException
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public void connect() throws IOException, SQLException, BusinessException {
		try {
			getDLMSConnection().connectMAC();
			if (this.connectionMode == 0) {
				log(Level.INFO, "Sign On procedure done.");
			}
			getDLMSConnection().setIskraWrapper(1);

			this.aso.createAssociation();

			// objectList
			checkCacheObjects();

			if (getMeterConfig().getInstantiatedObjectList() == null) { // should never do this
				getMeterConfig().setInstantiatedObjectList(this.dlmsCache.getObjectList());
			}

			// do some checks to know you are connected to the correct meter
			verifyMeterSerialNumber();
			log(Level.INFO, "FirmwareVersion: " + getFirmWareVersion());

			if (getMeter() != null) {
				// for incoming IP-calls
				updateIPAddress();
			}
			if (this.extendedLogging >= 1) {
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
	 * 
	 * @throws SQLException - if a database exception occured during the upgrade of the IP-address
	 * @throws BusinessException - if a businessexception occured during the upgrade of the IP-address
	 * @throws IOException - caused by an invalid reference type or invalid datatype
	 */
	private void updateIPAddress() throws SQLException, BusinessException, IOException {
		StringBuffer ipAddress = new StringBuffer();
		try {
			IPv4Setup ipv4Setup = getCosemObjectFactory().getIPv4Setup();
			ipAddress.append(ipv4Setup.getIPAddress());
			ipAddress.append(":");
			ipAddress.append(getPortNumber());

			RtuShadow shadow = getMeter().getShadow();
			shadow.setIpAddress(ipAddress.toString());
			// shadow.setPhoneNumber(ipAddress.toString());

			getMeter().update(shadow);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not set the IP address.");
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException("Could not update the IP address.");
		}
	}

	/**
	 * Look if there is a portnumber given with the property IpPortNumber, else use the default 2048
	 * 
	 * @return
	 */
	private String getPortNumber() {
		String port = getMeter().getProperties().getProperty("IpPortNumber");
		if (port != null) {
			return port;
		} else {
			return "4059"; // default port number
		}
	}

	/**
	 * Just to test some objects
	 */
	private void doSomeTestCalls(){
		
//		try {
//			getCosemObjectFactory().getGenericRead(ObisCode.fromString("0.0.42.0.0.255"), DLMSUtils.attrLN2SN(2), 1);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
//		try {
//			AssociationLN aln = getCosemObjectFactory().getAssociationLN();
//			
//			System.out.println(aln.getBuffer());
//			System.out.println(aln.getAssociatedPartnersId());
//			System.out.println(aln.getClientSAP());
//			System.out.println(aln.getServerSAP());
//			System.out.println(aln.getXdlmsContextInfo());
//			System.out.println(aln.readApplicationContextName());
//			System.out.println(aln.readAuthenticationMechanismName());
//			System.out.println(aln.readSecuritySetupReference());
//			
//			ActivityCalendar ac = getCosemObjectFactory().getActivityCalendar(ObisCode.fromString("0.0.13.0.0.255"));
//			Array dpta = ac.readDayProfileTableActive();
//			ac.writeDayProfileTablePassive(dpta);
//			
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	/**
	 * Handles all the MBus devices like a separate device
	 */
	private void handleMbusMeters() {
		for (int i = 0; i < this.maxMbusDevices; i++) {
			try {
				if (mbusDevices[i] != null) {
					mbusDevices[i].setWebRtu(this);
					mbusDevices[i].execute(scheduler, null, null);
					getLogger().info("MbusDevice " + (i+1) + " has finished.");
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
				getLogger().log(Level.SEVERE, "MBusMeter with serial: " + mbusDevices[i].getCustomerID() + " has failed. [" + e.getMessage() + "]");

			}
		}
	}

	/**
	 * Reading all the registers configured on the RTU
	 * 
	 * @throws IOException
	 */
	protected void doReadRegisters() throws IOException {
		Iterator<RtuRegister> it = getMeter().getRegisters().iterator();
		List groups = this.scheduler.getCommunicationProfile().getRtuRegisterGroups();
		ObisCode oc = null;
		RegisterValue rv = null;
		RtuRegister rr;
		while (it.hasNext()) {
			try {
				rr = it.next();
				if (isInRegisterGroup(groups, rr)) {
					oc = rr.getRtuRegisterSpec().getObisCode();
					try {
						rv = readRegister(oc);

						rv.setRtuRegisterId(rr.getId());

						if (rr.getReadingAt(rv.getReadTime()) == null) {
							getStoreObject().add(rr, rv);
						}
					} catch (NoSuchRegisterException e) {
						e.printStackTrace();
						getLogger().log(Level.INFO, "ObisCode " + oc + " is not supported by the meter.");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				getLogger().log(Level.INFO, "Reading register with obisCode " + oc + " FAILED.");
//				throw new IOException(e.getMessage());
			}
		}
	}

	public boolean isInRegisterGroup(List groups, RtuRegister rr) {
		if (rr.getGroup() == null) {
			if (groups.size() == 0) {
				return true;
			}
			return false;
		}
		Iterator it = groups.iterator();
		while (it.hasNext()) {
			if (rr.getGroup().equals((RtuRegisterGroup) it.next())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * TestMethod to read a certain obisCode from the meter
	 * 
	 * @param name - the Obiscode in String format
	 * @throws IOException
	 */
	private void readFromMeter(String name) throws IOException {
		try {
			CosemObject cobj = getCosemObjectFactory().getCosemObject(ObisCode.fromString(name));
			cobj.getText();
			long value = cobj.getValue();

			// String value = "";
			// getCosemObjectFactory().getGenericRead(getMeterConfig().getMbusSerialNumber(0)).getString();
			// System.out.println("Value: " + value);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Reading of object has failed!");
		}
	}

	public RegisterValue readRegister(ObisCode obisCode) throws IOException {
		if (ocm == null) {
			ocm = new ObisCodeMapper(getCosemObjectFactory());
		}
		return ocm.getRegisterValue(obisCode);
	}

	private String getFirmWareVersion() throws IOException {
		try {
			return getCosemObjectFactory().getGenericRead(getMeterConfig().getVersionObject()).getString();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not fetch the firmwareVersion.");
		}
	}

	private void verifyMeterSerialNumber() throws IOException {
		String serial = getSerialNumber();
		if (enforceSerialNumber && !this.serialNumber.equals(serial)) {
			throw new IOException("Wrong serialnumber, EIServer settings: " + this.serialNumber + " - Meter settings: " + serial);
		}
	}

	/**
	 * Get the serialNumber from the device
	 * @return
	 * @throws IOException
	 */
	public String getSerialNumber() throws IOException {
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
			for (int i = 0; i < getMeterConfig().getInstantiatedObjectList().length; i++) {
				UniversalObject uo = getMeterConfig().getInstantiatedObjectList()[i];
				strBuilder.append(uo.getObisCode().toString() + " " + uo.getObisCode().getDescription() + "\n");
			}

			strBuilder.append("********************* Objects captured into load profile *********************\n");
			Iterator<CapturedObject> it = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getProfileObject().getObisCode()).getCaptureObjects().iterator();
			while (it.hasNext()) {
				CapturedObject capturedObject = it.next();
				strBuilder.append(capturedObject.getLogicalName().getObisCode().toString() + " " + capturedObject.getLogicalName().getObisCode().getDescription() + " (load profile)\n");
			}

			strBuilder.append("********************* Objects captured into daily load profile *********************\n");
			Iterator<CapturedObject> it2 = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getDailyProfileObject().getObisCode()).getCaptureObjects().iterator();
			while (it2.hasNext()) {
				CapturedObject capturedObject = it2.next();
				strBuilder.append(capturedObject.getLogicalName().getObisCode().toString() + " " + capturedObject.getLogicalName().getObisCode().getDescription() + " (load profile)\n");
			}

			// strBuilder.append("********************* Objects captured into monthly load profile *********************\n");
			// Iterator<CapturedObject> it3 = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getMonthlyProfileObject().getObisCode()).getCaptureObjects().iterator();
			// while(it3.hasNext()) {
			// CapturedObject capturedObject = it3.next();
			// strBuilder.append(capturedObject.getLogicalName().getObisCode().toString()+" "+capturedObject.getLogicalName().getObisCode().getDescription()+" (load profile)\n");
			// }

			return strBuilder.toString();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not generate the extended loggings." + e);
		}
	}

	/**
	 * After every communication, we close the connection to the meter.
	 * 
	 * @throws IOException
	 * @throws DLMSConnectionException
	 */
	public void disConnect() throws IOException {
		try {
			if (connected) { // only send the disconnect command if you are connected
				// otherwise you will retry for a certain time ...
				// aarq.disConnect();
				this.aso.releaseAssociation();
			}
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
	 * 
	 * @throws IOException
	 */
	private void checkCacheObjects() throws IOException {

		int configNumber;
		if (dlmsCache.getObjectList() != null) { // the dlmsCache exists
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

			if (dlmsCache.getConfProgChange() != configNumber) {
				log(Level.INFO, "Meter configuration has changed, configuration is forced to be read.");
				requestConfiguration();
				dlmsCache.saveObjectList(getMeterConfig().getInstantiatedObjectList());
				dlmsCache.setConfProgChange(configNumber);
			}

		} else { // cache does not exist
			log(Level.INFO, "Cache does not exist, configuration is forced to be read.");
			requestConfiguration();
			try {
				configNumber = requestConfigurationChanges();
				dlmsCache.saveObjectList(getMeterConfig().getInstantiatedObjectList());
				dlmsCache.setConfProgChange(configNumber);
			} catch (IOException e) {
				e.printStackTrace();
				configNumber = -1;
			}
		}
	}

	/**
	 * Fill in all the parameters from the cached object. NOTE: do NOT mix this with the CAPTURED_OBJECTS
	 */
	protected void setCachedObjects() {
		getMeterConfig().setInstantiatedObjectList(this.dlmsCache.getObjectList());
	}

	/**
	 * Read the number of configuration changes in the meter
	 * 
	 * @return
	 * @throws IOException
	 */
	private int requestConfigurationChanges() throws IOException {
		try {
			return (int) getCosemObjectFactory().getCosemObject(getMeterConfig().getConfigObject().getObisCode()).getValue();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not retrieve the configuration change parameter" + e);
		}
	}

	/**
	 * Request all the configuration parameters out of the meter.
	 */
	private void requestConfiguration() {

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
	 * @return a Calendar object from the lastReading of the given channel, if the date is NULL, a date from one month ago is created at midnight.
	 */
	public Calendar getFromCalendar(Channel channel) {
		Date lastReading = channel.getLastReading();
		if (lastReading == null) {
			lastReading = com.energyict.genericprotocolimpl.common.ParseUtils.getClearLastMonthDate(channel.getRtu());
		}
		Calendar cal = ProtocolUtils.getCleanCalendar(getTimeZone());
		cal.setTime(lastReading);
		return cal;
	}

	/**
	 * @param rtu
	 * @return a Calendar object from the lastLogReading of the given channel, if the date is NULL, a date from one month ago is created at midnight
	 */
	public Calendar getFromLogCalendar(Rtu rtu) {
		Date lastLogReading = rtu.getLastLogbook();
		if (lastLogReading == null) {
			lastLogReading = com.energyict.genericprotocolimpl.common.ParseUtils.getClearLastMonthDate(rtu);
		}
		Calendar cal = ProtocolUtils.getCleanCalendar(getTimeZone());
		cal.setTime(lastLogReading);
		return cal;
	}

	public Calendar getToCalendar() {
		return ProtocolUtils.getCalendar(getTimeZone());
	}

	private void verifyAndWriteClock() throws IOException {
		try {
			Date meterTime = getTime();
			Date now = Calendar.getInstance(getTimeZone()).getTime();

			this.timeDifference = Math.abs(now.getTime() - meterTime.getTime());
			long diff = this.timeDifference / 1000;

			log(Level.INFO, "Difference between metertime(" + meterTime + ") and systemtime(" + now + ") is " + diff + "s.");
			if (this.commProfile.getWriteClock()) {
				if ((diff < this.commProfile.getMaximumClockDifference()) && (diff > this.commProfile.getMinimumClockDifference())) {
					log(Level.INFO, "Metertime will be set to systemtime: " + now);
					setClock(now);
				} else if (getMarkedAsBadTime()) {
					log(Level.INFO, "Metertime will not be set, timeDifference is to large.");
				}
			} else {
				log(Level.INFO, "WriteClock is disabled, metertime will not be set.");
			}

		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}

	}

	public void forceClock(Date currentTime) throws IOException {
		try {
			// getCosemObjectFactory().getClock().setTimeAttr(new DateTime(currentTime));
			getCosemObjectFactory().getClock().setAXDRDateTimeAttr(new AXDRDateTime(getRoundTripCorrected(currentTime)));
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not force to set the Clock object.");
		}
	}

	public Date getTime() throws IOException {
		try {
			Date meterTime;
			this.deviceClock = getCosemObjectFactory().getClock(ObisCode.fromString("0.0.1.0.0.255"));
			meterTime = deviceClock.getDateTime();
			return meterTime;
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not retrieve the Clock object.");
		}
	}

	public void setClock(Date time) throws IOException {
		try {
			// getCosemObjectFactory().getClock().setTimeAttr(new DateTime(time));
			// getCosemObjectFactory().getClock().setAXDRDateTimeAttr(new AXDRDateTime(time));
			getCosemObjectFactory().getClock().setAXDRDateTimeAttr(new AXDRDateTime(getRoundTripCorrected(time)));
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not set the Clock object.");
		}
	}

	private Date getRoundTripCorrected(Date time) {
		Calendar cal = Calendar.getInstance(getTimeZone());
		cal.setTime(time);
		cal.add(Calendar.MILLISECOND, getRoundTripCorrection());
		return cal.getTime();
	}

	private Clock getDeviceClock() {
		return this.deviceClock;
	}

	/**
	 * @return the current webRtu
	 */
	public Rtu getMeter() {
		return this.webRtuKP;
	}
	
	private int getValidMbusDevices(){
		int count = 0;
		for(int i = 0; i < maxMbusDevices; i++){
			if(this.mbusDevices[i] != null){
				count++;
			}
		}
		return count;
	}
	
	public void discoverMbusDevices() throws SQLException, BusinessException{
		
		// get a MbusDeviceMap 
		HashMap<String, Integer> mbusMap = getMbusMapper();
		
		// check if the current mbus slaves are still on the meter disappeared
		checkForDisappearedMbusMeters(mbusMap);
		// check if all the mbus devices are configured in EIServer
		checkToUpdateMbusMeters(mbusMap);
	}
	
	private void checkForDisappearedMbusMeters(HashMap<String, Integer> mbusMap){

		List<Rtu> mbusSlaves = getMeter().getDownstreamRtus();
		Iterator<Rtu> it = mbusSlaves.iterator();
		while(it.hasNext()){
			Rtu mbus = it.next();
			Class device = null;
			try {
				device = Class.forName(mbus.getRtuType().getShadow().getCommunicationProtocolShadow().getJavaClassName());
				if((device != null) && (device.newInstance() instanceof MbusDevice)){		// we check to see if it's an Mbus device and no TIC device
					if(!mbusMap.containsKey(mbus.getSerialNumber())){
						getLogger().log(Level.INFO, "MbusDevice " + mbus.getSerialNumber() + " is not installed on the physical device.");
						ghostMbusDevices.put(mbus.getSerialNumber(), mbusMap.get(mbus.getSerialNumber()));
					}
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				// should never come here because if the rtuType has the className, then you should be able to create a class for it...
			} catch (InstantiationException e) {
				e.printStackTrace();
				getLogger().log(Level.INFO, "Could not check if the mbusDevice " + mbus.getSerialNumber() + " exists.");
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				getLogger().log(Level.INFO, "Could not check if the mbusDevice " + mbus.getSerialNumber() + " exists.");
			}
		}
		
	}
	
	/**
	 * 
	 * Check the ghostMbusDevices and create the mbusDevices
	 * @param mbusMap
	 * @throws BusinessException 
	 * @throws SQLException 
	 */
	private void checkToUpdateMbusMeters(HashMap<String, Integer> mbusMap) throws SQLException, BusinessException{
		Iterator<Entry<String, Integer>>  mbusIt = mbusMap.entrySet().iterator();
		int count = 0;
		while(mbusIt.hasNext()){
			Map.Entry<String, Integer> entry = mbusIt.next();
			if(!ghostMbusDevices.containsKey(entry.getKey())){ // ghostMeters don't need to be read because they are not on the meter anymore
				Rtu mbus = findOrCreateMbusDevice(entry.getKey());
				if(mbus != null){
					this.mbusDevices[count++] = new MbusDevice(entry.getKey(), entry.getValue(), mbus, getLogger());
				}
			}
		}
	}
	
	private Rtu findOrCreateMbusDevice(String key) throws SQLException, BusinessException {
		List<Rtu> mbusList = mw().getRtuFactory().findBySerialNumber(key);
		if(mbusList.size() == 1){
			mbusList.get(0).updateGateway(getMeter());
			return mbusList.get(0);
		} else if(mbusList.size() > 1){
			getLogger().log(Level.SEVERE, "Multiple meters where found with serial: " + key + ". Meter will not be handled.");
			return null;
		}
		
        RtuType rtuType = getRtuType();
        if (rtuType == null){
        	return null;
        }
        else{
        	return createMeter(rtuType, key);
        }
	}

	private Rtu createMeter(RtuType rtuType, String key) throws SQLException, BusinessException {
        RtuShadow shadow = rtuType.newRtuShadow();
        
//        Date lastreading = shadow.getLastReading();
        
    	shadow.setName(key);
        shadow.setSerialNumber(key);

        String folderExtName = getProperty("FolderExtName");
    	if(folderExtName != null){
    		Folder result = mw().getFolderFactory().findByExternalName(folderExtName);
    		if(result != null){
    			shadow.setFolderId(result.getId());
    		} else {
    			getLogger().log(Level.INFO, "No folder found with external name: " + folderExtName + ", new meter will be placed in prototype folder.");
    		}
    	} else {
    		getLogger().log(Level.INFO, "New meter will be placed in prototype folder.");
    	}    
    	
    	shadow.setGatewayId(getMeter().getId());
//    	shadow.setLastReading(lastreading);
        return mw().getRtuFactory().create(shadow);	
	}

	private RtuType getRtuType(){
    	String type = getProperty("RtuType");
    	if (Utils.isNull(type)) {
    		getLogger().warning("No automatic meter creation: no property RtuType defined.");
    		return null;
    	}
    	else {
           RtuType rtuType = mw().getRtuTypeFactory().find(type);
           if (rtuType == null){
        	   getLogger().log(Level.INFO, "No rtutype defined with name '" + type + "'");
        	   return null;
           } else if (rtuType.getPrototypeRtu() == null){
        	   getLogger().log(Level.INFO, "Rtutype '" + type + "' has not prototype rtu");
        	   return null;
           }
           return rtuType;
        }
	}
	
    private String getProperty(String key){
        return (String)properties.get(key);
    }
	
	private HashMap<String, Integer> getMbusMapper(){
		String mbusSerial;
		HashMap<String, Integer> mbusMap = new HashMap<String, Integer>();
		for (int i = 0; i < this.maxMbusDevices; i++) {
			mbusSerial = "";
			try {
				mbusSerial = getCosemObjectFactory().getGenericRead(getMeterConfig().getMbusSerialNumber(i)).getString();
				if(!mbusSerial.equalsIgnoreCase("")){
					mbusMap.put(mbusSerial, i);
				}
			} catch (IOException e) {
				e.printStackTrace(); // catch and go to next
				log(Level.INFO, "Could not retrieve the mbusSerialNumber for channel " + (i + 1));
			}
		}
		return mbusMap;
	}

	/**
	 * @deprecated after implementing the autodiscovery you have to use two methods:
	 * - first 'discoverMbusDevices()'
	 * - then 'getValidMbusDevices()'
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 * @throws IOException
	 */
	private boolean hasMBusMeters() throws SQLException, BusinessException, IOException {

		String serialMbus = "";
		Rtu mbus;
		List<Rtu> slaves = getMeter().getDownstreamRtus();
		Iterator<Rtu> it = slaves.iterator();
		int count = 0;
		int mbusChannel;
		while (it.hasNext()) {
			mbusChannel = -1;
			mbus = it.next();
			Class device = null;
			
			try {
				device = Class.forName(mbus.getRtuType().getShadow().getCommunicationProtocolShadow().getJavaClassName());
				if((device != null) && (device.newInstance() instanceof MbusDevice)){
					serialMbus = mbus.getSerialNumber();
					mbusChannel = checkSerialForMbusChannel(serialMbus);
					// this.mbusDevices[count++] = new MbusDevice(serialMbus, mbus, getLogger());
					if (mbusChannel != -1) {
						this.mbusDevices[count++] = new MbusDevice(serialMbus, mbusChannel, mbus, getLogger());
					} else {
						getLogger().log(Level.INFO, "Mbusmeter with serialnumber " + serialMbus + " is not found on E-meter " + this.serialNumber);
					}
				} // else it should be a Tic device
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				// should never come here because if the rtuType has the className, then you should be able to create a class for it...
			} catch (InstantiationException e) {
				e.printStackTrace();
				getLogger().log(Level.INFO, "Could not check if the mbusDevice " + serialMbus + " exists.");
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				getLogger().log(Level.INFO, "Could not check if the mbusDevice " + serialMbus + " exists.");
			}
			
		}

		for (int i = 0; i < this.maxMbusDevices; i++) {
			if (mbusDevice(i) != null) {
				if (isValidMbusMeter(i)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Method to check which mbusSerialnumber is on which channel ...
	 * 
	 * @param serialMbus
	 * @return the channel corresponding with the serialnumber
	 */
	private int checkSerialForMbusChannel(String serialMbus) {
		String slaveSerial = "";
		for (int i = 0; i < this.maxMbusDevices; i++) {
			try {
				slaveSerial = getCosemObjectFactory().getGenericRead(getMeterConfig().getMbusSerialNumber(i)).getString();
				if (slaveSerial.equalsIgnoreCase(serialMbus)) {
					return i;
				}
			} catch (IOException e) {
				e.printStackTrace(); // catch and go to next
				log(Level.INFO, "Could not retrieve the mbusSerialNumber for channel " + (i + 1));
			}
		}
		return -1;
	}

	/** Short notation for MeteringWarehouse.getCurrent() */
	public MeteringWarehouse mw() {
		return MeteringWarehouse.getCurrent();
	}

	private boolean isValidMbusMeter(int i) {
		return mbusDevice(i).isValid();
	}

	private MbusDevice mbusDevice(int i) {
		return this.mbusDevices[i];
	}
	
	/**
	 * Return the value of the serialNumber property from the RTU
	 * @return
	 */
	public String getSerialNumberValue(){
		return this.serialNumber;
	}

	public void validateProperties() throws MissingPropertyException {
		Iterator<String> iterator = getRequiredKeys().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			if (properties.getProperty(key) == null)
				throw new MissingPropertyException(key + " key missing");
		}

		if (getMeter() != null && getMeter().getDeviceId() != "") {
			this.deviceId = getMeter().getDeviceId();
		} else {
			this.deviceId = "!";
		}
		if (getMeter() != null && getMeter().getPassword() != "") {
			this.password = getMeter().getPassword();
		} else {
			this.password = "";
		}
		if (getMeter() != null && getMeter().getSerialNumber() != "") {
			this.serialNumber = getMeter().getSerialNumber();
		} else {
			this.serialNumber = "";
		}

		/* the format of the securityLevel is changed, now authenticationSecurityLevel and dataTransportSecurityLevel are in one*/
		String securityLevel = properties.getProperty("SecurityLevel", "0");
		if(securityLevel.indexOf(":") != -1){
			this.authenticationSecurityLevel = Integer.parseInt(securityLevel.substring(0, securityLevel.indexOf(":")));
			this.datatransportSecurityLevel = Integer.parseInt(securityLevel.substring(securityLevel.indexOf(":")+1));
		} else {
			this.authenticationSecurityLevel = Integer.parseInt(securityLevel);
			this.datatransportSecurityLevel = 0;
		}
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
        this.roundTripCorrection = Long.parseLong(properties.getProperty("RoundTripCorrection","0"));
        
        this.iiapInvokeId = Integer.parseInt(properties.getProperty("IIAPInvokeId", "0"));
        this.iiapPriority = Integer.parseInt(properties.getProperty("IIAPPriority", "1"));
        this.iiapServiceClass = Integer.parseInt(properties.getProperty("IIAPServiceClass", "1"));
        
        this.wakeup = Integer.parseInt(properties.getProperty("WakeUp", "0"));
	}

	public void addProperties(Properties properties) {
		this.properties = properties;
	}

	public String getVersion() {
		return "$Date$";
	}

	public List<String> getOptionalKeys() {
		List<String> result = new ArrayList<String>(30);
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
		result.add("IIAPInvokeId");
		result.add("IIAPPriority");
		result.add("IIAPServiceClass");
		result.add("WakeUp");
		result.add("RoundTripCorrection");
		result.add("FolderExtName");
		result.add("DataTransportKey");
		result.add("MasterKey");
		return result;
	}

	public List<String> getRequiredKeys() {
		List<String> result = new ArrayList<String>();
		return result;
	}

	public DLMSConnection getDLMSConnection() {
		return dlmsConnection;
	}

	protected void setDLMSConnection(DLMSConnection connection) {
		this.dlmsConnection = connection;
	}

	public Logger getLogger() {
		return this.logger;
	}

	public void setLink(Link link) {
		this.link = link;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public void setEnforceSerialNumber(boolean enforce) {
		this.enforceSerialNumber = enforce;
	}

	public void log(Level level, String msg) {
		getLogger().log(level, msg);
	}

	public DLMSMeterConfig getMeterConfig() {
		return this.dlmsMeterConfig;
	}

	protected void setMeterConfig(DLMSMeterConfig meterConfig) {
		this.dlmsMeterConfig = meterConfig;
	}

	public int getReference() {
		return 0;
	}

	public int getRoundTripCorrection() {
		return (int) this.roundTripCorrection;
	}

	public StoredValues getStoredValues() {
		return null;
	}

	public TimeZone getTimeZone() {
		return getMeter().getDeviceTimeZone();
	}

	public TimeZone getMeterTimeZone() throws IOException {
		if (getDeviceClock() != null) {
			return TimeZone.getTimeZone(Integer.toString(getDeviceClock().getTimeZone()));
		} else {
			getTime(); // dummy to get the device DLMS clock
			return TimeZone.getTimeZone(Integer.toString(getDeviceClock().getTimeZone()));
		}
	}

	public boolean isRequestTimeZone() {
		return (this.requestTimeZone == 1) ? true : false;
	}

	public CosemObjectFactory getCosemObjectFactory() {
		return cosemObjectFactory;
	}

	protected void setCosemObjectFactory(CosemObjectFactory cof) {
		this.cosemObjectFactory = cof;
	}

	public StoreObject getStoreObject() {
		return this.storeObject;
	}

	protected void setStoreObject(StoreObject storeObject) {
		this.storeObject = storeObject;
	}

	/**
	 * Messages
	 * 
	 * @throws SQLException
	 * @throws BusinessException
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

	public int getConnectionMode() {
		return this.connectionMode;
	}

	public static void main(String args[]) {
		WebRTUKP wkp = new WebRTUKP();

		// try {
		// Utilities.createEnvironment();
		// MeteringWarehouse.createBatchContext(false);
		// MeteringWarehouse mw = MeteringWarehouse.getCurrent();
		// // CommunicationScheduler cs = mw.getCommunicationSchedulerFactory().find(8139);
		// CommunicationScheduler cs = mw.getCommunicationSchedulerFactory().find(8158);
		// Rtu rtu = cs.getRtu();
		// wkp.webRtuKP = rtu;
		// wkp.scheduler = cs;
		// wkp.scheduler.getCommunicationProfile().getShadow();
		// try {
		// wkp.doReadRegisters();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// RtuMessageShadow rms = new RtuMessageShadow();
		// rms.setContents("<Test_Message Test_File='460'> </Test_Message>");
		// rms.setRtuId(17492);
		//			
		// wkp.logger = Logger.getAnonymousLogger();
		//			
		// // wkp.handleMessage(wkp.mw().getRtuMessageFactory().create(rms));
		// } catch (BusinessException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (SQLException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// try {
		// AXDRDateTime axdrDateTime = wkp.convertUnixToGMTDateTime("1236761593", TimeZone.getTimeZone("GMT"));
		// System.out.println(axdrDateTime.getValue().getTime());
		// System.out.println(wkp.getFirstDate(axdrDateTime.getValue().getTime(), "day", TimeZone.getTimeZone("GMT")));
		// System.out.println(axdrDateTime.getValue().get(Calendar.HOUR_OF_DAY));
		// System.out.println(axdrDateTime.getValue().getTimeZone().getRawOffset()/3600000);
		// System.out.println(axdrDateTime.getValue().getTimeZone().getOffset(Long.parseLong("1236761593")*1000)/3600000);
		//			
		// axdrDateTime = wkp.convertUnixToGMTDateTime("1236761593", TimeZone.getTimeZone("Europe/Brussels"));
		// System.out.println(axdrDateTime.getValue().getTime());
		// System.out.println(wkp.getFirstDate(axdrDateTime.getValue().getTime(), "day", TimeZone.getTimeZone("Europe/Brussels")));
		// System.out.println(axdrDateTime.getValue().get(Calendar.HOUR_OF_DAY));
		// System.out.println(axdrDateTime.getValue().getTimeZone().getRawOffset()/3600000);
		// System.out.println(axdrDateTime.getValue().getTimeZone().getOffset(Long.parseLong("1236761593")*1000)/3600000);
		//			
		// axdrDateTime = wkp.convertUnixToGMTDateTime("1234947193", TimeZone.getTimeZone("GMT"));
		// System.out.println(axdrDateTime.getValue().getTime());
		// System.out.println(wkp.getFirstDate(axdrDateTime.getValue().getTime(), "day", TimeZone.getTimeZone("GMT")));
		// System.out.println(axdrDateTime.getValue().get(Calendar.HOUR_OF_DAY));
		// System.out.println(axdrDateTime.getValue().getTimeZone().getRawOffset()/3600000);
		// System.out.println(axdrDateTime.getValue().getTimeZone().getOffset(Long.parseLong("1234947193")*1000)/3600000);
		//			
		// axdrDateTime = wkp.convertUnixToGMTDateTime("1234947193", TimeZone.getTimeZone("Europe/Brussels"));
		// System.out.println(axdrDateTime.getValue().getTime());
		// System.out.println(wkp.getFirstDate(axdrDateTime.getValue().getTime(), "day", TimeZone.getTimeZone("Europe/Brussels")));
		// System.out.println(axdrDateTime.getValue().get(Calendar.HOUR_OF_DAY));
		// System.out.println(axdrDateTime.getValue().getTimeZone().getRawOffset()/3600000);
		// System.out.println(axdrDateTime.getValue().getTimeZone().getOffset(Long.parseLong("1234947193")*1000)/3600000);
		//			
		// Date nextDate = wkp.getFirstDate(axdrDateTime.getValue().getTime(), "month", TimeZone.getTimeZone("Europe/Brussels"));
		// int days = 0;
		// while(days < 60){
		//				
		// if(days == 36){
		// System.out.println("timeout");
		// }
		//				
		// System.out.println(nextDate);
		// nextDate = wkp.setBeforeNextInterval(nextDate, "month", TimeZone.getTimeZone("Europe/Brussels"));
		// days++;
		// }
		//			
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

//		String comm = "612aa109060760857405080101a203020100a305a103020100be11040f080100065f1f0400007c1f04000007";
//		String mvie = "6141A109060760857405080101A203020100A305A10302010E88020780890760857405080205AA0A8008503677524A323146BE10040E0800065F1F040000501F01F40007";
//
//		byte[] bComm = DLMSUtils.hexStringToByteArray(comm);
//		byte[] bmVie = DLMSUtils.hexStringToByteArray(mvie);
//
//		for (int i = 0; i < ((bComm.length > bmVie.length) ? bmVie.length : bComm.length); i++) {
//			if (bComm[i] != bmVie[i])
//				System.out.println("Difference at: " + i + "; Comm: " + bComm[i] + "(" + comm.charAt(i * 2) + comm.charAt(i * 2 + 1) + ") - MVie: " + bmVie[i] + "(" + mvie.charAt(i * 2)
//						+ mvie.charAt(i * 2 + 1) + ")");
//		}
	}

	public boolean isReadDaily() {
		return readDaily;
	}

	public boolean isReadMonthly() {
		return readMonthly;
	}

	protected void setRtu(Rtu rtu) {
		this.webRtuKP = rtu;
	}

	protected void setCommunicationScheduler(CommunicationScheduler communicationScheduler) {
		this.scheduler = communicationScheduler;
		this.commProfile = this.scheduler.getCommunicationProfile();
	}

	public boolean getMarkedAsBadTime() {
		return badTime;
	}

	/** EIServer 7.5 Cache mechanism, only the DLMSCache is in that database, the 8.x has a EISDEVICECACHE ... */

	public void setCache(Object cacheObject) {
		this.dlmsCache = (DLMSCache) cacheObject;
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
			} catch (NotFoundException e) {
				return new DLMSCache(null, -1);
			}
		} else
			throw new com.energyict.cbo.BusinessException("invalid RtuId!");
	}

	public void updateCache(int rtuid, Object cacheObject) throws java.sql.SQLException, com.energyict.cbo.BusinessException {
		if (rtuid != 0) {
			DLMSCache dc = (DLMSCache) cacheObject;
			if (dc.isChanged()) {
				RtuDLMSCache rtuCache = new RtuDLMSCache(rtuid);
				RtuDLMS rtu = new RtuDLMS(rtuid);
				rtuCache.saveObjectList(dc.getObjectList());
				rtu.setConfProgChange(dc.getConfProgChange());
			}
		} else
			throw new com.energyict.cbo.BusinessException("invalid RtuId!");
	}

}
