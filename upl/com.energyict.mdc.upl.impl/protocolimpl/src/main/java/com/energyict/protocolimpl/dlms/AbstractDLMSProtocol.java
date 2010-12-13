package com.energyict.protocolimpl.dlms;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.NotFoundException;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dlms.*;
import com.energyict.dlms.aso.*;
import com.energyict.dlms.cosem.CapturedObjectsHelper;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.genericprotocolimpl.common.LocalSecurityProvider;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.*;

import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 9-dec-2010
 * Time: 11:09:31
 */
public abstract class AbstractDLMSProtocol extends AbstractProtocol implements ProtocolLink {

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
    protected CapturedObjectsHelper capturedObjectsHelper;
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
    protected int roundTripCorrection;
    protected int wakeup;
    protected int cipheringType;
    protected String manufacturer;
    protected int opticalBaudrate;
    protected String serialNumber;
    protected String nodeId;
    protected String deviceId;
    protected int iConfigProgramChange = -1;
    protected int profileInterval = -1;
    protected int clockSetRoundtripTreshold = 0;
    protected int roundtripCorrection;

    private static final int CONNECTION_MODE_HDLC = 0;
    private static final int CONNECTION_MODE_TCPIP = 1;
    private static final int PROPOSED_QOS = -1;
    private static final int PROPOSED_DLMS_VERSION = 6;
    private static final int MAX_PDU_SIZE = 200;
    private static final int DEFAULT_MAXIMUM_NUMBER_OF_CLOCKSET_TRIES = 10;
    private static final int DEFAULT_CLOCKSET_ROUNDTRIP_CORRECTION_TRESHOLD = 5000;

    private static final String PROPNAME_INFORMATION_FIELD_SIZE = "InformationFieldSize";
    private static final String PROPNAME_IIAP_INVOKE_ID = "IIAPInvokeId";
    private static final String PROPNAME_IIAP_PRIORITY = "IIAPPriority";
    private static final String PROPNAME_IIAP_SERVICE_CLASS = "IIAPServiceClass";
    private static final String PROPNAME_CIPHERING_TYPE= "CipheringType";
    private static final String PROPNAME_ROUNDTRIP_CORRECITON = "RoundTripCorrection";
    private static final String PROPNAME_ADDRESSING_MODE = "AddressingMode";
    private static final String PROPNAME_CONNECTION = "Connection";
    private static final String PROPNAME_MANUFACTURER = "Manufacturer";
    private static final String PROPNAME_SERVER_LOWER_MAC_ADDRESS = "ServerLowerMacAddress";
    private static final String PROPNAME_SERVER_UPPER_MAC_ADDRESS = "ServerUpperMacAddress";
    private static final String PROPNAME_CLIENT_MAC_ADDRESS = "ClientMacAddress";
    private static final String PROPNAME_RETRIES = "Retries";
    private static final String PROPNAME_TIMEOUT = "Timeout";
    private static final String PROPNAME_FORCE_DELAY = "ForceDelay";
    private static final String PROPNAME_ROUNDTRIP_CORRECTION = "RoundtripCorrection";
    private static final String PROPNAME_MAXIMUM_NUMBER_OF_CLOCKSET_TRIES = "MaximumNumberOfClockSetTries";
    private static final String PROPNAME_CLOCKSET_ROUNDTRIP_CORRECTION_THRESHOLD = "ClockSetRoundtripCorrectionTreshold";


    @Override
    protected void doConnect() throws IOException {
        connect();
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
        this.properties = properties;
        validateProperties();
    }

    @Override
    protected List doGetOptionalKeys() {
        return getOptionalKeys();
    }

    public TimeZone getTimeZone() {
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
            logger.warning("Using default time zone.");
        }
        return timeZone;
    }

    public boolean isRequestTimeZone() {
        return false;
    }

    public int getRoundTripCorrection() {
        return 0;
    }

    public Object fetchCache(int rtuid) throws SQLException, BusinessException {
        if (rtuid != 0) {
            RtuDLMSCache rtuCache = new RtuDLMSCache(rtuid);
            RtuDLMS rtu = new RtuDLMS(rtuid);
            try {
                return new DLMSCache(rtuCache.getObjectList(), rtu.getConfProgChange());
            }
            catch (NotFoundException e) {
                return new DLMSCache(null, -1);
            }
        } else {
            throw new com.energyict.cbo.BusinessException("invalid RtuId!");
        }
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

    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        this.timeZone = timeZone;
        this.logger = logger;
        iConfigProgramChange = -1;
        this.cosemObjectFactory = new CosemObjectFactory(this);
        this.dlmsMeterConfig = DLMSMeterConfig.getInstance(manufacturer);
        initDLMSConnection(inputStream, outputStream);
    }

    protected byte[] getSystemIdentifier() {
        return serialNumber.getBytes();
    }

    protected void initDLMSConnection(InputStream inputStream, OutputStream outputStream) throws IOException {
        DLMSConnection connection;
        try {
            switch (connectionMode) {
                case CONNECTION_MODE_HDLC:
                    connection = new HDLC2Connection(inputStream, outputStream, timeOut, 100, retries, clientMacAddress,
                            serverLowerMacAddress, serverUpperMacAddress, addressingMode, -1, opticalBaudrate);
                    break;
                case CONNECTION_MODE_TCPIP:
                    connection = new TCPIPConnection(inputStream, outputStream, timeOut, forceDelay, retries, clientMacAddress, serverLowerMacAddress);
                    break;
                default:
                    throw new IOException("Unable to initialize dlmsConnection, connection property unknown: " + connectionMode);
            }
        } catch (DLMSConnectionException e) {
            throw new IOException(e.getMessage());
        }

        LocalSecurityProvider localSecurityProvider = new LocalSecurityProvider(this.properties);
        securityContext = new SecurityContext(datatransportSecurityLevel, authenticationSecurityLevel, 0, getSystemIdentifier(), localSecurityProvider, this.cipheringType);

        if (this.conformanceBlock == null) {
            if (getReference() == ProtocolLink.SN_REFERENCE) {
                this.conformanceBlock = new ConformanceBlock(ConformanceBlock.DEFAULT_SN_CONFORMANCE_BLOCK);
            } else if (getReference() == ProtocolLink.LN_REFERENCE) {
                this.conformanceBlock = new ConformanceBlock(ConformanceBlock.DEFAULT_LN_CONFORMANCE_BLOCK);
            } else {
                throw new InvalidPropertyException("Invalid reference method, only 0 and 1 are allowed.");
            }
        }

        XdlmsAse xdlmsAse = new XdlmsAse(isCiphered() ? localSecurityProvider.getDedicatedKey() : null, true, PROPOSED_QOS, PROPOSED_DLMS_VERSION, this.conformanceBlock, MAX_PDU_SIZE);
        aso = new ApplicationServiceObject(xdlmsAse, this, securityContext, getContextId());
        dlmsConnection = new SecureConnection(aso, connection);
        InvokeIdAndPriority iiap = buildInvokeIdAndPriority();
        this.dlmsConnection.setInvokeIdAndPriority(iiap);
    }

    protected InvokeIdAndPriority buildInvokeIdAndPriority() throws IOException {
        try {
            InvokeIdAndPriority iiap = new InvokeIdAndPriority();
            iiap.setPriority(this.iiapPriority);
            iiap.setServiceClass(this.iiapServiceClass);
            iiap.setTheInvokeId(this.iiapInvokeId);
            return iiap;
        } catch (DLMSConnectionException e) {
            getLogger().info("Some configured properties are invalid. " + e.getMessage());
            throw new IOException(e.getMessage());
        }
    }

    protected boolean isCiphered() {
        return (getContextId() == AssociationControlServiceElement.LOGICAL_NAME_REFERENCING_WITH_CIPHERING) || (getContextId() == AssociationControlServiceElement.SHORT_NAME_REFERENCING_WITH_CIPHERING);
    }

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

    public void setProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException {
        this.properties = properties;
        validateProperties();
    }

    protected void validateProperties() throws MissingPropertyException, InvalidPropertyException {
        Iterator<String> iterator = getRequiredKeys().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
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
        manufacturer = properties.getProperty(PROPNAME_MANUFACTURER);
        informationFieldSize = Integer.parseInt(properties.getProperty(PROPNAME_INFORMATION_FIELD_SIZE, "-1"));
        roundTripCorrection = Integer.parseInt(properties.getProperty(PROPNAME_ROUNDTRIP_CORRECITON, "0"));
        iiapInvokeId = Integer.parseInt(properties.getProperty(PROPNAME_IIAP_INVOKE_ID, "0"));
        iiapPriority = Integer.parseInt(properties.getProperty(PROPNAME_IIAP_PRIORITY, "1"));
        iiapServiceClass = Integer.parseInt(properties.getProperty(PROPNAME_IIAP_SERVICE_CLASS, "1"));
        cipheringType = Integer.parseInt(properties.getProperty(PROPNAME_CIPHERING_TYPE, Integer.toString(SecurityContext.CIPHERING_TYPE_GLOBAL)));
        roundtripCorrection = Integer.parseInt(properties.getProperty(PROPNAME_ROUNDTRIP_CORRECTION, "0").trim());

        if (cipheringType != SecurityContext.CIPHERING_TYPE_GLOBAL && cipheringType != SecurityContext.CIPHERING_TYPE_DEDICATED) {
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
    }

    public void connect() throws IOException {
        try {
            if (this.aso.getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_DISCONNECTED) {
                getDLMSConnection().connectMAC();
                this.aso.createAssociation();
                checkCacheObjects();
            }

        } catch (DLMSConnectionException e) {
            IOException exception = new IOException(e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

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

    protected void checkCacheObjects() throws IOException {
        try { // conf program change and object list stuff
            int iConf;
            if (dlmsCache.getObjectList() != null) {
                dlmsMeterConfig.setInstantiatedObjectList(dlmsCache.getObjectList());
                try {
                    iConf = requestConfigurationProgramChanges();
                }
                catch (IOException e) {
                    iConf = -1;
                    logger.severe("SimpleDLMSProtocol Configuration change count not accessible, request object list.");
                    requestObjectList();
                    dlmsCache.saveObjectList(dlmsMeterConfig.getInstantiatedObjectList());  // save object list in cache
                }

                if (iConf != dlmsCache.getConfProgChange()) {
                    logger.severe("SimpleDLMSProtocol Configuration changed, request object list.");
                    requestObjectList();           // request object list again from rtu
                    dlmsCache.saveObjectList(dlmsMeterConfig.getInstantiatedObjectList());  // save object list in cache
                    dlmsCache.setConfProgChange(iConf);  // set new configuration program change
                }
            } else { // Cache not exist
                logger.info("SimpleDLMSProtocol Cache does not exist, request object list.");
                requestObjectList();
                try {
                    iConf = requestConfigurationProgramChanges();
                    dlmsCache.saveObjectList(dlmsMeterConfig.getInstantiatedObjectList());  // save object list in cache
                    dlmsCache.setConfProgChange(iConf);  // set new configuration program change
                }
                catch (IOException e) {
                    iConf = -1;
                }
            }

        } catch (IOException e) {
            IOException exception = new IOException("connect() error, " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }

    }

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

    public void updateCache(int rtuid, Object cacheObject) throws SQLException, BusinessException {
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
}