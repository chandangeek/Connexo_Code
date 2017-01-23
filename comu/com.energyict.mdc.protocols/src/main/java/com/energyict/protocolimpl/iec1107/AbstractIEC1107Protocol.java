/*
 * AbstractIEC1107Protocol.java
 *
 * Created on 2 juli 2004, 17:30
 */

package com.energyict.protocolimpl.iec1107;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.HHUEnabler;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MeterExceptionInfo;
import com.energyict.mdc.protocol.api.MissingPropertyException;
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
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.base.ProtocolChannelMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * @author Koen
 */
public abstract class AbstractIEC1107Protocol extends PluggableMeterProtocol implements ProtocolLink, HHUEnabler, SerialNumber,
        MeterExceptionInfo, RegisterProtocol {

    /**
     * Implement additional code after the SignOn has passed successfully
     *
     * @throws IOException in case of an Exception
     */
    protected abstract void doConnect() throws IOException;

    /**
     * Validate some protocol specific properties.<br>
     * See {@link #setProperties(Properties)} for the overview of the default properties set for an
     * AbstractIEC1107Protocol
     *
     * @param properties - The properties fetched from the Device
     * @throws MissingPropertyException If a property from the {@link #getRequiredKeys()} list was missing.
     * @throws InvalidPropertyException If a property has an invalid value/format
     */
    protected abstract void doValidateProperties(Properties properties) throws MissingPropertyException,
            InvalidPropertyException;

    /**
     * Provide a List of Optional keys needed as Device Properties
     *
     * @return a String List of properties
     */
    protected abstract List<String> doGetOptionalKeys();

    TimeZone timeZone;
    Logger logger;

    protected String strID; // device id (default=null)
    protected String strPassword; // password (default=null)
    protected int iec1107TimeoutProperty; // protocol timeout in ms (default=10000)
    protected int protocolRetriesProperty; // nr of retries for the protocol (default=5)
    protected int roundtripCorrection; // roundtrip correction for the set/get time methods in ms (default=0)
    protected int securityLevel; // 0=public level, 1=non encrypted password, 2=encrypted password (default=0)
    protected String nodeId; // multidrop and iec1107 flag id (default=empty)
    protected int echoCancelling; // 0=disabled, 1=enabled (default=0)
    protected int iec1107Compatible; // 0=protocol has IEC1107 incompatible features (e.g. the A1700 has data streaming
    // mode), 1=protocol is fully compatible
    // with IEC1107
    // (default=1)
    protected int extendedLogging; // 0=disabled, 1=enabled (e.g. to get a list of all possible registers that can be
    // read in the meter) (default=0)
    protected String serialNumber; // meter's serial number (used for handheld connection to identify the meter)
    // (default=null)
    protected ChannelMap channelMap; // null=unused configuration info of the meter's channels (default=null)
    protected ProtocolChannelMap protocolChannelMap; // null=unused configuration info of the meter's channels
    // (default=null)
    protected int profileInterval; // meter's profile interval in seconds (default=900)
    protected int requestHeader; // Request Meter's profile header info (typycal VDEW)
    protected int scaler; // Scaler to use when retrieving data from the meter
    protected int forcedDelay; // Delay before sending data
    protected FlagIEC1107Connection flagIEC1107Connection; // lower layer IEC1107 communication
    protected MeterType meterType; // signon information of the meter

    protected byte[] dataReadout;
    protected boolean requestDataReadout;
    protected boolean software7E1;
    protected Encryptor encryptor;

    public AbstractIEC1107Protocol(PropertySpecService propertySpecService) {
        this(propertySpecService, false, null);
    }

    public AbstractIEC1107Protocol(PropertySpecService propertySpecService, boolean requestDataReadout) {
        this(propertySpecService, requestDataReadout, null);
    }

    public AbstractIEC1107Protocol(PropertySpecService propertySpecService, Encryptor encryptor) {
        this(propertySpecService, false, encryptor);
    }

    /**
     * Creates a new instance of AbstractIEC1107Protocol, default constructor
     *
     * @param requestDataReadout true if the datadump is needed to read registers. We only use a datadump if there is no possibility in
     *                           programming mode to read registers individual. Datadump registers are always cached.
     * @param encryptor          interface to an encryption algorithm implemented specific for the protocol.
     */
    public AbstractIEC1107Protocol(PropertySpecService propertySpecService, boolean requestDataReadout, Encryptor encryptor) {
        super(propertySpecService);
        this.requestDataReadout = requestDataReadout;
        this.encryptor = encryptor;
    }

    // *******************************************************************************************
    // M e t e r p r o t o c o l i n t e r f a c e
    // *******************************************************************************************/
    public String getFirmwareVersion() throws IOException {
        throw new UnsupportedException();
    }

    // obsolete
    public Quantity getMeterReading(String name) throws IOException {
        throw new UnsupportedException();
    }

    // obsolete
    public Quantity getMeterReading(int channelId) throws IOException {
        throw new UnsupportedException();
    }

    /*
      * Default, we ask for 2 months of profile data!
      */
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendar = Calendar.getInstance(getTimeZone());
        calendar.add(Calendar.MONTH, -2);
        return getProfileData(calendar.getTime(), includeEvents);
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return null;
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        throw new UnsupportedException();
    }

    /*
      * Override this method if the subclass wants to get the device time
      */
    public Date getTime() throws IOException {
        return null;
    }

    /*
      * Override this method if the subclass wants to set the device time
      */
    public void setTime() throws IOException {
    }

    /*
      * Override this method if the subclass wants to set a specific register
      */
    public void setRegister(String name, String value) throws IOException {
    }

    /*
      * Override this method if the subclass wants to get a specific register
      */
    public String getRegister(String name) throws IOException {
        return null;
    }

    public void setProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException {
        validateProperties(properties);
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
        List<String> result = new ArrayList();
        result.add("Timeout");
        result.add("Retries");
        result.add("SecurityLevel");
        result.add("EchoCancelling");
        result.add("IEC1107Compatible");
        result.add("ExtendedLogging");
        result.add("ChannelMap");
        result.add("ForcedDelay");
        result.add("Software7E1");
        // if needed, add following codelines into the overridden doGetOptionalKeys() method
        // result.add("RequestHeader"));
        // result.add("Scaler"));
        result.addAll(this.doGetOptionalKeys());
        return result;
    }

    public void connect() throws IOException {
        try {
            if (requestDataReadout) {
                dataReadout = getFlagIEC1107Connection().dataReadout(strID, nodeId);
                getFlagIEC1107Connection().disconnectMAC();
            }
            meterType = getFlagIEC1107Connection().connectMAC(strID, strPassword, securityLevel, nodeId);
            doConnect();
        } catch (FlagIEC1107ConnectionException e) {
            throw new IOException(e.getMessage());
        }

        try {
            validateSerialNumber();
        } catch (FlagIEC1107ConnectionException e) {
            disconnect();
            throw new IOException(e.getMessage());
        }

        if (extendedLogging >= 1) {
            logger.info(getRegistersInfo(extendedLogging));
        }
    }

    public void disconnect() throws NestedIOException {
        try {
            getFlagIEC1107Connection().disconnectMAC();
        } catch (FlagIEC1107ConnectionException e) {
            logger.severe("disconnect() error, " + e.getMessage());
        }
    }

    public void release() throws IOException {
    }

    public void initializeDevice() throws IOException {
        throw new UnsupportedException();
    }

    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger)
            throws IOException {
        this.timeZone = timeZone;
        this.logger = logger;
        try {
            flagIEC1107Connection = new FlagIEC1107Connection(inputStream, outputStream, iec1107TimeoutProperty,
                    protocolRetriesProperty, forcedDelay, echoCancelling, iec1107Compatible, encryptor, software7E1, logger);
        } catch (ConnectionException e) {
            logger.severe("IndigoPlus, init, " + e.getMessage());
        }
    }

    // Cach mechanism of the MeterProtocol interface
    public void updateCache(int rtuid, Object cacheObject) {
    }

    public void setCache(Object cacheObject) {
    }

    public Object fetchCache(int rtuid) {
        return null;
    }

    public Object getCache() {
        return null;
    }

    // *******************************************************************************************
    // * P r o t o c o l L i n k i n t e r f a c e M e t e r p r o t o c o l i n t e r f a c e
    // *******************************************************************************************/

    public int getProfileInterval() throws IOException {
        return profileInterval;
    }

    public int getNumberOfChannels() throws IOException {
        return channelMap.getNrOfChannels();
    }

    // *******************************************************************************************
    // * P r o t o c o l L i n k i n t e r f a c e
    // *******************************************************************************************/

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public boolean isIEC1107Compatible() {
        return (iec1107Compatible == 1);
    }

    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return flagIEC1107Connection;
    }

    public byte[] getDataReadout() {
        return dataReadout;
    }

    public String getPassword() {
        return strPassword;
    }

    public Logger getLogger() {
        return logger;
    }

    public ProtocolChannelMap getProtocolChannelMap() {
        return protocolChannelMap;
    }

    public ChannelMap getChannelMap() {
        return channelMap;
    }

    public int getNrOfRetries() {
        return protocolRetriesProperty;
    }

    // *******************************************************************************************
    // * R e g i s t e r P r o t o c o l i n t e r f a c e
    // *******************************************************************************************/

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return null;
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return null;
    }

    /**
     * ****************************************************************************************
     * H H U E n a b l e r i n t e r f a c e
     * *****************************************************************************************
     */
    /*
      * Default implementation of the HHU interfacing. These cklasses can be overridden by the subclass if the
      * implementation should be different.
      */
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, false);
    }

    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        HHUSignOn hhuSignOn = new IEC1107HHUConnection(commChannel, iec1107TimeoutProperty,
                protocolRetriesProperty, 300, echoCancelling);
        hhuSignOn.setMode(HHUSignOn.MODE_PROGRAMMING);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
        hhuSignOn.enableDataReadout(datareadout);
        getFlagIEC1107Connection().setHHUSignOn(hhuSignOn);
    }

    public byte[] getHHUDataReadout() {
        return getFlagIEC1107Connection().getHhuSignOn().getDataReadout();
    }

    /**
     * ****************************************************************************************
     * M e t e r E x c e p t i o n I n f o i n t e r f a c e
     * *****************************************************************************************
     */
    /*
      * This method must be overridden by the subclass to implement meter specific error messages. Us sample code of a
      * static map with error codes below as a sample and use code in method as a sample of how to retrieve the error
      * code. This code has been taken from a real protocol implementation.
      */
    /*
      * static Map exceptionInfoMap = new HashMap(); static { exceptionInfoMap.put("ER01","Unknown command");
      * exceptionInfoMap.put("ER02","Invalid command"); }
      */
    public String getExceptionInfo(String id) {
        /*
           * String exceptionInfo = (String)exceptionInfoMap.get(id); if (exceptionInfo != null) return
           * id+", "+exceptionInfo; else return "No meter specific exception info for "+id;
           */
        return null;
    }

    // *******************************************************************************************
    // * S e r i a l N u m b e r i n t e r f a c e
    // *******************************************************************************************/

    /**
     * Method to be overridden by the subclass if the serialnumber of the meter device is not in the datadump or the
     * meter does not provide a datadump. The method is used by the Hand-Held mechanism to uniquely identify a meter
     * device (com.energyict.protocolimpl.base.IEC1107HHUConnection). Use the sample code below as an example how to
     * retrieve the serialnumber from a meter using a level 0 security (=no password). This code has been taken from a
     * real protocol implementation.
     */
    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        /*
           * SerialCommunicationChannel commChannel = discoverInfo.getCommChannel(); String nodeId =
           * discoverInfo.getNodeId(); Properties properties = new Properties();
           * properties.setProperty("SecurityLevel","0"); properties.setProperty(MeterProtocol.NODEID,nodeId);
           * properties.setProperty("IEC1107Compatible","1"); setProperties(properties);
           * init(commChannel.getInputStream(),commChannel .getOutputStream(),null,null); enableHHUSignOn(commChannel);
           * connect(); String serialNumber = getRegister("SerialNumber"); disconnect(); return serialNumber;
           */
        throw new IOException("Not implemented!");
    }

    // *******************************************************************************************
    // * G e t t e r s & s e t t e r s o f p r o p e r t i e s
    // *******************************************************************************************/

    /**
     * Getter for property roundtripCorrection.
     *
     * @return Value of property roundtripCorrection.
     */
    protected int getRoundtripCorrection() {
        return roundtripCorrection;
    }

    /**
     * Getter for property meterType.
     *
     * @return Value of property meterType.
     */
    private MeterType getMeterType() {
        return meterType;
    }

    /**
     * Getter for the configured 'SerialNumber' infotype property.
     *
     * @return Value of infotype property meterType.
     */
    protected String getInfoTypeSerialNumber() {
        return serialNumber;
    }

    // *******************************************************************************************
    // * C l a s s i m p l e m e n t a t i o n c o d e
    // *******************************************************************************************/

    /**
     * Validate certain protocol specific properties
     *
     * @param properties - The properties fetched from the Device
     * @throws MissingPropertyException If a property from the {@link #getRequiredKeys()} list was missing.
     * @throws InvalidPropertyException If a property has an invalid value/format
     */
    private void validateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        try {
            Iterator iterator = getRequiredKeys().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                if (properties.getProperty(key) == null) {
                    throw new MissingPropertyException(key + " key missing");
                }
            }
            strID = properties.getProperty(MeterProtocol.ADDRESS);
            strPassword = properties.getProperty(MeterProtocol.PASSWORD);
            iec1107TimeoutProperty = Integer.parseInt(properties.getProperty("Timeout", "10000").trim());
            protocolRetriesProperty = Integer.parseInt(properties.getProperty("Retries", "5").trim());
            roundtripCorrection = Integer.parseInt(properties.getProperty("RoundtripCorrection", "0").trim());
            securityLevel = Integer.parseInt(properties.getProperty("SecurityLevel", "1").trim());
            nodeId = properties.getProperty(MeterProtocol.NODEID, "");
            echoCancelling = Integer.parseInt(properties.getProperty("EchoCancelling", "0").trim());
            iec1107Compatible = Integer.parseInt(properties.getProperty("IEC1107Compatible", "1").trim());
            extendedLogging = Integer.parseInt(properties.getProperty("ExtendedLogging", "0").trim());
            serialNumber = properties.getProperty(MeterProtocol.SERIALNUMBER);
            if (properties.getProperty("ChannelMap") != null) {
                channelMap = new ChannelMap(properties.getProperty("ChannelMap"));
                protocolChannelMap = new ProtocolChannelMap(properties.getProperty("ChannelMap"));
            }
            profileInterval = Integer.parseInt(properties.getProperty("ProfileInterval", "900").trim());
            requestHeader = Integer.parseInt(properties.getProperty("RequestHeader", "0").trim());
            scaler = Integer.parseInt(properties.getProperty("Scaler", "0").trim());
            forcedDelay = Integer.parseInt(properties.getProperty("ForcedDelay", "300").trim());
            software7E1 = !"0".equals(properties.getProperty("Software7E1", "0"));
            doValidateProperties(properties);
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(" validateProperties, NumberFormatException, " + e.getMessage());
        }
    }

    /**
     * Method must be overridden by the subclass to build a StringBuffer with all possible registers that can be read
     * from the particulart meter. The StringBuffer is then logged as info. This method is called if the
     * 'ExtendedLogging' property is set to 1
     */
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        return ("");
    }

    /**
     * Method must be overridden by the subclass to verify the property 'SerialNumber' against the serialnumber read
     * from the meter. Use code below as example to implement the method. This code has been taken from a real protocol
     * implementation.
     */
    protected void validateSerialNumber() throws IOException {
        /*
           * boolean check = true; if ((getInfoTypeSerialNumber() == null) ||
           * ("".compareTo(getInfoTypeSerialNumber())==0)) return; String sn =
           * (String)get[protocol]Registry().getRegister( "[name of register that contains serial nulber]"); if
           * (sn.compareTo(getInfoTypeSerialNumber()) == 0) return; throw new IOException
           * ("SerialNiumber mismatch! meter sn="+sn+", configured sn="+ getInfoTypeSerialNumber());
           */
    }

    /**
     * Setter for property nodeId.
     *
     * @param nodeId New value of property nodeId.
     */
    public void setNodeId(java.lang.String nodeId) {
        this.nodeId = nodeId;
    }

    public boolean isRequestHeader() {
        return (requestHeader == 1);
    }

    /**
     * Getter for property scaler.
     *
     * @return Value of property scaler.
     */
    public int getScaler() {
        return scaler;
    }

    /**
     * Setter for the {@link TimeZone}
     *
     * @param timeZone - the TimeZone to set
     */
    protected void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * Setter for the {@link Logger}
     *
     * @param logger - the Logger to set
     */
    protected void setLogger(Logger logger) {
        this.logger = logger;
    }
}
