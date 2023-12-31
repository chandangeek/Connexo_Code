package com.energyict.protocolimpl.powermeasurement.ion;

import com.energyict.mdc.upl.SerialNumberSupport;
import com.energyict.mdc.upl.UnsupportedException;
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
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.SerialNumber;
import com.energyict.protocol.meteridentification.DiscoverInfo;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.messages.nls.Thesaurus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.*;

/**
 * Integrated Object Network (ION) architecture
 * <p>
 * DataRecorder Energy and Demand
 *
 * @author fbl
 * @beginchanges FBL|09032007|
 * Property NodeId used to be required.  Is no longer required, default 100 is
 * used.
 * FBL|13072007|
 * Improved error handling and general robustness.
 * FBL|31082007|
 * Added Channelmap property.
 * Changed default data recorder that is used for ProfileData.
 * @endchanges
 */

public class Ion extends PluggableMeterProtocol implements RegisterProtocol, ProtocolLink,
        HHUEnabler, SerialNumber, SerialNumberSupport {

    /**
     * Property keys
     */
    private static final String PK_DATA_RECORDER_NAME = "DataRecorderName";
    private static final String PK_USER_ID = "UserId";
    private static final String PK_DTR_BEHAVIOUR = "DTRBehaviour";
    private static final String PK_FORCE_DELAY = "ForcedDelay";
    private static final String PK_CHANNEL_MAP = "ChannelMap";

    /**
     * Property Default values
     */
    private static final int PD_TIMEOUT = 10000;
    private static final int PD_RETRIES = 5;
    private static final int PD_ROUNDTRIP_CORRECTION = 0;
    private static final String PD_EXTENDED_LOGGING = "0";
    private static final String PD_DATA_RECORDER_NAME = "Revenue Log";
    private static final int PD_DTR_BEHAVIOUR = 2;
    private static final int PD_NODE_ID = 100;
    private static final long PD_FORCE_DELAY = 200;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;

    /**
     * Property values Required properties will have NO default value.
     * Optional properties make use of default value.
     */
    private int pNodeId;
    private String pSerialNumber = null;
    private int pProfileInterval;
    private int dtrBehaviour = PD_DTR_BEHAVIOUR;

    /* Protocol timeout fail in msec */
    private int pTimeout = PD_TIMEOUT;

    /* Max nr of consecutive protocol errors before end of communication */
    private int pRetries = PD_RETRIES;
    /* Offset in ms to the get/set time */
    private int pRountTripCorrection = PD_ROUNDTRIP_CORRECTION;
    private int pCorrectTime = 0;

    /* Delay in msec between protocol Message Sequences */
    private long pForceDelay = PD_FORCE_DELAY;

    private String pUserId;
    private String pPassword;

    private String pExtendedLogging = PD_EXTENDED_LOGGING;
    String pDataRecorderName = PD_DATA_RECORDER_NAME;
    ProtocolChannelMap pChannelMap = null;

    private SerialCommunicationChannel commChannel;

    private ObisCodeMapper obisCodeMapper = null;
    private TimeZone timeZone = null;
    Logger logger = null;

    private OutputStream outputStream;
    private InputStream inputStream;
    private static final int SOURCE = 0x4e20;
    private ApplicationLayer applicationLayer;
    private Authentication authentication;
    private IonParser parser;
    private Profile profile;

    public Ion(PropertySpecService propertySpecService, NlsService nlsService) {
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
    }

    @Override
    public String getSerialNumber() {
        Command c = toCmd(IonHandle.FAC_1_SERIAL_NUMBER_SR, IonMethod.READ_REGISTER_VALUE);
        try {
            applicationLayer.read(c);
            return (String) c.getResponse().getValue();
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getNrOfRetries() + 1);
        }
    }

    public List<String> getOptionalKeys() {
        return Arrays.asList(
                TIMEOUT.getName(),
                RETRIES.getName(),
                EXTENDED_LOGGING.getName(),
                PK_DATA_RECORDER_NAME,
                PK_USER_ID,
                PK_DTR_BEHAVIOUR,
                PK_FORCE_DELAY,
                PK_CHANNEL_MAP);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.integerSpec(NODEID.getName(), PropertyTranslationKeys.ION_NODEID),
                this.stringSpec(SERIALNUMBER.getName(), PropertyTranslationKeys.ION_SERIALNUMBER),
                this.integerSpec(PROFILEINTERVAL.getName(), PropertyTranslationKeys.ION_PROFILEINTERVAL),
                this.integerSpec(TIMEOUT.getName(), PropertyTranslationKeys.ION_TIMEOUT),
                this.integerSpec(RETRIES.getName(), PropertyTranslationKeys.ION_RETRIES),
                this.integerSpec(ROUNDTRIPCORRECTION.getName(), PropertyTranslationKeys.ION_ROUNDTRIPCORRECTION),
                this.integerSpec(CORRECTTIME.getName(), PropertyTranslationKeys.ION_CORRECTTIME),
                this.stringSpec(EXTENDED_LOGGING.getName(), PropertyTranslationKeys.ION_EXTENDED_LOGGING),
                this.stringSpec(PK_DATA_RECORDER_NAME, PropertyTranslationKeys.ION_DATA_RECORDER_NAME),
                this.integerSpec(PK_DTR_BEHAVIOUR, PropertyTranslationKeys.ION_DTR_BEHAVIOUR),
                this.integerSpec(PK_FORCE_DELAY, PropertyTranslationKeys.ION_FORCE_DELAY),
                ProtocolChannelMap.propertySpec(PK_CHANNEL_MAP, false, this.nlsService.getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.IEC1107_CHANNEL_MAP).format(), this.nlsService.getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.IEC1107_CHANNEL_MAP_DESCRIPTION).format()));
    }

    private <T> PropertySpec spec(String name, TranslationKey translationKey, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, false, translationKey, optionsSupplier).finish();
    }

    private PropertySpec stringSpec(String name, TranslationKey translationKey) {
        return this.spec(name, translationKey, this.propertySpecService::stringSpec);
    }

    private PropertySpec integerSpec(String name, TranslationKey translationKey) {
        return this.spec(name, translationKey, this.propertySpecService::integerSpec);
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws InvalidPropertyException, MissingPropertyException {
        pNodeId = properties.getTypedProperty(NODEID.getName());
        if (properties.getTypedProperty(PK_USER_ID) != null) {
            pUserId = properties.getTypedProperty(PK_USER_ID);
        }

        if (properties.getTypedProperty(PASSWORD.getName()) != null) {
            pPassword = properties.getTypedProperty(PASSWORD.getName());
        }

        try {
            if (!ProtocolTools.isNull(pPassword) && !ProtocolTools.isNull(pUserId)) {
                this.authentication = new Authentication(pPassword, pUserId);
            }
        } catch (InvalidPasswordException e) {
            throw new InvalidPropertyException(e, e.getMessage());
        }

        if (properties.getTypedProperty(SERIALNUMBER.getName()) != null) {
            pSerialNumber = properties.getTypedProperty(SERIALNUMBER.getName());
        }

        if (properties.getTypedProperty(PROFILEINTERVAL.getName()) != null) {
            pProfileInterval = properties.getTypedProperty(PROFILEINTERVAL.getName());
        }

        if (properties.getTypedProperty(TIMEOUT.getName()) != null) {
            pTimeout = properties.getTypedProperty(TIMEOUT.getName());
        }

        if (properties.getTypedProperty(RETRIES.getName()) != null) {
            pRetries = properties.getTypedProperty(RETRIES.getName());
        }

        if (properties.getTypedProperty(ROUNDTRIPCORRECTION.getName()) != null) {
            pRountTripCorrection = properties.getTypedProperty(ROUNDTRIPCORRECTION.getName());
        }

        if (properties.getTypedProperty(CORRECTTIME.getName()) != null) {
            pCorrectTime = properties.getTypedProperty(CORRECTTIME.getName());
        }

        if (properties.getTypedProperty(EXTENDED_LOGGING.getName()) != null) {
            pExtendedLogging = properties.getTypedProperty(EXTENDED_LOGGING.getName());
        }

        if (properties.getTypedProperty(PK_DATA_RECORDER_NAME) != null) {
            pDataRecorderName = properties.getTypedProperty(PK_DATA_RECORDER_NAME);
        }

        if (properties.getTypedProperty(PK_DTR_BEHAVIOUR) != null) {
            dtrBehaviour = properties.getTypedProperty(PK_DTR_BEHAVIOUR);
        }

        if (properties.getTypedProperty(PK_FORCE_DELAY) != null) {
            pForceDelay = properties.getTypedProperty(PK_FORCE_DELAY);
        }

        if (properties.getTypedProperty(PK_CHANNEL_MAP) != null) {
            pChannelMap = new ProtocolChannelMap(((String) properties.getTypedProperty(PK_CHANNEL_MAP)));
        }
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        this.timeZone = timeZone;
        this.logger = logger;

        this.parser = new IonParser();
        this.obisCodeMapper = new ObisCodeMapper(this);
        this.profile = new Profile(this);

        this.inputStream = inputStream;
        this.outputStream = outputStream;

        if (logger.isLoggable(Level.INFO)) {
            String info = "Ion protocol init \n" +
                    " NodeId = " + pNodeId + "," +
                    " SerialNr = " + pSerialNumber + "," +
                    " Timeout = " + pTimeout + "," +
                    " Retries = " + pRetries + "," +
                    " Ext. Logging = " + pExtendedLogging + "," +
                    " RoundTripCorr = " + pRountTripCorrection + "," +
                    " ForceDelay = " + pForceDelay + "," +
                    " Correct Time = " + pCorrectTime + "," +
                    " TimeZone = " + timeZone.getID();
            logger.info(info);
        }
    }

    @Override
    public void connect() throws IOException {
        if (commChannel != null) {
            this.inputStream = commChannel.getInputStream();
            this.outputStream = commChannel.getOutputStream();
        }

        this.applicationLayer = new ApplicationLayer(this, authentication);
        if (commChannel != null) {
            commChannel.setBaudrate(9600);
            if (dtrBehaviour == 0) {
                commChannel.setDTR(false);
            } else if (dtrBehaviour == 1) {
                commChannel.setDTR(true);
            }
        }
        connect(0);
    }

    void connect(int baudRate) throws IOException {
        try {
            doExtendedLogging();
            validateSecurityContext();
        } catch (NumberFormatException nex) {
            throw new IOException(nex.getMessage());
        }
    }

    /**
     * Validate whether the Security Context (password, userId, ...) is correct
     * Here we make use of the serialNumber
     */
    private void validateSecurityContext() throws IOException {
        Command c = toCmd(IonHandle.FAC_1_SERIAL_NUMBER_SR, IonMethod.READ_REGISTER_VALUE);
        applicationLayer.read(c);

        if (c.getResponse().isException()) {

            if (c.getResponse().isStrucure()) {
                if ((((IonStructure) c.getResponse()).get("reason").toString()).indexOf("Invalid Password") > 0) {
                    throw new ConnectionException("Incorrect password");
                }
            }
        }
    }

    @Override
    public void disconnect() throws IOException {
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        // A DataCollector always has 16 channels
        return profile.getNumberOfChannels();
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        return profile.getProfileData(includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return profile.getProfileData(lastReading, includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return profile.getProfileData(from, to, includeEvents);
    }

    @Override
    public int getProfileInterval() throws IOException {
        return pProfileInterval;
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapper.getRegisterValue(obisCode);
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    private void doExtendedLogging() throws IOException {
        if ("1".equals(pExtendedLogging)) {
            logger.log(Level.INFO, obisCodeMapper.getExtendedLogging() + "\n");
        }
        if ("2".equals(pExtendedLogging)) {
            logger.log(Level.INFO, obisCodeMapper.getDebugLogging() + "\n");
        }
    }

    @Override
    public String getProtocolDescription() {
        return "Power Measurement ION";
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:24:28 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        Command cRevisionNr =
                toCmd(IonHandle.FAC_1_REVISION_SR, IonMethod.READ_REGISTER_VALUE);

        applicationLayer.read(cRevisionNr);

        return cRevisionNr.getResponse().getValue().toString();
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
    public Date getTime() throws IOException {
        Command c = toCmd(IonHandle.CLK_1_UNIVERSAL_TIME_NVR, IonMethod.READ_REGISTER_VALUE);
        applicationLayer.read(c);
        int secs = ((Integer) c.getResponse().getValue()).intValue();
        return new Date(secs * 1000L);
    }

    @Override
    public void setTime() throws IOException {
        long milli = System.currentTimeMillis() + pRountTripCorrection;
        int sec = (int) (milli / 1000);

        ByteArray time =
                new ByteArray()
                        .add((byte) ((sec & 0xff000000) >> 24))
                        .add((byte) ((sec & 0x00ff0000) >> 16))
                        .add((byte) ((sec & 0x0000ff00) >> 8))
                        .add((byte) ((sec & 0x000000ff)));

        applicationLayer.sendTime(new Message().setData(time));
    }

    @Override
    public byte[] getDataReadout() {
        return null;
    }

    /**
     * for easy debugging
     */
    void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * for easy debugging
     */
    void setLogger(Logger logger) {
        this.logger = logger;
    }

    InputStream getInputStream() {
        return inputStream;
    }

    OutputStream getOutputStream() {
        return outputStream;
    }

    int getSource() {
        return SOURCE;
    }

    int getDestination() {
        return pNodeId;
    }

    int getRetries() {
        return pRetries;
    }

    int getTimeout() {
        return pTimeout;
    }

    IonObject parse(Assembly a) {
        return parser.parse(a);
    }

    IonObject parse(byte[] b) {
        return parser.parse(new Assembly(this, new ByteArray(b)));
    }

    ApplicationLayer getApplicationLayer() {
        return applicationLayer;
    }

    @Override
    public String getRegister(String name) throws IOException {
        return null;
    }

    @Override
    public void setRegister(String name, String value) throws IOException {
    }

    @Override
    public void initializeDevice() throws IOException {
    }

    @Override
    public void release() throws IOException {
    }

    @Override
    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return null;
    }

    @Override
    public boolean isIEC1107Compatible() {
        return false;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public ChannelMap getChannelMap() {
        return null;
    }

    @Override
    public ProtocolChannelMap getProtocolChannelMap() {
        return null;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public int getNrOfRetries() {
        return pRetries;
    }

    @Override
    public boolean isRequestHeader() {
        return false;
    }

    long getForceDelay() {
        return pForceDelay;
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, false);
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean enableDataReadout) throws ConnectionException {
        this.commChannel = commChannel;
    }

    @Override
    public byte[] getHHUDataReadout() {
        return null;
    }

    @Override
    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        SerialCommunicationChannel cChannel = discoverInfo.getCommChannel();

        final String nodeId = discoverInfo.getNodeId();
        final int baudrate = discoverInfo.getBaudrate();

        TypedProperties p = com.energyict.mdc.upl.TypedProperties.empty();
        p.setProperty("SecurityLevel", "0");
        p.setProperty(NODEID.getName(), nodeId == null ? "" : nodeId);

        setUPLProperties(p);

        init(cChannel.getInputStream(), cChannel.getOutputStream(), null, null);
        enableHHUSignOn(cChannel);
        connect(baudrate);

        Command c =
                new Command(IonHandle.FAC_1_SERIAL_NUMBER_SR, IonMethod.READ_REGISTER_VALUE);
        applicationLayer.read(c);

        String sn = (String) c.getResponse().getValue();
        disconnect();

        return sn;
    }

    List toCmd(List handles, IonMethod method) {
        List rslt = new ArrayList();
        Iterator i = handles.iterator();
        while (i.hasNext()) {
            IonHandle handle = (IonHandle) i.next();
            rslt.add(new Command(handle, method));
        }
        return rslt;
    }

    Command toCmd(IonHandle handle, IonMethod method) {
        return new Command(handle, method);
    }

    List collectHandles(List ionIntegers) {
        ArrayList rslt = new ArrayList();
        Iterator i = ionIntegers.iterator();
        while (i.hasNext()) {
            rslt.add(IonHandle.create(((IonInteger) i.next()).getIntValue()));
        }
        return rslt;
    }

}