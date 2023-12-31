/*
 * AbstractIEC1107Protocol.java
 *
 * Created on 2 juli 2004, 17:30
 */

package com.energyict.protocolimpl.base;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connections.IEC1107HHUConnection;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
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
import com.energyict.protocolimpl.properties.nls.PropertyTranslationKeys;
import com.energyict.protocolimplv2.messages.nls.Thesaurus;

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
import java.util.TimeZone;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.*;

/**
 * Abstract base class to create a new protocol.
 *
 * @author Koen
 */
public abstract class AbstractProtocol extends PluggableMeterProtocol implements HHUEnabler, SerialNumber, MeterExceptionInfo, RegisterProtocol, HalfDuplexEnabler {

    protected static final String PROP_TIMEOUT = TIMEOUT.getName();
    protected static final String PROP_RETRIES = RETRIES.getName();
    protected static final String PROP_FORCED_DELAY = "ForcedDelay";
    protected static final String PROP_HALF_DUPLEX = "HalfDuplex";

    private static final String PROP_ECHO_CANCELING = "EchoCancelling";
    private static final String PROP_PROTOCOL_COMPATIBLE = "ProtocolCompatible";
    private static final String PROP_CHANNEL_MAP = "ChannelMap";
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

    private TimeZone timeZone;
    private Logger logger;

    private String channelMap; // ChannelMap property (default=null)
    private String strID; // device id (default=null)
    private String strPassword; // password (default=null)
    private int timeoutProperty; // protocol timeout in ms (default=10000)
    private int protocolRetriesProperty; // nr of retries for the protocol (default=5)
    private int roundtripCorrection; // roundtrip correction for the set/get time methods in ms (default=0)
    private int securityLevel; // 0=public level, 1=non encrypted password, 2=encrypted password (default=0)
    private String nodeId; // multidrop and iec1107 flag id (default=empty)
    private int echoCancelling; // 0=disabled, 1=enabled (default=0)
    private int extendedLogging; // 0=disabled, 1=enabled (e.g. to get a list of all possible registers that can be read in the meter) (default=0)
    private String serialNumber; // meter's serial number (used for handheld connection to identify the meter) (default=null)
    private ProtocolChannelMap protocolChannelMap; // null=unused configuration info of the meter's channels (default=null)
    private int profileInterval; // meter's profile interval in seconds (default=900)
    private int protocolCompatible; // 0=protocol has specific incompatible features (e.g. the A1700 has data streaming mode but is a member of the IEC1107 family), 1=protocol is fully compatible with IEC1107 (default=1)
    private ProtocolConnection protocolConnection; // lower layer protocol communication
    private MeterType meterType; // signon information of the meter
    private int requestHeader; // Request Meter's profile header info (typycal VDEW)
    private int scaler; // Scaler to use when retrieving data from the meter
    private int forcedDelay; // Delay before data send
    private int halfDuplex; // halfduplex enable/disable & delay in ms. (0=disabled, >0 enabled and delay in ms.)

    private byte[] dataReadout;
    private boolean requestDataReadout;
    private Encryptor encryptor;
    private HalfDuplexController halfDuplexController = null;

    private BigDecimal adjustChannelMultiplier;
    private BigDecimal adjustRegisterMultiplier;

    private int dtrBehaviour; // 0=force low, 1 force high, 2 don't force anything
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;

    public AbstractProtocol(PropertySpecService propertySpecService, NlsService nlsService) {
        this(false, propertySpecService, nlsService);
    }

    public AbstractProtocol(boolean requestDataReadout, PropertySpecService propertySpecService, NlsService nlsService) {
        this(requestDataReadout, null, propertySpecService, nlsService);
    }

    public AbstractProtocol(Encryptor encryptor, PropertySpecService propertySpecService, NlsService nlsService) {
        this(false, encryptor, propertySpecService, nlsService);
    }

    public AbstractProtocol(boolean requestDataReadout, Encryptor encryptor, PropertySpecService propertySpecService, NlsService nlsService) {
        this.requestDataReadout = requestDataReadout;
        this.encryptor = encryptor;
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
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
    public List<PropertySpec> getUPLPropertySpecs() {
        return new ArrayList<>(Arrays.asList(
                this.stringSpec(ADDRESS.getName(), PropertyTranslationKeys.BASE_ADDRESS, false),
                this.stringSpec(TIMEOUT.getName(), PropertyTranslationKeys.BASE_TIMEOUT, false),
                this.integerSpec(RETRIES.getName(), PropertyTranslationKeys.BASE_RETRIES, false),
                this.integerSpec(ROUNDTRIPCORRECTION.getName(), PropertyTranslationKeys.BASE_ROUNDTRIPCORRECTION, false),
                // This class prefers security level to be of type int but subclasses prefer String and parse it into two properties :-(
                this.stringSpec(NODEID.getName(), PropertyTranslationKeys.BASE_NODEID, false),
                this.integerSpec(PROP_ECHO_CANCELING, PropertyTranslationKeys.BASE_ECHO_CANCELLING, false),
                this.integerSpec(PROP_PROTOCOL_COMPATIBLE, PropertyTranslationKeys.BASE_PROTOCOL_COMPATABLE, false),
                this.integerSpec(EXTENDED_LOGGING.getName(), PropertyTranslationKeys.BASE_EXTENDED_LOGGING, false),
                this.stringSpec(SERIALNUMBER.getName(), PropertyTranslationKeys.BASE_SERIALNUMBER, this.serialNumberIsRequired()),
                ProtocolChannelMap.propertySpec(PROP_CHANNEL_MAP, false, this.nlsService.getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.BASE_CHANNEL_MAP).format(), this.nlsService.getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.BASE_CHANNEL_MAP_DESCRIPTION).format()),
                this.integerSpec(PROFILEINTERVAL.getName(), PropertyTranslationKeys.BASE_PROFILE_INTERVAL, false),
                this.integerSpec(PROP_REQUEST_HEADER, PropertyTranslationKeys.BASE_REQUEST_HEADER, false),
                this.integerSpec(PROP_SCALER, PropertyTranslationKeys.BASE_SCALER, false),
                this.integerSpec(PROP_FORCED_DELAY, PropertyTranslationKeys.BASE_FORCED_DELAY, false),
                this.integerSpec(PROP_HALF_DUPLEX, PropertyTranslationKeys.BASE_HALF_DUPLEX, false),
                this.integerSpec(PROP_DTR_BEHAVIOUR, PropertyTranslationKeys.BASE_DTR_BEHAVIOUR, false),
                this.spec(PROP_ADJUST_CHANNEL_MULTIPLIER, PropertyTranslationKeys.BASE_ADJUST_CHANNEL_MULTIPLIER, false, this.propertySpecService::bigDecimalSpec),
                this.spec(PROP_ADJUST_REGISTER_MULTIPLIER, PropertyTranslationKeys.BASE_ADJUST_REGISTER_MULTIPLIER, false, this.propertySpecService::bigDecimalSpec)));
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    protected <T> PropertySpec spec(String name, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, false, optionsSupplier).finish();
    }

    protected <T> PropertySpec spec(String name, TranslationKey translationKey, boolean required, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, required, translationKey, optionsSupplier).finish();
    }

    protected PropertySpec stringSpec(String name, TranslationKey translationKey, boolean required) {
        return this.spec(name, translationKey, required, this.propertySpecService::stringSpec);
    }

    protected PropertySpec booleanSpec(String name, TranslationKey translationKey, boolean required) {
        return this.spec(name, translationKey, required, this.propertySpecService::booleanSpec);
    }

    protected PropertySpec integerSpec(String name, TranslationKey translationKey, boolean required) {
        return this.spec(name, translationKey, required, this.propertySpecService::integerSpec);
    }

    protected boolean passwordIsRequired() {
        return false;
    }

    protected boolean serialNumberIsRequired() {
        return false;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        try {
            strID = properties.getTypedProperty(ADDRESS.getName());
            strPassword = properties.getTypedProperty(PASSWORD.getName());
            setInfoTypeTimeoutProperty(Integer.parseInt(properties.getTypedProperty(TIMEOUT.getName(), "10000").trim()));
            setInfoTypeProtocolRetriesProperty(properties.getTypedProperty(RETRIES.getName(), 5));
            roundtripCorrection = properties.getTypedProperty(ROUNDTRIPCORRECTION.getName(), 0);
            this.setSecurityLevelFrom(properties);
            nodeId = properties.getTypedProperty(NODEID.getName(), "");
            echoCancelling = properties.getTypedProperty(PROP_ECHO_CANCELING, 0);
            protocolCompatible = properties.getTypedProperty(PROP_PROTOCOL_COMPATIBLE, 1);
            extendedLogging = properties.getTypedProperty(EXTENDED_LOGGING.getName(), 0);
            serialNumber = properties.getTypedProperty(SERIALNUMBER.getName());
            channelMap = properties.getTypedProperty(PROP_CHANNEL_MAP);
            if (channelMap != null) {
                protocolChannelMap = new ProtocolChannelMap(channelMap);
            }
            profileInterval = properties.getTypedProperty(PROFILEINTERVAL.getName(), 900);
            requestHeader = properties.getTypedProperty(PROP_REQUEST_HEADER, 0);
            scaler = properties.getTypedProperty(PROP_SCALER, 0);
            setForcedDelay(properties.getTypedProperty(PROP_FORCED_DELAY, defaultForcedDelayPropertyValue()));
            halfDuplex = properties.getTypedProperty(PROP_HALF_DUPLEX, 0);
            setDtrBehaviour(properties.getTypedProperty(PROP_DTR_BEHAVIOUR, 2));

            adjustChannelMultiplier = new BigDecimal(properties.getTypedProperty(PROP_ADJUST_CHANNEL_MULTIPLIER, "1").trim());
            adjustRegisterMultiplier = new BigDecimal(properties.getTypedProperty(PROP_ADJUST_REGISTER_MULTIPLIER, "1").trim());
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, this.getClass().getSimpleName() + ": validation of properties failed before");
        }
    }

    protected void setSecurityLevelFrom(TypedProperties properties) {
        securityLevel = Integer.parseInt(properties.getTypedProperty(SECURITYLEVEL.getName(), "1"));
    }

    protected int defaultForcedDelayPropertyValue() {
        return 300;
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
    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        throw new IOException("Not implemented!");
    }

    public MeterType getMeterType() {
        return meterType;
    }

    public ProtocolChannelMap getProtocolChannelMap() {
        return protocolChannelMap;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    protected ProtocolConnection getProtocolConnection() {
        return protocolConnection;
    }

    public byte[] getDataReadout() {
        return dataReadout;
    }

    public Logger getLogger() {
        return logger;
    }

    public int getInfoTypeRoundtripCorrection() {
        return roundtripCorrection;
    }

    protected int getInfoTypeRetries() {
        return getInfoTypeProtocolRetriesProperty();
    }

    public int getInfoTypeForcedDelay() {
        return forcedDelay;
    }

    protected int getInfoTypeScaler() {
        return scaler;
    }

    protected String getInfoTypeSerialNumber() {
        return serialNumber;
    }

    public String getInfoTypeDeviceID() {
        return strID;
    }

    protected void setInfoTypeDeviceID(String strID) {
        this.strID = strID;
    }

    public String getInfoTypePassword() {
        return strPassword;
    }

    protected void setInfoTypePassword(String strPassword) {
        this.strPassword = strPassword;
    }

    public String getInfoTypeNodeAddress() {
        return nodeId;
    }

    protected int getInfoTypeNodeAddressNumber() {
        if ((nodeId != null) && ("".compareTo(nodeId) != 0)) {
            return Integer.parseInt(nodeId);
        } else {
            return 0;
        }
    }

    public void setInfoTypeNodeAddress(String nodeId) {
        this.nodeId = nodeId;
    }

    public int getInfoTypeNodeAddressNumberHex() {
        if ((nodeId != null) && ("".compareTo(nodeId) != 0)) {
            return Integer.parseInt(nodeId, 16);
        } else {
            return 0;
        }
    }

    public int getInfoTypeProtocolCompatible() {
        return protocolCompatible;
    }

    protected int getInfoTypeEchoCancelling() {
        return echoCancelling;
    }

    public int getInfoTypeSecurityLevel() {
        return securityLevel;
    }

    public int getInfoTypeTimeout() {
        return timeoutProperty;
    }

    protected String getInfoTypeChannelMap() {
        return channelMap;
    }

    protected int getInfoTypeProfileInterval() {
        return profileInterval;
    }

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

    public boolean isRequestHeader() {
        return (requestHeader == 1);
    }

    public int getForcedDelay() {
        return forcedDelay;
    }

    public void setForcedDelay(int forcedDelay) {
        this.forcedDelay = forcedDelay;
    }

    @Override
    public void setHalfDuplexController(HalfDuplexController halfDuplexController) {
        this.halfDuplexController = halfDuplexController;
        this.halfDuplexController.setDelay(halfDuplex);

        if (getProtocolConnection() != null && getProtocolConnection() instanceof HalfDuplexEnabler) {
            ((HalfDuplexEnabler) getProtocolConnection()).setHalfDuplexController(halfDuplex != 0 ? this.halfDuplexController : null);
        }
    }

    public int getInfoTypeHalfDuplex() {
        return halfDuplex;
    }

    protected void setInfoTypeHalfDuplex(int halfDuplex) {
        this.halfDuplex = halfDuplex;
    }

    protected void setInfoTypeSecurityLevel(int securityLevel) {
        this.securityLevel = securityLevel;
    }

    public int getDtrBehaviour() {
        return dtrBehaviour;
    }

    private void setDtrBehaviour(int dtrBehaviour) {
        this.dtrBehaviour = dtrBehaviour;
    }

    public void setInfoTypeTimeoutProperty(int timeoutProperty) {
        this.timeoutProperty = timeoutProperty;
    }

    public BigDecimal getAdjustChannelMultiplier() {
        return adjustChannelMultiplier;
    }

    public BigDecimal getAdjustRegisterMultiplier() {
        return adjustRegisterMultiplier;
    }

    protected int getInfoTypeProtocolRetriesProperty() {
        return protocolRetriesProperty;
    }

    protected void setInfoTypeProtocolRetriesProperty(int protocolRetriesProperty) {
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
        return getInfoTypeSecurityLevel();
    }

    public String getStrPassword() {
        return strPassword;
    }

    protected void setAbstractLogger(Logger logger) {
        this.logger = logger;
    }

    protected NlsService getNlsService() {
        return nlsService;
    }
}