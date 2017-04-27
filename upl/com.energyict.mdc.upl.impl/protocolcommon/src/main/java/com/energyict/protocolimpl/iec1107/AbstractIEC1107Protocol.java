/*
 * AbstractIEC1107Protocol.java
 *
 * Created on 2 juli 2004, 17:30
 */

package com.energyict.protocolimpl.iec1107;

import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connections.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.SerialNumber;
import com.energyict.protocol.meteridentification.DiscoverInfo;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.properties.nls.PropertyTranslationKeys;
import com.energyict.protocolimplv2.messages.nls.Thesaurus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Supplier;
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
 * @author Koen
 */
public abstract class AbstractIEC1107Protocol extends PluggableMeterProtocol implements ProtocolLink, HHUEnabler, SerialNumber,
        MeterExceptionInfo, RegisterProtocol {

    private TimeZone timeZone;
    private Logger logger;

    protected String strID; // device id (default=null)
    protected String strPassword; // password (default=null)
    protected int iec1107TimeoutProperty; // protocol timeout in ms (default=10000)
    protected int protocolRetriesProperty; // nr of retries for the protocol (default=5)
    private int roundtripCorrection; // roundtrip correction for the set/get time methods in ms (default=0)
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
    private int requestHeader; // Request Meter's profile header info (typycal VDEW)
    protected int scaler; // Scaler to use when retrieving data from the meter
    protected int forcedDelay; // Delay before sending data
    protected FlagIEC1107Connection flagIEC1107Connection; // lower layer IEC1107 communication
    protected MeterType meterType; // signon information of the meter

    protected byte[] dataReadout;
    private boolean requestDataReadout;
    protected boolean software7E1;
    protected Encryptor encryptor;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;

    public AbstractIEC1107Protocol(PropertySpecService propertySpecService, NlsService nlsService) {
        this(false, propertySpecService, nlsService);
    }

    public AbstractIEC1107Protocol(boolean requestDataReadout, PropertySpecService propertySpecService, NlsService nlsService) {
        this(requestDataReadout, null, propertySpecService, nlsService);
    }

    public AbstractIEC1107Protocol(Encryptor encryptor, PropertySpecService propertySpecService, NlsService nlsService) {
        this(false, encryptor, propertySpecService, nlsService);
    }

    public AbstractIEC1107Protocol(boolean requestDataReadout, Encryptor encryptor, PropertySpecService propertySpecService, NlsService nlsService) {
        this.requestDataReadout = requestDataReadout;
        this.encryptor = encryptor;
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public Quantity getMeterReading(String name) throws UnsupportedException {
        throw new UnsupportedException();
    }

    @Override
    public Quantity getMeterReading(int channelId) throws UnsupportedException {
        throw new UnsupportedException();
    }

    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        // Default, we ask for 2 months of profile data!
        Calendar calendar = Calendar.getInstance(getTimeZone());
        calendar.add(Calendar.MONTH, -2);
        return getProfileData(calendar.getTime(), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return null;
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public Date getTime() throws IOException {
        return null;
    }

    @Override
    public void setTime() throws IOException {
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
        return Arrays.asList(
                this.stringSpec(ADDRESS.getName(), PropertyTranslationKeys.IEC1107_ADDRESS),
                this.stringSpec(PASSWORD.getName(), PropertyTranslationKeys.IEC1107_PASSWORD),
                this.integerSpec(TIMEOUT.getName(), PropertyTranslationKeys.IEC1107_TIMEOUT),
                this.integerSpec(RETRIES.getName(), PropertyTranslationKeys.IEC1107_RETRIES),
                this.integerSpec(ROUNDTRIPCORRECTION.getName(), PropertyTranslationKeys.IEC1107_ROUNDTRIPCORRECTION),
                this.integerSpec(SECURITYLEVEL.getName(), PropertyTranslationKeys.IEC1107_SECURITYLEVEL),
                this.stringSpec(NODEID.getName(), PropertyTranslationKeys.IEC1107_NODEID),
                this.integerSpec("EchoCancelling", PropertyTranslationKeys.IEC1107_ECHO_CANCELLING),
                this.integerSpec("IEC1107Compatible", PropertyTranslationKeys.IEC1107_COMPATIBLE),
                this.integerSpec("ExtendedLogging", PropertyTranslationKeys.IEC1107_EXTENDEDLOGGING),
                this.stringSpec(SERIALNUMBER.getName(), PropertyTranslationKeys.IEC1107_SERIALNUMBER),
                ProtocolChannelMap.propertySpec("ChannelMap", false, this.nlsService.getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.BASE_CHANNEL_MAP).format(), this.nlsService.getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.BASE_CHANNEL_MAP_DESCRIPTION).format()),
                this.integerSpec(PROFILEINTERVAL.getName(), PropertyTranslationKeys.IEC1107_PROFILEINTERVAL),
                this.integerSpec("RequestHeader", PropertyTranslationKeys.IEC1107_REQUESTHEADER),
                this.integerSpec("Scaler", PropertyTranslationKeys.IEC1107_SCALER),
                this.integerSpec("ForcedDelay", PropertyTranslationKeys.IEC1107_FORCEDDELAY),
                this.stringSpec("Software7E1", PropertyTranslationKeys.IEC1107_SOFTWARE7E1));
    }

    private <T> PropertySpec spec(String name, TranslationKey translationKey, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, false, translationKey, optionsSupplier).finish();
    }

    protected PropertySpec stringSpec(String name, TranslationKey translationKey) {
        return this.spec(name, translationKey, this.propertySpecService::stringSpec);
    }

    protected PropertySpec integerSpec(String name, TranslationKey translationKey) {
        return this.spec(name, translationKey, this.propertySpecService::integerSpec);
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws InvalidPropertyException, MissingPropertyException {
        try {
            strID = properties.getTypedProperty(ADDRESS.getName());
            strPassword = properties.getTypedProperty(PASSWORD.getName());
            iec1107TimeoutProperty = properties.getTypedProperty(TIMEOUT.getName(), 10000);
            protocolRetriesProperty = properties.getTypedProperty(RETRIES.getName(), 5);
            roundtripCorrection = properties.getTypedProperty(ROUNDTRIPCORRECTION.getName(), 0);
            securityLevel = properties.getTypedProperty(SECURITYLEVEL.getName(), 1);
            nodeId = properties.getTypedProperty(NODEID.getName(), "");
            echoCancelling = properties.getTypedProperty("EchoCancelling", 0);
            iec1107Compatible = properties.getTypedProperty("IEC1107Compatible", 1);
            extendedLogging = properties.getTypedProperty("ExtendedLogging", 0);
            serialNumber = properties.getTypedProperty(SERIALNUMBER.getName());
            if (properties.getTypedProperty("ChannelMap") != null) {
                channelMap = new ChannelMap(((String) properties.getTypedProperty("ChannelMap")));
                protocolChannelMap = new ProtocolChannelMap(((String) properties.getTypedProperty("ChannelMap")));
            }
            profileInterval = properties.getTypedProperty(PROFILEINTERVAL.getName(), 900);
            requestHeader = properties.getTypedProperty("RequestHeader", 0);
            scaler = properties.getTypedProperty("Scaler", 0);
            forcedDelay = properties.getTypedProperty("ForcedDelay", 300);
            software7E1 = !"0".equalsIgnoreCase(properties.getTypedProperty("Software7E1", "0"));
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, this.getClass().getSimpleName() + ": validation of properties failed before");
        }
    }

    /**
     * Implement additional code after the SignOn has passed successfully
     *
     * @throws IOException in case of an Exception
     */
    protected abstract void doConnect() throws IOException;

    @Override
    public void connect() throws IOException {
        try {
            if (requestDataReadout) {
                dataReadout = getFlagIEC1107Connection().dataReadout(strID, nodeId);
                getFlagIEC1107Connection().disconnectMAC();
            }
            meterType = getFlagIEC1107Connection().connectMAC(strID, strPassword, securityLevel, nodeId);
            doConnect();
        } catch (FlagIEC1107ConnectionException e) {
            throw new IOException(e.getMessage(), e);
        }

        if (extendedLogging >= 1) {
            logger.info(getRegistersInfo(extendedLogging));
        }
    }

    @Override
    public void disconnect() throws NestedIOException {
        try {
            getFlagIEC1107Connection().disconnectMAC();
        } catch (FlagIEC1107ConnectionException e) {
            logger.severe("disconnect() error, " + e.getMessage());
        }
    }

    @Override
    public void release() throws IOException {
    }

    @Override
    public void initializeDevice() throws UnsupportedException {
        throw new UnsupportedException();
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        this.timeZone = timeZone;
        this.logger = logger;
        try {
            flagIEC1107Connection = new FlagIEC1107Connection(inputStream, outputStream, iec1107TimeoutProperty,
                    protocolRetriesProperty, forcedDelay, echoCancelling, iec1107Compatible, encryptor, software7E1, logger);
        } catch (ConnectionException e) {
            logger.severe("IndigoPlus, init, " + e.getMessage());
        }
    }

    @Override
    public int getProfileInterval() throws IOException {
        return profileInterval;
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return channelMap.getNrOfChannels();
    }

    @Override
    public TimeZone getTimeZone() {
        return timeZone;
    }

    @Override
    public boolean isIEC1107Compatible() {
        return (iec1107Compatible == 1);
    }

    @Override
    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return flagIEC1107Connection;
    }

    @Override
    public byte[] getDataReadout() {
        return dataReadout;
    }

    @Override
    public String getPassword() {
        return strPassword;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public ProtocolChannelMap getProtocolChannelMap() {
        return protocolChannelMap;
    }

    @Override
    public ChannelMap getChannelMap() {
        return channelMap;
    }

    @Override
    public int getNrOfRetries() {
        return protocolRetriesProperty;
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
        HHUSignOn hhuSignOn = new IEC1107HHUConnection(commChannel, iec1107TimeoutProperty,
                protocolRetriesProperty, 300, echoCancelling);
        hhuSignOn.setMode(HHUSignOn.MODE_PROGRAMMING);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
        hhuSignOn.enableDataReadout(datareadout);
        getFlagIEC1107Connection().setHHUSignOn(hhuSignOn);
    }

    @Override
    public byte[] getHHUDataReadout() {
        return getFlagIEC1107Connection().getHhuSignOn().getDataReadout();
    }

    @Override
    public String getExceptionInfo(String id) {
        return null;
    }

    @Override
    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        throw new IOException("Not implemented!");
    }

    protected int getRoundtripCorrection() {
        return roundtripCorrection;
    }

    protected String getInfoTypeSerialNumber() {
        return serialNumber;
    }

    protected String getRegistersInfo(int extendedLogging) throws IOException {
        return ("");
    }

    public void setNodeId(java.lang.String nodeId) {
        this.nodeId = nodeId;
    }

    public boolean isRequestHeader() {
        return (requestHeader == 1);
    }

    public int getScaler() {
        return scaler;
    }

    protected void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    protected void setLogger(Logger logger) {
        this.logger = logger;
    }

    protected NlsService getNlsService() {
        return nlsService;
    }
}