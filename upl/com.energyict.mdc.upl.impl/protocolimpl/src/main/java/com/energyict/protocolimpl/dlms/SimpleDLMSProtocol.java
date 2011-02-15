package com.energyict.protocolimpl.dlms;

import com.energyict.cbo.*;
import com.energyict.dialer.connection.*;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.*;
import com.energyict.dlms.aso.*;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * Copyrights EnergyICT
 * Date: 2-jul-2010
 * Time: 11:09:10
 * </p>
 */
public class SimpleDLMSProtocol implements MeterProtocol, ProtocolLink, HHUEnabler, CacheMechanism {

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
     * The used {@link com.energyict.protocolimpl.dlms.DLMSCache}
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
    private static final int CONNECTION_MODE_COSEM_PDU = 2;
    private static final int CONNECTION_MODE_LLC = 3;

    private static final int MAX_PDU_SIZE = 200;
    private static final int PROPOSED_QOS = -1;
    private static final int PROPOSED_DLMS_VERSION = 6;

    /**
     * <p>
     * Sets the protocol specific properties.
     * </p><p>
     * This method can also be called at device configuration time to check the validity of
     * the configured values </p><p>
     * The implementer has to specify which keys are mandatory,
     * and which are optional. Convention is to use lower case keys.</p><p>
     * Typical keys are: <br>
     * "address"  (MeterProtocol.ADDRESS) <br>
     * "password"  (MeterProtocol.PASSWORD) </p>
     *
     * @param properties contains a set of protocol specific key value pairs
     * @throws com.energyict.protocol.InvalidPropertyException
     *          if a property value is not compatible with the device type
     * @throws com.energyict.protocol.MissingPropertyException
     *          if a required property is not present
     */
    public void setProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException {
        this.properties = properties;
        validateProperties();
    }

    /**
     * Validates the required and optional key properties
     *
     * @throws MissingPropertyException if a required property is missing
     * @throws InvalidPropertyException if the value of a certain property doesn't match a compatible value
     */
    private void validateProperties() throws MissingPropertyException, InvalidPropertyException {
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

		this.nodeId = properties.getProperty(MeterProtocol.NODEID, "");
        this.deviceId = properties.getProperty(MeterProtocol.ADDRESS,"");
		this.serialNumber = properties.getProperty(MeterProtocol.SERIALNUMBER, "");
        this.connectionMode = Integer.parseInt(properties.getProperty("Connection", "1"));
        this.clientMacAddress = Integer.parseInt(properties.getProperty("ClientMacAddress", "16"));
        this.serverLowerMacAddress = Integer.parseInt(properties.getProperty("ServerLowerMacAddress", "1"));
        this.serverUpperMacAddress = Integer.parseInt(properties.getProperty("ServerUpperMacAddress", "17"));
        this.timeOut = Integer.parseInt(properties.getProperty("Timeout", (this.connectionMode == 0) ? "5000" : "60000"));    // set the HDLC timeout to 5000 for the WebRTU KP
        this.forceDelay = Integer.parseInt(properties.getProperty("ForceDelay", "1"));
        this.retries = Integer.parseInt(properties.getProperty("Retries", "3"));
        this.addressingMode = Integer.parseInt(properties.getProperty("AddressingMode", "2"));
        this.manufacturer = properties.getProperty("Manufacturer", "WKP");
        this.informationFieldSize = Integer.parseInt(properties.getProperty("InformationFieldSize", "-1"));
        this.roundTripCorrection = Integer.parseInt(properties.getProperty("RoundTripCorrection", "0"));
        this.iiapInvokeId = Integer.parseInt(properties.getProperty("IIAPInvokeId", "0"));
        this.iiapPriority = Integer.parseInt(properties.getProperty("IIAPPriority", "1"));
        this.iiapServiceClass = Integer.parseInt(properties.getProperty("IIAPServiceClass", "1"));
        this.cipheringType = Integer.parseInt(properties.getProperty("CipheringType", Integer.toString(SecurityContext.CIPHERING_TYPE_GLOBAL)));
        if (cipheringType != SecurityContext.CIPHERING_TYPE_GLOBAL && cipheringType != SecurityContext.CIPHERING_TYPE_DEDICATED) {
            throw new InvalidPropertyException("Only 0 or 1 is allowed for the CipheringType property");
        }
    }

    /**
     * <p>
     * Initializes the MeterProtocol.
     * </p><p>
     * Implementers should save the arguments for future use.
     * </p><p>
     * All times exchanged between the data collection system and a MeterProtocol are java.util.Date ,
     * expressed in milliseconds since 1/1/1970 in UTC. The implementer has
     * to convert the device times to UTC. </p><p>
     * The timeZone argument is the timezone that is configured in the collecting system
     * for the device. If the device knows its own timezone, this argument can be ignored </p><p>
     * Implementers can use the argument to convert from device
     * time format to java.util.Date, e.g.</p>
     * <PRE>
     * Calendar deviceCalendar = Calendar.getInstance(timeZone);
     * deviceCalendar.clear();
     * deviceCalendar.set(year, month - 1 , day , hour , minute , second);
     * java.util.Date deviceDate = deviceCalendar.getTime();
     * </PRE>
     * <p>
     * The last argument is used to inform the data collection system of problems and/or progress.
     * Messages with level INFO or above are logged to the collection system's
     * logbook. Messages with level below INFO are only displayed in diagnostic mode
     * </p>
     *
     * @param inputStream  byte stream to read data from the device
     * @param outputStream byte stream to send data to the device
     * @param timeZone     the device's timezone
     * @param logger       used to provide feedback to the collection system
     * @throws java.io.IOException Thrown when an exception happens
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

    	private InvokeIdAndPriority buildInvokeIdAndPriority() throws IOException {
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

    /**
     * <p>
     * Sets up the logical connection with the device.
     * </p><p>
     * As the physical connection has already been setup by the collection system,
     * it is up to the implementer to decide if any additional implementation is needed
     * </p>
     *
     * @throws java.io.IOException <br>
     */
    public void connect() throws IOException {
        try {
            if (this.aso.getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_DISCONNECTED) {
                getDLMSConnection().connectMAC();
                this.aso.createAssociation();

                // objectList
                checkCacheObjects();
            }

        } catch (DLMSConnectionException e) {
            IOException exception = new IOException(e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    /**
     * Check the objectList. If it doesn't exist, then read it from the device
     *
     * @throws IOException
     */
    private void checkCacheObjects() throws IOException {
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

	/**
	 * This method requests for the COSEM object list in the remote meter. A
	 * list is byuild with LN and SN references.
	 * This method must be executed before other request methods.
	 *
	 * @exception IOException
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

    /**
     * Getter for the current {@link com.energyict.dlms.DLMSConnection}
     *
     * @return the DLMSConnection
     */
    public DLMSConnection getDLMSConnection() {
        return this.dlmsConnection;
    }

    /**
     * Getter for property meterConfig.
     *
     * @return Value of property meterConfig.
     */
    public DLMSMeterConfig getMeterConfig() {
        return this.dlmsMeterConfig;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * @return the {@link java.util.TimeZone}
     */
    public TimeZone getTimeZone() {
        return this.timeZone;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Check if the {@link java.util.TimeZone} is read from the DLMS device, or if the
     * {@link java.util.TimeZone} from the {@link com.energyict.protocol.MeterProtocol} should be used.
     *
     * @return true is the {@link java.util.TimeZone} is read from the device
     */
    public boolean isRequestTimeZone() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Getter for the round trip correction.
     *
     * @return the value of the round trip correction
     */
    public int getRoundTripCorrection() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Getter for the {@link java.util.logging.Logger}
     *
     * @return the current {@link java.util.logging.Logger}
     */
    public Logger getLogger() {
        return this.logger;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Getter for the type of reference used in the DLMS protocol. This can be
     * {@link com.energyict.dlms.ProtocolLink}.SN_REFERENCE or {@link com.energyict.dlms.ProtocolLink}.LN_REFERENCE
     *
     * @return {@link com.energyict.dlms.ProtocolLink}.SN_REFERENCE for short name or
     *         {@link com.energyict.dlms.ProtocolLink}.LN_REFERENCE for long name
     */
    public int getReference() {
        return ProtocolLink.LN_REFERENCE;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Getter for the {@link com.energyict.dlms.cosem.StoredValues} object
     *
     * @return the {@link com.energyict.dlms.cosem.StoredValues} object
     */
    public StoredValues getStoredValues() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Terminates the logical connection with the device.
     * The implementer should not close the inputStream and outputStream. This
     * is the responsibility of the collection system
     *
     * @throws java.io.IOException thrown in case of an exception
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

    /**
     * @return the version of the specific protocol implementation
     */
    public String getProtocolVersion() {
        return "$Date$";  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Get the firmware version of the meter
     *
     * @return the version of the meter firmware
     *         </p>
     * @throws java.io.IOException Thrown in case of an exception
     * @throws com.energyict.protocol.UnsupportedException
     *                             Thrown if method is not supported
     */
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return "UnKnow";  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * <p>
     * Fetches profile data from the device.
     * </p><p>
     * The includeEvents flag indicates whether the data collection
     * system will process the MeterEvents in the returned data. This
     * is only provided as a hint. An implementation is free to ignore
     * this value based on the protocol capabilities</p>
     *
     * @param includeEvents indicates whether events need to be included
     * @return profile data containing interval records and optional meter events
     *         </p>
     * @throws java.io.IOException <br>
     */
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        throw new UnsupportedException();
    }

    /**
     * <p>
     * Fetches profile data from the device.
     * </p><p>
     * The includeEvents flag indicates whether the data collection
     * system will process the MeterEvents in the returned data. This
     * is only provided as a hint. An implementation is free to ignore
     * this value based on the protocol capabilities</p>
     * Implementors should throw an exception if all data since lastReading
     * can not be fetched, as the collecting system will update its lastReading
     * setting based on the returned ProfileData
     * </p><p>
     *
     * @param includeEvents indicates whether events need to be included
     * @param lastReading   retrieve all data younger than lastReading
     *                      </p><p>
     * @return profile data containing interval records and optional meter events
     *         </p>
     * @throws java.io.IOException <br>
     */
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        throw new UnsupportedException();
    }

    /**
     * <p>
     * Fetches profile data from the device.
     * </p><p>
     * The includeEvents flag indicates whether the data collection
     * system will process the MeterEvents in the returned data. This
     * is only provided as a hint. An implementation is free to ignore
     * this value based on the protocol capabilities</p>
     * Implementors should throw an exception if data between from and to
     * can not be fetched, as the collecting system will update its lastReading
     * setting based on the returned ProfileData
     * </p><p>
     *
     * @param includeEvents indicates whether events need to be included
     * @param from          retrieve all data starting with from date
     * @param to            retrieve all data until to date
     *                      </p><p>
     * @return profile data containing interval records and optional meter events between from and to
     *         </p>
     * @throws java.io.IOException <br>
     * @throws com.energyict.protocol.UnsupportedException
     *                             if meter does not support a to date to request the profile data
     */
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
        throw new UnsupportedException();
    }

    /**
     * Fetches the meter reading for the specified logical device channel.
     *
     * @param channelId index of the channel. Indexes start with 1
     *                  </p><p>
     * @return meter register value as Quantity
     * @throws java.io.IOException <br>
     * @throws com.energyict.protocol.UnsupportedException
     *                             if the device does not support this operation
     * @deprecated Replaced by the RegisterProtocol interface method readRegister(...)
     */
    public Quantity getMeterReading(int channelId) throws UnsupportedException, IOException {
        throw new UnsupportedException();
    }

    /**
     * Fetches the meterreading for the specified register, represented as a String
     *
     * @param name register name
     * @return meter register value as Quantity
     * @throws com.energyict.protocol.UnsupportedException
     *                             Thrown if the method is not supported by the protocol
     * @throws java.io.IOException Thrown in case of an exception
     * @deprecated Replaced by the RegisterProtocol interface method readRegister(...)
     */
    public Quantity getMeterReading(String name) throws UnsupportedException, IOException {
        throw new UnsupportedException();
    }

    /**
     * <p></p>
     *
     * @return the device's number of logical channels
     *         </p>
     * @throws java.io.IOException <br>
     * @throws com.energyict.protocol.UnsupportedException
     *                             if the device does not support this operation
     */
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * <p></p>
     *
     * @return the device's current profile interval in seconds
     *         </p>
     * @throws java.io.IOException <br>
     * @throws com.energyict.protocol.UnsupportedException
     *                             if the device does not support this operation
     */
    public int getProfileInterval() throws UnsupportedException, IOException {
        return 900;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * <p></p>
     *
     * @return the current device time
     * @throws java.io.IOException <br>
     */
    public Date getTime() throws IOException {
        return new Date();  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * <p></p>
     *
     * @param name Register name. Devices supporting OBIS codes,
     *             should use the OBIS code as register name
     *             </p>
     * @return the value for the specified register
     *         </p><p>
     * @throws java.io.IOException <br>
     * @throws com.energyict.protocol.UnsupportedException
     *                             if the device does not support this operation
     * @throws com.energyict.protocol.NoSuchRegisterException
     *                             if the device does not support the specified register
     */
    public String getRegister(String name) throws IOException, UnsupportedException, NoSuchRegisterException {
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
		} else if (name.indexOf("-") >= 0) { // you get a from/to
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

    	private final String requestAttribute(final short sIC, final byte[] LN, final byte bAttr) throws IOException {
		return this.doRequestAttribute(sIC, LN, bAttr).print2strDataContainer();
	}

	// IOException

	private final DataContainer doRequestAttribute(final int classId, final byte[] ln, final int lnAttr) throws IOException {
		final DataContainer dc = getCosemObjectFactory().getGenericRead(ObisCode.fromByteArray(ln), DLMSUtils.attrLN2SN(lnAttr), classId).getDataContainer();
		return dc;
	}

	private Calendar convertStringToCalendar(final String strDate) {
		final Calendar cal = Calendar.getInstance(getTimeZone());
		cal.set(Integer.parseInt(strDate.substring(strDate.lastIndexOf("/") + 1, strDate.indexOf(" "))) & 0xFFFF, (Integer.parseInt(strDate.substring(strDate.indexOf("/") + 1, strDate.lastIndexOf("/"))) & 0xFF) - 1, Integer.parseInt(strDate.substring(0, strDate.indexOf("/"))) & 0xFF, Integer.parseInt(strDate.substring(strDate.indexOf(" ") + 1, strDate.indexOf(":"))) & 0xFF, Integer.parseInt(strDate.substring(strDate.indexOf(":") + 1, strDate.lastIndexOf(":"))) & 0xFF, Integer.parseInt(strDate.substring(strDate.lastIndexOf(":") + 1, strDate.length())) & 0xFF);
		return cal;
	}

    /**
     * <p>
     * sets the specified register to value
     * </p>
     *
     * @param name  Register name. Devices supporting OBIS codes,
     *              should use the OBIS code as register name
     * @param value to set the register.
     *              </p>
     * @throws java.io.IOException <br>
     * @throws com.energyict.protocol.UnsupportedException
     *                             if the device does not support this operation
     * @throws com.energyict.protocol.NoSuchRegisterException
     *                             if the device does not support the specified register
     */
    public void setRegister(String name, String value) throws IOException, NoSuchRegisterException, UnsupportedException {
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

	/**
	 * Converts the given string.
	 *
	 * @param s
	 *            The string.
	 *
	 * @return
	 */
	private final byte[] convert(final String s) {
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

    /**
     * <p>
     * sets the device time to the current system time.
     * </p><p>
     *
     * @throws java.io.IOException Thrown in case of an exception
     */
    public void setTime() throws IOException {
        throw new UnsupportedException();
    }

    /**
     * <p>
     * Initializes the device, typically clearing all profile data
     * </p>
     *
     * @throws java.io.IOException <br>
     * @throws com.energyict.protocol.UnsupportedException
     *                             if the device does not support this operation
     */
    public void initializeDevice() throws IOException, UnsupportedException {
        throw new UnsupportedException();
    }

    /**
     * Set the cache object. the object itself is an implementation of a protocol
     * specific cache object representing persistent data to be used with the protocol.
     *
     * @param cacheObject a protocol specific cache object
     */
    public void setCache(Object cacheObject) {
        this.dlmsCache=(DLMSCache)cacheObject;
    }

    /**
     * Returns the protocol specific cache object from the meter protocol implementation.
     *
     * @return the protocol specific cache object
     */
    public Object getCache() {
        return dlmsCache;
    }

    public String getFileName() {
        final Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.YEAR) + "_" + (calendar.get(Calendar.MONTH) + 1) + "_" + calendar.get(Calendar.DAY_OF_MONTH) + "_" + this.deviceId  + "_" + this.serialNumber + "_" + serverUpperMacAddress + "_SimpleDLMS.cache";
    }

    /**
     * Fetch the protocol specific cache object from the database.
     *
     * @param rtuid Database ID of the RTU
     * @return the protocol specific cache object
     * @throws java.sql.SQLException Thrown in case of an SQLException
     * @throws com.energyict.cbo.BusinessException
     *                               Thrown in case of an BusinessException
     */
    public Object fetchCache(int rtuid) throws SQLException, BusinessException {
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

    /**
     * Update the protocol specific cach object information in the database.
     *
     * @param rtuid       Database ID of the RTU
     * @param cacheObject the protocol specific cach object
     * @throws java.sql.SQLException Thrown in case of an SQLException
     * @throws com.energyict.cbo.BusinessException
     *                               Thrown in case of an BusinessException
     */
    public void updateCache(int rtuid, Object cacheObject) throws SQLException, BusinessException {
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

    /**
     * This method is called by the collection software before the disconnect()
     * and can be used to free resources that cannot be freed in the disconnect()
     * method.
     *
     * @throws java.io.IOException Thrown in case of an exception
     */
    public void release() throws IOException {
        // do nothing
    }

    /**
     * Returns a list of required property keys
     *
     * @return a List of String objects
     */
    public List getRequiredKeys() {
        List<String> requiredKeys = new ArrayList<String>();
        return requiredKeys;
    }

    /**
     * Returns a list of optional property keys
     *
     * @return a List of String objects
     */
    public List getOptionalKeys() {
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
        optionalKeys.add("IpPortNumber");
        optionalKeys.add("WakeUp");
        optionalKeys.add("CipheringType");
		optionalKeys.add(LocalSecurityProvider.DATATRANSPORTKEY);
		optionalKeys.add(LocalSecurityProvider.DATATRANSPORT_AUTHENTICATIONKEY);
		optionalKeys.add(LocalSecurityProvider.MASTERKEY);
		optionalKeys.add(LocalSecurityProvider.NEW_GLOBAL_KEY);
		optionalKeys.add(LocalSecurityProvider.NEW_AUTHENTICATION_KEY);
		optionalKeys.add(LocalSecurityProvider.NEW_HLS_SECRET);
        return optionalKeys;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel,false);
    }

    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean enableDataReadout) throws ConnectionException {
		HHUSignOn hhuSignOn = (HHUSignOn) new IEC1107HHUConnection(commChannel, this.timeOut, this.retries, 300, 0);
		hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
		hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
		hhuSignOn.enableDataReadout(enableDataReadout);
		getDLMSConnection().setHHUSignOn(hhuSignOn, this.deviceId);
    }

    public byte[] getHHUDataReadout() {
        return getDLMSConnection().getHhuSignOn().getDataReadout();
    }
}
