package com.energyict.protocolimpl.dlms.as220;

import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.cache.CacheMechanism;
import com.energyict.mdc.upl.cache.CachingProtocol;
import com.energyict.mdc.upl.cache.ProtocolCacheFetchException;
import com.energyict.mdc.upl.cache.ProtocolCacheUpdateException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.CipheringType;
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
import com.energyict.dlms.aso.SecurityPolicy;
import com.energyict.dlms.aso.XdlmsAse;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.dlms.RtuDLMS;
import com.energyict.protocolimpl.dlms.RtuDLMSCache;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.google.common.base.Supplier;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS;
import static com.energyict.mdc.upl.MeterProtocol.Property.NODEID;
import static com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD;
import static com.energyict.mdc.upl.MeterProtocol.Property.RETRIES;
import static com.energyict.mdc.upl.MeterProtocol.Property.ROUNDTRIPCORRECTION;
import static com.energyict.mdc.upl.MeterProtocol.Property.SECURITYLEVEL;
import static com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER;
import static com.energyict.mdc.upl.MeterProtocol.Property.TIMEOUT;

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
public abstract class DLMSSNAS220 extends PluggableMeterProtocol implements HHUEnabler, ProtocolLink, CacheMechanism, CachingProtocol, SerialNumberSupport {

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
    private static final String PR_RETRIES = RETRIES.getName();
    private static final String PR_FORCED_DELAY = "ForcedDelay";
    private static final String PR_TIMEOUT = TIMEOUT.getName();
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
    private static final int MAX_ADDRESS_LENGTH = 16;

    protected int iInterval = -1;
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
    private final PropertySpecService propertySpecService;

    public DLMSSNAS220(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    protected abstract void doConnect();

    protected abstract String getRegistersInfo() throws IOException;

    /**
     * Getter for the manufacturer deviceId.
     * In our case, this is Elster so the ID = 'GEC'
     *
     * @return the deviceId
     */
    public String getDeviceID() {
        return "GEC";
    }

    @Override
    public DLMSConnection getDLMSConnection() {
        return dlmsConnection;
    }

    public void setDlmsConnection(DLMSConnection connection) {
        this.dlmsConnection = connection;
    }

    public int getProfileType() {
        return profileType;
    }

    @Override
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

    @Override
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

    @Override
    public void connect() throws IOException {
        try {
            getDLMSConnection().connectMAC();
            aso.createAssociation();
            checkCache();
            setObjectList();
            doConnect();
        } catch (DLMSConnectionException e) {
            IOException exception = new IOException(e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @SuppressWarnings("deprecation")
    private void setObjectList() throws IOException {
        meterConfig.setCapturedObjectList(getCosemObjectFactory().getLoadProfile().getProfileGeneric().getCaptureObjectsAsUniversalObjects());
    }

    @Override
    public int getProfileInterval() throws IOException {
        if (iInterval == -1) {
            iInterval = getCosemObjectFactory().getLoadProfile().getProfileGeneric().getCapturePeriod();
        }
        return iInterval;
    }

    @Override
    public Quantity getMeterReading(String name) throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public Quantity getMeterReading(int channelId) throws IOException {
        throw new UnsupportedException();
    }

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

    @Override
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

    @Override
    public String getSerialNumber() {
        do {
            try {
                UniversalObject uo = getMeterConfig().getSerialNumberObject();
                byte[] responsedata = getCosemObjectFactory().getGenericRead(uo.getBaseName(), uo.getValueAttributeOffset()).getResponseData();
                return AXDRDecoder.decode(responsedata).getOctetString().stringValue();
            }
            catch (IOException e) {
               throw DLMSIOExceptionHandler.handle(e, getProtocolRetries() + 1);
            }
        } while (true);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                this.stringSpec(NODEID.getName()),
                this.stringSpec(SERIALNUMBER.getName()),
                this.integerSpec(PR_EXTENDED_LOGGING),
                this.integerSpec(PR_ADDRESSING_MODE),
                this.integerSpec(PR_CONNECTION),
                this.stringSpecOfMaxLength(ADDRESS.getName(), MAX_ADDRESS_LENGTH),
                this.stringSpec(PASSWORD.getName()),
                this.integerSpec(PR_TIMEOUT),
                this.integerSpec(PR_FORCED_DELAY),
                this.integerSpec(PR_RETRIES),
                this.integerSpec(PR_REQUEST_TIME_ZONE),
                this.integerSpec(ROUNDTRIPCORRECTION.getName()),
                this.stringSpec(SECURITYLEVEL.getName()),
                this.integerSpec(PR_CLIENT_MAC_ADDRESS),
                this.integerSpec(PR_CLIENT_MAC_ADDRESS),
                this.integerSpec(PR_SRV_UP_MACADDR),
                this.integerSpec(PR_SRV_LOW_MACADDR),
                this.integerSpec(PR_TRANSP_CONNECT_TIME),
                this.integerSpec(PR_TRANSP_BAUDRATE),
                this.integerSpec(PR_TRANSP_DATABITS),
                this.integerSpec(PR_TRANSP_STOPBITS),
                this.integerSpec(PR_TRANSP_PARITY),
                this.integerSpec(PR_PROFILE_TYPE),
                this.integerSpec(PR_OPTICAL_BAUDRATE),
                this.integerSpec(PR_CIPHERING_TYPE, CipheringType.GLOBAL.getType(), CipheringType.DEDICATED.getType()),
                this.integerSpec(PR_LIMIT_MAX_NR_OF_DAYS),
                this.stringSpec(PR_READ_PLC_LOG));
    }

    private <T> PropertySpec spec(String name, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, false, optionsSupplier).finish();
    }

    private PropertySpec stringSpec(String name) {
        return this.spec(name, this.propertySpecService::stringSpec);
    }

    private PropertySpec stringSpecOfMaxLength(String name, int length) {
        return this.spec(name, () -> this.propertySpecService.stringSpecOfMaximumLength(length));
    }

    private PropertySpec integerSpec(String name) {
        return this.spec(name, this.propertySpecService::integerSpec);
    }

    private PropertySpec integerSpec(String name, Integer... validValues) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, this.propertySpecService::integerSpec)
                .addValues(validValues)
                .markExhaustive()
                .finish();
    }

    @Override
    public void setProperties(TypedProperties properties) throws MissingPropertyException, InvalidPropertyException {
        try {
            nodeId = properties.getTypedProperty(Property.NODEID.getName(), "");
            serialNumber = properties.getTypedProperty(Property.SERIALNUMBER.getName(), "");
            extendedLogging = Integer.parseInt(properties.getTypedProperty(PR_EXTENDED_LOGGING, "0"));
            addressingMode = Integer.parseInt(properties.getTypedProperty(PR_ADDRESSING_MODE, "-1"));
            connectionMode = Integer.parseInt(properties.getTypedProperty(PR_CONNECTION, "0")); // 0=HDLC, 1= TCP/IP, 2=cosemPDUconnection 3=LLCConnection

            strID = properties.getTypedProperty(Property.ADDRESS.getName());

            strPassword = properties.getTypedProperty(Property.PASSWORD.getName(), "00000000");
            iTimeoutProperty = Integer.parseInt(properties.getTypedProperty(PR_TIMEOUT, "10000").trim());
            iForcedDelay = Integer.parseInt(properties.getTypedProperty(PR_FORCED_DELAY, "10").trim());
            iProtocolRetriesProperty = Integer.parseInt(properties.getTypedProperty(PR_RETRIES, "5").trim());
            iRequestTimeZone = Integer.parseInt(properties.getTypedProperty(PR_REQUEST_TIME_ZONE, "0").trim());
            setRoundtripCorrection(Integer.parseInt(properties.getTypedProperty(ROUNDTRIPCORRECTION.getName(), "0").trim()));

            String[] securityLevel = properties.getTypedProperty(PR_SECURITY_LEVEL, "0:" + SecurityPolicy.SECURITYPOLICY_NONE).split(":");
            this.authenticationSecurityLevel = Integer.parseInt(securityLevel[0]);
            if (securityLevel.length == 2) {
                this.datatransportSecurityLevel = Integer.parseInt(securityLevel[1]);
            } else if (securityLevel.length == 1) {
                this.datatransportSecurityLevel = SecurityPolicy.SECURITYPOLICY_NONE;
            } else {
                throw new IllegalArgumentException("SecurityLevel property contains an illegal value " + properties.getTypedProperty(PR_SECURITY_LEVEL, "0"));
            }

            iClientMacAddress = Integer.parseInt(properties.getTypedProperty(PR_CLIENT_MAC_ADDRESS, "32").trim());
            iServerUpperMacAddress = Integer.parseInt(properties.getTypedProperty(PR_SRV_UP_MACADDR, "1").trim());
            iServerLowerMacAddress = Integer.parseInt(properties.getTypedProperty(PR_SRV_LOW_MACADDR, "0").trim());
            transparentConnectTime = Integer.parseInt(properties.getTypedProperty(PR_TRANSP_CONNECT_TIME, "10"));
            transparentBaudrate = Integer.parseInt(properties.getTypedProperty(PR_TRANSP_BAUDRATE, "9600"));
            transparentDatabits = Integer.parseInt(properties.getTypedProperty(PR_TRANSP_DATABITS, "8"));
            transparentStopbits = Integer.parseInt(properties.getTypedProperty(PR_TRANSP_STOPBITS, "1"));
            transparentParity = Integer.parseInt(properties.getTypedProperty(PR_TRANSP_PARITY, "0"));
            profileType = Integer.parseInt(properties.getTypedProperty(PR_PROFILE_TYPE, "3"));

            opticalBaudrate = Integer.parseInt(properties.getTypedProperty(PR_OPTICAL_BAUDRATE, "-1"));

            this.cipheringType = Integer.parseInt(properties.getTypedProperty("CipheringType", Integer.toString(CipheringType.DEDICATED.getType())));

            this.limitMaxNrOfDays = Integer.parseInt(properties.getTypedProperty(PR_LIMIT_MAX_NR_OF_DAYS, "0"));
            this.readPlcLogbook = ProtocolTools.getBooleanFromString(properties.getTypedProperty(PR_READ_PLC_LOG, "0"));
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, this.getClass().getSimpleName() + ": validation of properties failed before");
        }
        this.properties = properties.toStringProperties();
    }

    @Override
    public String getRegister(String name) throws IOException {
        throw new UnsupportedException("getRegister(String name) not implemented.");
    }

    @Override
    public void setRegister(String name, String value) throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public void initializeDevice() throws IOException {
        throw new UnsupportedException();
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

    @Override
    public boolean isRequestTimeZone() {
        return (iRequestTimeZone != 0);
    }

    @Override
    public TimeZone getTimeZone() {
        return timeZone;
    }

    public Serializable getCache() {
        return dlmsCache;
    }

    @Override
    public void setCache(Serializable cacheObject) {
        this.dlmsCache = (DLMSCache) cacheObject;
    }

    @Override
    public Serializable fetchCache(int deviceId, Connection connection) throws SQLException {
        if (deviceId != 0) {
            RtuDLMSCache rtuCache = new RtuDLMSCache(deviceId);
            RtuDLMS rtu = new RtuDLMS(deviceId);
            try {
                return new DLMSCache(rtuCache.getObjectList(connection), rtu.getConfProgChange(connection));
            } catch (ProtocolCacheFetchException e) {
                return new DLMSCache(null, -1);
            }
        } else {
            throw new IllegalArgumentException("invalid RtuId!");
        }
    }

    @Override
    public void updateCache(int deviceId, Serializable cacheObject, Connection connection) throws SQLException, ProtocolCacheUpdateException {
        if (deviceId != 0) {
            DLMSCache dc = (DLMSCache) cacheObject;
            if (dc.contentChanged()) {
                RtuDLMSCache rtuCache = new RtuDLMSCache(deviceId);
                RtuDLMS rtu = new RtuDLMS(deviceId);
                rtuCache.saveObjectList(dc.getObjectList(), connection);
                rtu.setConfProgChange(dc.getConfProgChange(), connection);
            }
        } else {
            throw new IllegalArgumentException("invalid RtuId!");
        }
    }

    @Override
    public String getFileName() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) + "_" + (calendar.get(Calendar.MONTH) + 1) + "_" + calendar.get(Calendar.DAY_OF_MONTH) + "_" + strID + "_" + strPassword + "_" + serialNumber + "_" + iServerUpperMacAddress + "_DLMSSNAS220.cache";
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, false);
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        HHUSignOn hhuSignOn = new AS220TransparentConnection(commChannel, transparentConnectTime, transparentBaudrate, transparentDatabits, transparentStopbits,
                transparentParity, authenticationSecurityLevel, strPassword, logger);
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(datareadout);
        getDLMSConnection().setHHUSignOn(hhuSignOn, nodeId);
    }

    @Override
    public byte[] getHHUDataReadout() {
        return getDLMSConnection().getHhuSignOn().getDataReadout();
    }

    @Override
    public void release() throws IOException {
    }

    @Override
    public DLMSMeterConfig getMeterConfig() {
        return meterConfig;
    }

    @Override
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

    public void setCosemObjectFactory(CosemObjectFactory cof) {
        this.cosemObjectFactory = cof;
    }

    @Override
    public int getReference() {
        return ProtocolLink.SN_REFERENCE;
    }

    @Override
    public StoredValues getStoredValues() {
        return storedValuesImpl;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * @return the dstFlag
     */
    public int getDstFlag() {
        return dstFlag;
    }

    public void setDstFlag(int dstFlag) {
        this.dstFlag = dstFlag;
    }

    /**
     * @param iRoundtripCorrection the iRoundtripCorrection to set
     */
    public void setRoundtripCorrection(int iRoundtripCorrection) {
        this.iRoundtripCorrection = iRoundtripCorrection;
    }

    @Override
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

}