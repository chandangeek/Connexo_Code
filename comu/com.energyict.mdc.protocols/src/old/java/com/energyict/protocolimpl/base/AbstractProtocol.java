/*
 * AbstractIEC1107Protocol.java
 *
 * Created on 2 juli 2004, 17:30
 */

package com.energyict.protocolimpl.base;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DemandResetProtocol;
import com.energyict.mdc.protocol.api.DialinScheduleProtocol;
import com.energyict.mdc.protocol.api.HHUEnabler;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MeterExceptionInfo;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.SerialNumber;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterProtocol;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.mdc.protocol.api.inbound.DiscoverInfo;
import com.energyict.mdc.protocol.api.inbound.MeterType;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpecFactory;

import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HalfDuplexEnabler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Abstract base class to create a new protocol
 *
 * @author Koen
 */
public abstract class AbstractProtocol extends PluggableMeterProtocol implements HHUEnabler, SerialNumber, MeterExceptionInfo, RegisterProtocol, HalfDuplexEnabler, DialinScheduleProtocol, DemandResetProtocol {

    public static final String PROP_TIMEOUT = "Timeout";
    public static final String PROP_RETRIES = "Retries";
    public static final String PROP_SECURITY_LEVEL = "SecurityLevel";
    public static final String PROP_ECHO_CANCELING = "EchoCancelling";
    public static final String PROP_PROTOCOL_COMPATIBLE = "ProtocolCompatible";
    public static final String PROP_EXTENDED_LOGGING = "ExtendedLogging";
    public static final String PROP_CHANNEL_MAP = "ChannelMap";
    public static final String PROP_FORCED_DELAY = "ForcedDelay";
    public static final String PROP_HALF_DUPLEX = "HalfDuplex";
    public static final String PROP_DTR_BEHAVIOUR = "DTRBehaviour";
    public static final String PROP_ADJUST_CHANNEL_MULTIPLIER = "AdjustChannelMultiplier";
    public static final String PROP_ADJUST_REGISTER_MULTIPLIER = "AdjustRegisterMultiplier";
    public static final String PROP_REQUEST_HEADER = "RequestHeader";
    public static final String PROP_SCALER = "Scaler";

    /**
     * Abstract method to implement the logon and authentication.
     *
     * @throws IOException Exception thrown when the logon fails.
     */
    protected abstract void doConnect() throws IOException;

    /**
     * Abstract method to implement the logoff
     *
     * @throws IOException thrown when the logoff fails
     */
    protected abstract void doDisConnect() throws IOException;

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
     * @throws UnsupportedException Thrown when that method is not supported
     */
    public abstract String getFirmwareVersion() throws IOException;

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

    public AbstractProtocol(PropertySpecService propertySpecService) {
        this(propertySpecService, false, null);
    }

    /**
     * constructor if datareadout is requested
     *
     * @param requestDataReadout enable or disable datareadout
     */
    public AbstractProtocol(PropertySpecService propertySpecService, boolean requestDataReadout) {
        this(propertySpecService, requestDataReadout, null);
    }

    /**
     * Constructor when encryption is needed
     *
     * @param encryptor Encryption interface
     */
    public AbstractProtocol(PropertySpecService propertySpecService, Encryptor encryptor) {
        this(propertySpecService, false, encryptor);
    }

    /* Creates a new instance of AbstractProtocol, default constructor
     *  @param requestDataReadout true if the datadump is needed to read registers.
     *         We only use a datadump if there is no possibility in programming mode to read registers individual.
     *         Datadump registers are always cached.
     *  @param encryptor interface to an encryption algorithm implemented specific for the protocol.
     */

    /**
     * constructor if datareadout is requested and encryption is needed
     *
     * @param requestDataReadout enable or disable datareadout
     * @param encryptor          Encryption interface
     */
    public AbstractProtocol(PropertySpecService propertySpecService, boolean requestDataReadout, Encryptor encryptor) {
        super(propertySpecService);
        this.requestDataReadout = requestDataReadout;
        this.encryptor = encryptor;
    }

    /**
     * This method is only for debugging purposes.
     *
     * @param name register name
     * @return Quantity with register value and Unit object
     * @throws UnsupportedException thrown when not supported
     * @throws IOException thrown when something goes wrong
     */
//    public String getProtocolVersion() {
//        return "$Revision: 1.35 $";
//    }
//    public String getFirmwareVersion() throws IOException, UnsupportedException {
//        throw new UnsupportedException();
//    }
    // obsolete
    public Quantity getMeterReading(String name) throws IOException {
        throw new UnsupportedException();
    }
    // obsolete

    /**
     * This method is only for debugging purposes.
     *
     * @param channelId id of a register.
     * @return Quantity with register value and Unit object
     * @throws UnsupportedException
     *                             thrown when not supported
     * @throws IOException thrown when something goes wrong
     */
    public Quantity getMeterReading(int channelId) throws IOException {
        throw new UnsupportedException();
    }
    /*
     *  Default, we ask for 2 months of profile data!
     */

    /**
     * Override this method to request the load profile from the meter.
     *
     * @param includeEvents enable or disable tht reading of meterevents
     * @return All load profile data in the meter
     * @throws IOException When something goes wrong
     */
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendar = Calendar.getInstance(getTimeZone());
        calendar.add(Calendar.MONTH, -2);
        return getProfileData(calendar.getTime(), includeEvents);
    }

    /**
     * Override this method to request the load profile from the meter starting at lastreading until now.
     *
     * @param lastReading   request from
     * @param includeEvents enable or disable tht reading of meterevents
     * @return All load profile data in the meter from lastReading
     * @throws IOException When something goes wrong
     */
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getProfileData(lastReading, null, includeEvents);
    }

    /**
     * Override this method to request the load profile from the meter from to.
     *
     * @param from          request from
     * @param to            request to
     * @param includeEvents eneble or disable requesting of meterevents
     * @return ProfileData object
     * @throws IOException Thrown when something goes wrong
     * @throws UnsupportedException
     *                             Thrown when not supported
     */
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        throw new UnsupportedException();
    }

    /*
    * Override this method if the subclass wants to set the device time
    */
//    public void setTime() throws IOException {
//    }
    /*
     * Override this method if the subclass wants to set a specific register
     */

    /**
     * For debugging only
     *
     * @param name  For debugging only
     * @param value For debugging only
     * @throws IOException For debugging only
     * @throws NoSuchRegisterException For debugging only
     * @throws UnsupportedException For debugging only
     */
    public void setRegister(String name, String value) throws IOException {
    }
    /*
     * Override this method if the subclass wants to get a specific register
     */

    /**
     * For debugging only
     *
     * @param name For debugging only
     * @return For debugging only
     * @throws IOException For debugging only
     * @throws UnsupportedException For debugging only
     * @throws NoSuchRegisterException For debugging only
     */
    public String getRegister(String name) throws IOException {
        return null;
    }

    /**
     * Used by the framework
     *
     * @param properties Used by the framework
     * @throws InvalidPropertyException Thrown when a particular property has an invalid value.
     * @throws MissingPropertyException Thrown when a particular proiperty is mandatory.
     */
    public void setProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException {
        validateProperties(properties);
    }

    /**
     * the implementation returns both the address and password key
     *
     * @return a list of strings
     */
    public List<String> getRequiredKeys() {
        return new ArrayList<>(0);
    }

    /**
     * this implementation returns an empty list
     *
     * @return a list of strings
     */
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

// if needed, add following codelines into the overridden doGetOptionalKeys() method
//        result.add("RequestHeader"));
//        result.add("Scaler"));

        List<String> result2 = doGetOptionalKeys();
        if (result2 != null) {
            result.addAll(result2);
        }
        return result;
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
     * Used by the framework. This method calls the abstract doConnect method
     *
     * @throws IOException thrown when something goes wrong
     */
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
        }

        try {
            validateSerialNumber();
        } catch (ProtocolConnectionException e) {
            disconnect();
            throw new IOException(e.getMessage());
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


    /**
     * Used by the framework. This method calls the abstract doDisconnect method
     *
     * @throws IOException thrown when something goes wrong
     */
    public void disconnect() throws IOException {
        try {
            doDisConnect();
            getProtocolConnection().disconnectMAC();
        } catch (ProtocolConnectionException e) {
            if (logger != null) {
                logger.severe("disconnect() error, " + e.getMessage());
            }

        }
    }

    /**
     * Override this method to cleanup allocated resources in case of an exception that causes the protocolsession to end.
     *
     * @throws IOException thrown when something goes wrong
     */
    public void release() throws IOException {
    }

    /**
     * Used by the framework
     *
     * @throws IOException thrown when something goes wrong
     * @throws UnsupportedException
     *                             thrown when not supported
     */
    public void initializeDevice() throws IOException {
        throw new UnsupportedException();
    }

    /**
     * Used by the framework. This method calls the abstract doInit method
     *
     * @param inputStream  communication inputstream
     * @param outputStream communication outputstream
     * @param timeZone     timezone of the meter
     * @param logger       framework logger object to be used by the protocol to log info
     * @throws IOException thrown when something goes wrong
     */
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        this.timeZone = timeZone;
        this.logger = logger;
        protocolConnection = doInit(inputStream, outputStream, timeoutProperty, getInfoTypeProtocolRetriesProperty(), forcedDelay, echoCancelling, protocolCompatible, encryptor, halfDuplex != 0 ? halfDuplexController : null);
    }
    // Cach mechanism of the MeterProtocol interface

    public void updateCache(int rtuid, Object cacheObject) throws SQLException {
    }

    /**
     * Override this method to set the cache data object
     *
     * @param cacheObject cach data
     */
    public void setCache(Object cacheObject) {
    }

    public Object fetchCache(int rtuid) throws SQLException {
        return null;
    }

    /**
     * Override this method to get the cache data object
     *
     * @return cache data object
     */
    public Object getCache() {
        return null;
    }

    /**
     * Override this method to requesting the load profile integration time
     *
     * @return integration time in seconds
     * @throws UnsupportedException
     *                             thrown when not supported
     * @throws IOException Thrown when something goes wrong
     */
    public int getProfileInterval() throws IOException {
        return profileInterval;
    }

    /**
     * Override this method to requesting the nr of load profile channels from the meter. If not overridden, the default implementation uses the ChannelMap object to get the nr of channels. The ChannelMap object is constructed from the ChannelMap custom property containing a comma separated string. The nr of comma separated tokens is the nr of channels.
     *
     * @return nr of load profile channels
     * @throws UnsupportedException
     *                             thrown when not supported
     * @throws IOException thrown when something goes wrong
     */
    public int getNumberOfChannels() throws IOException {
        if (protocolChannelMap == null) {
            throw new IOException("getNumberOfChannels(), ChannelMap property not given. Cannot determine the nr of channels...");
        }
        return protocolChannelMap.getNrOfProtocolChannels();
    }


    /**
     * Override this method to provide meter specific info for an obiscode mapped register. This method is called outside the communication session. So the info provided is static info in the protocol.
     *
     * @param obisCode obiscode of the register to lookup
     * @return RegisterInfo object
     * @throws IOException thrown when somethiong goes wrong
     */
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return null;
    }

    /**
     * Override this method when requesting an obiscode mapped register from the meter.
     *
     * @param obisCode obiscode rmapped register to request from the meter
     * @return RegisterValue object
     * @throws IOException thrown when somethiong goes wrong
     */
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return null;
    }

    /**
     * Used by the framework
     *
     * @param commChannel communication channel object
     * @throws ConnectionException
     *          thrown when a connection exception happens
     */
    /*
     *  Default implementation of the HHU interfacing. These classes can be overridden by
     *  the subclass if the implementation should be different.
     */
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, false);
    }

    /**
     * Used by the framework
     *
     * @param commChannel communication channel object
     * @param datareadout enable or disable data readout
     * @throws ConnectionException
     *          thrown when a connection exception happens
     */
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        HHUSignOn hhuSignOn = new IEC1107HHUConnection(commChannel, timeoutProperty, getInfoTypeProtocolRetriesProperty(), 300, echoCancelling);
        hhuSignOn.setMode(HHUSignOn.MODE_PROGRAMMING);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
        hhuSignOn.enableDataReadout(datareadout);
        getProtocolConnection().setHHUSignOn(hhuSignOn);
    }

    /**
     * Getter for the data readout
     *
     * @return byte[] with data readout
     */
    public byte[] getHHUDataReadout() {
        return getProtocolConnection().getHhuSignOn().getDataReadout();
    }

    /**
     * Override this method if you need specific meter exception info for a certain id.
     *
     * @param id id of the exception
     * @return String with exception info
     */
    /*
     *  This method must be overridden by the subclass to implement meter specific error
     *  messages. Us sample code of a static map with error codes below as a sample and
     *  use code in method as a sample of how to retrieve the error code.
     *  This code has been taken from a real protocol implementation.
     */
/*
    static Map exceptionInfoMap = new HashMap();
    static {
           exceptionInfoMap.put("ER01","Unknown command");
           exceptionInfoMap.put("ER02","Invalid command");
    }
 */
    public String getExceptionInfo(String id) {
        /*
        String exceptionInfo = (String)exceptionInfoMap.get(id);
        if (exceptionInfo != null)
           return id+", "+exceptionInfo;
        else
           return "No meter specific exception info for "+id;
        */
        return null;
    }


    /*******************************************************************************************
     D i a l i n S c h e d u l e P r o t o c o l  i n t e r f a c e
     *******************************************************************************************/
    /**
     * Setter for setting the meters next scheduled dialin time & date. It is the time & date
     * at which the meter will perform an inbound call into the server.
     *
     * @param date next scheduled dialin time & date
     * @throws IOException in case of communication or other errors
     */
    public void setDialinScheduleTime(Date date) throws IOException {
        throw new UnsupportedException();
    }

    /**
     * Setter for setting the meters phoneNr to dialin to
     * at which the meter will perform an inbound call into the server.
     *
     * @param phoneNr, the phone number as string
     * @throws IOException in case of communication or other errors
     */
    public void setPhoneNr(String phoneNr) throws IOException {
        //throw new UnsupportedException();
    }

    /**
     * Override this method if you want to perform a demand reset in the meter.
     *
     * @throws IOException thrown when something goes wrong
     */
    /*
     * This method performs a demand & billing reset in the meter.
     * @throws IOException in case of communication or other errors
     */
    public void resetDemand() throws IOException {
        throw new UnsupportedException();
    }

    /**
     * Override this method if you want to request the meter serial number during discover an inbound call.
     *
     * @param discoverInfo discoverinfo to use to connect to the meter
     * @return String serial number
     * @throws IOException thrown when somethiong goes wrong
     */
    /*
     *  Method to be overridden by the subclass if the serialnumber of the meter device is not in the datadump
     *  or the meter does not provide a datadump. The method is used by the Hand-Held mechanism to uniquely identify
     *  a meter device (com.energyict.protocolimpl.base.IEC1107HHUConnection).
     *  Use the sample code below as an example how to retrieve the serialnumber from a meter using a level 0
     *  security (=no password).
     *  This code has been taken from a real protocol implementation.
     */
    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
/*
        Properties properties = new Properties();
        properties.setProperty("SecurityLevel","0");
        properties.setProperty(MeterProtocol.NODEID,nodeId);
        properties.setProperty("IEC1107Compatible","1");
        setProperties(properties);
        init(discoverInfo.getCommChannel().getInputStream(),discoverInfo.getCommChannel().getOutputStream(),null,null);
        enableHHUSignOn(commChannel);
        connect();
        String serialNumber =  getRegister("SerialNumber");
        disconnect();
        return serialNumber;
*/
        throw new IOException("Not implemented!");
    }

    /*******************************************************************************************
     G e t t e r s  &  s e t t e r s  o f  p r o p e r t i e s
     *******************************************************************************************/

    /**
     * Getter for property meterType.
     *
     * @return Value of property meterType.
     */
    public MeterType getMeterType() {
        return meterType;
    }

    /*
    * Getter for the configured 'ChannelMap' infotype property.
    * @return Value of property ChannelMap (infoType
    * e.g. 1:4:6
    */

    /**
     * Getter for property "ChannelMap"
     *
     * @return ChannelMap object
     */
    public ProtocolChannelMap getProtocolChannelMap() {
        return protocolChannelMap;
    }

    /*
    * Getter for the configured device TimeZone.
    * @return Value of the device TimeZone.
    */

    /**
     * Getter for meter timezone
     *
     * @return TimeZone
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /*
    * Getter for the lower layer ProtocolConnection interface
    * @return Value of the lower layer ProtocolConnection interface
    */

    /**
     * Getter for the ProtocolConnection
     *
     * @return ProtocolConnection
     */
    public ProtocolConnection getProtocolConnection() {
        return protocolConnection;
    }

    /*
    * Getter for the datadump byte array.
    * @return Value of the datadump byte array
    */

    /**
     * Getter for the data readout
     *
     * @return byte[] data readout
     */
    public byte[] getDataReadout() {
        return dataReadout;
    }

    /*
    * Getter for the Logger instance.
    * @return Value of the Logger instance
    */

    /**
     * Getter for the framework Logger object
     *
     * @return Logger object
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Getter for the custom property "RoundTripCorrection"
     *
     * @return roundtrip correction in ms
     */

    /*
    * Getter for the configured infotype property RoundtripCorrection.
    * @return Value of property roundtripCorrection (infoType).
    */
    public int getInfoTypeRoundtripCorrection() {
        return roundtripCorrection;
    }

    /*
     * Getter for the configured infotype property Retries.
     * @return Value of property protocolRetriesProperty (infoType).
     */

    /**
     * Getter for the custom property "Retries"
     *
     * @return retries
     */
    protected int getInfoTypeRetries() {
        return getInfoTypeProtocolRetriesProperty();
    }

    /*
    * Getter for the configured infotype property forcedDelay.
    * @return Value of property forcedDelay (infoType).
    */

    /**
     * Getter for the custom property "ForcedDelay"
     *
     * @return forced delay in ms
     */
    public int getInfoTypeForcedDelay() {
        return forcedDelay;
    }

    /*
    * Getter for the configured infotype Scaler.
    * @return Value of property Scaler (infoType).
    */

    /**
     * Getter for the custom property "Scaler"
     *
     * @return int scaler
     */
    protected int getInfoTypeScaler() {
        return scaler;
    }

    /*
    * Getter for the configured infotype property SerialNumber. SerialNumber is the infotype property value MeterProtocol.SERIALNUMBER
    * @return Value of property serialNumber (infoType).
    */

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

    /*
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

    /*
    * Getter for the configured infotype property ProtocolCompatible.
    * @return Value of property protocolCompatible (infoType).
    */

    /**
     * Getter for the custom property "ProtocolCompatible"
     *
     * @return int enable (1) or disable (0) protocol compatibility
     */
    public int getInfoTypeProtocolCompatible() {
        return protocolCompatible;
    }

    /*
    * Getter for the configured infotype property EchoCancelling.
    * @return Value of property echoCancelling (infoType).
    */

    /**
     * Getter for the custom property "EchoCancelling"
     *
     * @return int enable (1) or disable (0) echo cancelling
     */
    public int getInfoTypeEchoCancelling() {
        return echoCancelling;
    }


    /*
    * Getter for the configured infotype property SecurityLevel.
    * @return Value of property securityLevel (infoType).
    */

    /**
     * Getter for the custom property "SecurityLevel"
     *
     * @return int securitylevel starting from 0
     */
    public int getInfoTypeSecurityLevel() {
        return securityLevel;
    }

    /*
    * Getter for the configured infotype property Timeout.
    * @return Value of property timeoutProperty (infoType).
    */

    /**
     * Getter for the property "Timeout"
     *
     * @return int timeout in milliseconds
     */
    public int getInfoTypeTimeout() {
        return timeoutProperty;
    }

    /*
     * Getter for the configured infotype property ChannelMap.
     * @return Value of property channelMap (infoType).
     */

    /**
     * Getter for the custom property "ChannelMap"
     *
     * @return String ChannelMap
     */
    public String getInfoTypeChannelMap() {
        return channelMap;
    }

    /*
    * Getter for the configured infotype property ProfileInterval.
    * @return Value of property profileInterval (infoType).
    */

    /**
     * Getter for property "ProfileInterval"
     *
     * @return int profile interval in seconds
     */
    public int getInfoTypeProfileInterval() {
        return profileInterval;
    }

    /*
    * Getter for the configured infotype property ExtendedLogging.
    * @return Value of property extendedLogging (infoType).
    */

    /**
     * Getter for the custom property "ExtendedLogging"
     *
     * @return int enable(1) or disable(0) extended logging
     */
    public int getInfoTypeExtendedLogging() {
        return extendedLogging;
    }

    /**
     * ****************************************************************************************
     * C l a s s  i m p l e m e n t a t i o n  c o d e
     * *****************************************************************************************
     */
    private void validateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        try {
            for (String key : getRequiredKeys()) {
                if (properties.getProperty(key) == null) {
                    throw new MissingPropertyException(key + " key missing");
                }
            }
            strID = properties.getProperty(MeterProtocol.ADDRESS);
            strPassword = properties.getProperty(MeterProtocol.PASSWORD);
            setInfoTypeTimeoutProperty(Integer.parseInt(properties.getProperty(PROP_TIMEOUT, "10000").trim()));
            setInfoTypeProtocolRetriesProperty(Integer.parseInt(properties.getProperty(PROP_RETRIES, "5").trim()));
            roundtripCorrection = Integer.parseInt(properties.getProperty(ROUNDTRIPCORR, "0").trim());
            securityLevel = Integer.parseInt(properties.getProperty(PROP_SECURITY_LEVEL, "1").trim());
            nodeId = properties.getProperty(MeterProtocol.NODEID, "");
            echoCancelling = Integer.parseInt(properties.getProperty(PROP_ECHO_CANCELING, "0").trim());
            protocolCompatible = Integer.parseInt(properties.getProperty(PROP_PROTOCOL_COMPATIBLE, "1").trim());
            extendedLogging = Integer.parseInt(properties.getProperty(PROP_EXTENDED_LOGGING, "0").trim());
            serialNumber = properties.getProperty(MeterProtocol.SERIALNUMBER);
            channelMap = properties.getProperty(PROP_CHANNEL_MAP);
            if (channelMap != null) {
                protocolChannelMap = new ProtocolChannelMap(channelMap);
            }
            profileInterval = Integer.parseInt(properties.getProperty(PROFILEINTERVAL, "900").trim());
            requestHeader = Integer.parseInt(properties.getProperty(PROP_REQUEST_HEADER, "0").trim());
            scaler = Integer.parseInt(properties.getProperty(PROP_SCALER, "0").trim());
            setForcedDelay(Integer.parseInt(properties.getProperty(PROP_FORCED_DELAY, "300").trim()));
            halfDuplex = Integer.parseInt(properties.getProperty(PROP_HALF_DUPLEX, "0").trim());
            setDtrBehaviour(Integer.parseInt(properties.getProperty(PROP_DTR_BEHAVIOUR, "2").trim()));

            adjustChannelMultiplier = new BigDecimal(properties.getProperty(PROP_ADJUST_CHANNEL_MULTIPLIER, "1").trim());
            adjustRegisterMultiplier = new BigDecimal(properties.getProperty(PROP_ADJUST_REGISTER_MULTIPLIER, "1").trim());

            doValidateProperties(properties);
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(" validateProperties, NumberFormatException, " + e.getMessage());
        }
    }

    /*
    *  Method must be overridden by the subclass to build a StringBuffer with all
    *  possible registers that can be read from the particular meter. The StringBuffer
    *  is then logged as info.
    *  This method is called if the 'ExtendedLogging' property is set to 1
    */

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
     * Method must be overridden by the subclass to verify the property 'SerialNumber'
     * against the serialnumber read from the meter. Code below as example to implement the method.
     * This code has been taken from a real protocol implementation.
     *
     * @throws IOException thrown when the serial numbers do not match
     */
    protected void validateSerialNumber() throws IOException {
        /*
         boolean check = true;
        if ((getInfoTypeSerialNumber() == null) || ("".compareTo(getInfoTypeSerialNumber())==0)) return;
        String sn = (String)get[protocol]Registry().getRegister("[name of register that contains serial nulber]");
        if (sn.compareTo(getInfoTypeSerialNumber()) == 0) return;
        throw new IOException("SerialNumber mismatch! meter sn="+sn+", configured sn="+getInfoTypeSerialNumber());
        */
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

    // implement HalfDuplexEnabler

    /**
     * Used by the Framework
     *
     * @param halfDuplexController HalfDuplexController
     */
    public void setHalfDuplexController(HalfDuplexController halfDuplexController) {
        this.halfDuplexController = halfDuplexController;
        halfDuplexController.setDelay(halfDuplex);
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

    public Calendar getCleanCalendar(TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.clear();
        return calendar;
    }

}