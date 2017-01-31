/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.HHUEnabler;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.legacy.HalfDuplexController;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.protocols.mdc.services.impl.OrmClient;

import com.energyict.dlms.CipheringType;
import com.energyict.dlms.CosemPDUConnection;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.HDLC2Connection;
import com.energyict.dlms.InvokeIdAndPriority;
import com.energyict.dlms.InvokeIdAndPriorityHandler;
import com.energyict.dlms.LLCConnection;
import com.energyict.dlms.NonIncrementalInvokeIdAndPriorityHandler;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.SecureConnection;
import com.energyict.dlms.TCPIPConnection;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.aso.AssociationControlServiceElement;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.aso.XdlmsAse;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.RTUCache;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.dlms.common.NTASecurityProvider;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractDLMSProtocol extends AbstractProtocol implements ProtocolLink, HHUEnabler {

    private final OrmClient ormClient;
    protected static final int CONNECTION_MODE_HDLC = 0;
    protected static final int CONNECTION_MODE_TCPIP = 1;
    protected static final int CONNECTION_MODE_COSEM_PDU = 2;
    protected static final int CONNECTION_MODE_LLC = 3;
    protected static final int PROPOSED_QOS = -1;
    protected static final int PROPOSED_DLMS_VERSION = 6;
    protected static final int MAX_PDU_SIZE = 200;
    protected static final int DEFAULT_MAXIMUM_NUMBER_OF_CLOCKSET_TRIES = 10;
    protected static final int DEFAULT_CLOCKSET_ROUNDTRIP_CORRECTION_TRESHOLD = 5000;
    protected static final String PROPNAME_INFORMATION_FIELD_SIZE = "InformationFieldSize";
    protected static final String PROPNAME_IIAP_INVOKE_ID = "IIAPInvokeId";
    protected static final String PROPNAME_IIAP_PRIORITY = "IIAPPriority";
    protected static final String PROPNAME_IIAP_SERVICE_CLASS = "IIAPServiceClass";
    protected static final String PROPNAME_CIPHERING_TYPE = "CipheringType";
    protected static final String PROPNAME_ADDRESSING_MODE = "AddressingMode";
    protected static final String PROPNAME_CONNECTION = "Connection";
    protected static final String PROPNAME_MANUFACTURER = "Manufacturer";
    protected static final String PROPNAME_SERVER_LOWER_MAC_ADDRESS = "ServerLowerMacAddress";
    protected static final String PROPNAME_SERVER_UPPER_MAC_ADDRESS = "ServerUpperMacAddress";
    protected static final String PROPNAME_CLIENT_MAC_ADDRESS = "ClientMacAddress";
    protected static final String PROPNAME_RETRIES = "Retries";
    protected static final String PROPNAME_TIMEOUT = "Timeout";
    protected static final String PROPNAME_FORCE_DELAY = "ForceDelay";
    protected static final String PROPNAME_MAXIMUM_NUMBER_OF_CLOCKSET_TRIES = "MaximumNumberOfClockSetTries";
    protected static final String PROPNAME_CLOCKSET_ROUNDTRIP_CORRECTION_THRESHOLD = "ClockSetRoundtripCorrectionTreshold";
    private static final String ISKRA_WRAPPER_DEFAULT = "1";
    private static final String INCREMENT_FRAMECOUNTER_FOR_RETRIES_DEFAULT = "1";
    protected ApplicationServiceObject aso;
    protected DLMSCache dlmsCache;
    protected ConformanceBlock conformanceBlock;
    protected CosemObjectFactory cosemObjectFactory;
    protected DLMSConnection dlmsConnection;
    protected DLMSMeterConfig dlmsMeterConfig;
    protected Properties properties;
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
    protected int wakeup;
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


    protected AbstractDLMSProtocol(PropertySpecService propertySpecService, OrmClient ormClient) {
        super(propertySpecService);
        this.ormClient = ormClient;
    }

    protected OrmClient getOrmClient() {
        return ormClient;
    }

    @Override
    protected void doConnect() throws IOException {
        connect();
    }

    public ApplicationServiceObject getAso() {
        return aso;
    }

    @Override
    protected ProtocolConnection doInit(InputStream inputStream, OutputStream outputStream, int timeoutProperty, int protocolRetriesProperty, int forcedDelay, int echoCancelling, int protocolCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) throws IOException {
        return null;
    }

    @Override
    protected void doDisConnect() throws IOException {
        disconnect();
    }

    @Override
    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
    }

    @Override
    protected List doGetOptionalKeys() {
        return getOptionalKeys();
    }

    /**
     * Getter for the time zone
     *
     * @return the time zone
     */
    public TimeZone getTimeZone() {
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
            logger.warning("Using default time zone.");
        }
        return timeZone;
    }

    public DLMSConnection getDLMSConnection() {
        return this.dlmsConnection;
    }

    public DLMSMeterConfig getMeterConfig() {
        return this.dlmsMeterConfig;
    }

    public void setCache(Object cacheObject) {
        this.dlmsCache = (DLMSCache) cacheObject;
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }

    public Object getCache() {
        if (dlmsCache == null) {
            dlmsCache = new DLMSCache();
        }
        return dlmsCache;
    }

    @Override
    public Object fetchCache(int rtuid) throws SQLException {
        if (rtuid != 0) {

            /* Use the RTUCache to get the blob from the database */
            RTUCache rtu = new RTUCache(rtuid, ormClient);
            try {
                return rtu.getCacheObject();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    @Override
    public void updateCache(int rtuid, Object cacheObject) throws SQLException {
        if (rtuid != 0) {
            /* Use the RTUCache to set the blob (cache) to the database */
            RTUCache rtu = new RTUCache(rtuid, ormClient);
            rtu.setBlob(cacheObject);
        }
    }

    /**
     * Starts the protocol, sets up the DLMS connection
     *
     * @param inputStream  communication inputstream
     * @param outputStream communication outputstream
     * @param timeZone     timezone of the meter
     * @param logger       framework logger object to be used by the protocol to log info
     * @throws IOException
     */
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
                    connection = new TCPIPConnection(inputStream, outputStream, timeOut, forceDelay, retries, clientMacAddress, serverLowerMacAddress, getLogger());
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
        securityContext = new SecurityContext(datatransportSecurityLevel, authenticationSecurityLevel, 0, getCallingAPTitle(), localSecurityProvider, this.cipheringType);

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

    /**
     * Sets and validates the properties
     *
     * @param properties Used by the framework
     * @throws InvalidPropertyException
     * @throws MissingPropertyException
     */
    public void setProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException {
        this.properties = properties;
        validateProperties();
    }

    /**
     * Validates the properties
     *
     * @throws MissingPropertyException when a property is missing
     * @throws InvalidPropertyException when a property is invalid
     */
    protected void validateProperties() throws MissingPropertyException, InvalidPropertyException {
        for (Object o : getRequiredKeys()) {
            String key = (String) o;
            if (this.properties.getProperty(key) == null) {
                throw new MissingPropertyException(key + " key missing");
            }
        }

        String[] securityLevel = properties.getProperty("SecurityLevel", "0").split(":");
        this.authenticationSecurityLevel = Integer.parseInt(securityLevel[0]);
        if (securityLevel.length == 2) {
            this.datatransportSecurityLevel = Integer.parseInt(securityLevel[1]);
        } else if (securityLevel.length == 1) {
            this.datatransportSecurityLevel = 0;
        } else {
            throw new IllegalArgumentException("SecurityLevel property contains an illegal value " + properties.getProperty("SecurityLevel", "0"));
        }

        nodeId = properties.getProperty(MeterProtocol.NODEID, "");
        deviceId = properties.getProperty(MeterProtocol.ADDRESS, "");
        serialNumber = properties.getProperty(MeterProtocol.SERIALNUMBER, "");
        connectionMode = Integer.parseInt(properties.getProperty(PROPNAME_CONNECTION, "1"));
        clientMacAddress = Integer.parseInt(properties.getProperty(PROPNAME_CLIENT_MAC_ADDRESS, "32"));
        serverLowerMacAddress = Integer.parseInt(properties.getProperty(PROPNAME_SERVER_LOWER_MAC_ADDRESS, "1"));
        serverUpperMacAddress = Integer.parseInt(properties.getProperty(PROPNAME_SERVER_UPPER_MAC_ADDRESS, "17"));
        timeOut = Integer.parseInt(properties.getProperty(PROPNAME_TIMEOUT, (this.connectionMode == 0) ? "5000" : "60000"));    // set the HDLC timeout to 5000 for the WebRTU KP
        forceDelay = Integer.parseInt(properties.getProperty(PROPNAME_FORCE_DELAY, "1"));
        retries = Integer.parseInt(properties.getProperty(PROPNAME_RETRIES, "3"));
        addressingMode = Integer.parseInt(properties.getProperty(PROPNAME_ADDRESSING_MODE, "2"));
        manufacturer = properties.getProperty(PROPNAME_MANUFACTURER, "EIT");
        informationFieldSize = Integer.parseInt(properties.getProperty(PROPNAME_INFORMATION_FIELD_SIZE, "-1"));
        iiapInvokeId = Integer.parseInt(properties.getProperty(PROPNAME_IIAP_INVOKE_ID, "0"));
        iiapPriority = Integer.parseInt(properties.getProperty(PROPNAME_IIAP_PRIORITY, "1"));
        iiapServiceClass = Integer.parseInt(properties.getProperty(PROPNAME_IIAP_SERVICE_CLASS, "1"));
        cipheringType = Integer.parseInt(properties.getProperty(PROPNAME_CIPHERING_TYPE, Integer.toString(CipheringType.GLOBAL.getType())));
        //roundtripCorrection = Integer.parseInt(properties.getProperty(PROPNAME_ROUNDTRIP_CORRECTION, "0").trim());

        if (cipheringType != CipheringType.GLOBAL.getType() && cipheringType != CipheringType.DEDICATED.getType()) {
            throw new InvalidPropertyException("Only 0 or 1 is allowed for the CipheringType property");
        }
        try {
            this.numberOfClocksetTries = Integer.parseInt(properties.getProperty(PROPNAME_MAXIMUM_NUMBER_OF_CLOCKSET_TRIES, String.valueOf(DEFAULT_MAXIMUM_NUMBER_OF_CLOCKSET_TRIES)));
        } catch (final NumberFormatException e) {
            logger.log(Level.SEVERE, "Cannot parse the number of clockset tries to a numeric value, setting to default value of [" + DEFAULT_MAXIMUM_NUMBER_OF_CLOCKSET_TRIES + "]", e);
            this.numberOfClocksetTries = DEFAULT_MAXIMUM_NUMBER_OF_CLOCKSET_TRIES;
        }
        try {
            this.clockSetRoundtripTreshold = 0;
            clockSetRoundtripTreshold = Integer.parseInt(properties.getProperty(PROPNAME_CLOCKSET_ROUNDTRIP_CORRECTION_THRESHOLD, String.valueOf(DEFAULT_CLOCKSET_ROUNDTRIP_CORRECTION_TRESHOLD)));
        } catch (final NumberFormatException e) {
            logger.log(Level.SEVERE, "Cannot parse the number of roundtrip correction probes to be done, setting to default value of [" + DEFAULT_CLOCKSET_ROUNDTRIP_CORRECTION_TRESHOLD + "]", e);
            this.clockSetRoundtripTreshold = DEFAULT_CLOCKSET_ROUNDTRIP_CORRECTION_TRESHOLD;
        }
        this.maxRecPduSize = Integer.parseInt(properties.getProperty(DlmsProtocolProperties.MAX_REC_PDU_SIZE, Integer.toString(MAX_PDU_SIZE)));
        this.iskraWrapper = Integer.parseInt(properties.getProperty(DlmsProtocolProperties.ISKRA_WRAPPER, ISKRA_WRAPPER_DEFAULT));
        this.incrementFrameCounterForRetries = Boolean.parseBoolean(properties.getProperty(DlmsProtocolProperties.INCREMENT_FRAMECOUNTER_FOR_RETRIES, INCREMENT_FRAMECOUNTER_FOR_RETRIES_DEFAULT));

        doValidateProperties(properties);
    }

    /**
     * Creates an association session
     *
     * @throws IOException when the communication with the meter failed
     */
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

    /**
     * Disconnect, stop the association session
     *
     * @throws IOException when the communication with the meter failed
     */
    public void disconnect() throws IOException {
        try {
            if ((this.aso != null) && (this.aso.getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_CONNECTED)) {
                this.aso.releaseAssociation();
            }
            if (getDLMSConnection() != null) {
                getDLMSConnection().disconnectMAC();
            }
        } catch (IOException e) {
            //absorb -> trying to close communication
            getLogger().log(Level.FINEST, e.getMessage());
        } catch (DLMSConnectionException e) {
            //absorb -> trying to close communication
            getLogger().log(Level.FINEST, e.getMessage());
        }
    }
}