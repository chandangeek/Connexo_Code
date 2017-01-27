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

package com.energyict.protocolimpl.dlms;

import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.CipheringType;
import com.energyict.dlms.CosemPDUConnection;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DLMSObis;
import com.energyict.dlms.HDLC2Connection;
import com.energyict.dlms.InvokeIdAndPriority;
import com.energyict.dlms.InvokeIdAndPriorityHandler;
import com.energyict.dlms.NonIncrementalInvokeIdAndPriorityHandler;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.SecureConnection;
import com.energyict.dlms.TCPIPConnection;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.aso.AssociationControlServiceElement;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.dlms.aso.XdlmsAse;
import com.energyict.dlms.axrdencoding.AxdrType;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.cache.CacheMechanism;
import com.energyict.mdc.upl.cache.ProtocolCacheFetchException;
import com.energyict.mdc.upl.cache.ProtocolCacheUpdateException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.dlms.siemenszmd.StoredValuesImpl;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS;
import static com.energyict.mdc.upl.MeterProtocol.Property.NODEID;
import static com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD;
import static com.energyict.mdc.upl.MeterProtocol.Property.RETRIES;
import static com.energyict.mdc.upl.MeterProtocol.Property.ROUNDTRIPCORRECTION;
import static com.energyict.mdc.upl.MeterProtocol.Property.SECURITYLEVEL;
import static com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER;
import static com.energyict.mdc.upl.MeterProtocol.Property.TIMEOUT;


abstract class DLMSSN extends PluggableMeterProtocol implements HHUEnabler, ProtocolLink, CacheMechanism, SerialNumberSupport {

    private static final byte DEBUG = 0;
    private static final int CONNECTION_MODE_HDLC = 0;
    private static final int CONNECTION_MODE_TCPIP = 1;
    private static final int CONNECTION_MODE_COSEMPDU = 2;
    private static final int PROPOSED_QOS = -1;
    private static final int PROPOSED_DLMS_VERSION = 6;
    private static final String MAX_PDU_SIZE = "-1";
    private static final String PROPNAME_EXTENDED_LOGGING = "ExtendedLogging";
    private static final String PROPNAME_IIAP_INVOKE_ID = "IIAPInvokeId";
    private static final String PROPNAME_IIAP_PRIORITY = "IIAPPriority";
    private static final String PROPNAME_IIAP_SERVICE_CLASS = "IIAPServiceClass";
    private static final String PROPNAME_CIPHERING_TYPE = "CipheringType";
    private static final String PROPNAME_MAX_PDU_SIZE = "MaxPduSize";
    private static final String PROPNAME_IFORCEDELAY_BEFORE_SEND = "ForcedDelay";
    private static final String PROPNAME_ADDRESSING_MODE = "AddressingMode";
    private static final String PROPNAME_CONNECTION = "Connection";
    private static final String PROPNAME_CHANNEL_MAP = "ChannelMap";
    private static final String PROPNAME_DELAY_AFTERFAIL = "DelayAfterfail";
    private static final String PROPNAME_REQUEST_TIME_ZONE = "RequestTimeZone";
    private static final String PROPNAME_REQUEST_CLOCK_OBJECT = "RequestClockObject";
    static final String PROPNAME_CLIENT_MAC_ADDRESS = "ClientMacAddress";
    public static final String PROPNAME_SERVER_UPPER_MAC_ADDRESS = "ServerUpperMacAddress";
    public static final String PROPNAME_SERVER_LOWER_MAC_ADDRESS = "ServerLowerMacAddress";

    private DLMSCache dlmsCache = new DLMSCache();
    protected ApplicationServiceObject aso;
    protected String firmwareVersion;

    private String strID;
    private String strPassword;
    private int iHDLCTimeoutProperty;
    private int iProtocolRetriesProperty;
    private int iDelayAfterFailProperty;
    private int iSecurityLevelProperty;
    private int iRequestTimeZone;
    private int iRoundtripCorrection;
    private int iClientMacAddress;
    private int iServerUpperMacAddress;
    private int iServerLowerMacAddress;
    private int iRequestClockObject;
    private int datatransportSecurityLevel;
    private int authenticationSecurityLevel;
    private int iiapPriority;
    private int iiapServiceClass;
    private int iiapInvokeId;
    private int cipheringType;
    private int maxPduSize;
    private String nodeId;
    private String configuredSerialNumber;
    private int iInterval = -1;
    private int iNROfIntervals = -1;
    private int extendedLogging;
    protected ProtocolChannelMap channelMap;
    private int iForceDelay; // delay before each command is send over the line (default 100ms)

    private DLMSConnection dlmsConnection = null;
    private CosemObjectFactory cosemObjectFactory = null;
    private StoredValuesImpl storedValuesImpl = null;

    // lazy initializing
    private int iNumberOfChannels = -1;
    private int iMeterTimeZoneOffset = 255;
    private int iConfigProgramChange = -1;

    /**
     * Contains the Configuration of a DLMS meter
     */
    private DLMSMeterConfig meterConfig = DLMSMeterConfig.getInstance();

    // Added for MeterProtocol interface implementation
    private Logger logger = null;
    private TimeZone timeZone = null;
    private Properties properties = null;

    // filled in when getTime is invoked!
    private int dstFlag; // -1=unknown, 0=not set, 1=set
    private int addressingMode;
    private int connectionMode;

    private final PropertySpecService propertySpecService;

    DLMSSN(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    protected abstract String getDeviceID();

    protected abstract void buildProfileData(byte bNROfChannels, ProfileData profileData, ScalerUnit[] scalerunit, UniversalObject[] intervalList) throws IOException;

    protected abstract void getEventLog(ProfileData profileDate, Calendar fromCalendar, Calendar toCalendar) throws IOException;

    /**
     * @return the used {@link com.energyict.dlms.aso.SecurityProvider}
     */
    protected abstract SecurityProvider getSecurityProvider();

    /**
     * Configure the {@link ConformanceBlock} which is used for the DLMS association.
     *
     * @return the conformanceBlock, if null is returned then depending on the reference,
     *         the default value({@link ConformanceBlock#DEFAULT_LN_CONFORMANCE_BLOCK} or {@link ConformanceBlock#DEFAULT_SN_CONFORMANCE_BLOCK}) will be used
     */
    protected abstract ConformanceBlock configureConformanceBlock();

    public DLMSConnection getDLMSConnection() {
        return dlmsConnection;
    }

    protected int getProtocolRetriesProperty() {
        return this.iProtocolRetriesProperty;
    }

    void setSecurityLevelProperty(int value) {
        this.iSecurityLevelProperty = value;
    }

    protected void setClientMacAddress(int value) {
        this.iClientMacAddress = value;
    }

    void setServerUpperMacAddress(int value) {
        this.iServerUpperMacAddress = value;
    }

    void setServerLowerMacAddress(int value) {
        this.iServerLowerMacAddress = value;
    }

    /**
     * initializes the receiver
     *
     * @param inputStream  <br>
     * @param outputStream <br>
     * @param timeZone     <br>
     * @param logger       <br>
     */
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        this.timeZone = timeZone;
        this.logger = logger;

        dstFlag = -1;
        iNumberOfChannels = -1; // Lazy initializing
        iMeterTimeZoneOffset = 255; // Lazy initializing
        iConfigProgramChange = -1; // Lazy initializing
        iInterval = -1;
        iNROfIntervals = -1;

        cosemObjectFactory = new CosemObjectFactory(this);
        storedValuesImpl = new StoredValuesImpl(cosemObjectFactory);
        initDLMSConnection(inputStream, outputStream);
    }

    protected byte[] getSystemIdentifier() {
        return null;
    }

    /**
     * Starts the DLMS connection
     *
     * @throws IOException when the connection failed
     */
    private void initDLMSConnection(InputStream inputStream, OutputStream outputStream) throws IOException {
        DLMSConnection connection;
        try {
            switch (connectionMode) {
                case CONNECTION_MODE_HDLC:
                    connection = new HDLC2Connection(inputStream, outputStream, iHDLCTimeoutProperty, iForceDelay, iProtocolRetriesProperty, iClientMacAddress,
                            iServerLowerMacAddress, iServerUpperMacAddress, addressingMode, -1, -1);
                    break;
                case CONNECTION_MODE_TCPIP:
                    connection = new TCPIPConnection(inputStream, outputStream, iHDLCTimeoutProperty, iForceDelay, iProtocolRetriesProperty, iClientMacAddress, iServerLowerMacAddress, getLogger());
                    break;
                case CONNECTION_MODE_COSEMPDU:
                    connection = new CosemPDUConnection(inputStream, outputStream, iHDLCTimeoutProperty, iForceDelay, iProtocolRetriesProperty, iClientMacAddress, iServerLowerMacAddress);
                    break;
                default:
                    throw new IOException("Unable to initialize dlmsConnection, connection property unknown: " + connectionMode);
            }
        } catch (DLMSConnectionException e) {
            throw new IOException(e.getMessage());
        }

        SecurityProvider localSecurityProvider = getSecurityProvider();
        SecurityContext securityContext = new SecurityContext(datatransportSecurityLevel, authenticationSecurityLevel, 0, getSystemIdentifier(), localSecurityProvider, this.cipheringType);
        ConformanceBlock conformanceBlock = configureConformanceBlock();
        if (conformanceBlock == null) {
            if (getReference() == ProtocolLink.SN_REFERENCE) {
                conformanceBlock = new ConformanceBlock(ConformanceBlock.DEFAULT_SN_CONFORMANCE_BLOCK);
            } else if (getReference() == ProtocolLink.LN_REFERENCE) {
                conformanceBlock = new ConformanceBlock(ConformanceBlock.DEFAULT_LN_CONFORMANCE_BLOCK);
            } else {
                throw new InvalidPropertyException("Invalid reference method, only 0 and 1 are allowed.");
            }
        }

        XdlmsAse xdlmsAse = new XdlmsAse(isCiphered() ? localSecurityProvider.getDedicatedKey() : null, true, PROPOSED_QOS, PROPOSED_DLMS_VERSION, conformanceBlock, maxPduSize);
        aso = new ApplicationServiceObject(xdlmsAse, this, securityContext, getContextId());
        dlmsConnection = new SecureConnection(aso, connection);
        InvokeIdAndPriorityHandler iiapHandler = buildInvokeIdAndPriorityHandler();
        this.dlmsConnection.setInvokeIdAndPriorityHandler(iiapHandler);
    }

    /**
     * Returns a boolean whether or not there's encryption used
     *
     * @return boolean
     */
    private boolean isCiphered() {
        return (getContextId() == AssociationControlServiceElement.LOGICAL_NAME_REFERENCING_WITH_CIPHERING) || (getContextId() == AssociationControlServiceElement.SHORT_NAME_REFERENCING_WITH_CIPHERING);
    }

    /**
     * Getter for the context ID
     *
     * @return the context ID
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
     * Creates an InvokeIdAndPriorityHandler using the defined properties
     *
     * @return the constructed object
     * @throws IOException if some of the properties aren't valid
     */
    private InvokeIdAndPriorityHandler buildInvokeIdAndPriorityHandler() throws IOException {
        try {
            InvokeIdAndPriority iiap = new InvokeIdAndPriority();
            iiap.setPriority(this.iiapPriority);
            iiap.setServiceClass(this.iiapServiceClass);
            iiap.setTheInvokeId(this.iiapInvokeId);
            return new NonIncrementalInvokeIdAndPriorityHandler(iiap);
        } catch (DLMSConnectionException e) {
            getLogger().info("Some configured properties are invalid. " + e.getMessage());
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public int getProfileInterval() throws IOException {
        if (iInterval == -1) {
            iInterval = getCosemObjectFactory().getLoadProfile().getProfileGeneric().getCapturePeriod();
        }
        return iInterval;
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        if (iNumberOfChannels == -1) {
            meterConfig.setCapturedObjectList(getCosemObjectFactory().getLoadProfile().getProfileGeneric().getCaptureObjectsAsUniversalObjects());
            iNumberOfChannels = meterConfig.getNumberOfChannels();
        }
        return iNumberOfChannels;
    }

    @Override
    public Quantity getMeterReading(String name) throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public Quantity getMeterReading(int channelId) throws IOException {
        throw new UnsupportedException();
    }

    private ScalerUnit getMeterDemandRegisterScalerUnit(int iChannelNR) throws IOException {
        ObisCode obisCode = meterConfig.getMeterDemandObject(iChannelNR).getObisCode();
        return getCosemObjectFactory().getCosemObject(obisCode).getScalerUnit();
    }

    @Override
    public ApplicationServiceObject getAso() {
        return aso;
    }

    @Override
    public void connect() throws IOException {
        try {
            if (this.aso.getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_DISCONNECTED) {
                getDLMSConnection().connectMAC();
                this.aso.createAssociation();
                checkCacheObjects();
            }

        } catch (DLMSConnectionException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * Check the cached objects, update them if necessary (indicated by the iConfig value)
     *
     * @throws IOException when the communication with the meter failed
     */
    protected void checkCacheObjects() throws IOException {
        try { // conf program change and object list stuff
            int iConf;

            if (dlmsCache == null) {
                dlmsCache = new DLMSCache();
            }

            if (dlmsCache.getObjectList() != null) {
                meterConfig.setInstantiatedObjectList(dlmsCache.getObjectList());
                try {
                    iConf = requestConfigurationProgramChanges();
                } catch (IOException e) {
                    iConf = -1;
                    logger.severe("DLMSSN Configuration change count not accessible, request object list.");
                    requestObjectList();
                    dlmsCache.saveObjectList(meterConfig.getInstantiatedObjectList());  // save object list in cache
                }

                if (iConf != dlmsCache.getConfProgChange()) {
                    logger.severe("DLMSSN configuration changed, request object list.");
                    requestObjectList();           // request object list again from rtu
                    dlmsCache.saveObjectList(meterConfig.getInstantiatedObjectList());  // save object list in cache
                    dlmsCache.setConfProgChange(iConf);  // set new configuration program change
                }
            } else { // Cache not exist
                logger.info("DLMSSN cache does not exist, request object list.");
                requestObjectList();
                try {
                    iConf = requestConfigurationProgramChanges();
                    dlmsCache.saveObjectList(meterConfig.getInstantiatedObjectList());  // save object list in cache
                    dlmsCache.setConfProgChange(iConf);  // set new configuration program change
                } catch (IOException e) {
                    iConf = -1;
                }
            }

        } catch (IOException e) {
            throw new IOException("connect() error, " + e.getMessage(), e);
        }

    }

    /*
     *  extendedLogging = 1 current set of logical addresses, extendedLogging = 2..17 historical set 1..16
     */
    protected String getRegistersInfo() throws IOException {
        StringBuilder builder = new StringBuilder();
        Iterator it;
        // all total and rate values...
        builder.append("********************* All instantiated objects in the meter *********************\n");
        for (int i = 0; i < getMeterConfig().getInstantiatedObjectList().length; i++) {
            UniversalObject uo = getMeterConfig().getInstantiatedObjectList()[i];
            builder.append(uo.getObisCode().toString())
                    .append(" ")
                    .append(uo.getObisCode().toString())
                    .append(" - ShortName : ")
                    .append(uo.getBaseName())
                    .append(" (0x")
                    .append(Integer.toHexString(uo.getBaseName()))
                    .append(") - ClassId : ")
                    .append(uo.getClassID())
                    .append("\n");
        }

        if (getDeviceID().compareTo("EIT") != 0) {
            // all billing points values...
            builder.append("********************* Objects captured into billing points *********************\n");
            it = getCosemObjectFactory().getStoredValues().getProfileGeneric().getCaptureObjects().iterator();
            while (it.hasNext()) {
                CapturedObject capturedObject = (CapturedObject) it.next();
                builder.append(capturedObject.getLogicalName().getObisCode().toString()).append(" ").append(capturedObject.getLogicalName().getObisCode().toString()).append(" (billing point)\n");
            }
        }

        builder.append("********************* Objects captured into load profile *********************\n");
        it = getCosemObjectFactory().getLoadProfile().getProfileGeneric().getCaptureObjects().iterator();
        while (it.hasNext()) {
            CapturedObject capturedObject = (CapturedObject) it.next();
            builder.append(capturedObject.getLogicalName().getObisCode().toString()).append(" ").append(capturedObject.getLogicalName().getObisCode().toString()).append(" (load profile)\n");
        }

        return builder.toString();
    }

    @Override
    public void disconnect() throws IOException {
        try {
            if ((this.aso != null) && (this.aso.getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_CONNECTED)) {
                this.aso.releaseAssociation();
            }
            if (getDLMSConnection() != null) {
                getDLMSConnection().disconnectMAC();
            }
        } catch (IOException | DLMSConnectionException e) {
            //absorb -> trying to close communication
            getLogger().log(Level.FINEST, e.getMessage());
        }
    }

    @Override
    public void setTime() throws IOException {
        Calendar calendar;
        if (isRequestTimeZone()) {
            if (dstFlag == 0) {
                calendar = ProtocolUtils.getCalendar(false, requestTimeZone());
            } else if (dstFlag == 1) {
                calendar = ProtocolUtils.getCalendar(true, requestTimeZone());
            } else {
                throw new IOException("setTime(), dst flag is unknown! setTime() before getTime()!");
            }
        } else {
            calendar = ProtocolUtils.initCalendar(false, getTimeZone());
        }

        calendar.add(Calendar.MILLISECOND, iRoundtripCorrection);
        doSetTime(calendar);
    }

    private void doSetTime(Calendar calendar) throws IOException {
        byte[] byteTimeBuffer = new byte[14];
        byteTimeBuffer[0] = AxdrType.OCTET_STRING.getTag();
        byteTimeBuffer[1] = 12; // length
        byteTimeBuffer[2] = (byte) (calendar.get(Calendar.YEAR) >> 8);
        byteTimeBuffer[3] = (byte) calendar.get(Calendar.YEAR);
        byteTimeBuffer[4] = (byte) (calendar.get(Calendar.MONTH) + 1);
        byteTimeBuffer[5] = (byte) calendar.get(Calendar.DAY_OF_MONTH);
        byte bDOW = (byte) calendar.get(Calendar.DAY_OF_WEEK);
        byteTimeBuffer[6] = bDOW-- == 1 ? (byte) 7 : bDOW;
        byteTimeBuffer[7] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
        byteTimeBuffer[8] = (byte) calendar.get(Calendar.MINUTE);
        byteTimeBuffer[9] = (byte) calendar.get(Calendar.SECOND);
        byteTimeBuffer[10] = (byte) 0xFF;
        byteTimeBuffer[11] = (byte) 0x80;
        byteTimeBuffer[12] = 0x00;

        if (isRequestTimeZone()) {
            if (dstFlag == 0) {
                byteTimeBuffer[13] = 0x00;
            } else if (dstFlag == 1) {
                byteTimeBuffer[13] = (byte) 0x80;
            } else {
                throw new IOException("doSetTime(), dst flag is unknown! setTime() before getTime()!");
            }
        } else {
            if (getTimeZone().inDaylightTime(calendar.getTime())) {
                byteTimeBuffer[13] = (byte) 0x80;
            } else {
                byteTimeBuffer[13] = 0x00;
            }
        }
        getCosemObjectFactory().getGenericWrite((short) meterConfig.getClockSN(), DLMSCOSEMGlobals.TIME_TIME).write(byteTimeBuffer);
    }

    @Override
    public Date getTime() throws IOException {
        Clock clock = getCosemObjectFactory().getClock();
        Date date = clock.getDateTime();
        dstFlag = clock.getDstFlag();
        return date;
    }

    private boolean requestDaylightSavingEnabled() throws IOException {
        return getCosemObjectFactory().getClock().isDsEnabled();
    }

    /**
     * This method requests for the COSEM object list in the remote meter. A list is byuild with LN and SN references.
     * This method must be executed before other request methods.
     *
     * @throws IOException
     */
    private void requestObjectList() throws IOException {
        meterConfig.setInstantiatedObjectList(getCosemObjectFactory().getAssociationSN().getBuffer());
    }

    public String getConfiguredSerialNumber() {
        return configuredSerialNumber;
    }

    /**
     * This method requests for the COSEM object Logical Name register.
     *
     * @throws IOException
     */
    private String requestAttribute(int iBaseName, int iOffset) throws IOException {
        return getCosemObjectFactory().getGenericRead(iBaseName, iOffset).getDataContainer().toString();
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        UniversalObject uo = meterConfig.getVersionObject();
        return getCosemObjectFactory().getGenericRead(uo.getBaseName(), uo.getValueAttributeOffset()).getString();
    }

    @Override
    public String getSerialNumber()  {
        UniversalObject uo;
        try {
            uo = meterConfig.getSerialNumberObject();
            return getCosemObjectFactory().getGenericRead(uo.getBaseName(), uo.getValueAttributeOffset()).getString();
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, iProtocolRetriesProperty + 1);
        }
    }

    /**
     * This method requests for the NR of intervals that can be stored in the memory of the remote meter.
     *
     * @return NR of intervals that can be stored in the memory of the remote meter.
     * @throws IOException
     */
    private int getNROfIntervals() throws IOException {
        if (iNROfIntervals == -1) {
            iNROfIntervals = getCosemObjectFactory().getLoadProfile().getProfileGeneric().getProfileEntries();
        }
        return iNROfIntervals;
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        int iNROfIntervals = getNROfIntervals();
        int iInterval = getProfileInterval() / 60;
        Calendar fromCalendar = ProtocolUtils.getCalendar(getTimeZone());
        fromCalendar.add(Calendar.MINUTE, (-1) * iNROfIntervals * iInterval);
        return doGetProfileData(fromCalendar, ProtocolUtils.getCalendar(getTimeZone()), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(getTimeZone());
        fromCalendar.setTime(lastReading);
        return doGetProfileData(fromCalendar, ProtocolUtils.getCalendar(getTimeZone()), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(getTimeZone());
        fromCalendar.setTime(from);
        Calendar toCalendar = ProtocolUtils.getCleanCalendar(getTimeZone());
        toCalendar.setTime(to);
        return doGetProfileData(fromCalendar, toCalendar, includeEvents);
    }

    private ProfileData doGetProfileData(Calendar fromCalendar, Calendar toCalendar, boolean includeEvents) throws IOException {
        byte bNROfChannels = (byte) getNumberOfChannels();     //GN |13052008| otherwise this stays at -1
        return doGetDemandValues(fromCalendar, toCalendar, bNROfChannels, includeEvents);
    }

    private ProfileData doGetDemandValues(Calendar fromCalendar, Calendar toCalendar, byte bNROfChannels, boolean includeEvents) throws IOException {
        ProfileData profileData;
        ScalerUnit[] scalerunit;

        UniversalObject[] intervalList = getCosemObjectFactory().getLoadProfile().getProfileGeneric().getBufferAsUniversalObjects(fromCalendar, toCalendar);
        profileData = new ProfileData();
        scalerunit = new ScalerUnit[bNROfChannels];
        for (int i = 0; i < bNROfChannels; i++) {
            scalerunit[i] = getMeterDemandRegisterScalerUnit(i);
            ChannelInfo channelInfo = new ChannelInfo(i, "dlms" + getDeviceID() + "_channel_" + i, scalerunit[i].getEisUnit());
            if (scalerunit[i].getEisUnit().isUndefined()) {
                channelInfo.setMultiplier(new BigDecimal(new BigInteger("1"), -scalerunit[i].getEisUnit().getScale()));
            }

            if (DEBUG >= 1) {
                System.out.println("KV_DEBUG> " + meterConfig.getChannelObject(i).toStringCo());
            }

            if (meterConfig.getChannelObject(i).isCapturedObjectCumulative()) {

                if (meterConfig.getChannelObject(i).isCapturedObjectPulses()) {
                    if (channelMap != null) {
                        channelInfo.setCumulativeWrapValue((channelMap.getProtocolChannel(i) != null) ? channelMap.getProtocolChannel(i).getWrapAroundValue() : BigDecimal.valueOf(Long.MAX_VALUE));
                    } else {
                        channelInfo.setCumulativeWrapValue(BigDecimal.valueOf(Long.MAX_VALUE));
                        if (DEBUG >= 1) {
                            System.out.println("KV_DEBUG> channel " + i + " is cumulative 64 bit");
                        }
                    }
                } else {
                    if (channelMap != null) {
                        channelInfo.setCumulativeWrapValue((channelMap.getProtocolChannel(i) != null) ? channelMap.getProtocolChannel(i).getWrapAroundValue() : BigDecimal.valueOf(2 ^ 32));
                    } else {
                        channelInfo.setCumulativeWrapValue(BigDecimal.valueOf(0xFFFFFFFFl));
                        if (DEBUG >= 1) {
                            System.out.println("KV_DEBUG> channel " + i + " is cumulative 32 bit");
                        }
                    }
                }
            }
            profileData.addChannel(channelInfo);
        }

        buildProfileData(bNROfChannels, profileData, scalerunit, intervalList);

        if (includeEvents) {
            getEventLog(profileData, fromCalendar, toCalendar);
            // Apply the events to the channel statusvalues
            profileData.applyEvents(getProfileInterval() / 60);
        }
        return profileData;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.stringSpec(NODEID.getName()),
                this.stringSpecOfMaxLength(ADDRESS.getName(), 16),
                this.stringSpec(PASSWORD.getName()),
                this.stringSpec(SERIALNUMBER.getName()),
                this.integerSpec(TIMEOUT.getName()),
                this.integerSpec(RETRIES.getName()),
                this.integerSpec(PROPNAME_DELAY_AFTERFAIL),
                this.integerSpec(PROPNAME_REQUEST_TIME_ZONE),
                this.integerSpec(PROPNAME_REQUEST_CLOCK_OBJECT),
                this.integerSpec(PROPNAME_CLIENT_MAC_ADDRESS),
                this.integerSpec(PROPNAME_SERVER_LOWER_MAC_ADDRESS),
                this.integerSpec(PROPNAME_SERVER_UPPER_MAC_ADDRESS),
                this.integerSpec(ROUNDTRIPCORRECTION.getName()),
                this.integerSpec(PROPNAME_EXTENDED_LOGGING),
                this.integerSpec(PROPNAME_ADDRESSING_MODE),
                this.integerSpec(PROPNAME_CONNECTION),
                ProtocolChannelMap.propertySpec(PROPNAME_CHANNEL_MAP, false),
                this.stringSpec(SECURITYLEVEL.getName()),
                this.integerSpec(PROPNAME_IIAP_INVOKE_ID),
                this.integerSpec(PROPNAME_IIAP_PRIORITY),
                this.integerSpec(PROPNAME_IIAP_SERVICE_CLASS),
                this.integerSpec(PROPNAME_CIPHERING_TYPE),
                this.integerSpec(PROPNAME_MAX_PDU_SIZE),
                this.integerSpec(PROPNAME_IFORCEDELAY_BEFORE_SEND));
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

    protected PropertySpec integerSpec(String name) {
        return this.spec(name, this.propertySpecService::integerSpec);
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        try {
            this.doSetProperties(properties);
            this.properties = properties.toStringProperties();
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, this.getClass().getSimpleName() + ": validation of properties failed before");
        }
    }

    protected void doSetProperties(TypedProperties properties) throws PropertyValidationException {
        nodeId = properties.getTypedProperty(NODEID.getName(), "");
        strID = properties.getTypedProperty(ADDRESS.getName());
        strPassword = properties.getTypedProperty(PASSWORD.getName());
        iHDLCTimeoutProperty = Integer.parseInt(properties.getTypedProperty(TIMEOUT.getName(), "10000").trim());
        iProtocolRetriesProperty = Integer.parseInt(properties.getTypedProperty(RETRIES.getName(), "5").trim());
        iDelayAfterFailProperty = Integer.parseInt(properties.getTypedProperty(PROPNAME_DELAY_AFTERFAIL, "3000").trim());
        iRequestTimeZone = Integer.parseInt(properties.getTypedProperty(PROPNAME_REQUEST_TIME_ZONE, "0").trim());
        iRequestClockObject = Integer.parseInt(properties.getTypedProperty(PROPNAME_REQUEST_CLOCK_OBJECT, "0").trim());
        iRoundtripCorrection = Integer.parseInt(properties.getTypedProperty(ROUNDTRIPCORRECTION.getName(), "0").trim());
        // KV 19012004 get the serialNumber
        configuredSerialNumber = properties.getTypedProperty(SERIALNUMBER.getName(), "");
        extendedLogging = Integer.parseInt(properties.getTypedProperty(PROPNAME_EXTENDED_LOGGING, "0"));
        addressingMode = Integer.parseInt(properties.getTypedProperty(PROPNAME_ADDRESSING_MODE, "-1"));
        connectionMode = Integer.parseInt(properties.getTypedProperty(PROPNAME_CONNECTION, "0")); // 0=HDLC, 1= TCP/IP, 2=cosemPDUconnection
        if ("".equalsIgnoreCase(properties.getTypedProperty(PROPNAME_CHANNEL_MAP, ""))) {
            channelMap = null;
        } else {
            channelMap = new ProtocolChannelMap(((String) properties.getTypedProperty(PROPNAME_CHANNEL_MAP)));
        }
        String[] securityLevel = properties.getTypedProperty(SECURITYLEVEL.getName(), "1").split(":");
        this.authenticationSecurityLevel = Integer.parseInt(securityLevel[0]);
        if (securityLevel.length == 2) {
            this.datatransportSecurityLevel = Integer.parseInt(securityLevel[1]);
        } else if (securityLevel.length == 1) {
            this.datatransportSecurityLevel = 0;
        } else {
            throw new IllegalArgumentException("SecurityLevel property contains an illegal value " + properties.getTypedProperty("SecurityLevel", "1"));
        }
        iiapInvokeId = Integer.parseInt(properties.getTypedProperty(PROPNAME_IIAP_INVOKE_ID, "0"));
        iiapPriority = Integer.parseInt(properties.getTypedProperty(PROPNAME_IIAP_PRIORITY, "1"));
        iiapServiceClass = Integer.parseInt(properties.getTypedProperty(PROPNAME_IIAP_SERVICE_CLASS, "1"));
        cipheringType = Integer.parseInt(properties.getTypedProperty(PROPNAME_CIPHERING_TYPE, Integer.toString(CipheringType.GLOBAL.getType())));
        maxPduSize = Integer.parseInt(properties.getTypedProperty(PROPNAME_MAX_PDU_SIZE, MAX_PDU_SIZE));
        iForceDelay = Integer.parseInt(properties.getTypedProperty(PROPNAME_IFORCEDELAY_BEFORE_SEND, "100"));
    }

    @Override
    public String getRegister(String name) throws IOException {
        DLMSObis ln = new DLMSObis(name);
        if (ln.isLogicalName()) {
            return requestAttribute(meterConfig.getObject(ln).getBaseName(), (short) ((ln.getOffset() - 1) * 8));
        } else if (name.compareTo("PROGRAM_CONF_CHANGES") == 0) {
            return String.valueOf(requestConfigurationProgramChanges());
        } else if (name.compareTo("GET_CLOCK_OBJECT") == 0) {
            requestClockObject();
            return null;
        } else {
            throw new NoSuchRegisterException("DLMS,getRegister, register " + name + " does not exist.");
        }
    }

    @Override
    public void setRegister(String name, String value) throws UnsupportedException {
        throw new UnsupportedException();
    }

    @Override
    public void initializeDevice() throws UnsupportedException {
        throw new UnsupportedException();
    }

    private void requestClockObject() {
        if (iRequestClockObject == 1) {
            try {
                logger.severe("DLMSSN Clock time                       : " + getTime());
            } catch (IOException e) {
                logger.severe("time attribute error");
            }
            //try{logger.severe ("DLMSSN Clock time_zone                  : "+requestTimeZone());}catch(IOException e){logger.severe ("time_zone attribute error");}
            try {
                logger.severe("DLMSSN Clock time_zone                  : " + requestAttributeLong(meterConfig.getClockSN(), DLMSCOSEMGlobals.TIME_TIME_ZONE));
            } catch (IOException e) {
                logger.severe("time_zone attribute error");
            }
            try {
                logger.severe("DLMSSN Clock status                     : " + requestAttributeLong(meterConfig.getClockSN(), DLMSCOSEMGlobals.TIME_STATUS));
            } catch (IOException e) {
                logger.severe("status attribute error");
            }
            try {
                logger.severe("DLMSSN Clock daylight_savings_begin     : " + requestAttributeString(meterConfig.getClockSN(), DLMSCOSEMGlobals.TIME_DS_BEGIN));
            } catch (IOException e) {
                logger.severe("DS begin attribute error");
            }
            try {
                logger.severe("DLMSSN Clock daylight_savings_end       : " + requestAttributeString(meterConfig.getClockSN(), DLMSCOSEMGlobals.TIME_DS_END));
            } catch (IOException e) {
                logger.severe("DS end attribute error");
            }
            try {
                logger.severe("DLMSSN Clock daylight_savings_deviation : " + requestAttributeLong(meterConfig.getClockSN(), DLMSCOSEMGlobals.TIME_DS_DEVIATION));
            } catch (IOException e) {
                logger.severe("DS deviation attribute error");
            }
            try {
                logger.severe("DLMSSN Clock daylight_saving_enabled    : " + requestDaylightSavingEnabled());
            } catch (IOException e) {
                logger.severe("DS enebled attribute error");
            }
        }
    }

    public int requestConfigurationProgramChanges() throws IOException {
        if (iConfigProgramChange == -1) {
            iConfigProgramChange = (int) getCosemObjectFactory().getCosemObject(getMeterConfig().getConfigObject().getObisCode()).getValue();
        }
        return iConfigProgramChange;
    }

    protected int requestTimeZone() throws IOException {
        if (iMeterTimeZoneOffset == 255) {
            iMeterTimeZoneOffset = getCosemObjectFactory().getClock().getTimeZone();
        }
        return iMeterTimeZoneOffset;
    }

    private long requestAttributeLong(int iBaseName, int iOffset) throws IOException {
        return getCosemObjectFactory().getGenericRead(iBaseName, iOffset).getValue();
    }

    private String requestAttributeString(int iBaseName, int iOffset) {
        return getCosemObjectFactory().getGenericRead(iBaseName, iOffset).toString();
    }

    @Override
    public boolean isRequestTimeZone() {
        return (iRequestTimeZone != 0);
    }

    protected void setRequestTimeZone(int requestTimeZone) {
        this.iRequestTimeZone = requestTimeZone;
    }

    @Override
    public TimeZone getTimeZone() {
        return timeZone;
    }

    @Override
    public void setCache(Serializable cacheObject) {
        this.dlmsCache = (DLMSCache) cacheObject;
    }

    @Override
    public Serializable getCache() {
        return dlmsCache;
    }

    @Override
    public Serializable fetchCache(int deviceId, Connection connection) throws SQLException, ProtocolCacheFetchException {
        if (deviceId != 0) {
            RtuDLMSCache rtuCache = new RtuDLMSCache(deviceId);
            RtuDLMS rtu = new RtuDLMS(deviceId);
            return new DLMSCache(rtuCache.getObjectList(connection), rtu.getConfProgChange(connection));
        } else {
            throw new IllegalArgumentException("invalid RtuId!");
        }
    }

    @Override
    public void updateCache(int deviceId, Serializable cacheObject, Connection connection) throws SQLException, ProtocolCacheUpdateException {
        if (deviceId != 0) {
            DLMSCache dc = (DLMSCache) cacheObject;
            if (dc.contentChanged()) {
                //System.out.println("KV_DEBUG>> deviceId="+deviceId+", "+new Date()+" update cache="+dc.getObjectList()+", confchange="+dc.getConfProgChange()+", ischanged="+dc.isChanged()); // KV_DEBUG
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
        return calendar.get(Calendar.YEAR) + "_" + (calendar.get(Calendar.MONTH) + 1) + "_" + calendar.get(Calendar.DAY_OF_MONTH) + "_" + strID + "_" + strPassword + "_" + configuredSerialNumber + "_" + iServerUpperMacAddress + "_DLMSSN.cache";
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, false);
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        HHUSignOn hhuSignOn =
                new IEC1107HHUConnection(commChannel, iHDLCTimeoutProperty, iProtocolRetriesProperty, 300, 0);
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
    public int getRoundTripCorrection() {
        return iRoundtripCorrection;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    public com.energyict.dlms.cosem.CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }

    @Override
    public int getReference() {
        return ProtocolLink.SN_REFERENCE;
    }

    @Override
    public StoredValues getStoredValues() {
        return storedValuesImpl;
    }

    public void setDLMSConnection(DLMSConnection connection) {
        this.dlmsConnection = connection;
    }

    public Properties getProperties() {
        return this.properties;
    }

}