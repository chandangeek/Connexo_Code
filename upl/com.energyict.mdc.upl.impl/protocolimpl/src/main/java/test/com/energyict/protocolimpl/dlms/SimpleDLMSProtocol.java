package test.com.energyict.protocolimpl.dlms;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.cache.CacheMechanism;
import com.energyict.mdc.upl.cache.ProtocolCacheFetchException;
import com.energyict.mdc.upl.cache.ProtocolCacheUpdateException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.cbo.NotFoundException;
import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.CipheringType;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DLMSObis;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.HDLC2Connection;
import com.energyict.dlms.InvokeIdAndPriority;
import com.energyict.dlms.InvokeIdAndPriorityHandler;
import com.energyict.dlms.NonIncrementalInvokeIdAndPriorityHandler;
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
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.dlms.RtuDLMS;
import com.energyict.protocolimpl.dlms.RtuDLMSCache;
import com.energyict.protocolimpl.dlms.common.NTASecurityProvider;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS;
import static com.energyict.mdc.upl.MeterProtocol.Property.NODEID;
import static com.energyict.mdc.upl.MeterProtocol.Property.RETRIES;
import static com.energyict.mdc.upl.MeterProtocol.Property.ROUNDTRIPCORRECTION;
import static com.energyict.mdc.upl.MeterProtocol.Property.SECURITYLEVEL;
import static com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER;
import static com.energyict.mdc.upl.MeterProtocol.Property.TIMEOUT;

/**
 * <p>
 * Copyrights EnergyICT
 * Date: 2-jul-2010
 * Time: 11:09:10
 * </p>
 */
public class SimpleDLMSProtocol extends PluggableMeterProtocol implements ProtocolLink, HHUEnabler, CacheMechanism, SerialNumberSupport {

    private final PropertySpecService propertySpecService;

    /**
     * The {@link com.energyict.dlms.aso.ConformanceBlock} used
     */
    private ConformanceBlock conformanceBlock;

    /**
     * The {@link com.energyict.dlms.aso.XdlmsAse} used
     */
    private XdlmsAse xdlmsAse;

    /**
     * The {@link com.energyict.dlms.InvokeIdAndPriority} used
     */
    private InvokeIdAndPriority invokeIdAndPriority;

    /**
     * The {@link com.energyict.dlms.cosem.CosemObjectFactory} used
     */
    private CosemObjectFactory cosemObjectFactory;

    /**
     * The {@link com.energyict.dlms.DLMSConnection} used
     */
    private DLMSConnection dlmsConnection;

    /**
     * The {@link com.energyict.dlms.DLMSMeterConfig} used
     */
    private DLMSMeterConfig dlmsMeterConfig;

    /**
     * The {@link com.energyict.dlms.aso.ApplicationServiceObject} used
     */
    private ApplicationServiceObject aso;

    /**
     * The {@link com.energyict.dlms.aso.SecurityProvider} used for DLMS communication
     */
    private SecurityProvider securityProvider;

    /**
     * The {@link Properties} of the current RTU
     */
    private Properties properties;

    /**
     * The {@link Logger} provided by the ComServer
     */
    private Logger logger;

    /**
     * The {@link TimeZone} provided by the ComServer
     */
    private TimeZone timeZone = null;

    /**
     * The used {@link com.energyict.dlms.aso.SecurityContext}
     */
    private SecurityContext securityContext;

    /**
     * The used {@link com.energyict.dlms.DLMSCache}
     */
    private DLMSCache dlmsCache = new DLMSCache();

    /* Properties */
    private int connectionMode;
    private int datatransportSecurityLevel;
    private int authenticationSecurityLevel;
    private int iiapPriority;
    private int iiapServiceClass;
    private int iiapInvokeId;
    private int clientMacAddress;
    private int serverUpperMacAddress;
    private int serverLowerMacAddress;
    private int timeOut;
    private int forceDelay;
    private int retries;
    private int addressingMode;
    private int informationFieldSize;
    private int roundTripCorrection;
    private int wakeup;
    private int cipheringType;
    private String ipPortNumber;
    private String manufacturer;
    private int opticalBaudrate;
    private String serialNumber;
    private String nodeId;
    private String deviceId;

    private int iConfigProgramChange = -1;


    private static final int CONNECTION_MODE_HDLC = 0;
    private static final int CONNECTION_MODE_TCPIP = 1;

    private static final int MAX_PDU_SIZE = 200;
    private static final int PROPOSED_QOS = -1;
    private static final int PROPOSED_DLMS_VERSION = 6;

    public SimpleDLMSProtocol(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getSerialNumber() {
        try {
            UniversalObject uo = getMeterConfig().getSerialNumberObject();
            byte[] responsedata = getCosemObjectFactory().getGenericRead(uo.getBaseName(), uo.getValueAttributeOffset()).getResponseData();
            return AXDRDecoder.decode(responsedata).getOctetString().stringValue();
        }
        catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, dlmsConnection.getMaxRetries() + 1);
        }
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                UPLPropertySpecFactory.string(SECURITYLEVEL.getName(), false),
                UPLPropertySpecFactory.string(NODEID.getName(), false),
                UPLPropertySpecFactory.string(ADDRESS.getName(), false),
                UPLPropertySpecFactory.string(SERIALNUMBER.getName(), false),
                UPLPropertySpecFactory.integer("Connection", false),
                UPLPropertySpecFactory.integer("ClientMacAddress", false),
                UPLPropertySpecFactory.integer("ServerUpperMacAddress", false),
                UPLPropertySpecFactory.integer("ServerLowerMacAddress", false),
                UPLPropertySpecFactory.integer(TIMEOUT.getName(), false),
                UPLPropertySpecFactory.integer(RETRIES.getName(), false),
                UPLPropertySpecFactory.integer(ROUNDTRIPCORRECTION.getName(), false),
                UPLPropertySpecFactory.integer("ForceDelay", false),
                UPLPropertySpecFactory.integer("AddressingMode", false),
                UPLPropertySpecFactory.string("Manufacturer", false),
                UPLPropertySpecFactory.integer("InformationFieldSize", false),
                UPLPropertySpecFactory.integer("IIAPInvokeId", false),
                UPLPropertySpecFactory.integer("IIAPPriority", false),
                UPLPropertySpecFactory.integer("IIAPServiceClass", false),
                UPLPropertySpecFactory.integer("CipheringType", false, CipheringType.GLOBAL.getType(), CipheringType.DEDICATED.getType()));
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws InvalidPropertyException, MissingPropertyException {
        try {
            String[] securityLevel = properties.getTypedProperty(SECURITYLEVEL.getName(), "0").split(":");
            this.authenticationSecurityLevel = Integer.parseInt(securityLevel[0]);
            if (securityLevel.length == 2) {
                this.datatransportSecurityLevel = Integer.parseInt(securityLevel[1]);
            } else if (securityLevel.length == 1) {
                this.datatransportSecurityLevel = 0;
            } else {
                throw new InvalidPropertyException("SecurityLevel property contains an illegal value " + properties.getTypedProperty("SecurityLevel", "0"));
            }

            this.nodeId = properties.getTypedProperty(NODEID.getName(), "");
            this.deviceId = properties.getTypedProperty(ADDRESS.getName(), "");
            this.serialNumber = properties.getTypedProperty(SERIALNUMBER.getName(), "");
            this.connectionMode = Integer.parseInt(properties.getTypedProperty("Connection", "1"));
            this.clientMacAddress = Integer.parseInt(properties.getTypedProperty("ClientMacAddress", "16"));
            this.serverLowerMacAddress = Integer.parseInt(properties.getTypedProperty("ServerLowerMacAddress", "1"));
            this.serverUpperMacAddress = Integer.parseInt(properties.getTypedProperty("ServerUpperMacAddress", "17"));
            this.timeOut = Integer.parseInt(properties.getTypedProperty(TIMEOUT.getName(), (this.connectionMode == 0) ? "5000" : "60000"));    // set the HDLC timeout to 5000 for the WebRTU KP
            this.retries = Integer.parseInt(properties.getTypedProperty(RETRIES.getName(), "3"));
            this.roundTripCorrection = Integer.parseInt(properties.getTypedProperty(ROUNDTRIPCORRECTION.getName(), "0"));
            this.forceDelay = Integer.parseInt(properties.getTypedProperty("ForceDelay", "1"));
            this.addressingMode = Integer.parseInt(properties.getTypedProperty("AddressingMode", "2"));
            this.manufacturer = properties.getTypedProperty("Manufacturer", "WKP");
            this.informationFieldSize = Integer.parseInt(properties.getTypedProperty("InformationFieldSize", "-1"));
            this.iiapInvokeId = Integer.parseInt(properties.getTypedProperty("IIAPInvokeId", "0"));
            this.iiapPriority = Integer.parseInt(properties.getTypedProperty("IIAPPriority", "1"));
            this.iiapServiceClass = Integer.parseInt(properties.getTypedProperty("IIAPServiceClass", "1"));
            this.cipheringType = Integer.parseInt(properties.getTypedProperty("CipheringType", Integer.toString(CipheringType.GLOBAL.getType())));
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, this.getClass().getSimpleName() + ": validation of properties failed before");
        }
        this.properties = properties.toStringProperties();
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
                    connection = new HDLC2Connection(inputStream, outputStream, timeOut, 100, retries, clientMacAddress,
                            serverLowerMacAddress, serverUpperMacAddress, addressingMode, -1, opticalBaudrate);
                    break;
                case CONNECTION_MODE_TCPIP:
                    connection = new TCPIPConnection(inputStream, outputStream, timeOut, forceDelay, retries, clientMacAddress, serverLowerMacAddress, getLogger());
                    break;
                default:
                    throw new IOException("Unable to initialize dlmsConnection, connection property unknown: " + connectionMode);
            }
        } catch (DLMSConnectionException e) {
            throw new IOException(e.getMessage());
        }

        NTASecurityProvider localSecurityProvider = new NTASecurityProvider(this.properties);
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
        InvokeIdAndPriorityHandler iiapHandler = buildInvokeIdAndPriorityHandler();
        this.dlmsConnection.setInvokeIdAndPriorityHandler(iiapHandler);
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

    private boolean isCiphered() {
        return (getContextId() == AssociationControlServiceElement.LOGICAL_NAME_REFERENCING_WITH_CIPHERING) || (getContextId() == AssociationControlServiceElement.SHORT_NAME_REFERENCING_WITH_CIPHERING);
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

    @Override
    public void connect() throws IOException {
        try {
            if (this.aso.getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_DISCONNECTED) {
                getDLMSConnection().connectMAC();
                this.aso.createAssociation();

                // objectList
                checkCacheObjects();
            }

        } catch (DLMSConnectionException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public ApplicationServiceObject getAso() {
        return aso;
    }

    /**
     * Check the objectList. If it doesn't exist, then read it from the device
     *
     * @throws IOException
     */
    private void checkCacheObjects() throws IOException {
        try { // conf program change and object list stuff
            int iConf;

            if (dlmsCache == null) {
                dlmsCache = new DLMSCache();
            }

            if (dlmsCache.getObjectList() != null) {
                dlmsMeterConfig.setInstantiatedObjectList(dlmsCache.getObjectList());
            } else { // Cache not exist
                logger.info("SimpleDLMSProtocol Cache does not exist, request object list.");
                requestObjectList();
                try {
                    iConf = requestConfigurationProgramChanges();
                    dlmsCache.saveObjectList(dlmsMeterConfig.getInstantiatedObjectList());  // save object list in cache
                    dlmsCache.setConfProgChange(iConf);  // set new configuration program change                    dlmsCache.saveObjectList(dlmsMeterConfig.getInstantiatedObjectList());  // save object list in cache
                } catch (IOException e) {
                    iConf = -1;
                    dlmsCache.saveObjectList(dlmsMeterConfig.getInstantiatedObjectList());  // save object list in cache
                    dlmsCache.setConfProgChange(iConf);  // set new configuration program change
                }
            }
        } catch (IOException e) {
            throw new IOException("connect() error, " + e.getMessage(), e);
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

        try {
            if (getReference() == ProtocolLink.LN_REFERENCE) {
                getMeterConfig().setInstantiatedObjectList(getCosemObjectFactory().getAssociationLN().getBuffer());
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
     * Getter for property cosemObjectFactory.
     *
     * @return Value of property cosemObjectFactory.
     */
    public com.energyict.dlms.cosem.CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }

    public int requestConfigurationProgramChanges() throws IOException {
        if (iConfigProgramChange == -1) {
            iConfigProgramChange = (int) getCosemObjectFactory().getCosemObject(getMeterConfig().getConfigObject().getObisCode()).getValue();
        }
        return iConfigProgramChange;
    }

    @Override
    public DLMSConnection getDLMSConnection() {
        return this.dlmsConnection;
    }

    @Override
    public DLMSMeterConfig getMeterConfig() {
        return this.dlmsMeterConfig;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public TimeZone getTimeZone() {
        return this.timeZone;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isRequestTimeZone() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getRoundTripCorrection() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Logger getLogger() {
        return this.logger;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getReference() {
        return ProtocolLink.LN_REFERENCE;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StoredValues getStoredValues() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:24:28 +0200 (Thu, 26 Nov 2015)$";  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return "UnKnow";  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public Quantity getMeterReading(int channelId) throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public Quantity getMeterReading(String name) throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public int getNumberOfChannels() {
        return 0;
    }

    @Override
    public int getProfileInterval() {
        return 900;
    }

    @Override
    public Date getTime() throws IOException {
        return new Date();
    }

    @Override
    public String getRegister(String name) throws IOException {
        return doGetRegister(name);
    }

    private String doGetRegister(final String name) throws IOException {
        boolean classSpecified = false;
        if (name.indexOf(':') >= 0) {
            classSpecified = true;
        }
        final DLMSObis ln = new DLMSObis(name);
        if (ln.isLogicalName()) {
            if (classSpecified) {
                return requestAttribute(ln.getDLMSClass(), ln.getLN(), (byte) ln.getOffset());
            } else {
                final UniversalObject uo = getMeterConfig().getObject(ln);
                return getCosemObjectFactory().getGenericRead(uo).getDataContainer().print2strDataContainer();
            }
        } else if (name.contains("-")) { // you get a from/to
            final DLMSObis ln2 = new DLMSObis(name.substring(0, name.indexOf("-")));
            if (ln2.isLogicalName()) {
                final String from = name.substring(name.indexOf("-") + 1, name.indexOf("-", name.indexOf("-") + 1));
                final String to = name.substring(name.indexOf(from) + from.length() + 1);
                if (ln2.getDLMSClass() == 7) {
                    return getCosemObjectFactory().getProfileGeneric(getMeterConfig().getObject(ln2).getObisCode()).getBuffer(convertStringToCalendar(from), convertStringToCalendar(to)).print2strDataContainer();
                } else {
                    throw new NoSuchRegisterException("GenericGetSet,getRegister, register " + name + " is not a profile.");
                }
            } else {
                throw new NoSuchRegisterException("GenericGetSet,getRegister, register " + name + " does not exist.");
            }
        } else {
            throw new NoSuchRegisterException("GenericGetSet,getRegister, register " + name + " does not exist.");
        }
    }

    private String requestAttribute(final short sIC, final byte[] LN, final byte bAttr) throws IOException {
        return this.doRequestAttribute(sIC, LN, bAttr).print2strDataContainer();
    }

    private DataContainer doRequestAttribute(final int classId, final byte[] ln, final int lnAttr) throws IOException {
        return getCosemObjectFactory().getGenericRead(ObisCode.fromByteArray(ln), DLMSUtils.attrLN2SN(lnAttr), classId).getDataContainer();
    }

    private Calendar convertStringToCalendar(final String strDate) {
        final Calendar cal = Calendar.getInstance(getTimeZone());
        cal.set(Integer.parseInt(strDate.substring(strDate.lastIndexOf("/") + 1, strDate.indexOf(" "))) & 0xFFFF, (Integer.parseInt(strDate.substring(strDate.indexOf("/") + 1, strDate.lastIndexOf("/"))) & 0xFF) - 1, Integer.parseInt(strDate.substring(0, strDate.indexOf("/"))) & 0xFF, Integer.parseInt(strDate.substring(strDate.indexOf(" ") + 1, strDate.indexOf(":"))) & 0xFF, Integer.parseInt(strDate.substring(strDate.indexOf(":") + 1, strDate.lastIndexOf(":"))) & 0xFF, Integer.parseInt(strDate.substring(strDate.lastIndexOf(":") + 1, strDate.length())) & 0xFF);
        return cal;
    }

    @Override
    public void setRegister(String name, String value) throws IOException {
        boolean classSpecified = false;
        if (name.indexOf(':') >= 0) {
            classSpecified = true;
        }
        final DLMSObis ln = new DLMSObis(name);
        if ((ln.isLogicalName()) && (classSpecified)) {
            getCosemObjectFactory().getGenericWrite(ObisCode.fromByteArray(ln.getLN()), ln.getOffset(), ln.getDLMSClass()).write(convert(value));
        } else {
            throw new NoSuchRegisterException("GenericGetSet, setRegister, register " + name + " does not exist.");
        }
    }

    private byte[] convert(final String s) {
        if ((s.length() % 2) != 0) {
            throw new IllegalArgumentException("String length is not a modulo 2 hex representation!");
        } else {
            final byte[] data = new byte[s.length() / 2];

            for (int i = 0; i < (s.length() / 2); i++) {
                data[i] = (byte) Integer.parseInt(s.substring(i * 2, (i * 2) + 2), 16);
            }

            return data;
        }
    }

    @Override
    public void setTime() throws UnsupportedException {
        throw new UnsupportedException();
    }

    @Override
    public void initializeDevice() throws UnsupportedException {
        throw new UnsupportedException();
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
            try {
                return new DLMSCache(rtuCache.getObjectList(connection), rtu.getConfProgChange(connection));
            } catch (NotFoundException e) {
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
        final Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) + "_" + (calendar.get(Calendar.MONTH) + 1) + "_" + calendar.get(Calendar.DAY_OF_MONTH) + "_" + this.deviceId + "_" + this.serialNumber + "_" + serverUpperMacAddress + "_SimpleDLMS.cache";
    }

    @Override
    public void release() throws IOException {
        // do nothing
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, false);
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean enableDataReadout) throws ConnectionException {
        HHUSignOn hhuSignOn = new IEC1107HHUConnection(commChannel, this.timeOut, this.retries, 300, 0);
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(enableDataReadout);
        getDLMSConnection().setHHUSignOn(hhuSignOn, this.deviceId);
    }

    @Override
    public byte[] getHHUDataReadout() {
        return getDLMSConnection().getHhuSignOn().getDataReadout();
    }

}