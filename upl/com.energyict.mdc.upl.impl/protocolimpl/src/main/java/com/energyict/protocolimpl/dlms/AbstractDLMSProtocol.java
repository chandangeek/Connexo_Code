package com.energyict.protocolimpl.dlms;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dlms.*;
import com.energyict.dlms.aso.*;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.*;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.dlms.common.NTASecurityProvider;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.*;
import static com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.*;

/**
 * Copyrights EnergyICT
 * Date: 9-dec-2010
 * Time: 11:09:31
 */
public abstract class AbstractDLMSProtocol extends AbstractProtocol implements ProtocolLink, HHUEnabler {

    protected static final String PROPNAME_INFORMATION_FIELD_SIZE = "InformationFieldSize";
    protected static final String PROPNAME_CONNECTION = "Connection";
    protected static final String PROPNAME_CLIENT_MAC_ADDRESS = "ClientMacAddress";
    protected static final String PROPNAME_SERVER_LOWER_MAC_ADDRESS = "ServerLowerMacAddress";
    protected static final String PROPNAME_SERVER_UPPER_MAC_ADDRESS = "ServerUpperMacAddress";
    private static final int CONNECTION_MODE_HDLC = 0;
    private static final int CONNECTION_MODE_TCPIP = 1;
    private static final int CONNECTION_MODE_COSEM_PDU = 2;
    private static final int CONNECTION_MODE_LLC = 3;
    private static final int PROPOSED_QOS = -1;
    private static final int PROPOSED_DLMS_VERSION = 6;
    private static final int MAX_PDU_SIZE = 200;
    private static final int DEFAULT_MAXIMUM_NUMBER_OF_CLOCKSET_TRIES = 10;
    private static final int DEFAULT_CLOCKSET_ROUNDTRIP_CORRECTION_TRESHOLD = 5000;
    private static final String PROPNAME_IIAP_INVOKE_ID = "IIAPInvokeId";
    private static final String PROPNAME_IIAP_PRIORITY = "IIAPPriority";
    private static final String PROPNAME_IIAP_SERVICE_CLASS = "IIAPServiceClass";
    private static final String PROPNAME_CIPHERING_TYPE = "CipheringType";
    private static final String PROPNAME_ADDRESSING_MODE = "AddressingMode";
    private static final String PROPNAME_MANUFACTURER = "Manufacturer";
    private static final String PROPNAME_RETRIES = RETRIES.getName();
    private static final String PROPNAME_TIMEOUT = TIMEOUT.getName();
    private static final String PROPNAME_FORCE_DELAY = PROP_FORCED_DELAY;
    private static final String PROPNAME_MAXIMUM_NUMBER_OF_CLOCKSET_TRIES = "MaximumNumberOfClockSetTries";
    private static final String PROPNAME_CLOCKSET_ROUNDTRIP_CORRECTION_THRESHOLD = "ClockSetRoundtripCorrectionTreshold";
    private static final int ISKRA_WRAPPER_DEFAULT = 1;
    private static final String INCREMENT_FRAMECOUNTER_FOR_RETRIES_DEFAULT = "1";
    /** Default for the increment fc for reploy-to-hls. */
    private static final String INCREMENT_FRAMECOUNTER_FOR_REPLY_TO_HLS_DEFAULT = "1";
    protected ApplicationServiceObject aso;
    protected DLMSCache dlmsCache;
    protected ConformanceBlock conformanceBlock;
    protected CosemObjectFactory cosemObjectFactory;
    protected DLMSConnection dlmsConnection;
    protected DLMSMeterConfig dlmsMeterConfig;
    protected TypedProperties properties;
    protected Logger logger;
    protected TimeZone timeZone = null;
    protected SecurityContext securityContext;
    protected String firmwareVersion;
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
    protected int numberOfClocksetTries;
    protected int numberOfChannels = -1;
    protected int forceDelay;
    protected int retries;
    protected int addressingMode;
    protected int informationFieldSize;
    protected int cipheringType;
    protected String manufacturer;
    protected int opticalBaudrate;
    protected String callingAPTitle = "";
    protected String serialNumber;
    protected String nodeId;
    protected String deviceId;
    protected int iConfigProgramChange = -1;
    protected int profileInterval = -1;
    protected int clockSetRoundtripTreshold = 0;
    protected String maxTimeDifference;
    protected int maxRecPduSize;
    private int iskraWrapper = 1;
    private boolean incrementFrameCounterForRetries;
    /** Indicates whether or not to increment the FC for the reply-to-hls. */
    private boolean incrementFrameCounterForReplyToHLS;

    public AbstractDLMSProtocol(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    @Override
    protected void doConnect() throws IOException {
        connect();
    }

    @Override
    public ApplicationServiceObject getAso() {
        return aso;
    }

    @Override
    protected ProtocolConnection doInit(InputStream inputStream, OutputStream outputStream, int timeoutProperty, int protocolRetriesProperty, int forcedDelay, int echoCancelling, int protocolCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) throws IOException {
        return null;
    }

    @Override
    protected void doDisconnect() throws IOException {
        disconnect();
    }

    @Override
    public TimeZone getTimeZone() {
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
            logger.warning("Using default time zone.");
        }
        return timeZone;
    }

    @Override
    public DLMSConnection getDLMSConnection() {
        return this.dlmsConnection;
    }

    @Override
    public DLMSMeterConfig getMeterConfig() {
        return this.dlmsMeterConfig;
    }

    @Override
    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }

    public Serializable getCache() {
        if (dlmsCache == null) {
            dlmsCache = new DLMSCache();
        }
        return dlmsCache;
    }

    public void setCache(Serializable cacheObject) {
        this.dlmsCache = (DLMSCache) cacheObject;
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        this.timeZone = timeZone;
        this.logger = logger;
        iConfigProgramChange = -1;
        this.cosemObjectFactory = new CosemObjectFactory(this);
        this.dlmsMeterConfig = DLMSMeterConfig.getInstance(manufacturer);
        initDLMSConnection(inputStream, outputStream);
    }

    /**
     * Identifier of the system calling a meter (e.g. the RTU+Server)
     *
     * @return callingAPTitle
     */
    protected byte[] getCallingAPTitle() {
        return ProtocolTools.getBytesFromHexString(callingAPTitle, "");
    }

    public void setCallingAPTitle(String callingAPTitle) {
        this.callingAPTitle = callingAPTitle;
    }

    /**
     * This should be the serial number of the called meter.
     * Subclasses can override and change behaviour.
     *
     * @return serial number of the called meter
     */
    protected byte[] getCalledAPTitle() {
        return serialNumber.getBytes();
    }

    protected String getSerialNumberProperty() {
        return serialNumber;
    }

    /**
     * Starts the DLMS connection
     *
     * @throws IOException when the connection failed
     */
    protected void initDLMSConnection(InputStream inputStream, OutputStream outputStream) throws IOException {
        DLMSConnection connection;
        try {
            switch (connectionMode) {
                case CONNECTION_MODE_HDLC:
                    connection = new HDLC2Connection(inputStream, outputStream, timeOut, 100, retries, clientMacAddress,
                            serverLowerMacAddress, serverUpperMacAddress, addressingMode, -1, -1);
                    break;
                case CONNECTION_MODE_TCPIP:
                    connection = new TCPIPConnection(inputStream, outputStream, timeOut, forceDelay, retries, clientMacAddress, serverLowerMacAddress, incrementFrameCounterForRetries, getLogger());
                    break;
                case CONNECTION_MODE_COSEM_PDU:
                    connection = new CosemPDUConnection(inputStream, outputStream, timeOut, forceDelay, retries, clientMacAddress, serverLowerMacAddress);
                    break;
                case CONNECTION_MODE_LLC:
                    connection = new LLCConnection(inputStream, outputStream, timeOut, forceDelay, retries, clientMacAddress, serverLowerMacAddress);
                    break;
                default:
                    throw new IOException("Unable to initialize dlmsConnection, connection property unknown: " + connectionMode);
            }
        } catch (DLMSConnectionException e) {
            throw new IOException(e.getMessage());
        }

        NTASecurityProvider localSecurityProvider = new NTASecurityProvider(this.properties);
        securityContext = new SecurityContext(datatransportSecurityLevel, authenticationSecurityLevel, 0, getCallingAPTitle(), localSecurityProvider, this.cipheringType, this.incrementFrameCounterForReplyToHLS);

        updateConformanceBlock();

        XdlmsAse xdlmsAse = new XdlmsAse(isCiphered() ? localSecurityProvider.getDedicatedKey() : null, true, PROPOSED_QOS, PROPOSED_DLMS_VERSION, this.conformanceBlock, maxRecPduSize);
        aso = new ApplicationServiceObject(xdlmsAse, this, securityContext, getContextId(), getCalledAPTitle(), null, null);
        dlmsConnection = new SecureConnection(aso, connection);
        this.dlmsConnection.setIskraWrapper(this.iskraWrapper);
        InvokeIdAndPriorityHandler iiapHandler = buildInvokeIdAndPriorityHandler();
        this.dlmsConnection.setInvokeIdAndPriorityHandler(iiapHandler);
    }

    protected void updateConformanceBlock() throws InvalidPropertyException {
        if (this.conformanceBlock == null) {
            if (getReference() == ProtocolLink.SN_REFERENCE) {
                this.conformanceBlock = new ConformanceBlock(ConformanceBlock.DEFAULT_SN_CONFORMANCE_BLOCK);
            } else if (getReference() == ProtocolLink.LN_REFERENCE) {
                this.conformanceBlock = new ConformanceBlock(ConformanceBlock.DEFAULT_LN_CONFORMANCE_BLOCK);
            } else {
                throw new InvalidPropertyException("Invalid reference method, only 0 and 1 are allowed.");
            }
        }
    }

    protected InvokeIdAndPriorityHandler buildInvokeIdAndPriorityHandler() throws IOException {
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

    /**
     * Returns a boolean whether or not there's encryption used
     *
     * @return boolean
     */
    protected boolean isCiphered() {
        return (getContextId() == AssociationControlServiceElement.LOGICAL_NAME_REFERENCING_WITH_CIPHERING) || (getContextId() == AssociationControlServiceElement.SHORT_NAME_REFERENCING_WITH_CIPHERING);
    }

    /**
     * Getter for the context ID
     *
     * @return the context ID
     */
    protected int getContextId() {
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
    public List<PropertySpec> getUPLPropertySpecs() {
        PropertySpecService propertySpecService = this.getPropertySpecService();
        List<PropertySpec> myPropertySpecs = new ArrayList<>();
        myPropertySpecs.add(this.integerSpec(PROPNAME_CONNECTION, PropertyTranslationKeys.DLMS_CONNECTION, false));
        myPropertySpecs.add(this.integerSpec(PROPNAME_SERVER_LOWER_MAC_ADDRESS, PropertyTranslationKeys.DLMS_SERVER_LOWER_MAC_ADDRESS, false));
        myPropertySpecs.add(this.integerSpec(PROPNAME_SERVER_UPPER_MAC_ADDRESS, PropertyTranslationKeys.DLMS_SERVER_UPPER_MAC_ADDRESS, false));
        myPropertySpecs.add(this.integerSpec(PROPNAME_ADDRESSING_MODE, PropertyTranslationKeys.DLMS_ADDRESSING_MODE, false));
        myPropertySpecs.add(this.stringSpec(PROPNAME_MANUFACTURER, PropertyTranslationKeys.DLMS_MANUFACTURER, false));
        myPropertySpecs.add(this.integerSpec(PROPNAME_INFORMATION_FIELD_SIZE, PropertyTranslationKeys.DLMS_MANUFACTURER, false));
        myPropertySpecs.add(this.integerSpec(PROPNAME_IIAP_INVOKE_ID, PropertyTranslationKeys.DLMS_IIAP_INVOKE_ID, false));
        myPropertySpecs.add(this.integerSpec(PROPNAME_IIAP_PRIORITY, PropertyTranslationKeys.DLMS_IIAP_PRIORITY, false));
        myPropertySpecs.add(this.integerSpec(PROPNAME_IIAP_SERVICE_CLASS, PropertyTranslationKeys.DLMS_SERVICE_CLASS, false));
        myPropertySpecs.add(
                UPLPropertySpecFactory
                        .specBuilder(PROPNAME_CIPHERING_TYPE, false, PropertyTranslationKeys.DLMS_CIPHERING_TYPE, propertySpecService::integerSpec)
                        .addValues(CipheringType.GLOBAL.getType(), CipheringType.DEDICATED.getType())
                        .markExhaustive()
                        .finish());
        myPropertySpecs.add(this.integerSpec(PROPNAME_MAXIMUM_NUMBER_OF_CLOCKSET_TRIES, PropertyTranslationKeys.DLMS_MAXIMUM_NUMBER_OF_CLOCKSET_TRIES, false));
        myPropertySpecs.add(this.integerSpec(PROPNAME_CLOCKSET_ROUNDTRIP_CORRECTION_THRESHOLD, PropertyTranslationKeys.DLMS_CLOCKSET_ROUDTRIP_CORRECTION_TRESHOLD, false));
        myPropertySpecs.add(this.integerSpec(MAX_REC_PDU_SIZE, PropertyTranslationKeys.DLMS_MAX_REC_PDU_SIZE, false));
        myPropertySpecs.add(this.integerSpec(ISKRA_WRAPPER, PropertyTranslationKeys.DLMS_ISKRA_WRAPPER, false));
        myPropertySpecs.add(this.stringSpec(INCREMENT_FRAMECOUNTER_FOR_RETRIES, PropertyTranslationKeys.DLMS_INCREMENT_FRAMECOUNTER_FOR_RETRIES, false));
        super.getUPLPropertySpecs().forEach(myPropertySpecs::add);
        return myPropertySpecs;
    }

    @Override
    protected int defaultForcedDelayPropertyValue() {
        return 1;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        this.properties = properties;

        nodeId = properties.getTypedProperty(NODEID.getName(), "");
        deviceId = properties.getTypedProperty(ADDRESS.getName(), "");
        serialNumber = properties.getTypedProperty(SERIALNUMBER.getName(), "");
        connectionMode = properties.getTypedProperty(PROPNAME_CONNECTION, 1);
        clientMacAddress = properties.getTypedProperty(PROPNAME_CLIENT_MAC_ADDRESS, BigDecimal.valueOf(32)).intValue();
        serverLowerMacAddress = properties.getTypedProperty(PROPNAME_SERVER_LOWER_MAC_ADDRESS, 1);
        serverUpperMacAddress = properties.getTypedProperty(PROPNAME_SERVER_UPPER_MAC_ADDRESS, 17);
        timeOut = Integer.parseInt(properties.getTypedProperty(PROPNAME_TIMEOUT, (this.connectionMode == 0) ? "5000" : "60000"));    // set the HDLC timeout to 5000 for the WebRTU KP
        forceDelay = properties.getTypedProperty(PROPNAME_FORCE_DELAY, 1);
        retries = Integer.parseInt(properties.getTypedProperty(PROPNAME_RETRIES, "3"));
        addressingMode = properties.getTypedProperty(PROPNAME_ADDRESSING_MODE, 2);
        manufacturer = properties.getTypedProperty(PROPNAME_MANUFACTURER, "EIT");
        informationFieldSize = properties.getTypedProperty(PROPNAME_INFORMATION_FIELD_SIZE, -1);
        iiapInvokeId = properties.getTypedProperty(PROPNAME_IIAP_INVOKE_ID, 0);
        iiapPriority = properties.getTypedProperty(PROPNAME_IIAP_PRIORITY, 1);
        iiapServiceClass = properties.getTypedProperty(PROPNAME_IIAP_SERVICE_CLASS, 1);
        cipheringType = properties.getTypedProperty(PROPNAME_CIPHERING_TYPE, CipheringType.GLOBAL.getType());

        this.numberOfClocksetTries = properties.getTypedProperty(PROPNAME_MAXIMUM_NUMBER_OF_CLOCKSET_TRIES, DEFAULT_MAXIMUM_NUMBER_OF_CLOCKSET_TRIES);
        this.clockSetRoundtripTreshold = properties.getTypedProperty(PROPNAME_CLOCKSET_ROUNDTRIP_CORRECTION_THRESHOLD, DEFAULT_CLOCKSET_ROUNDTRIP_CORRECTION_TRESHOLD);
        this.maxRecPduSize = properties.getTypedProperty(MAX_REC_PDU_SIZE, MAX_PDU_SIZE);
        this.iskraWrapper = properties.getTypedProperty(ISKRA_WRAPPER, ISKRA_WRAPPER_DEFAULT);
        this.incrementFrameCounterForRetries = Boolean.parseBoolean(properties.getTypedProperty(INCREMENT_FRAMECOUNTER_FOR_RETRIES, INCREMENT_FRAMECOUNTER_FOR_RETRIES_DEFAULT));
        this.incrementFrameCounterForReplyToHLS = Boolean.parseBoolean(properties.getTypedProperty(INCREMENT_FRAMECOUNTER_FOR_REPLY_TO_HLS, INCREMENT_FRAMECOUNTER_FOR_REPLY_TO_HLS_DEFAULT));
    }

    @Override
    protected void setSecurityLevelFrom(TypedProperties properties) {
        String securityLevelPropertyValue = properties.getTypedProperty(SECURITYLEVEL.getName(), "0");
        String[] securityLevel = securityLevelPropertyValue.split(":");
        this.authenticationSecurityLevel = Integer.parseInt(securityLevel[0]);
        if (securityLevel.length == 2) {
            this.datatransportSecurityLevel = Integer.parseInt(securityLevel[1]);
        } else if (securityLevel.length == 1) {
            this.datatransportSecurityLevel = 0;
        } else {
            throw new IllegalArgumentException("SecurityLevel property contains an illegal value " + securityLevelPropertyValue);
        }
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
            throw new NestedIOException(e);
        }
    }

    /**
     * Return the iConfig value. This indicates if a parameter in the meter has been changed
     *
     * @return
     * @throws IOException when the communication with the meter failed
     */
    public int requestConfigurationProgramChanges() throws IOException {
        if (iConfigProgramChange == -1) {
            iConfigProgramChange = (int) getCosemObjectFactory().getCosemObject(getMeterConfig().getConfigObject().getObisCode()).getValue();
        }
        return iConfigProgramChange;
    }

    public CosemObjectFactory getCosemObjectFactory() {
        if (cosemObjectFactory == null) {
            this.cosemObjectFactory = new CosemObjectFactory(this);
        }
        return cosemObjectFactory;
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
                dlmsMeterConfig.setInstantiatedObjectList(dlmsCache.getObjectList());
                try {
                    iConf = requestConfigurationProgramChanges();
                } catch (IOException e) {
                    iConf = -1;
                    logger.severe("Meter configuration change count not accessible, request object list.");
                    requestObjectList();
                    dlmsCache.saveObjectList(dlmsMeterConfig.getInstantiatedObjectList());  // save object list in cache
                }

                if (iConf != dlmsCache.getConfProgChange()) {
                    logger.severe("Meter configuration changed, request object list.");
                    requestObjectList();           // request object list again from rtu
                    dlmsCache.saveObjectList(dlmsMeterConfig.getInstantiatedObjectList());  // save object list in cache
                    dlmsCache.setConfProgChange(iConf);  // set new configuration program change
                }
            } else { // Cache not exist
                logger.info("Meter cache does not exist, request object list.");
                requestObjectList();
                try {
                    iConf = requestConfigurationProgramChanges();
                    dlmsCache.saveObjectList(dlmsMeterConfig.getInstantiatedObjectList());  // save object list in cache
                    dlmsCache.setConfProgChange(iConf);  // set new configuration program change
                } catch (IOException e) {
                    iConf = -1;
                }
            }

        } catch (IOException e) {
            IOException exception = new IOException("connect() error, " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }

    }

    /**
     * Gets a new list with the objects in the meter, to update the protocol's cache
     *
     * @throws IOException when the communication with the meter failed
     */
    protected void requestObjectList() throws IOException {
        try {
            if (getReference() == ProtocolLink.LN_REFERENCE) {
                UniversalObject[] objects = getCosemObjectFactory().getAssociationLN().getBuffer();
                getMeterConfig().setInstantiatedObjectList(objects);
            } else if (getReference() == ProtocolLink.SN_REFERENCE) {
                getMeterConfig().setInstantiatedObjectList(getCosemObjectFactory().getAssociationSN().getBuffer());
            } else {
                throw new IllegalArgumentException("Invalid reference method, only 0 and 1 are allowed.");
            }
        } catch (IOException e) {
            getLogger().log(Level.FINEST, e.getMessage());
            throw new IOException("Requesting configuration failed." + e);
        }
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

}