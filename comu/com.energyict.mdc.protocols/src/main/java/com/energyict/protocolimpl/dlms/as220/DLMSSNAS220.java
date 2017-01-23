package com.energyict.protocolimpl.dlms.as220;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.CosemPDUConnection;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.HDLC2Connection;
import com.energyict.dlms.LLCConnection;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.SecureConnection;
import com.energyict.dlms.TCPIPConnection;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.aso.AssociationControlServiceElement;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.aso.LocalSecurityProvider;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.aso.XdlmsAse;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.cosem.ActivityCalendar;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.HHUEnabler;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpecFactory;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.base.RetryHandler;
import com.energyict.protocolimpl.dlms.RtuDLMS;
import com.energyict.protocolimpl.dlms.RtuDLMSCache;
import com.energyict.protocolimpl.dlms.common.DLMSActivityCalendarController;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocols.mdc.services.impl.OrmClient;
import com.energyict.protocols.util.CacheMechanism;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * @version 2.0
 * @author Koenraad Vanderschaeve
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
public abstract class DLMSSNAS220 extends PluggableMeterProtocol implements HHUEnabler, ProtocolLink, CacheMechanism {

    private static final String PR_OPTICAL_BAUDRATE = "OpticalBaudrate";
    private static final String PR_PROFILE_TYPE = "ProfileType";
    private static final String PR_TRANSP_PARITY = "TransparentParity";
    private static final String PR_TRANSP_STOPBITS = "TransparentStopbits";
    private static final String PR_TRANSP_DATABITS = "TransparentDatabits";
    private static final String PR_TRANSP_BAUDRATE = "TransparentBaudrate";
    private static final String PR_TRANSP_CONNECT_TIME = "TransparentConnectTime";
    private static final String PR_DATA_KEY = LocalSecurityProvider.DATATRANSPORTKEY;
    private static final String PR_DATA_AUTH_KEY = LocalSecurityProvider.DATATRANSPORT_AUTHENTICATIONKEY;
    private static final String PR_CONNECTION = "Connection";
    private static final String PR_ADDRESSING_MODE = "AddressingMode";
    private static final String PR_EXTENDED_LOGGING = "ExtendedLogging";
    private static final String PR_SRV_LOW_MACADDR = "ServerLowerMacAddress";
    private static final String PR_SRV_UP_MACADDR = "ServerUpperMacAddress";
    private static final String PR_CLIENT_MAC_ADDRESS = "ClientMacAddress";
    private static final String PR_SECURITY_LEVEL = "SecurityLevel";
    private static final String PR_REQUEST_TIME_ZONE = "RequestTimeZone";
    private static final String PR_RETRIES = "Retries";
    private static final String PR_FORCED_DELAY = "ForcedDelay";
    private static final String PR_TIMEOUT = "Timeout";
    private static final String PR_CIPHERING_TYPE = "CipheringType";
    private static final String PR_LIMIT_MAX_NR_OF_DAYS = "LimitMaxNrOfDays";
    private static final String PR_READ_PLC_LOG = "ReadPlcLog";

    private static final int MAX_PDU_SIZE = 200;
    private static final int PROPOSED_QOS = -1;
    private static final int PROPOSED_DLMS_VERSION = 6;

    private static final int CONNECTION_MODE_HDLC = 0;
    private static final int CONNECTION_MODE_TCPIP = 1;
    private static final int CONNECTION_MODE_COSEM_PDU = 2;
    private static final int CONNECTION_MODE_LLC = 3;

    private boolean debug = false;
    private DLMSCache dlmsCache = new DLMSCache();

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
    private String nodeId;
    private String serialNumber;
    protected int iInterval = -1;
    private int extendedLogging;
    private int opticalBaudrate;
    private int profileType = 0;
    private int cipheringType;

    private int authenticationSecurityLevel;
    private int datatransportSecurityLevel;

    private DLMSConnection dlmsConnection = null;
    private SecurityContext securityContext = null;
    private ApplicationServiceObject aso = null;

    private CosemObjectFactory cosemObjectFactory = null;
    private AS220StoredValues storedValuesImpl = null;

    // lazy initializing
    private int iMeterTimeZoneOffset = 255;
    private int iConfigProgramChange = -1;

    private DLMSMeterConfig meterConfig = DLMSMeterConfig.getInstance();

    // Added for MeterProtocol interface implementation
    private Logger logger = null;
    private TimeZone timeZone = null;

    // filled in when getTime is invoked!
    private int dstFlag; // -1=unknown, 0=not set, 1=set
    private int addressingMode;
    private int connectionMode;

    private Properties properties;

    private int iForcedDelay;
    private int limitMaxNrOfDays;
    private boolean readPlcLogbook;
    private final OrmClient ormClient;

    /**
     * Do some extra connect settings
     */
    protected abstract void doConnect();

    protected abstract String getRegistersInfo() throws IOException;

    public DLMSSNAS220(PropertySpecService propertySpecService, OrmClient ormClient) {
        super(propertySpecService);
        this.ormClient = ormClient;
    }

    /**
     * Getter for the manufacturer deviceId.
     * In our case, this is Elster so the ID = 'GEC'
     *
     * @return the deviceId
     */
    public String getDeviceID() {
        return "GEC";
    }

    public DLMSConnection getDLMSConnection() {
        return dlmsConnection;
    }

    public void setDlmsConnection(DLMSConnection connection) {
        this.dlmsConnection = connection;
    }

    public void setCosemObjectFactory(CosemObjectFactory cof) {
        this.cosemObjectFactory = cof;
    }

    public int getProfileType() {
        return profileType;
    }

    /**
     * Initializes the receiver
     *
     * @param inputStream  - the inputStream form the dialer
     * @param outputStream - the outputStream from the dialer
     * @param tz           - the {@link TimeZone} used
     * @param log          - the {@link Logger} used
     */
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone tz, Logger log) throws IOException {
        this.timeZone = tz;
        this.logger = log;

        setDstFlag(-1);
        iMeterTimeZoneOffset = 255;
        iConfigProgramChange = -1;

        cosemObjectFactory = new CosemObjectFactory(this);
        storedValuesImpl = new AS220StoredValues(AS220StoredValues.DAILY_OBISCODE, getCosemObjectFactory());

        initDLMSConnection(inputStream, outputStream);
        iInterval = -1;

    }

    /**
     * Initialize DLMS specific objects
     *
     * @param inputStream  - the inputStream form the dialer
     * @param outputStream - the outputStream from the dialer
     * @throws IOException if initializing the connection failed of the connectionMode is invalid
     */
    private void initDLMSConnection(InputStream inputStream, OutputStream outputStream) throws IOException {

        DLMSConnection connection;

        try {
            switch (connectionMode) {
                case CONNECTION_MODE_HDLC:
                    connection = new HDLC2Connection(inputStream, outputStream, iTimeoutProperty, 100, iProtocolRetriesProperty, iClientMacAddress,
                            iServerLowerMacAddress, iServerUpperMacAddress, addressingMode, -1, opticalBaudrate);
                    break;
                case CONNECTION_MODE_TCPIP:
                    connection = new TCPIPConnection(inputStream, outputStream, iTimeoutProperty, iForcedDelay, iProtocolRetriesProperty, iClientMacAddress, iServerLowerMacAddress, getLogger());
                    break;
                case CONNECTION_MODE_COSEM_PDU:
                    connection = new CosemPDUConnection(inputStream, outputStream, iTimeoutProperty, iForcedDelay, iProtocolRetriesProperty, iClientMacAddress, iServerLowerMacAddress);
                    break;
                case CONNECTION_MODE_LLC:
                    connection = new LLCConnection(inputStream, outputStream, iTimeoutProperty, iForcedDelay, iProtocolRetriesProperty, iClientMacAddress, iServerLowerMacAddress);
                    break;
                default:
                    throw new IOException("Unable to initialize dlmsConnection, connection property unknown: " + connectionMode);
            }
        } catch (DLMSConnectionException e) {
            throw new IOException(e.getMessage());
        }

        LocalSecurityProvider localSecurityProvider = new LocalSecurityProvider(this.properties);
        securityContext = new SecurityContext(datatransportSecurityLevel, authenticationSecurityLevel, 0, getSystemIdentifier(), localSecurityProvider, this.cipheringType);
        ConformanceBlock cb = new ConformanceBlock(ConformanceBlock.DEFAULT_SN_CONFORMANCE_BLOCK);
        XdlmsAse xdlmsAse = new XdlmsAse(isCiphered() ? localSecurityProvider.getDedicatedKey() : null, true, PROPOSED_QOS, PROPOSED_DLMS_VERSION, cb, MAX_PDU_SIZE);
        aso = new ApplicationServiceObject(xdlmsAse, this, securityContext, getContextId());
        dlmsConnection = new SecureConnection(aso, connection);
    }

    public ApplicationServiceObject getAso() {
        return aso;
    }

    /**
     * Return the SystemTitle to be used in the DLMS association request.
     * For the AM500 modules, this is the serialNumber of the E-METER
     *
     * @return the SystemTitle
     */
    protected byte[] getSystemIdentifier() {
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
    private int getContextId() {
        if (getReference() == ProtocolLink.LN_REFERENCE) {
            return (this.datatransportSecurityLevel == 0) ? AssociationControlServiceElement.LOGICAL_NAME_REFERENCING_NO_CIPHERING :
                    AssociationControlServiceElement.LOGICAL_NAME_REFERENCING_WITH_CIPHERING;
        } else if (getReference() == ProtocolLink.SN_REFERENCE) {
            return (this.datatransportSecurityLevel == 0) ? AssociationControlServiceElement.SHORT_NAME_REFERENCING_NO_CIPHERING :
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
            setObjectList();
            doConnect();
            validateSerialNumber();
        } catch (DLMSConnectionException e) {
            IOException exception = new IOException(e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    /**
     * @throws IOException
     */
    @SuppressWarnings("deprecation")
    private void setObjectList() throws IOException {
        meterConfig.setCapturedObjectList(getCosemObjectFactory().getLoadProfile().getProfileGeneric().getCaptureObjectsAsUniversalObjects());
    }

    public int getProfileInterval() throws IOException {
        if (iInterval == -1) {
            iInterval = getCosemObjectFactory().getLoadProfile().getProfileGeneric().getCapturePeriod();
        }
        return iInterval;
    }

    public Quantity getMeterReading(String name) throws IOException {
        throw new UnsupportedException();
    }

    public Quantity getMeterReading(int channelId) throws IOException {
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
                } catch (IOException e) {
                    iConf = -1;
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
            } else { // Cache not exist
                logger.info("DLMSSNAS220 Cache does not exist, request object list.");
                requestObjectList();
                try {
                    iConf = requestConfigurationProgramChanges();
                    dlmsCache.saveObjectList(meterConfig.getInstantiatedObjectList());  // save object list in cache
                    dlmsCache.setConfProgChange(iConf);  // set new configuration program change
                } catch (IOException e) {
                    iConf = -1;
                }
            }

            if (extendedLogging >= 1) {
                logger.info(getRegistersInfo());
            }

        } catch (IOException e) {
            IOException exception = new IOException("connect() error, " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    /**
     * This method initiates the MAC disconnect for the HDLC layer.
     *
     * @throws IOException
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
            logger.severe("DLMSSNAS220AS220: disconnect(), " + e.getMessage());
        }
    }

    /**
     * This method requests for the COSEM object list in the remote meter. A
     * list is byuild with LN and SN references.
     * This method must be executed before other request methods.
     *
     * @throws IOException
     */
    private void requestObjectList() throws IOException {
        meterConfig.setInstantiatedObjectList(getCosemObjectFactory().getAssociationSN().getBuffer());
    }

    /**
     * This method requests for the COSEM object SAP.
     *
     * @throws IOException
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
     * Read the serialNumber from the device
     *
     * @return the serial number from the device as {@link String}
     * @throws IOException
     */
    public String getSerialNumber() throws IOException {
        RetryHandler rh = new RetryHandler();
        do {
            try {
                UniversalObject uo = getMeterConfig().getSerialNumberObject();
                byte[] responsedata = getCosemObjectFactory().getGenericRead(uo.getBaseName(), uo.getValueAttributeOffset()).getResponseData();
                return AXDRDecoder.decode(responsedata).getOctetString().stringValue();
            } catch (DataAccessResultException e) {
                rh.logFailure(e);
            }
        } while (true);
    }

    /**
     * this implementation calls <code> validateProperties </code>
     * and assigns the argument to the properties field
     *
     * @param properties <br>
     * @throws MissingPropertyException <br>
     * @throws InvalidPropertyException <br>
     */
    public void setProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        validateProperties(properties);
        this.properties = properties;
    }

    /**
     * <p>validates the properties.</p><p>
     * The default implementation checks that all required parameters are present.
     * </p>
     *
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
            extendedLogging = Integer.parseInt(properties.getProperty(PR_EXTENDED_LOGGING, "0"));
            addressingMode = Integer.parseInt(properties.getProperty(PR_ADDRESSING_MODE, "-1"));
            connectionMode = Integer.parseInt(properties.getProperty(PR_CONNECTION, "0")); // 0=HDLC, 1= TCP/IP, 2=cosemPDUconnection 3=LLCConnection

            strID = properties.getProperty(MeterProtocol.ADDRESS);
            if ((strID != null) && (strID.length() > 16)) {
                throw new InvalidPropertyException("ID must be less or equal then 16 characters.");
            }

            strPassword = properties.getProperty(MeterProtocol.PASSWORD, "00000000");
            iTimeoutProperty = Integer.parseInt(properties.getProperty(PR_TIMEOUT, "10000").trim());
            iForcedDelay = Integer.parseInt(properties.getProperty(PR_FORCED_DELAY, "10").trim());
            iProtocolRetriesProperty = Integer.parseInt(properties.getProperty(PR_RETRIES, "5").trim());
            iRequestTimeZone = Integer.parseInt(properties.getProperty(PR_REQUEST_TIME_ZONE, "0").trim());
            setRoundtripCorrection(Integer.parseInt(properties.getProperty("RoundtripCorrection", "0").trim()));

            String[] securityLevel = properties.getProperty(PR_SECURITY_LEVEL, "0:" + SecurityContext.SECURITYPOLICY_NONE).split(":");
            this.authenticationSecurityLevel = Integer.parseInt(securityLevel[0]);
            if (securityLevel.length == 2) {
                this.datatransportSecurityLevel = Integer.parseInt(securityLevel[1]);
            } else if (securityLevel.length == 1) {
                this.datatransportSecurityLevel = SecurityContext.SECURITYPOLICY_NONE;
            } else {
                throw new IllegalArgumentException("SecurityLevel property contains an illegal value " + properties.getProperty(PR_SECURITY_LEVEL, "0"));
            }

            iClientMacAddress = Integer.parseInt(properties.getProperty(PR_CLIENT_MAC_ADDRESS, "32").trim());
            iServerUpperMacAddress = Integer.parseInt(properties.getProperty(PR_SRV_UP_MACADDR, "1").trim());
            iServerLowerMacAddress = Integer.parseInt(properties.getProperty(PR_SRV_LOW_MACADDR, "0").trim());
            transparentConnectTime = Integer.parseInt(properties.getProperty(PR_TRANSP_CONNECT_TIME, "10"));
            transparentBaudrate = Integer.parseInt(properties.getProperty(PR_TRANSP_BAUDRATE, "9600"));
            transparentDatabits = Integer.parseInt(properties.getProperty(PR_TRANSP_DATABITS, "8"));
            transparentStopbits = Integer.parseInt(properties.getProperty(PR_TRANSP_STOPBITS, "1"));
            transparentParity = Integer.parseInt(properties.getProperty(PR_TRANSP_PARITY, "0"));
            profileType = Integer.parseInt(properties.getProperty(PR_PROFILE_TYPE, "3"));

            opticalBaudrate = Integer.parseInt(properties.getProperty(PR_OPTICAL_BAUDRATE, "-1"));

            this.cipheringType = Integer.parseInt(properties.getProperty("CipheringType", Integer.toString(SecurityContext.CIPHERING_TYPE_DEDICATED)));
            if (cipheringType != SecurityContext.CIPHERING_TYPE_GLOBAL && cipheringType != SecurityContext.CIPHERING_TYPE_DEDICATED) {
                throw new InvalidPropertyException("Only 0 or 1 is allowed for the CipheringType property");
            }

            this.limitMaxNrOfDays = Integer.parseInt(properties.getProperty(PR_LIMIT_MAX_NR_OF_DAYS, "0"));
            this.readPlcLogbook = ProtocolTools.getBooleanFromString(properties.getProperty(PR_READ_PLC_LOG, "0"));

        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(" validateProperties, NumberFormatException, " + e.getMessage());
        }

    }

    /**
     * this implementation throws UnsupportedException. Subclasses may override
     *
     * @param name <br>
     * @return the register value
     * @throws IOException <br>
     */
    public String getRegister(String name) throws IOException {
        throw new UnsupportedException("getRegister(String name) not implemented.");
    }

    /**
     * this implementation throws UnsupportedException. Subclasses may override
     *
     * @param name  <br>
     * @param value <br>
     * @throws IOException <br>
     */
    public void setRegister(String name, String value) throws IOException {
        throw new UnsupportedException();
    }

    /**
     * this implementation throws UnsupportedException. Subclasses may override
     *
     * @throws IOException          <br>
     * @throws UnsupportedException <br>
     */
    public void initializeDevice() throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return PropertySpecFactory.toPropertySpecs(getRequiredKeys(), this.getPropertySpecService());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return PropertySpecFactory.toPropertySpecs(getOptionalKeys(), this.getPropertySpecService());
    }

    /**
     * the implementation returns both the address and password key
     *
     * @return a list of strings
     */
    public List<String> getRequiredKeys() {
        return Collections.emptyList();
    }

    /**
     * this implementation returns an empty list
     *
     * @return a list of strings
     */
    public List<String> getOptionalKeys() {
        return Arrays.asList(
                    PR_TIMEOUT,
                    PR_FORCED_DELAY,
                    PR_RETRIES,
                    PR_REQUEST_TIME_ZONE,
                    PR_SECURITY_LEVEL,
                    PR_CLIENT_MAC_ADDRESS,
                    PR_SRV_UP_MACADDR,
                    PR_SRV_LOW_MACADDR,
                    PR_EXTENDED_LOGGING,
                    PR_ADDRESSING_MODE,
                    PR_CONNECTION,
                    PR_DATA_KEY,
                    PR_DATA_AUTH_KEY,
                    PR_TRANSP_CONNECT_TIME,
                    PR_TRANSP_BAUDRATE,
                    PR_TRANSP_DATABITS,
                    PR_TRANSP_STOPBITS,
                    PR_TRANSP_PARITY,
                    PR_PROFILE_TYPE,
                    PR_OPTICAL_BAUDRATE,
                    PR_CIPHERING_TYPE,
                    PR_LIMIT_MAX_NR_OF_DAYS,
                    PR_READ_PLC_LOG);
    }

    public int requestConfigurationProgramChanges() throws IOException {
        if (iConfigProgramChange == -1) {
            iConfigProgramChange = (int) getCosemObjectFactory().getCosemObject(getMeterConfig().getConfigObject().getObisCode()).getValue();
        }
        return iConfigProgramChange;
    }

    public int requestTimeZone() throws IOException {
        if (iMeterTimeZoneOffset == 255) {
            iMeterTimeZoneOffset = getCosemObjectFactory().getClock().getTimeZone();
        }
        return iMeterTimeZoneOffset;
    }

    public boolean isRequestTimeZone() {
        return (iRequestTimeZone != 0);
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setCache(Object cacheObject) {
        this.dlmsCache = (DLMSCache) cacheObject;
    }

    public Object getCache() {
        return dlmsCache;
    }

    public Object fetchCache(int rtuid) throws SQLException {
        if (rtuid != 0) {
            RtuDLMSCache rtuCache = new RtuDLMSCache(rtuid, ormClient);
            RtuDLMS rtu = new RtuDLMS(rtuid, ormClient);
            try {
                return new DLMSCache(rtuCache.getObjectList(), rtu.getConfProgChange());
            } catch (NotFoundException e) {
                return new DLMSCache(null, -1);
            }
        } else {
            throw new IllegalArgumentException("invalid RtuId!");
        }
    }

    public void updateCache(int rtuid, Object cacheObject) throws SQLException {
        if (rtuid != 0) {
            DLMSCache dc = (DLMSCache) cacheObject;
            if (dc.isDirty()) {
                RtuDLMSCache rtuCache = new RtuDLMSCache(rtuid, ormClient);
                RtuDLMS rtu = new RtuDLMS(rtuid, ormClient);
                rtuCache.saveObjectList(dc.getObjectList());
                rtu.setConfProgChange(dc.getConfProgChange());
            }
        } else {
            throw new IllegalArgumentException("invalid RtuId!");
        }
    }

    public String getFileName() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) + "_" + (calendar.get(Calendar.MONTH) + 1) + "_" + calendar.get(Calendar.DAY_OF_MONTH) + "_" + strID + "_" + strPassword + "_" + serialNumber + "_" + iServerUpperMacAddress + "_DLMSSNAS220.cache";
    }

    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, false);
    }

    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        HHUSignOn hhuSignOn = new AS220TransparentConnection(commChannel, transparentConnectTime, transparentBaudrate, transparentDatabits, transparentStopbits,
                transparentParity, authenticationSecurityLevel, strPassword, logger);
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(datareadout);
        getDLMSConnection().setHHUSignOn(hhuSignOn, nodeId);
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
     *
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
     * @return
     */
    public int getLimitMaxNrOfDays() {
        return limitMaxNrOfDays;
    }

    /**
     * Getter for the number of retries property. Default this is 5. Change it with the property {@link #PR_RETRIES}
     *
     * @return the number of retries
     */
    public int getProtocolRetries() {
        return this.iProtocolRetriesProperty;
    }

    /**
     * Check if we have to read the PLC logbook
     *
     * @return
     */
    public boolean isReadPlcLogbook() {
        return this.readPlcLogbook;
    }

    @Override
    public Optional<String> getActiveCalendarName() throws IOException {
        ActivityCalendar activityCalendar = this.getCosemObjectFactory().getActivityCalendar(DLMSActivityCalendarController.ACTIVITY_CALENDAR_OBISCODE);
        String calendarName = activityCalendar.readCalendarNameActive().stringValue();
        if (calendarName != null && !calendarName.isEmpty()) {
            return Optional.of(calendarName);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> getPassiveCalendarName() throws IOException {
        ActivityCalendar activityCalendar = this.getCosemObjectFactory().getActivityCalendar(DLMSActivityCalendarController.ACTIVITY_CALENDAR_OBISCODE);
        String calendarName = activityCalendar.readCalendarNamePassive().stringValue();
        if (calendarName != null && !calendarName.isEmpty()) {
            return Optional.of(calendarName);
        } else {
            return Optional.empty();
        }
    }

}

