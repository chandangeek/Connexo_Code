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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.energyict.cbo.BusinessException;
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
import com.energyict.dlms.SecureConnection;
import com.energyict.dlms.TCPIPConnection;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.aso.AssociationControlServiceElement;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.aso.XdlmsAse;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.genericprotocolimpl.common.LocalSecurityProvider;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.CacheMechanism;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.messaging.FirmwareUpdateMessageBuilder;
import com.energyict.protocol.messaging.FirmwareUpdateMessaging;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.dlms.DLMSCache;
import com.energyict.protocolimpl.dlms.HDLC2Connection;
import com.energyict.protocolimpl.dlms.RtuDLMS;
import com.energyict.protocolimpl.dlms.RtuDLMSCache;
import com.energyict.protocolimpl.dlms.siemenszmd.StoredValuesImpl;

abstract public class DLMSSNAS220 implements MeterProtocol, HHUEnabler, ProtocolLink, CacheMechanism, FirmwareUpdateMessaging {

	private static final int			NR_OF_PLC_CHANNELS			= 0;
	private static final int			MAX_PDU_SIZE				= 200;
	private static final int			PROPOSED_QOS				= -1;
	private static final int			PROPOSED_DLMS_VERSION		= 6;

	private static final int			CONNECTION_MODE_HDLC		= 0;
	private static final int			CONNECTION_MODE_TCPIP		= 1;
	private static final int			CONNECTION_MODE_COSEM_PDU	= 2;
	private static final int			CONNECTION_MODE_LLC			= 3;


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
    private int extendedLogging;
    private ProtocolChannelMap channelMap;
    private int opticalBaudrate;
    private int profileType = 0;

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

	private int	iForcedDelay;

	/**
	 * Do some extra connect settings
	 * @throws BusinessException if no correct MBus device is found
	 */
	protected abstract void doConnect() throws BusinessException;
	
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

    public int getProfileType() {
		return profileType;
	}

    /** Initializes the receiver
     * 
	 * @param inputStream
	 * 			- the inputStream form the dialer
	 * 
	 * @param outputStream
	 * 			- the outputStream from the dialer
     * @param tz
     * 			- the {@link TimeZone} used
     * 
     * @param log
     * 			- the {@link Logger} used
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

    }

	/**
	 * Initialize DLMS specific objects
	 * 
	 * @param inputStream
	 * 			- the inputStream form the dialer
	 * 
	 * @param outputStream
	 * 			- the outputStream from the dialer
	 * 
	 * @throws ConnectionException if initializing the connection failed
	 * @throws IOException if initializing the connection failed of the connectionMode is invalid
	 */
	private void initDLMSConnection(InputStream inputStream, OutputStream outputStream) throws ConnectionException, IOException {

		DLMSConnection connection;

		try {
            switch (connectionMode) {
				case CONNECTION_MODE_HDLC:
				    connection = new HDLC2Connection(inputStream, outputStream, iTimeoutProperty, 100, iProtocolRetriesProperty, iClientMacAddress,
			iServerLowerMacAddress, iServerUpperMacAddress, addressingMode, -1, opticalBaudrate);
					break;
				case CONNECTION_MODE_TCPIP:
					connection = new TCPIPConnection(inputStream,outputStream,iTimeoutProperty,iForcedDelay,iProtocolRetriesProperty,iClientMacAddress,iServerLowerMacAddress);
					break;
				case CONNECTION_MODE_COSEM_PDU:
					connection = new CosemPDUConnection(inputStream,outputStream,iTimeoutProperty,iForcedDelay,iProtocolRetriesProperty,iClientMacAddress,iServerLowerMacAddress);
					break;
				case CONNECTION_MODE_LLC:
					connection = new LLCConnection(inputStream,outputStream,iTimeoutProperty,iForcedDelay,iProtocolRetriesProperty,iClientMacAddress,iServerLowerMacAddress);
					break;
				default:
					throw new IOException("Unable to initialize dlmsConnection, connection property unknown: " + connectionMode);
			}
		} catch (DLMSConnectionException e) {
			throw new IOException(e.getMessage());
        }

		LocalSecurityProvider localSecurityProvider = new LocalSecurityProvider(this.properties);
		securityContext = new SecurityContext(datatransportSecurityLevel, authenticationSecurityLevel, 0, getSystemIdentifier(), localSecurityProvider);
		ConformanceBlock cb = new ConformanceBlock(ConformanceBlock.DEFAULT_SN_CONFORMANCE_BLOCK);
		XdlmsAse xdlmsAse = new XdlmsAse(isCiphered() ? localSecurityProvider.getDedicatedKey() : null, true, PROPOSED_QOS, PROPOSED_DLMS_VERSION, cb, MAX_PDU_SIZE);
		aso = new ApplicationServiceObject(xdlmsAse, this, securityContext, getContextId());
		dlmsConnection = new SecureConnection(aso, connection);
	}
	
	/**
	 * Return the SystemTitle to be used in the DLMS association request.
	 * For the AM500 modules, this is the serialNumber of the E-METER
	 *  
	 * @return the SystemTitle
	 */
	protected byte[] getSystemIdentifier(){
		return serialNumber.getBytes();
	}

	private boolean isCiphered() {
		return (getContextId() == AssociationControlServiceElement.LOGICAL_NAME_REFERENCING_WITH_CIPHERING) || (getContextId() == AssociationControlServiceElement.SHORT_NAME_REFERENCING_WITH_CIPHERING);
	}

	/**
	 * Define the contextID of the associationServiceObject.
	 * Depending on the reference(see {@link ProtocolLink#LN_REFERENCE} and {@link ProtocolLink#SN_REFERENCE}, the value can be different.
	 *
	 * @return the contextId
	 */
	private int getContextId(){
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
	 * {@inheritDoc}
	 */
	public void connect() throws IOException {
		try {
			getDLMSConnection().connectMAC();
			aso.createAssociation();
			checkCache();
			
	        doConnect();
			
			validateSerialNumber();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		} catch (DLMSConnectionException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		} catch (BusinessException e) {
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
			iNumberOfChannels = getCosemObjectFactory().getLoadProfile().getProfileGeneric().getNumberOfProfileChannels() + NR_OF_PLC_CHANNELS;
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
        	strBuff.append(oc.toString()).append(" = ").append(RegisterDescription.INFO.get(oc.toString()));
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
                	aso.releaseAssociation();
    			}
				getDLMSConnection().disconnectMAC();
			}
		} catch (DLMSConnectionException e) {
            logger.severe("DLMSSNAS220AS220: disconnect(), "+e.getMessage());
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
	public String getSerialNumber() throws IOException {
		UniversalObject uo = getMeterConfig().getSerialNumberObject();
		byte[] responsedata = getCosemObjectFactory().getGenericRead(uo.getBaseName(), uo.getValueAttributeOffset()).getResponseData();
		return AXDRDecoder.decode(responsedata).getOctetString().stringValue();
	}

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
			serialNumber = properties.getProperty(MeterProtocol.SERIALNUMBER, "");
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
			iForcedDelay = Integer.parseInt(properties.getProperty("ForcedDelay", "10").trim());
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
			profileType = Integer.parseInt(properties.getProperty("ProfileType", "0"));

			opticalBaudrate = Integer.parseInt(properties.getProperty("OpticalBaudrate", "-1"));

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
        result.add("ForcedDelay");
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
        result.add("ProfileType");
        result.add("OpticalBaudrate");
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

	/**
	 * {@inheritDoc}
	 */
	public FirmwareUpdateMessageBuilder getFirmwareUpdateMessageBuilder() {
	    return new FirmwareUpdateMessageBuilder();
	}

	/**
	 * {@inheritDoc}
	 *
	 * Currently URL's are not supported
	 */
	public boolean supportsUrls() {
	    return false;
	}

	/**
	 * {@inheritDoc}
	 *
	 * We don't have database access so we don't need references
	 */
	public boolean supportsUserFileReferences() {
	    return false;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Userfiles are supported for upgrades
	 */
	public boolean supportsUserFilesForFirmwareUpdate() {
	    return true;
	}
}

