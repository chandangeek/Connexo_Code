/*
 * AbstractIEC1107Protocol.java
 *
 * Created on 2 juli 2004, 17:30
 */

package com.energyict.protocolimpl.base;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.DemandResetProtocol;
import com.energyict.protocol.DialinScheduleProtocol;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.HalfDuplexEnabler;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.SerialNumber;
import com.energyict.protocol.meteridentification.DiscoverInfo;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS;
import static com.energyict.mdc.upl.MeterProtocol.Property.NODEID;
import static com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD;
import static com.energyict.mdc.upl.MeterProtocol.Property.PROFILEINTERVAL;
import static com.energyict.mdc.upl.MeterProtocol.Property.RETRIES;
import static com.energyict.mdc.upl.MeterProtocol.Property.ROUNDTRIPCORRECTION;
import static com.energyict.mdc.upl.MeterProtocol.Property.SECURITYLEVEL;
import static com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER;
import static com.energyict.mdc.upl.MeterProtocol.Property.TIMEOUT;

/**
 * Abstract base class to create a new protocol.
 *
 * @author Koen
 */
public abstract class AbstractProtocol extends PluggableMeterProtocol implements HHUEnabler, SerialNumber, MeterExceptionInfo, RegisterProtocol, HalfDuplexEnabler, DialinScheduleProtocol, DemandResetProtocol {

    protected static final String PROP_TIMEOUT = TIMEOUT.getName();
    protected static final String PROP_RETRIES = RETRIES.getName();
    protected static final String PROP_ECHO_CANCELING = "EchoCancelling";
    protected static final String PROP_FORCED_DELAY = "ForcedDelay";

    private static final String PROP_SECURITY_LEVEL = SECURITYLEVEL.getName();
    private static final String PROP_PROTOCOL_COMPATIBLE = "ProtocolCompatible";
    private static final String PROP_EXTENDED_LOGGING = "ExtendedLogging";
    private static final String PROP_CHANNEL_MAP = "ChannelMap";
    private static final String PROP_HALF_DUPLEX = "HalfDuplex";
    private static final String PROP_DTR_BEHAVIOUR = "DTRBehaviour";
    private static final String PROP_ADJUST_CHANNEL_MULTIPLIER = "AdjustChannelMultiplier";
    private static final String PROP_ADJUST_REGISTER_MULTIPLIER = "AdjustRegisterMultiplier";
    private static final String PROP_REQUEST_HEADER = "RequestHeader";
    private static final String PROP_SCALER = "Scaler";

    /**
     * Abstract method to implement the logon and authentication.
     *
     * @throws IOException Exception thrown when the logon fails.
     */
    protected abstract void doConnect() throws IOException, ParseException;

    /**
     * Abstract method to implement the logoff
     *
     * @throws IOException thrown when the logoff fails
     */
    protected abstract void doDisconnect() throws IOException;

    /**
     * Abstract method to add custom properties
     *
     * @param properties The properties map to get properties from.
     * @throws MissingPropertyException Thrown when a particular proiperty is mandatory.
     * @throws InvalidPropertyException Thrown when a particular property has an invalid value.
     */
    protected abstract void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException;

    /**
     * Abstract method to add the optional custom properties to as string.
     *
     * @return List the optional custom properties list of Strings
     */
    protected abstract List<String> doGetOptionalKeys();

    /**
     * Abstract method that implements the construction of all objects needed during the meter protocol session. Last construction is a ProtocolConnection.
     *
     * @param inputStream             Communication inputstream
     * @param outputStream            Communication outputstream
     * @param timeoutProperty         Protocol timeout property. Used to control the interframe timeout. Value of the custom property "Timeout"
     * @param protocolRetriesProperty Used to control the nr of retries whan a CRC, timeout, .. or other error happens during communication. Value of the custom property "Retries"
     * @param forcedDelay             A delay parameter that can be used in the communication classes for example to add delays between communication frames. Value of the custom property "ForcedDelay"
     * @param echoCancelling          Enable or disable echo cancelling. Value of the custom property "EchoCancelling"
     * @param protocolCompatible      Used to control protocol compatibility when the protocol is a member of a group protocols. Value of the custom property "ProtocolCompatible"
     * @param encryptor               Interface to control encryption
     * @param halfDuplexController    Interface to control the HalfDuplex behaviour
     * @return ProtocolConnection interface. Most of the time a connection class is build that implements the ProtocolConnection interface. Thet connection class contains the datalink and phy communication routiones.
     * @throws IOException Thrown when something goes wrong
     */
    protected abstract ProtocolConnection doInit(InputStream inputStream, OutputStream outputStream, int timeoutProperty, int protocolRetriesProperty, int forcedDelay, int echoCancelling, int protocolCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) throws IOException;

    /**
     * Override this method when requesting time from the meter is needed.
     *
     * @return Date object with the metertime
     * @throws IOException thrown when something goes wrong
     */
    public abstract Date getTime() throws IOException;

    /**
     * Override this method when setting the time in the meter is needed
     *
     * @throws IOException thrown when something goes wrong
     */
    public abstract void setTime() throws IOException;

    /**
     * Override this method to control the protocolversion This method is informational only.
     *
     * @return String with protocol version
     */
    public abstract String getProtocolVersion();

    /**
     * Override this method when requesting the meter firmware version is needed. This method is informational only.
     *
     * @return String with firmware version. This can also contain other important info of the meter.
     * @throws IOException thrown when something goes wrong
     * @throws UnsupportedException
     *                             Thrown when that method is not supported
     */
    public abstract String getFirmwareVersion() throws IOException;
    //abstract public String getSerialNumber() throws IOException;

    TimeZone timeZone;
    Logger logger;


    String channelMap; // ChannelMap property (default=null)
    String strID; // device id (default=null)
    String strPassword; // password (default=null)
    private int timeoutProperty; // protocol timeout in ms (default=10000)
    private int protocolRetriesProperty; // nr of retries for the protocol (default=5)
    int roundtripCorrection; // roundtrip correction for the set/get time methods in ms (default=0)
    int securityLevel; // 0=public level, 1=non encrypted password, 2=encrypted password (default=0)
    String nodeId; // multidrop and iec1107 flag id (default=empty)
    int echoCancelling; // 0=disabled, 1=enabled (default=0)
    int extendedLogging; // 0=disabled, 1=enabled (e.g. to get a list of all possible registers that can be read in the meter) (default=0)
    String serialNumber; // meter's serial number (used for handheld connection to identify the meter) (default=null)
    ProtocolChannelMap protocolChannelMap; // null=unused configuration info of the meter's channels (default=null)
    int profileInterval; // meter's profile interval in seconds (default=900)
    int protocolCompatible; // 0=protocol has specific incompatible features (e.g. the A1700 has data streaming mode but is a member of the IEC1107 family), 1=protocol is fully compatible with IEC1107 (default=1)
    ProtocolConnection protocolConnection; // lower layer protocol communication
    MeterType meterType; // signon information of the meter
    int requestHeader; // Request Meter's profile header info (typycal VDEW)
    int scaler; // Scaler to use when retrieving data from the meter
    int forcedDelay; // Delay before data send
    int halfDuplex; // halfduplex enable/disable & delay in ms. (0=disabled, >0 enabled and delay in ms.)

    byte[] dataReadout;
    boolean requestDataReadout;
    Encryptor encryptor;
    HalfDuplexController halfDuplexController = null;

    private BigDecimal adjustChannelMultiplier;
    private BigDecimal adjustRegisterMultiplier;

    private int dtrBehaviour; // 0=force low, 1 force high, 2 don't force anything

    public AbstractProtocol() {
        this(false, null);
    }

    public AbstractProtocol(boolean requestDataReadout) {
        this(requestDataReadout, null);
    }

    public AbstractProtocol(Encryptor encryptor) {
        this(false, encryptor);
    }

    public AbstractProtocol(boolean requestDataReadout, Encryptor encryptor) {
        this.requestDataReadout = requestDataReadout;
        this.encryptor = encryptor;
    }

    @Override
    public Quantity getMeterReading(String name) throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public Quantity getMeterReading(int channelId) throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendar = Calendar.getInstance(getTimeZone());
        calendar.add(Calendar.MONTH, -2);
        return getProfileData(calendar.getTime(), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getProfileData(lastReading, null, includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public void setRegister(String name, String value) throws IOException {
    }

    @Override
    public String getRegister(String name) throws IOException {
        return null;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                UPLPropertySpecFactory.string(ADDRESS.getName(), false),
                UPLPropertySpecFactory.string(PASSWORD.getName(), this.passwordIsRequired()),
                UPLPropertySpecFactory.string(PROP_TIMEOUT, false),
                UPLPropertySpecFactory.integer(PROP_RETRIES, false),
                UPLPropertySpecFactory.integer(ROUNDTRIPCORRECTION.getName(), false),
                UPLPropertySpecFactory.integer(PROP_SECURITY_LEVEL, false),
                UPLPropertySpecFactory.string(NODEID.getName(), false),
                UPLPropertySpecFactory.integer(PROP_ECHO_CANCELING, false),
                UPLPropertySpecFactory.integer(PROP_PROTOCOL_COMPATIBLE, false),
                UPLPropertySpecFactory.integer(PROP_EXTENDED_LOGGING, false),
                UPLPropertySpecFactory.string(SERIALNUMBER.getName(), this.serialNumberIsRequired()),
                ProtocolChannelMap.propertySpec(PROP_CHANNEL_MAP, false),
                UPLPropertySpecFactory.integer(PROFILEINTERVAL.getName(), false),
                UPLPropertySpecFactory.integer(PROP_REQUEST_HEADER, false),
                UPLPropertySpecFactory.integer(PROP_SCALER, false),
                UPLPropertySpecFactory.integer(PROP_FORCED_DELAY, false),
                UPLPropertySpecFactory.integer(PROP_HALF_DUPLEX, false),
                UPLPropertySpecFactory.integer(PROP_DTR_BEHAVIOUR, false),
                UPLPropertySpecFactory.bigDecimal(PROP_ADJUST_CHANNEL_MULTIPLIER, false),
                UPLPropertySpecFactory.bigDecimal(PROP_ADJUST_REGISTER_MULTIPLIER, false));
    }

    protected boolean passwordIsRequired() {
        return false;
    }

    protected boolean serialNumberIsRequired() {
        return false;
    }

    @Override
    public void setProperties(Properties properties) throws PropertyValidationException {
        try {
            strID = properties.getProperty(ADDRESS.getName());
            strPassword = properties.getProperty(PASSWORD.getName());
            setInfoTypeTimeoutProperty(Integer.parseInt(properties.getProperty(PROP_TIMEOUT, "10000").trim()));
            setInfoTypeProtocolRetriesProperty(Integer.parseInt(properties.getProperty(PROP_RETRIES, "5").trim()));
            roundtripCorrection = Integer.parseInt(properties.getProperty(ROUNDTRIPCORRECTION.getName(), "0").trim());
            securityLevel = Integer.parseInt(properties.getProperty(PROP_SECURITY_LEVEL, "1").trim());
            nodeId = properties.getProperty(NODEID.getName(), "");
            echoCancelling = Integer.parseInt(properties.getProperty(PROP_ECHO_CANCELING, "0").trim());
            protocolCompatible = Integer.parseInt(properties.getProperty(PROP_PROTOCOL_COMPATIBLE, "1").trim());
            extendedLogging = Integer.parseInt(properties.getProperty(PROP_EXTENDED_LOGGING, "0").trim());
            serialNumber = properties.getProperty(SERIALNUMBER.getName());
            channelMap = properties.getProperty(PROP_CHANNEL_MAP);
            if (channelMap != null) {
                protocolChannelMap = new ProtocolChannelMap(channelMap);
            }
            profileInterval = Integer.parseInt(properties.getProperty(PROFILEINTERVAL.getName(), "900").trim());
            requestHeader = Integer.parseInt(properties.getProperty(PROP_REQUEST_HEADER, "0").trim());
            scaler = Integer.parseInt(properties.getProperty(PROP_SCALER, "0").trim());
            setForcedDelay(Integer.parseInt(properties.getProperty(PROP_FORCED_DELAY, defaultForcedDelayPropertyValue()).trim()));
            halfDuplex = Integer.parseInt(properties.getProperty(PROP_HALF_DUPLEX, "0").trim());
            setDtrBehaviour(Integer.parseInt(properties.getProperty(PROP_DTR_BEHAVIOUR, "2").trim()));

            adjustChannelMultiplier = new BigDecimal(properties.getProperty(PROP_ADJUST_CHANNEL_MULTIPLIER, "1").trim());
            adjustRegisterMultiplier = new BigDecimal(properties.getProperty(PROP_ADJUST_REGISTER_MULTIPLIER, "1").trim());

            doValidateProperties(properties);
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, this.getClass().getSimpleName() + ": validation of properties failed before");
        }
    }

    protected String defaultForcedDelayPropertyValue() {
        return "300";
    }

    @Override
    public List<String> getOptionalKeys() {
        List<String> result = new ArrayList<>();
        result.add(PROP_TIMEOUT);
        result.add(PROP_RETRIES);
        result.add(PROP_SECURITY_LEVEL);
        result.add(PROP_ECHO_CANCELING);
        result.add(PROP_PROTOCOL_COMPATIBLE);
        result.add(PROP_EXTENDED_LOGGING);
        result.add(PROP_CHANNEL_MAP);
        result.add(PROP_FORCED_DELAY);
        result.add(PROP_HALF_DUPLEX);
        result.add(PROP_DTR_BEHAVIOUR);
        result.add(PROP_ADJUST_CHANNEL_MULTIPLIER);
        result.add(PROP_ADJUST_REGISTER_MULTIPLIER);
        result.addAll(doGetOptionalKeys());
        return result;
    }

    @Override
    public void connect() throws IOException {
        try {
            if (requestDataReadout) {
                dataReadout = getProtocolConnection().dataReadout(strID, nodeId);
                getProtocolConnection().disconnectMAC();
            }
            meterType = getProtocolConnection().connectMAC(strID, strPassword, securityLevel, nodeId);
            doConnect();
        } catch (ProtocolConnectionException e) {
            throw new IOException(e.getMessage());
        } catch (ParseException e) {
            throw new ProtocolException(e);
        }

        try {
            validateDeviceId();
        } catch (ProtocolConnectionException e) {
            disconnect();
            throw new IOException(e.getMessage());
        }

        if (extendedLogging >= 1) {
            logger.info(getRegistersInfo(extendedLogging));
        }
    }


    @Override
    public void disconnect() throws IOException {
        try {
            doDisconnect();
            getProtocolConnection().disconnectMAC();
        } catch (ProtocolConnectionException e) {
            if (logger != null) {
                logger.severe("disconnect() error, " + e.getMessage());
            }

        }
    }

    @Override
    public void release() throws IOException {
    }

    @Override
    public void initializeDevice() throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        this.timeZone = timeZone;
        this.logger = logger;
        protocolConnection = doInit(inputStream, outputStream, timeoutProperty, getInfoTypeProtocolRetriesProperty(), forcedDelay, echoCancelling, protocolCompatible, encryptor, halfDuplex != 0 ? halfDuplexController : null);
    }

    @Override
    public int getProfileInterval() throws IOException {
        return profileInterval;
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        if (protocolChannelMap == null) {
            throw new IOException("getNumberOfChannels(), ChannelMap property not given. Cannot determine the nr of channels...");
        }
        return protocolChannelMap.getNrOfProtocolChannels();
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return null;
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return null;
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, false);
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        HHUSignOn hhuSignOn =
                new IEC1107HHUConnection(commChannel, timeoutProperty, getInfoTypeProtocolRetriesProperty(), 300, echoCancelling);
        hhuSignOn.setMode(HHUSignOn.MODE_PROGRAMMING);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
        hhuSignOn.enableDataReadout(datareadout);
        getProtocolConnection().setHHUSignOn(hhuSignOn);
    }

    @Override
    public byte[] getHHUDataReadout() {
        return getProtocolConnection().getHhuSignOn().getDataReadout();
    }

    @Override
    public String getExceptionInfo(String id) {
        return null;
    }

    @Override
    public void setDialinScheduleTime(Date date) throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public void setPhoneNr(String phoneNr) throws IOException {
        //throw new UnsupportedException();
    }

    @Override
    public void resetDemand() throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        throw new IOException("Not implemented!");
    }

    /**
     * Getter for property meterType.
     *
     * @return Value of property meterType.
     */
    public MeterType getMeterType() {
        return meterType;
    }

    /**
     * Getter for property "ChannelMap"
     *
     * @return ChannelMap object
     */
    @Override
    public ProtocolChannelMap getProtocolChannelMap() {
        return protocolChannelMap;
    }

    @Override
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * Getter for the ProtocolConnection
     *
     * @return ProtocolConnection
     */
    public ProtocolConnection getProtocolConnection() {
        return protocolConnection;
    }

    /**
     * Getter for the data readout
     *
     * @return byte[] data readout
     */
    public byte[] getDataReadout() {
        return dataReadout;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public int getInfoTypeRoundtripCorrection() {
        return roundtripCorrection;
    }

    /**
     * Getter for the custom property "Retries"
     *
     * @return retries
     */
    protected int getInfoTypeRetries() {
        return getInfoTypeProtocolRetriesProperty();
    }

    /**
     * Getter for the custom property "ForcedDelay"
     *
     * @return forced delay in ms
     */
    public int getInfoTypeForcedDelay() {
        return forcedDelay;
    }

    /**
     * Getter for the custom property "Scaler"
     *
     * @return int scaler
     */
    protected int getInfoTypeScaler() {
        return scaler;
    }

    /**
     * Getter for the custom property "SerialNumber"
     *
     * @return String serial number
     */
    protected String getInfoTypeSerialNumber() {
        return serialNumber;
    }

    /**
     * Getter for property strID. strID is the infotype property value MeterProtocol.ADDRESS
     *
     * @return Value of property strID.
     */
    public String getInfoTypeDeviceID() {
        return strID;
    }

    public void setInfoTypeDeviceID(String strID) {
        this.strID = strID;
    }

    /**

     * Getter for property strPassword. strPassword is the infotype property value MeterProtocol.PASSWORD
    * @return Value of property strPassword (infoType).
    */

    /**
     * Getter for the property "Password". Password is the infotype property value MeterProtocol.PASSWORD
     *
     * @return String password
     */
    public String getInfoTypePassword() {
        return strPassword;
    }

    /**
     * Setter for the property "Password". Password is the infotype property value MeterProtocol.PASSWORD
     * This setter is used when a protocol wants to implement another default.
     *
     * @param strPassword password String
     */
    public void setInfoTypePassword(String strPassword) {
        this.strPassword = strPassword;
    }

    /*
    * Getter for property nodeId. nodeId is the infotype property value MeterProtocol.NODEID
    * In the ProtocolTester/Eiserver application, NODEID is the property NodeAddress
    * @return Value of property nodeId (infoType).
    */

    /**
     * Getter for the property "NodeAddress". Password is the infotype property value MeterProtocol.NODEID
     *
     * @return string node address
     */
    public String getInfoTypeNodeAddress() {
        return nodeId;
    }

    /**
     * Getter for the property "NodeAddress". Password is the infotype property value MeterProtocol.NODEID
     * This parses the node address to a numeric value radix 10.
     *
     * @return int nodeAddress
     */
    public int getInfoTypeNodeAddressNumber() {
        if ((nodeId != null) && ("".compareTo(nodeId) != 0)) {
            return Integer.parseInt(nodeId);
        } else {
            return 0;
        }
    }

    /**
     * Setter for the property "NodeAddress". NodeAddress is the infotype property value MeterProtocol.NODEID
     * This setter is used when a protocol wants to implement another default.
     *
     * @param nodeId String nodeAddress
     */
    public void setInfoTypeNodeAddress(String nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * Getter for the property "NodeAddress". Password is the infotype property value MeterProtocol.NODEID
     * This parses the node address to a numeric value radix 16.
     *
     * @return int nodeAddress
     */
    public int getInfoTypeNodeAddressNumberHex() {
        if ((nodeId != null) && ("".compareTo(nodeId) != 0)) {
            return Integer.parseInt(nodeId, 16);
        } else {
            return 0;
        }
    }

    /**
     * Getter for the custom property "ProtocolCompatible"
     *
     * @return int enable (1) or disable (0) protocol compatibility
     */
    public int getInfoTypeProtocolCompatible() {
        return protocolCompatible;
    }

    /**
     * Getter for the custom property "EchoCancelling"
     *
     * @return int enable (1) or disable (0) echo cancelling
     */
    public int getInfoTypeEchoCancelling() {
        return echoCancelling;
    }


    /**
     * Getter for the custom property "SecurityLevel"
     *
     * @return int securitylevel starting from 0
     */
    public int getInfoTypeSecurityLevel() {
        return securityLevel;
    }

    /**
     * Getter for the property "Timeout"
     *
     * @return int timeout in milliseconds
     */
    public int getInfoTypeTimeout() {
        return timeoutProperty;
    }

    /**
     * Getter for the custom property "ChannelMap"
     *
     * @return String ChannelMap
     */
    public String getInfoTypeChannelMap() {
        return channelMap;
    }

    /**
     * Getter for property "ProfileInterval"
     *
     * @return int profile interval in seconds
     */
    public int getInfoTypeProfileInterval() {
        return profileInterval;
    }

    /**
     * Getter for the custom property "ExtendedLogging"
     *
     * @return int enable(1) or disable(0) extended logging
     */
    public int getInfoTypeExtendedLogging() {
        return extendedLogging;
    }

    /**
     * Override if you want to provide info of the meter setup and registers when the "ExtendedLogging" custom property > 0
     *
     * @param extendedLogging int
     * @return String with info
     * @throws IOException thrown when somethoing goes wrong
     */
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        return ("");
    }

    /**
     * Method must be overridden by the subclass to verify the property 'device ID'
     * against the serialnumber read from the meter. Code below as example to implement the method.
     * This code has been taken from a real protocol implementation.
     *
     * @throws IOException Thrown when device id's do not match
     */
    protected void validateDeviceId() throws IOException {

    }

    /**
     * Getter for the custom property "RequestHeader"
     *
     * @return boolean
     */
    public boolean isRequestHeader() {
        return (requestHeader == 1);
    }

    /**
     * Getter for property forcedDelay.
     *
     * @return Value of property forcedDelay.
     */
    public int getForcedDelay() {
        return forcedDelay;
    }

    @Override
    public void setHalfDuplexController(HalfDuplexController halfDuplexController) {
        this.halfDuplexController = halfDuplexController;
        this.halfDuplexController.setDelay(halfDuplex);

        if (getProtocolConnection() != null && getProtocolConnection() instanceof HalfDuplexEnabler) {
            ((HalfDuplexEnabler) getProtocolConnection()).setHalfDuplexController(halfDuplex != 0 ? this.halfDuplexController : null);
        }
    }

    /**
     * Setter for the custom property "ForcedDelay"
     *
     * @param forcedDelay int forceddelay in ms
     */
    public void setForcedDelay(int forcedDelay) {
        this.forcedDelay = forcedDelay;
    }

    /**
     * Setter for the custom property "SecurityLevel"
     *
     * @param securityLevel int starting from 0
     */
    public void setInfoTypeSecurityLevel(int securityLevel) {
        this.securityLevel = securityLevel;
    }

    /**
     * Getter for the custom property "DTRBehaviour"
     *
     * @return int DTR false (0), DTR true (1), DTR auto (2)
     */
    public int getDtrBehaviour() {
        return dtrBehaviour;
    }

    /**
     * Setter for the custom property "DTRBehaviour"
     *
     * @param dtrBehaviour int DTR false (0), DTR true (1), DTR auto (2)
     */
    public void setDtrBehaviour(int dtrBehaviour) {
        this.dtrBehaviour = dtrBehaviour;
    }

    /**
     * Setter for the custom property "HalfDuplex"
     *
     * @param halfDuplex int disabled(0) enabled (>0, in ms)
     */
    public void setInfoTypeHalfDuplex(int halfDuplex) {
        this.halfDuplex = halfDuplex;
    }

    /**
     * Getter for the custom property "HalfDuplex"
     *
     * @return int disabled(0) enabled (>0, in ms)
     */
    public int getInfoTypeHalfDuplex() {
        return halfDuplex;
    }


    /**
     * Setter for the custom property "Timeout"
     *
     * @param timeoutProperty int in ms
     */
    public void setInfoTypeTimeoutProperty(int timeoutProperty) {
        this.timeoutProperty = timeoutProperty;
    }


    /**
     * Getter for the custom property "AdjustChannelMultiplier"
     *
     * @return BigDecimal translated from a string
     */
    public BigDecimal getAdjustChannelMultiplier() {
        return adjustChannelMultiplier;
    }

    /**
     * Getter for the custom property "AdjustRegisterMultiplier"
     *
     * @return BigDecimal translated from a string
     */
    public BigDecimal getAdjustRegisterMultiplier() {
        return adjustRegisterMultiplier;
    }

    @Override
    public int getInfoTypeProtocolRetriesProperty() {
        return protocolRetriesProperty;
    }

    public void setInfoTypeProtocolRetriesProperty(int protocolRetriesProperty) {
        this.protocolRetriesProperty = protocolRetriesProperty;
    }

    public boolean isRequestDataReadout() {
        return requestDataReadout;
    }

    public void setDataReadout(byte[] dataReadout) {
        this.dataReadout = dataReadout;
    }

    public String getStrID() {
        return strID;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setMeterType(MeterType meterType) {
        this.meterType = meterType;
    }

    public int getExtendedLogging() {
        return extendedLogging;
    }

    public int getSecurityLevel() {
        return securityLevel;
    }

    public String getStrPassword() {
        return strPassword;
    }

    protected void setAbstractLogger(Logger logger) {
        this.logger = logger;
    }

}