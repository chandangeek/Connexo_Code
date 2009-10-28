package com.energyict.genericprotocolimpl.common;

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

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.NotFoundException;
import com.energyict.dialer.core.Link;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
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
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.genericprotocolimpl.common.messages.GenericMessaging;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.amr.RtuRegisterGroup;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.CommunicationProfile;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.Rtu;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.DLMSCache;
import com.energyict.protocolimpl.dlms.HDLC2Connection;
import com.energyict.protocolimpl.dlms.RtuDLMS;
import com.energyict.protocolimpl.dlms.RtuDLMSCache;

/**
 * Abstract class for implementation of a Generic DLMS protocol.
 * 
 * @author gna
 *
 */
public abstract class DLMSProtocol extends GenericMessaging implements GenericProtocol, ProtocolLink{

	/** The {@link ConformanceBlock} used */
	private ConformanceBlock conformanceBlock;
	
	/** The {@link XdlmsAse} used */
	private XdlmsAse xdlmsAse;
	
	/** The {@link InvokeIdAndPriority} used */
	private InvokeIdAndPriority invokeIdAndPriority;
	
	/** The {@link CosemObjectFactory} used */
	protected CosemObjectFactory cosemObjectFactory;
	
	/** The {@link DLMSConnection} used */
	protected DLMSConnection dlmsConnection;
	
	/** The {@link DLMSMeterConfig} used */
	private DLMSMeterConfig dlmsMeterConfig;
	
	/** The {@link ApplicationServiceObject} used */
	protected ApplicationServiceObject aso;
	
	/** The {@link SecurityProvider} used for DLMS communication */
	private SecurityProvider securityProvider;
	
	/** The {@link Logger} provided by the ComServer */
	protected Logger logger;
	
	/** The {@link Properties} of the current RTU */
	protected Properties properties;
	
	/** The {@link DLMSCache} of the current RTU */
	private DLMSCache dlmsCache;
	
	/** The current {@link Rtu} */
	protected Rtu meter;
	
	/** The used {@link CommunicationScheduler} */
	protected CommunicationScheduler communicationScheduler;
	
	/** The used {@link Link} */
	protected Link link;
	
	/** The timeDifference between the system and the meter */
	protected long timeDifference;
	
	/** The devices DLMS clock object */
	protected Clock deviceClock;
	
	
	/* Properties */
	protected int connectionMode;
	protected int datatransportSecurityLevel;
	protected int authenticationSecurityLevel;
	protected int iiapPriority;
	protected int iiapServiceClass;
	protected int iiapInvokeId;
	protected int clientMacAddress;
	protected int serverUpperMacAddress;
	protected int serverLowerMacAddress;
	protected int timeOut;
	protected int forceDelay;
	protected int retries;
	protected int addressingMode;
	protected int informationFieldSize;
	protected int roundTripCorrection;
	protected String manufacturer;
	
	/**
	 * Handle the protocol tasks
	 * 
	 * @param link - the ComServer link
	 * @throws BusinessException
	 * @throws SQLException
	 * @throws IOException
	 */
	abstract protected void doExecute() throws BusinessException, SQLException, IOException;
	
	/**
	 * Configure the {@link ConformanceBlock} which is used for the DLMS association.
	 * @return the conformanceBlock, if null is returned then depending on the reference,
	 *  the default value({@link ConformanceBlock#DEFAULT_LN_CONFORMANCE_BLOCK} or {@link ConformanceBlock#DEFAULT_SN_CONFORMANCE_BLOCK}) will be used
	 */
	abstract protected ConformanceBlock configureConformanceBlock();
	
	/**
	 * Configure the {@link XdlmsAse} which is used for the DLMS association.
	 * @return the xdlmsAse, if null is returned then the default values will be used
	 */
	abstract protected XdlmsAse configureXdlmsAse();
	
	/**
	 * Configure the {@link InvokeIdAndPriority} bitString which is used during DLMS communication.
	 * @return the invokeIdAndPriority bitString, if null is returned then the default value({@link DLMSProtocol#buildDefaultInvokeIdAndPriority()}) will be used
	 */
	abstract protected InvokeIdAndPriority configureInvokeIdAndPriority();
	
	/**
	 * Define a list of REQUIRED properties, other then the ones configure in {@link #getRequiredKeys()}.
	 * These properties can be used specifically for the protocol
	 * @return the properties list
	 */
	abstract protected List<String> doGetRequiredKeys();
	
	/**
	 * Define a list of OPTIONAL properties, other then the ones configured in {@linkplain #getOptionalKeys()}.
	 * These properties can be used specifically for the protocol 
	 * @return the properties list
	 */
	abstract protected List<String> doGetOptionalKeys();
	
	/**
	 * Configuration of the protocol specific Required and Optional properties
	 */
	abstract protected void doValidateProperties();
	
	/** 
	 * Build the logic to provide the desired {@link SecurityProvider}.
	 * If no securityProvider should be used, then return null and the default({@link LocalSecurityProvider}) will be used.
	 * */
	abstract protected SecurityProvider getSecurityProvider();
	
	/**
	 * Implement functionality right AFTER the DLMS association has been established
	 * @throws IOException 
	 */
	abstract protected void doConnect() throws IOException;
	
	/**
	 * Implement functionality right BEFORE the DLMS association is released
	 */
	abstract protected void doDisconnect();
	
	/**
	 * Implement functionality right AFTER the initializeGlobals method
	 * @throws IOException 
	 * @throws BusinessException 
	 * @throws SQLException 
	 */
	abstract protected void doInit() throws SQLException, BusinessException, IOException;
	
	/**
	 * Read the register using your custom define ObisCodeMapper
	 * @param obisCode - the obisCode from the register to read
	 * @return the read RegisterValue
	 * @throws IOException
	 */
	abstract protected RegisterValue readRegister(ObisCode obisCode) throws IOException;

	/* (non-Javadoc)
	 * @see com.energyict.mdw.amr.GenericProtocol#execute(com.energyict.mdw.core.CommunicationScheduler, com.energyict.dialer.core.Link, java.util.logging.Logger)
	 */
	public void execute(CommunicationScheduler scheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {
		
		try {
			
			this.meter = scheduler.getRtu();
			this.communicationScheduler = scheduler; 
			
			validateProperties();
			configureDLMSProperties();
			
			initializeGlobals(link.getInputStream(), link.getOutputStream(), logger);
			
			connect();
			
			doExecute();
			
		} catch (DLMSConnectionException e) {
			e.printStackTrace();
			disconnect();
		} finally{
			disconnect();
			if (getMeter() != null) {
				updateCache(getMeter().getId(), dlmsCache);
			}
		}
		
	}
	
	/**
	 * Handle all DLMS property related methods
	 * 
	 * @throws DLMSConnectionException if some of the invokeIdAndPriority bits aren't valid
	 */
	protected void configureDLMSProperties() throws DLMSConnectionException{
		
		this.conformanceBlock = configureConformanceBlock();
		if(this.conformanceBlock == null){
			if(getReference() == ProtocolLink.SN_REFERENCE){
				this.conformanceBlock = new ConformanceBlock(ConformanceBlock.DEFAULT_SN_CONFORMANCE_BLOCK);
			} else if(getReference() == ProtocolLink.LN_REFERENCE){
				this.conformanceBlock = new ConformanceBlock(ConformanceBlock.DEFAULT_LN_CONFORMANCE_BLOCK);
			} else {
				throw new IllegalArgumentException("Invalid reference method, only 0 and 1 are allowed.");
			}
		}
		
		this.xdlmsAse = configureXdlmsAse();
		if(this.xdlmsAse == null){
			this.xdlmsAse = new XdlmsAse(null, true, -1, 6, this.conformanceBlock, 1200);
		}
		
		this.invokeIdAndPriority = configureInvokeIdAndPriority();
		if(this.invokeIdAndPriority == null){
			this.invokeIdAndPriority = buildDefaultInvokeIdAndPriority();
		}
		
		this.securityProvider = getSecurityProvider();
		if(this.securityProvider == null){
			this.securityProvider = new LocalSecurityProvider(this.properties);
		}
		
	}
	
	/**
	 * Provide all properties with there configured value
	 * 
	 * @throws MissingPropertyException if one of the required keys is missing in {@link #properties}
	 */
	protected void validateProperties() throws MissingPropertyException{
		
		Iterator<String> iterator = getRequiredKeys().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			if (this.properties.getProperty(key) == null) {
				throw new MissingPropertyException(key + " key missing");
			}
		}
		
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
        
        this.timeOut = Integer.parseInt(properties.getProperty("Timeout", (this.connectionMode==0)?"5000":"60000"));	// set the HDLC timeout to 5000 for the WebRTU KP
        
        this.forceDelay = Integer.parseInt(properties.getProperty("ForceDelay", "1"));
        
        this.retries = Integer.parseInt(properties.getProperty("Retries", "3"));
        
        this.addressingMode = Integer.parseInt(properties.getProperty("AddressingMode", "2"));
        
        this.manufacturer = properties.getProperty("Manufacturer", "WKP");
        
        this.informationFieldSize = Integer.parseInt(properties.getProperty("InformationFieldSize","-1"));
        
        this.roundTripCorrection = Integer.parseInt(properties.getProperty("RoundTripCorrection","0"));
        
        doValidateProperties();
	}
	
	/**
	 * Initialize global variables
	 * 
	 * @throws IOException
	 * @throws DLMSConnectionException if addressingMode is unknown
	 * @throws BusinessException if multiple records were found for the current Rtu ID
	 * @throws SQLException if a database access error occurs
	 */
	protected void initializeGlobals(InputStream ip, OutputStream os, Logger logger) throws IOException, DLMSConnectionException, SQLException, BusinessException{
		setLogger(logger);
		this.cosemObjectFactory	= new CosemObjectFactory((ProtocolLink)this);
		
		SecurityContext sc = new SecurityContext(this.datatransportSecurityLevel, this.authenticationSecurityLevel, 0, this.securityProvider);
		
		this.aso = new ApplicationServiceObject(this.xdlmsAse, this, sc, getContextId());

		this.dlmsConnection = new SecureConnection(this.aso, defineTransportDLMSConnection(ip, os));

		this.dlmsConnection.setInvokeIdAndPriority(this.invokeIdAndPriority);
		this.dlmsConnection.setIskraWrapper(1);
		
		this.dlmsMeterConfig = DLMSMeterConfig.getInstance(this.manufacturer);
	
		if (getMeter() != null) {
			setCache(fetchCache(getMeter().getId()));
		}
		doInit();
	}
	
	/**
	 * @return the current RTU
	 */
	public Rtu getMeter(){
		return this.meter;
	}
	
	/**
	 * Define the contextID of the associationServiceObject.
	 * Depending on the reference(see {@link ProtocolLink#LN_REFERENCE} and {@link ProtocolLink#SN_REFERENCE}, the value can be different.
	 * 
	 * @return the contextId
	 */
	protected int getContextId(){
		if(getReference() == ProtocolLink.LN_REFERENCE){
			return (this.datatransportSecurityLevel == 0)?AssociationControlServiceElement.LOGICAL_NAME_REFERENCING_NO_CIPHERING:
				AssociationControlServiceElement.LOGICAL_NAME_REFERENCING_WITH_CIPHERING;
		} else if( getReference() == ProtocolLink.SN_REFERENCE){
			return (this.datatransportSecurityLevel == 0)?AssociationControlServiceElement.SHORT_NAME_REFERENCING_NO_CIPHERING:
				AssociationControlServiceElement.SHORT_NAME_REFERENCING_WITH_CIPHERING;
		} else {
			throw new IllegalArgumentException("Invalid reference method, only 0 and 1 are allowed.");
		}
	}
	
	/**
	 * Configure the DLMSConnection which is used for dataTransportation
	 * 
	 * @param is the inputStream from the Link
	 * @param os the outputStream from the Link
	 * 
	 * @return the newly defined DLMSConnection
	 * @throws DLMSConnectionException if addressingMode is unknown
	 * @throws IOException if connectionMode is unknown
	 */
	protected DLMSConnection defineTransportDLMSConnection(InputStream is, OutputStream os) throws DLMSConnectionException, IOException{
		DLMSConnection transportConnection;
		if (this.connectionMode == 0) {
			transportConnection = new HDLC2Connection(is, os, this.timeOut, this.forceDelay, this.retries, this.clientMacAddress,
					this.serverLowerMacAddress, this.serverUpperMacAddress, this.addressingMode, this.informationFieldSize, 5);
		} else if (this.connectionMode == 1) {
			transportConnection = new TCPIPConnection(is, os, this.timeOut, this.forceDelay, this.retries, this.clientMacAddress,
					this.serverLowerMacAddress);
		} else {
			throw new IOException("Unknown connectionMode: " + this.connectionMode + " - Only 0(HDLC) and 1(TCP) are allowed");
		}
		return transportConnection;
	}
	
	/**
	 * Make a connection to the physical device.
	 * Setup the association and check the objectList
	 * 
	 * @throws IOException if errors occurred during data fetching
	 * @throws DLMSConnectionException if errors occurred during connection setup
	 */
	protected void connect() throws IOException, DLMSConnectionException{
		if(this.aso.getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_DISCONNECTED){
			getDLMSConnection().connectMAC();
			this.aso.createAssociation();
			
			// objectList
			checkCacheObjects();
			
			doConnect();
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
			getMeterConfig().setInstantiatedObjectList(this.dlmsCache.getObjectList());

			this.logger.info("Checking the configuration parameters.");
			configNumber = requestConfigurationChanges();
	
			if (dlmsCache.getConfProgChange() != configNumber) {
				this.logger.info("Meter configuration has changed, configuration is forced to be read.");
				requestConfiguration();
				dlmsCache.saveObjectList(getMeterConfig().getInstantiatedObjectList());
				dlmsCache.setConfProgChange(configNumber);
			}

		} else { // cache does not exist
			this.logger.info("Cache does not exist, configuration is forced to be read.");
			requestConfiguration();
			configNumber = requestConfigurationChanges();
				dlmsCache.saveObjectList(getMeterConfig().getInstantiatedObjectList());
				dlmsCache.setConfProgChange(configNumber);
		}
	}
	
	/**
	 * Read the number of configuration changes in the meter
	 * The number should increase if something in the configuration or firmware changed. This can cause the objectlist to change.
	 * 
	 * @return the number of configuration changes.
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
	 * Request Association buffer list out of the meter.
	 * 
	 * @throws IOException if something fails during the request or the parsing of the buffer
	 */
	private void requestConfiguration() throws IOException {

		try {
			if(getReference() == ProtocolLink.LN_REFERENCE){
				getMeterConfig().setInstantiatedObjectList(getCosemObjectFactory().getAssociationLN().getBuffer());
			} else if(getReference() == ProtocolLink.SN_REFERENCE){
				getMeterConfig().setInstantiatedObjectList(getCosemObjectFactory().getAssociationSN().getBuffer());
			} else {
				throw new IllegalArgumentException("Invalid reference method, only 0 and 1 are allowed.");
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Requesting configuration failed." + e.getCause());
		}
	}
	
	/**
	 * Disconnect the DLMS meter and release the association
	 */
	protected void disconnect(){
		try {
			if(this.aso.getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_CONNECTED){
				doDisconnect();
				this.aso.releaseAssociation();
			}
			getDLMSConnection().disconnectMAC();
		} catch (IOException e) {
			//absorb -> trying to close communication
			e.printStackTrace();
		} catch (DLMSConnectionException e) {
			//absorb -> trying to close communication
			e.printStackTrace();
		}
	}
	
	/**
	 * <pre>
	 * Create a default InvokeIdAndPriority BitString:
	 *  - priority = normal
	 *  - serviceClass = confirmed
	 *  - invokeId = 1
	 *  </pre>
	 * @return the default invokeIdAndPriority BitString
	 * @throws DLMSConnectionException if some of the properties aren't valid
	 */
	protected InvokeIdAndPriority buildDefaultInvokeIdAndPriority() throws DLMSConnectionException{
		InvokeIdAndPriority iiap = new InvokeIdAndPriority();
		iiap.setPriority(0);
		iiap.setServiceClass(1);
		iiap.setTheInvokeId(1);
		return iiap;
	}
	
	/**
	 * Creates an InvokeIdAndPriority object using the defined properties
	 * @return the constructed object
	 * @throws DLMSConnectionException if some of the properties aren't valid
	 */
	protected InvokeIdAndPriority buildInvokeIdAndPriority() throws DLMSConnectionException {
		InvokeIdAndPriority iiap = new InvokeIdAndPriority();
		iiap.setPriority(this.iiapPriority);
		iiap.setServiceClass(this.iiapServiceClass);
		iiap.setTheInvokeId(this.iiapInvokeId);
		return iiap;
	}

	/* (non-Javadoc)
	 * @see com.energyict.dlms.ProtocolLink#getDLMSConnection()
	 */
	public DLMSConnection getDLMSConnection() {
		return this.dlmsConnection;
	}


	/* (non-Javadoc)
	 * @see com.energyict.dlms.ProtocolLink#getLogger()
	 */
	public Logger getLogger() {
		return this.logger;
	}

	/**
	 * Setter for logger
	 * @param logger - to logger to set
	 */
	protected void setLogger(Logger logger){
		this.logger = logger;
	}

	/* (non-Javadoc)
	 * @see com.energyict.dlms.ProtocolLink#getMeterConfig()
	 */
	public DLMSMeterConfig getMeterConfig() {
		return this.dlmsMeterConfig;
	}
	
	/**
	 * @return the current {@link CosemObjectFactory}
	 */
	public CosemObjectFactory getCosemObjectFactory(){
		return this.cosemObjectFactory;
	}

	/* (non-Javadoc)
	 * @see com.energyict.cbo.ConfigurationSupport#getOptionalKeys()
	 */
	public List<String> getOptionalKeys() {
		List<String> optionalKeys = new ArrayList<String>();
		optionalKeys.add("ForceDelay");
		optionalKeys.add("TimeOut");
		optionalKeys.add("Retries");
		optionalKeys.add("Connection");
		optionalKeys.add("SecurityLevel");
		optionalKeys.add("ClientMacAddress");
		optionalKeys.add("ServerUpperMacAddress");
		optionalKeys.add("ServerLowerMacAddress");
		optionalKeys.add("InformationFieldSize");
		optionalKeys.add("LoadProfileId");
		optionalKeys.add("AddressingMode");
		optionalKeys.add("MaxMbusDevices");
		optionalKeys.add("IIAPInvokeId");
		optionalKeys.add("IIAPPriority");
		optionalKeys.add("IIAPServiceClass");
		optionalKeys.add("Manufacturer");
		optionalKeys.add("InformationFieldSize");
		optionalKeys.add("RoundTripCorrection");
		List<String> protocolKeys = doGetOptionalKeys();
		if(protocolKeys != null){
			optionalKeys.addAll(protocolKeys);
		}
		return optionalKeys;
	}

	/* (non-Javadoc)
	 * @see com.energyict.cbo.ConfigurationSupport#getRequiredKeys()
	 */
	public List<String> getRequiredKeys() {
		if(doGetRequiredKeys() != null){
			return doGetOptionalKeys();
		}	
		return new ArrayList<String>();
	}

	/* (non-Javadoc)
	 * @see com.energyict.mdw.core.Pluggable#addProperties(java.util.Properties)
	 */
	public void addProperties(Properties properties) {
		this.properties = properties;
	}
	
	/** EIServer 7.5 Cache mechanism, only the DLMSCache is in that database, the 8.x has a EISDEVICECACHE ... */

	public void setCache(Object cacheObject) {
		this.dlmsCache = (DLMSCache) cacheObject;
	}

	public Object getCache() {
		return dlmsCache;
	}
	
	/**
	 * Check if the timeDifference exceeds the maximum.
	 * Depending on the result, all intervalvalues are marked as dirty
	 * @return true if timedifference exceeds maximum, false otherwise
	 * @throws IOException if timeDifference exceeds maximum and the flag collectOutSideBoundary isn't checked,
	 * or when reading the time failed
	 */
	protected boolean verifyMaxTimeDifference() throws IOException {
		Date systemTime = Calendar.getInstance().getTime();
		Date meterTime = getTime();

		this.timeDifference = Math.abs(meterTime.getTime() - systemTime.getTime());
		long diff = this.timeDifference; // in milliseconds
		if ((diff / 1000 > communicationScheduler.getCommunicationProfile().getMaximumClockDifference())) {

			String msg = "Time difference exceeds configured maximum: (" + (diff / 1000) + " s > " + communicationScheduler.getCommunicationProfile().getMaximumClockDifference() + " s )";

			getLogger().log(Level.SEVERE, msg);

			if (communicationScheduler.getCommunicationProfile().getCollectOutsideBoundary()) {
				// TODO should set the completion code to TIMEERROR, but that's not possible without changing the interface ...
				return true;
			} else {
				throw new IOException(msg);
			}
		}
		return false;
	}
	
	/**
	 * Fetch the meter's time
	 * @return
	 * @throws IOException
	 */
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

	/**
	 * Fetch the DLMS cache from the database
	 * @param rtuid - the RTU database id
	 * @return a DLMS cache object
	 * @throws java.sql.SQLException if a database access error occurs
	 * @throws com.energyict.cbo.BusinessException if multiple records were found
	 */
	public Object fetchCache(int rtuid) throws java.sql.SQLException, com.energyict.cbo.BusinessException {
		if (rtuid != 0) {
			RtuDLMSCache rtuCache = new RtuDLMSCache(rtuid);
			RtuDLMS rtu = new RtuDLMS(rtuid);
			try {
				return new DLMSCache(rtuCache.getObjectList(), rtu.getConfProgChange());
			} catch (NotFoundException e) {
				return new DLMSCache(null, -1);
			}
		} else {
			throw new com.energyict.cbo.BusinessException("invalid RtuId!");
		}
	}

	/**
	 * Write the DLMSCache back to the database
	 * @param rtuid - the RTU database id
	 * @param cacheObject - the DLMSCache
	 * @throws java.sql.SQLException if a database access error occurs
	 * @throws com.energyict.cbo.BusinessException if multiple records were found
	 */
	public void updateCache(int rtuid, Object cacheObject) throws java.sql.SQLException, com.energyict.cbo.BusinessException {
		if (rtuid != 0) {
			DLMSCache dc = (DLMSCache) cacheObject;
			if (dc.isChanged()) {
				RtuDLMSCache rtuCache = new RtuDLMSCache(rtuid);
				RtuDLMS rtu = new RtuDLMS(rtuid);
				rtuCache.saveObjectList(dc.getObjectList());
				rtu.setConfProgChange(dc.getConfProgChange());
			}
		} else {
			throw new com.energyict.cbo.BusinessException("invalid RtuId!");
		}
	}


	public long getTimeDifference() {
		return this.timeDifference;
	}

	/**
	 * @return the current CommunicationProfile
	 */
	protected CommunicationProfile getCommunicationProfile(){
		return communicationScheduler.getCommunicationProfile();
	}
	
	/**
	 * Check if a given ObisCode is in the objectList
	 * @param obisCode to check
	 * @return true if the list is null, or when the object is found. False if it's not found
	 */
	protected boolean doesObisCodeExistInObjectList(ObisCode obisCode) {
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
	 * Read all the register from the device
	 * @return a HashMap containing the RtuRegister and the RegisterValue
	 * @throws IOException 
	 */
	public HashMap<RtuRegister, RegisterValue> doReadRegisters() throws IOException {
		HashMap<RtuRegister, RegisterValue> regValueMap = new HashMap<RtuRegister, RegisterValue>();
		Iterator<RtuRegister> it = getMeter().getRegisters().iterator();
		List<RtuRegisterGroup> groups = getCommunicationProfile().getRtuRegisterGroups();
		ObisCode oc = null;
		RegisterValue rv = null;
		RtuRegister rr;
		while (it.hasNext()) {
			try {
				rr = it.next();
				if (CommonUtils.isInRegisterGroup(groups, rr)) {
					oc = rr.getRtuRegisterSpec().getObisCode();
					try {
						rv = readRegister(oc);

						rv.setRtuRegisterId(rr.getId());

						if (rr.getReadingAt(rv.getReadTime()) == null) {
							regValueMap.put(rr, rv);
						}
					} catch (NoSuchRegisterException e) {
						e.printStackTrace();
						getLogger().log(Level.INFO, "ObisCode " + oc + " is not supported by the meter.");
					}
				}
			} catch (IOException e) {
				// TODO if the connection is out you should not try and read the others as well...
				e.printStackTrace();
				getLogger().log(Level.INFO, "Reading register with obisCode " + oc + " FAILED.");
			}
		}
		return regValueMap;
	}
	
	/**
	 * Set the meter's clock to a certain time
	 * @param currentTime - the given time to set
	 * @throws IOException if forcing the clock failed
	 */
	public void setClock(Date currentTime) throws IOException {
		try {
			getCosemObjectFactory().getClock().setAXDRDateTimeAttr(new AXDRDateTime(currentTime));
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not set the Clock object.");
		}
	}
	
	/**
	 * Force the meter's clock to a certain time
	 * @param currentTime - the given time to set
	 * @throws IOException if forcing the clock failed
	 */
	public void forceClock(Date currentTime) throws IOException {
		try {
			setClock(currentTime);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not force to set the Clock object.");
		}
	}
	
	protected void verifyAndWriteClock() throws IOException {
		try {
			Date meterTime = getTime();
			Date now = Calendar.getInstance(getTimeZone()).getTime();

			this.timeDifference = Math.abs(now.getTime() - meterTime.getTime());
			long diff = this.timeDifference / 1000;

			logger.log(Level.INFO, "Difference between metertime(" + meterTime + ") and systemtime(" + now + ") is " + diff + "s.");
			if (getCommunicationProfile().getWriteClock()) {
				if ((diff < getCommunicationProfile().getMaximumClockDifference()) && (diff > getCommunicationProfile().getMinimumClockDifference())) {
					logger.log(Level.INFO, "Metertime will be set to systemtime: " + now);
					setClock(now);
				} else if (diff > getCommunicationProfile().getMaximumClockDifference()) {
					logger.log(Level.INFO, "Metertime will not be set, timeDifference is to large.");
				}
			} else {
				logger.log(Level.INFO, "WriteClock is disabled, metertime will not be set.");
			}

		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}

	}
	
	/**
	 * @return the meter's {@link TimeZone}
	 */
	public TimeZone getTimeZone() {
		return getMeter().getDeviceTimeZone();
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
	 * @return the current time
	 */
	public Calendar getToCalendar() {
		return ProtocolUtils.getCalendar(getTimeZone());
	}
}
