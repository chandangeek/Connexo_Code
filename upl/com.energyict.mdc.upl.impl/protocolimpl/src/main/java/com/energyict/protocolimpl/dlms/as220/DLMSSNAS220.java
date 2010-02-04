/**
 * @version  2.0
 * @author   Koenraad Vanderschaeve
 * <P>
 * <B>Description :</B><BR>
 * Base class that implements the DLMS SN (short name) protocol
 * <BR>
 * <B>@beginchanges</B><BR>
 *      KV 08042003 Initial version.<BR>
 *      KV 08102003 Save dstFlag when getTime() to be used in setTime()
 *      KV 14072004 DLMSMeterConfig made multithreaded! singleton pattern implementation removed!
 *      KV 20082004 Extended with obiscode mapping for register reading + start reengineering to use cosem package
 *      KV 30082004 Reengineered to use cosem package
 *@endchanges
 */


package com.energyict.protocolimpl.dlms.as220;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import com.energyict.cbo.NotFoundException;
import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.CosemPDUConnection;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DLMSObis;
import com.energyict.dlms.LLCConnection;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.SecureConnection;
import com.energyict.dlms.TCPIPConnection;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.aso.AssociationControlServiceElement;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.aso.XdlmsAse;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.cosem.*;
import com.energyict.genericprotocolimpl.common.LocalSecurityProvider;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.dlms.*;
import com.energyict.protocolimpl.dlms.siemenszmd.StoredValuesImpl;

abstract public class DLMSSNAS220 implements MeterProtocol, HHUEnabler, ProtocolLink, CacheMechanism {

	private static final String			SERIAL_NUMBER_PREFIX		= "35";

	private static final int			MAX_PDU_SIZE				= 1200;
	private static final int			PROPOSED_QOS				= -1;
	private static final int			PROPOSED_DLMS_VERSION		= 6;

	private static final int			SEC_PER_MIN					= 60;

	private static final int			CONNECTION_MODE_HDLC		= 0;
	private static final int			CONNECTION_MODE_TCPIP		= 1;
	private static final int			CONNECTION_MODE_COSEM_PDU	= 2;
	private static final int			CONNECTION_MODE_LLC			= 3;

	private static final ObisCode	ENERGY_PROFILE_OBISCODE	= ObisCode.fromString("1.1.99.1.0.255");

	private boolean debug = false;

    private DLMSCache dlmsCache=new DLMSCache();

    private String strID;
    private String strPassword;

    private int iTimeoutProperty;
    private int iProtocolRetriesProperty;
    private int iRequestTimeZone;
    private int iRoundtripCorrection;
    private int iClientMacAddress;
    private int iServerUpperMacAddress;
    private int iServerLowerMacAddress;
    private int transparentConnectTime;
    private int transparentDatabits;
    private int transparentStopbits;
    private int transparentParity;
    private int transparentBaudrate;
    private int iRequestClockObject;
    private String nodeId;
    private String serialNumber;
    private int iInterval=-1;
    private int iNROfIntervals=-1;
    private int extendedLogging;
    private ProtocolChannelMap channelMap;

    private int authenticationSecurityLevel;
	private int	datatransportSecurityLevel;

    private DLMSConnection dlmsConnection = null;
    private SecurityContext securityContext = null;
    private ApplicationServiceObject aso = null;

    private CosemObjectFactory cosemObjectFactory=null;
    private StoredValuesImpl storedValuesImpl=null;

    // lazy initializing
    private int iNumberOfChannels=-1;
    private int iMeterTimeZoneOffset=255;
    private int iConfigProgramChange=-1;

    private DLMSMeterConfig meterConfig = DLMSMeterConfig.getInstance();

    // Added for MeterProtocol interface implementation
    private Logger logger=null;
    private TimeZone timeZone=null;

    // filled in when getTime is invoked!
    private int dstFlag; // -1=unknown, 0=not set, 1=set
    private int addressingMode;
    private int connectionMode;

	private Properties properties;

    /**
	 *  Creates a new instance of DLMSSNAS220, empty constructor
     */
    public DLMSSNAS220() {

    }

	/**
	 * Getter for the manufacturer deviceId.
	 * In our case, this is Elster so the ID = 'GEC'
	 * @return the deviceId
	 */
	public String getDeviceID() {
        return "GEC";
    }

    public DLMSConnection getDLMSConnection() {
        return dlmsConnection;
    }

    /** initializes the receiver
     * @param inputStream <br>
     * @param outputStream <br>
     * @param tz <br>
     * @param log <br>
     */
	public void init(InputStream inputStream, OutputStream outputStream, TimeZone tz, Logger log) throws IOException {
        this.timeZone = tz;
        this.logger = log;

        setDstFlag(-1);
        iNumberOfChannels = -1;
        iMeterTimeZoneOffset = 255;
        iConfigProgramChange = -1;

        cosemObjectFactory = new CosemObjectFactory(this);
        storedValuesImpl = new StoredValuesImpl(cosemObjectFactory);

        initDLMSConnection(inputStream, outputStream);

        iInterval=-1;
        iNROfIntervals=-1;

    }

	/**
	 * @param inputStream
	 * @param outputStream
	 * @throws ConnectionException
	 * @throws IOException
	 */
	private void initDLMSConnection(InputStream inputStream, OutputStream outputStream) throws ConnectionException, IOException {

		DLMSConnection connection;

		try {
            switch (connectionMode) {
				case CONNECTION_MODE_HDLC:
					connection = new HDLCConnection(inputStream,outputStream,iTimeoutProperty,100,iProtocolRetriesProperty,iClientMacAddress,iServerLowerMacAddress,iServerUpperMacAddress,addressingMode);
					break;
				case CONNECTION_MODE_TCPIP:
					connection = new TCPIPConnection(inputStream,outputStream,iTimeoutProperty,100,iProtocolRetriesProperty,iClientMacAddress,iServerLowerMacAddress);
					break;
				case CONNECTION_MODE_COSEM_PDU:
					connection = new CosemPDUConnection(inputStream,outputStream,iTimeoutProperty,100,iProtocolRetriesProperty,iClientMacAddress,iServerLowerMacAddress);
					break;
				case CONNECTION_MODE_LLC:
					connection = new LLCConnection(inputStream,outputStream,iTimeoutProperty,200,iProtocolRetriesProperty,iClientMacAddress,iServerLowerMacAddress);
					break;
				default:
					throw new IOException("Unable to initialize dlmsConnection, connection property unknown: " + connectionMode);
			}
		} catch (DLMSConnectionException e) {
			throw new IOException(e.getMessage());
        }

		byte[] dedicatedKey = new byte[0x10];
		for (byte i = 0; i < dedicatedKey.length; i++) {
			dedicatedKey[i] = (byte) (i+1);
		}

		LocalSecurityProvider localSecurityProvider = new LocalSecurityProvider(this.properties);
		securityContext = new SecurityContext(datatransportSecurityLevel, authenticationSecurityLevel, 0, serialNumber.getBytes(), localSecurityProvider);
		ConformanceBlock cb = new ConformanceBlock(ConformanceBlock.DEFAULT_SN_CONFORMANCE_BLOCK);
		XdlmsAse xdlmsAse = new XdlmsAse(localSecurityProvider.getDedicatedKey(), true, PROPOSED_QOS, PROPOSED_DLMS_VERSION, cb, MAX_PDU_SIZE);
		aso = new ApplicationServiceObject(xdlmsAse, this, securityContext, AssociationControlServiceElement.SHORT_NAME_REFERENCING_WITH_CIPHERING);
		dlmsConnection = new SecureConnection(aso, connection);
	}

	public void connect() throws IOException {
		try {
			getDLMSConnection().connectMAC();
			aso.createAssociation();
			checkCache();
			validateSerialNumber();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		} catch (DLMSConnectionException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}

    public int getProfileInterval() throws UnsupportedException, IOException {
        if (iInterval == -1) {
           iInterval = getCosemObjectFactory().getLoadProfile().getProfileGeneric().getCapturePeriod();
        }
        return iInterval;
    }

	public int getNumberOfChannels() throws IOException {
		if (iNumberOfChannels == -1) {
            meterConfig.setCapturedObjectList(getCosemObjectFactory().getLoadProfile().getProfileGeneric().getCaptureObjectsAsUniversalObjects());
			iNumberOfChannels = getCosemObjectFactory().getLoadProfile().getProfileGeneric().getNumberOfProfileChannels();
		}
		return iNumberOfChannels;
	}

    public Quantity getMeterReading(String name) throws UnsupportedException, IOException {
        throw new UnsupportedException();
    }

    public Quantity getMeterReading(int channelId) throws UnsupportedException, IOException {
        throw new UnsupportedException();
    }

	/**
	 * @throws IOException
	 */
	private void checkCache() throws IOException {
        System.out.println("cache="+dlmsCache.getObjectList()+", confchange="+dlmsCache.getConfProgChange()+", ischanged="+dlmsCache.isChanged());
		try { // conf program change and object list stuff
		    int iConf;
		    if (dlmsCache.getObjectList() != null) {
		        meterConfig.setInstantiatedObjectList(dlmsCache.getObjectList());
		        try {
		            iConf = requestConfigurationProgramChanges();
		        }
		        catch(IOException e) {
		            iConf=-1;
		            logger.severe("DLMSSNAS220 Configuration change count not accessible, request object list.");
		            requestObjectList();
		            dlmsCache.saveObjectList(meterConfig.getInstantiatedObjectList());  // save object list in cache
		        }

		        if (iConf != dlmsCache.getConfProgChange()) {
		            logger.severe("DLMSSNAS220 Configuration changed, request object list.");
		            requestObjectList();           // request object list again from rtu
		            dlmsCache.saveObjectList(meterConfig.getInstantiatedObjectList());  // save object list in cache
		            dlmsCache.setConfProgChange(iConf);  // set new configuration program change
		        }
		    }
		    else { // Cache not exist
		        logger.info("DLMSSNAS220 Cache does not exist, request object list.");
		        requestObjectList();
		        try {
		            iConf = requestConfigurationProgramChanges();
		            dlmsCache.saveObjectList(meterConfig.getInstantiatedObjectList());  // save object list in cache
		            dlmsCache.setConfProgChange(iConf);  // set new configuration program change
		        }
		        catch(IOException e) {
		            iConf=-1;
		        }
		    }

		    if (extendedLogging >= 1) {
				logger.info(getRegistersInfo());
			}

		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("connect() error, " + e.getMessage());
		}
	}

    /*
     *  extendedLogging = 1 current set of logical addresses, extendedLogging = 2..17 historical set 1..16
     */
    protected String getRegistersInfo() throws IOException {
        StringBuffer strBuff = new StringBuffer();

        Iterator it;

        // all total and rate values...
        strBuff.append("********************* All instantiated objects in the meter *********************\n");
        for (int i=0;i<getMeterConfig().getInstantiatedObjectList().length;i++) {
            UniversalObject uo = getMeterConfig().getInstantiatedObjectList()[i];
            strBuff.append(uo.getObisCode().toString()+" "+uo.getObisCode().getDescription()+"\n");
        }

        if (getDeviceID().compareTo("EIT")!=0) {
            // all billing points values...
            strBuff.append("********************* Objects captured into billing points *********************\n");
            it = getCosemObjectFactory().getStoredValues().getProfileGeneric().getCaptureObjects().iterator();
            while(it.hasNext()) {
                CapturedObject capturedObject = (CapturedObject)it.next();
                strBuff.append(capturedObject.getLogicalName().getObisCode().toString()+" "+capturedObject.getLogicalName().getObisCode().getDescription()+" (billing point)\n");
            }
        }

        strBuff.append("********************* Objects captured into load profile *********************\n");
        it = getCosemObjectFactory().getLoadProfile().getProfileGeneric().getCaptureObjects().iterator();
        while(it.hasNext()) {
            CapturedObject capturedObject = (CapturedObject)it.next();
            strBuff.append(capturedObject.getLogicalName().getObisCode().toString()+" "+capturedObject.getLogicalName().getObisCode().getDescription()+" (load profile)\n");
        }

        strBuff.append("************************** Custom registers **************************\n");
        it = RegisterDescription.INFO.keySet().iterator();
        while(it.hasNext()) {
        	ObisCode oc = ObisCode.fromString((String) it.next());
        }


        return strBuff.toString();
    }

    /**
     * This method initiates the MAC disconnect for the HDLC layer.
     * @exception IOException
     */
    public void disconnect() throws IOException {
        try {
            if (getDLMSConnection() != null) {
    			if ((aso != null) && (aso.getAssociationStatus() != ApplicationServiceObject.ASSOCIATION_DISCONNECTED)) {
                	//aso.releaseAssociation(); //FIXME: check RLRQ
    			}
				getDLMSConnection().disconnectMAC();
			}
		} catch (DLMSConnectionException e) {
            logger.severe("DLMSSNAS220AS220: disconnect(), "+e.getMessage());
            //throw new IOException(e.getMessage());
        }

    }

    private boolean requestDaylightSavingEnabled() throws IOException {
       return getCosemObjectFactory().getClock().isDsEnabled();
    }

	/**
	 * This method requests for the COSEM object list in the remote meter. A
	 * list is byuild with LN and SN references.
	 * This method must be executed before other request methods.
	 *
	 * @exception IOException
	 */
    private void requestObjectList() throws IOException {
        meterConfig.setInstantiatedObjectList(getCosemObjectFactory().getAssociationSN().getBuffer());
    }

    /**
     * This method requests for the COSEM object SAP.
     * @exception IOException
     */
	public void requestSAP() throws IOException {
		String devID = (String) getCosemObjectFactory().getSAPAssignment().getLogicalDeviceNames().get(0);
		if ((strID != null) && ("".compareTo(strID) != 0)) {
			if (strID.compareTo(devID) != 0) {
				throw new IOException("DLMSSNAS220, requestSAP, Wrong DeviceID!, settings=" + strID + ", meter=" + devID);
			}
		}
	}

	/**
	 * Check if the serial number property is used, and compare it with the
	 * serial number of the device. When there's no serial number entered in the
	 * protocol properties, then no check is performed.
	 *
	 * @throws IOException when the serial numbers didn't match
	 */
	private void validateSerialNumber() throws IOException {
		if ((serialNumber != null) && ("".compareTo(serialNumber) != 0)) {
			String sn = getSerialNumber();
			if ((sn == null) || (sn.compareTo(serialNumber) != 0)) {
				throw new IOException("SerialNumber mismatch! meter sn=" + sn + ", configured sn=" + serialNumber);
			}
		}
	}

	/**
	 * This method requests for the COSEM object Logical Name register.
	 *
	 * @exception IOException
	 */
    private String requestAttribute(int iBaseName,int iOffset) throws IOException {
        return getCosemObjectFactory().getGenericRead(iBaseName,iOffset).getDataContainer().toString();
    }

	/**
	 * Read the serialNumber from the device
	 *
	 * @return the serial number from the device as {@link String}
	 * @throws IOException
	 */
	private String getSerialNumber() throws IOException {
		UniversalObject uo = getMeterConfig().getSerialNumberObject();
		byte[] responsedata = getCosemObjectFactory().getGenericRead(uo.getBaseName(), uo.getValueAttributeOffset()).getResponseData();
		return SERIAL_NUMBER_PREFIX + AXDRDecoder.decode(responsedata).getOctetString().stringValue();
	}

	/**
	 * This method requests for the NR of intervals that can be stored in the
	 * memory of the remote meter.
	 *
	 * @return NR of intervals that can be stored in the memory of the remote meter.
	 * @exception IOException
	 */
    private int getNROfIntervals() throws IOException {
        if (iNROfIntervals == -1) {
            iNROfIntervals = getCosemObjectFactory().getLoadProfile().getProfileGeneric().getProfileEntries();
        }
        return iNROfIntervals;
    }


	public ProfileData getProfileData(boolean includeEvents) throws IOException {
		Calendar fromCalendar = ProtocolUtils.getCalendar(getTimeZone());
		fromCalendar.add(Calendar.MINUTE, (-1) * getNROfIntervals() * (getProfileInterval() / SEC_PER_MIN));
		return getProfileData(fromCalendar.getTime(), includeEvents);
	}

	public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
		return getProfileData(lastReading, new Date(), includeEvents);
	}

	public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
		Calendar fromCalendar = ProtocolUtils.getCleanCalendar(getTimeZone());
		fromCalendar.setTime(from);
		Calendar toCalendar = ProtocolUtils.getCleanCalendar(getTimeZone());
		toCalendar.setTime(to);
		return doGetDemandValues(fromCalendar, toCalendar, includeEvents);
	}

    /**
     * Read the profile dta from the device
     *
     * @param fromCalendar
     * @param toCalendar
     * @param includeEvents
     * @return the {@link ProfileData}
     * @throws IOException
     */
    private ProfileData doGetDemandValues(Calendar fromCalendar,Calendar toCalendar, boolean includeEvents) throws IOException {
        ProfileBuilder profileBuilder = new ProfileBuilder(this);
		ProfileData profileData = new ProfileData();
		ScalerUnit[] scalerunit = profileBuilder.buildScalerUnits((byte) getNumberOfChannels());

        List<ChannelInfo> channelInfos = profileBuilder.buildChannelInfos(scalerunit);
        profileData.setChannelInfos(channelInfos);

        // decode the compact array here and convert to a universallist...
        ProfileGeneric pg = getCosemObjectFactory().getProfileGeneric(ENERGY_PROFILE_OBISCODE);
		LoadProfileCompactArray loadProfileCompactArray = new LoadProfileCompactArray();
		loadProfileCompactArray.parse(pg.getBufferData(fromCalendar, toCalendar));
		List<LoadProfileCompactArrayEntry> loadProfileCompactArrayEntries = loadProfileCompactArray.getLoadProfileCompactArrayEntries();

        List<IntervalData> intervalDatas = profileBuilder.buildIntervalData(scalerunit,loadProfileCompactArrayEntries);
        profileData.setIntervalDatas(intervalDatas);

        if (includeEvents) {
			EventLogs eventLogs = new EventLogs(this);
			List<MeterEvent> meterEvents = eventLogs.getEventLog(fromCalendar, toCalendar);
			profileData.setMeterEvents(meterEvents);
			profileData.applyEvents(getProfileInterval() / SEC_PER_MIN);
        }

        profileData.sort();
        return profileData;

    } // private ProfileData doGetDemandValues(Calendar fromCalendar,Calendar toCalendar, byte bNROfChannels) throws IOException

    /** this implementation calls <code> validateProperties </code>
     * and assigns the argument to the properties field
     * @param properties <br>
     * @throws MissingPropertyException <br>
     * @throws InvalidPropertyException <br>
     * @see AbstractMeterProtocol#validateProperties
     */
    public void setProperties(Properties properties) throws MissingPropertyException , InvalidPropertyException {
        validateProperties(properties);
        this.properties = properties;
    }

    /** <p>validates the properties.</p><p>
     * The default implementation checks that all required parameters are present.
     * </p>
     * @param properties <br>
     * @throws MissingPropertyException <br>
     * @throws InvalidPropertyException <br>
     */
	private void validateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
		try {

			Iterator<String> iterator = getRequiredKeys().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				if (properties.getProperty(key) == null) {
					throw new MissingPropertyException(key + " key missing");
				}
			}

			nodeId = properties.getProperty(MeterProtocol.NODEID, "");
			serialNumber = properties.getProperty(MeterProtocol.SERIALNUMBER);
			extendedLogging = Integer.parseInt(properties.getProperty("ExtendedLogging", "0"));
			addressingMode = Integer.parseInt(properties.getProperty("AddressingMode", "-1"));
			connectionMode = Integer.parseInt(properties.getProperty("Connection", "0")); // 0=HDLC, 1= TCP/IP, 2=cosemPDUconnection 3=LLCConnection

			if (properties.getProperty("ChannelMap", "").equalsIgnoreCase("")) {
				channelMap = null;
			} else {
				channelMap = new ProtocolChannelMap(properties.getProperty("ChannelMap"));
			}

			strID = properties.getProperty(MeterProtocol.ADDRESS);
			if ((strID != null) && (strID.length() > 16)) {
				throw new InvalidPropertyException("ID must be less or equal then 16 characters.");
			}

			strPassword = properties.getProperty(MeterProtocol.PASSWORD, "00000000");
			iTimeoutProperty = Integer.parseInt(properties.getProperty("Timeout", "10000").trim());
			iProtocolRetriesProperty = Integer.parseInt(properties.getProperty("Retries", "5").trim());
			iRequestTimeZone = Integer.parseInt(properties.getProperty("RequestTimeZone", "0").trim());
			iRequestClockObject = Integer.parseInt(properties.getProperty("RequestClockObject", "0").trim());
			setRoundtripCorrection(Integer.parseInt(properties.getProperty("RoundtripCorrection", "0").trim()));

			String[] securityLevel = properties.getProperty("SecurityLevel", "0:" + SecurityContext.SECURITYPOLICY_NONE).split(":");
			this.authenticationSecurityLevel = Integer.parseInt(securityLevel[0]);
			if (securityLevel.length == 2) {
				this.datatransportSecurityLevel = Integer.parseInt(securityLevel[1]);
			} else if (securityLevel.length == 1) {
				this.datatransportSecurityLevel = SecurityContext.SECURITYPOLICY_NONE;
			} else {
				throw new IllegalArgumentException("SecurityLevel property contains an illegal value " + properties.getProperty("SecurityLevel", "0"));
			}

			iClientMacAddress = Integer.parseInt(properties.getProperty("ClientMacAddress", "32").trim());
			iServerUpperMacAddress = Integer.parseInt(properties.getProperty("ServerUpperMacAddress", "1").trim());
			iServerLowerMacAddress = Integer.parseInt(properties.getProperty("ServerLowerMacAddress", "0").trim());
			transparentConnectTime = Integer.parseInt(properties.getProperty("TransparentConnectTime", "10"));
			transparentBaudrate = Integer.parseInt(properties.getProperty("TransparentBaudrate", "9600"));
			transparentDatabits = Integer.parseInt(properties.getProperty("TransparentDatabits", "8"));
			transparentStopbits = Integer.parseInt(properties.getProperty("TransparentStopbits", "1"));
			transparentParity = Integer.parseInt(properties.getProperty("TransparentParity", "0"));

		} catch (NumberFormatException e) {
			throw new InvalidPropertyException(" validateProperties, NumberFormatException, " + e.getMessage());
		}

	}

    /** this implementation throws UnsupportedException. Subclasses may override
     * @param name <br>
     * @return the register value
     * @throws IOException <br>
     * @throws UnsupportedException <br>
     * @throws NoSuchRegisterException <br>
     */
	public String getRegister(String name) throws IOException, UnsupportedException, NoSuchRegisterException {
		DLMSObis ln = new DLMSObis(name);
		if (ln.isLogicalName()) {
			String str = requestAttribute(meterConfig.getObject(ln).getBaseName(), (short) ((ln.getOffset() - 1) * 8));
			return str;
		} else if (name.compareTo("PROGRAM_CONF_CHANGES") == 0) {
			return String.valueOf(requestConfigurationProgramChanges());
		} else if (name.compareTo("GET_CLOCK_OBJECT") == 0) {
			requestClockObject();
			return null;
		} else {
			throw new NoSuchRegisterException("DLMS,getRegister, register " + name + " does not exist.");
		}
	}

    /** this implementation throws UnsupportedException. Subclasses may override
     * @param name <br>
     * @param value <br>
     * @throws IOException <br>
     * @throws NoSuchRegisterException <br>
     * @throws UnsupportedException <br>
     */
    public void setRegister(String name, String value) throws IOException, NoSuchRegisterException, UnsupportedException {
        throw new UnsupportedException();
    }

    /** this implementation throws UnsupportedException. Subclasses may override
     * @throws IOException <br>
     * @throws UnsupportedException <br>
     */
    public void initializeDevice() throws IOException, UnsupportedException {
        throw new UnsupportedException();
    }

    /** the implementation returns both the address and password key
     * @return a list of strings
     */
    public List<String> getRequiredKeys() {
        List<String> result = new ArrayList<String>();
        return result;
    }

    /** this implementation returns an empty list
     * @return a list of strings
     */
    public List<String> getOptionalKeys() {
        List<String> result = new ArrayList<String>();
        result.add("Timeout");
        result.add("Retries");
        result.add("DelayAfterFail");
        result.add("RequestTimeZone");
        result.add("RequestClockObject");
        result.add("SecurityLevel");
        result.add("ClientMacAddress");
        result.add("ServerUpperMacAddress");
        result.add("ServerLowerMacAddress");
        result.add("ExtendedLogging");
        result.add("AddressingMode");
        result.add("EventIdIndex");
        result.add("ChannelMap");
        result.add("Connection");
        result.add(LocalSecurityProvider.DATATRANSPORT_AUTHENTICATIONKEY);  //TODO: check these optional keys
        result.add(LocalSecurityProvider.DATATRANSPORTKEY);
        result.add(LocalSecurityProvider.MASTERKEY);
        result.add("TransparentConnectTime");
        result.add("TransparentBaudrate");
        result.add("TransparentDatabits");
        result.add("TransparentStopbits");
        result.add("TransparentParity");
        return result;
    }

    private void requestClockObject() {
        if (iRequestClockObject == 1) {
            try{logger.severe("DLMSSNAS220 Clock time                       : "+getTime());}catch(IOException e){logger.severe("time attribute error");}
            //try{logger.severe ("DLMSSNAS220 Clock time_zone                  : "+requestTimeZone());}catch(IOException e){logger.severe ("time_zone attribute error");}
            try{logger.severe("DLMSSNAS220 Clock time_zone                  : "+requestAttributeLong(meterConfig.getClockSN(),DLMSCOSEMGlobals.TIME_TIME_ZONE));}catch(IOException e){logger.severe("time_zone attribute error");}
            try{logger.severe("DLMSSNAS220 Clock status                     : "+requestAttributeLong(meterConfig.getClockSN(),DLMSCOSEMGlobals.TIME_STATUS));}catch(IOException e){logger.severe("status attribute error");}
            try{logger.severe("DLMSSNAS220 Clock daylight_savings_begin     : "+requestAttributeString(meterConfig.getClockSN(),DLMSCOSEMGlobals.TIME_DS_BEGIN));}catch(IOException e){logger.severe("DS begin attribute error");}
            try{logger.severe("DLMSSNAS220 Clock daylight_savings_end       : "+requestAttributeString(meterConfig.getClockSN(),DLMSCOSEMGlobals.TIME_DS_END));}catch(IOException e){logger.severe("DS end attribute error");}
            try{logger.severe("DLMSSNAS220 Clock daylight_savings_deviation : "+requestAttributeLong(meterConfig.getClockSN(),DLMSCOSEMGlobals.TIME_DS_DEVIATION));}catch(IOException e){logger.severe("DS deviation attribute error");}
            try{logger.severe("DLMSSNAS220 Clock daylight_saving_enabled    : "+requestDaylightSavingEnabled());}catch(IOException e){logger.severe("DS enebled attribute error");}
        }
    }

    public int requestConfigurationProgramChanges() throws IOException {
        if (iConfigProgramChange == -1) {
			iConfigProgramChange = (int)getCosemObjectFactory().getCosemObject(getMeterConfig().getConfigObject().getObisCode()).getValue();
		}
        return iConfigProgramChange;
    }

    public int requestTimeZone() throws IOException {
        if (iMeterTimeZoneOffset == 255) {
			iMeterTimeZoneOffset = getCosemObjectFactory().getClock().getTimeZone();
		}
        return iMeterTimeZoneOffset;
    }

    private long requestAttributeLong(int iBaseName,int iOffset) throws IOException {
        return getCosemObjectFactory().getGenericRead(iBaseName,iOffset).getValue();
    }

    private String requestAttributeString(int iBaseName,int iOffset) throws IOException {
        return getCosemObjectFactory().getGenericRead(iBaseName,iOffset).toString();
    }

    public boolean isRequestTimeZone() {
        return (iRequestTimeZone != 0);
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

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
        } else {
			throw new com.energyict.cbo.BusinessException("invalid RtuId!");
		}
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
        } else {
			throw new com.energyict.cbo.BusinessException("invalid RtuId!");
		}
    }

    public String getFileName() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR)+"_"+(calendar.get(Calendar.MONTH)+1)+"_"+calendar.get(Calendar.DAY_OF_MONTH)+"_"+strID+"_"+strPassword+"_"+serialNumber+"_"+iServerUpperMacAddress+"_DLMSSNAS220.cache";
    }

    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel,false);
    }

    public void enableHHUSignOn(SerialCommunicationChannel commChannel,boolean datareadout) throws ConnectionException {
    	HHUSignOn hhuSignOn = new AS220TransparentConnection(commChannel, transparentConnectTime, transparentBaudrate, transparentDatabits, transparentStopbits,
    			transparentParity, authenticationSecurityLevel, strPassword);
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(datareadout);
        getDLMSConnection().setHHUSignOn(hhuSignOn,nodeId);
    }
    public byte[] getHHUDataReadout() {
        return getDLMSConnection().getHhuSignOn().getDataReadout();
    }

    public void release() throws IOException {

    }

    public DLMSMeterConfig getMeterConfig() {
        return meterConfig;
    }

    public Logger getLogger() {
        return logger;
    }

    /**
     * Getter for property cosemObjectFactory.
     * @return Value of property cosemObjectFactory.
     */
    public com.energyict.dlms.cosem.CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }

    public int getReference() {
        return ProtocolLink.SN_REFERENCE;
    }

    public StoredValues getStoredValues() {
        return storedValuesImpl;
    }

    public ProtocolChannelMap getChannelMap() {
		return channelMap;
	}

    public boolean isDebug() {
		return debug;
	}

    public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setDstFlag(int dstFlag) {
		this.dstFlag = dstFlag;
	}

	/**
	 * @return the dstFlag
	 */
	public int getDstFlag() {
		return dstFlag;
	}

	/**
	 * @param iRoundtripCorrection the iRoundtripCorrection to set
	 */
	public void setRoundtripCorrection(int iRoundtripCorrection) {
		this.iRoundtripCorrection = iRoundtripCorrection;
	}

	/**
	 * @return the iRoundtripCorrection
	 */
	public int getRoundTripCorrection() {
		return iRoundtripCorrection;
	}

}

