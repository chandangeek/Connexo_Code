package com.energyict.protocolimpl.coronis.waveflow.core;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.BubbleUp;
import com.energyict.protocol.EventMapper;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.coronis.core.ProtocolLink;
import com.energyict.protocolimpl.coronis.core.WaveFlowConnect;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.core.wavecell.WaveCellConnect;
import com.energyict.protocolimpl.coronis.waveflow.core.messages.WaveFlowMessageParser;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.ParameterFactory;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.PulseWeight;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.RadioCommandFactory;
import com.energyict.protocolimpl.coronis.waveflow.waveflowV2.WaveFlowV2;
import com.energyict.protocolimpl.dlms.common.ObisCodePropertySpec;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import com.energyict.protocolimplv2.messages.nls.Thesaurus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS;
import static com.energyict.mdc.upl.MeterProtocol.Property.CORRECTTIME;

public abstract class WaveFlow extends AbstractProtocol implements ProtocolLink, EventMapper, BubbleUp, IncomingAlarmFrameParser {

    private static final String PROP_SCALE_A = "ScaleA";
    private static final String PROP_SCALE_B = "ScaleB";
    private static final String PROP_SCALE_C = "ScaleC";
    private static final String PROP_SCALE_D = "ScaleD";
    private static final String PROP_MULTIPLIER_A = "MultiplierA";
    private static final String PROP_MULTIPLIER_B = "MultiplierB";
    private static final String PROP_MULTIPLIER_C = "MultiplierC";
    private static final String PROP_MULTIPLIER_D = "MultiplierD";
    public static final int DEFAULT_TIMEOUT = 5000;
    public static final int DEFAULT_RETRIES = 2;
    private int connectionMode = 0;
    private int waveFlowId = -1;

    private static final int MUC_WAVECELL_CONNECTION = 0;
    public static final String LEGACY_WAVECELL_CONNECTION = "1";
    private static final String CONNECTION_PROPERTY = "Connection";

    public WaveFlow(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    protected abstract void doTheInit() throws IOException;

    protected abstract ProfileData getTheProfileData(Date lastReading, Date toDate, boolean includeEvents) throws IOException;

    private boolean multiFrame;         //Custom property enabling multiframe mode. This mode is not available when using repeaters to reach the waveflow module.
    protected boolean verifyProfileInterval = true;
    private boolean roundDownToNearestInterval = false;
    private int initialRFCommand = 0;
    private boolean isV1 = false;
    private int bubbleUpStartMoment;
    private int deviceType;
    private PulseWeight[] pulseWeights = new PulseWeight[4];
    private boolean isV210 = false;
    protected WaveFlowMessageParser waveFlowMessages;
    protected CommonObisCodeMapper commonObisCodeMapper = null;
    protected ParameterFactory parameterFactory = null;

    /**
     * reference to the lower connect layers of the wavenis stack
     */
    private WaveFlowConnect waveFlowConnect;

    /**
     * reference to the radio commands factory
     */
    private RadioCommandFactory radioCommandFactory;

    /**
     * the correcttime property. this property is set from the protocolreader in order to allow to sync the time...
     */
    private int correctTime;

    /**
     * The obiscode for the load profile.
     */
    private ObisCode loadProfileObisCode;

    public boolean usesInitialRFCommand() {
        return getInitialRFCommand() == 0x06 || getInitialRFCommand() == 0x27;
    }

    /**
     * Check the number of connected meters, based on the pulse weight properties that are filled in or are empty
     */
    protected int calcNumberOfInputs() {
        int numberOfInputs = 0;
        for (PulseWeight pulseWeight : pulseWeights) {
            if (pulseWeight != null) {
                numberOfInputs++;
            }
        }
        return numberOfInputs;
    }

    public boolean isRoundDownToNearestInterval() {
        return roundDownToNearestInterval;
    }

    public boolean isV1() {
        return isV1;
    }

    public boolean isV210() {
        return isV210;
    }

    public boolean isMultiFrame() {
        return multiFrame;
    }

    public void setIsV1(boolean v1) {
        isV1 = v1;
    }

    protected void setIsV210(boolean v210) {
        isV210 = v210;
    }

    private boolean isVerifyProfileInterval() {
        return verifyProfileInterval;
    }

    public int getInitialRFCommand() {
        return initialRFCommand;
    }

    public int getBubbleUpStartMoment() {
        return bubbleUpStartMoment;
    }

    protected abstract WaveFlowMessageParser getWaveFlowMessages();

    public ParameterFactory getParameterFactory() {
        if (parameterFactory == null) {
            parameterFactory = new ParameterFactory(this);
        }
        return parameterFactory;
    }

    public RadioCommandFactory getRadioCommandFactory() {
        if (radioCommandFactory == null) {
            radioCommandFactory = new RadioCommandFactory(this);
        }
        return radioCommandFactory;
    }

    @Override
    protected void doConnect() throws IOException {
        if (getExtendedLogging() >= 1) {
            getCommonObisCodeMapper().getRegisterExtendedLogging();
        }
        if (getInitialRFCommand() == 0x06) {
            getRadioCommandFactory().readExtendedIndexConfiguration();    //Cache its contents
        } else if ((getInitialRFCommand() == 0x27) && isV2()) {           //Only V2 supports daily consumption (0x27)
            getRadioCommandFactory().readDailyConsumption();
        }
    }

    /**
     * In case of a bubble up frame, no requests are allowed.
     * Use this method to enable "no request" behavior in the parsing
     */
    public void enableInitialRFCommand() {
        this.initialRFCommand = 0x06;
    }

    public CommonObisCodeMapper getCommonObisCodeMapper() {
        return commonObisCodeMapper;
    }

    private boolean isV2() {
        return (!isV1() && !isV210());
    }

    @Override
    protected void doDisconnect() throws IOException {
    }

    @Override
    protected ProtocolConnection doInit(InputStream inputStream,
                                        OutputStream outputStream, int timeoutProperty,
                                        int protocolRetriesProperty, int forcedDelay, int echoCancelling,
                                        int protocolCompatible, Encryptor encryptor,
                                        HalfDuplexController halfDuplexController) throws IOException {

        radioCommandFactory = new RadioCommandFactory(this);
        if (connectionMode == 1) {
            waveFlowConnect = new WaveCellConnect(inputStream, outputStream, timeoutProperty, getLogger(), forcedDelay, getInfoTypeProtocolRetriesProperty());
            waveFlowConnect.setWaveFlowId(waveFlowId);      //Necessary in requests
        } else {
            waveFlowConnect = new WaveFlowConnect(inputStream, outputStream, timeoutProperty, getLogger(), forcedDelay, getInfoTypeProtocolRetriesProperty());
        }
        doTheInit();

        return waveFlowConnect;

    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(new ObisCodePropertySpec("LoadProfileObisCode", false, getNlsService().getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.WAVEFLOW_LOADPROFILE_OBISCODE).format(), getNlsService().getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.WAVEFLOW_LOADPROFILE_OBISCODE_DESCRIPTION).format()));
        propertySpecs.add(this.integerSpec(CORRECTTIME.getName(), PropertyTranslationKeys.WAVEFLOW_CORRECTTIME, false));
        propertySpecs.add(this.integerSpec("EnableMultiFrameMode", PropertyTranslationKeys.WAVEFLOW_ENABLE_MULTIFRAME_MODE, false));
        propertySpecs.add(this.integerSpec("verifyProfileInterval", PropertyTranslationKeys.WAVEFLOW_VERIFY_PROFILE_INTERVAL, false));
        propertySpecs.add(this.stringSpec("WavenisBubbleUpInfo", PropertyTranslationKeys.WAVEFLOW_WAVENIS_BUBBLE_UP_INFO, false));
        propertySpecs.add(this.integerSpec("ApplicationStatusVariant", PropertyTranslationKeys.WAVEFLOW_APPLICATION_STATUS_VARIANT, false));
        propertySpecs.add(this.integerSpec("RoundDownToNearestInterval", PropertyTranslationKeys.WAVEFLOW_ROUND_DOWN_TO_NEAREST_INTERVAL, false));
        propertySpecs.add(this.integerSpec("InitialRFCommand", PropertyTranslationKeys.WAVEFLOW_INITIAL_RF_COMMAND, false));
        propertySpecs.add(this.integerSpec(CONNECTION_PROPERTY, PropertyTranslationKeys.WAVEFLOW_CONNECTION, false));
        propertySpecs.add(this.integerSpec(PROP_SCALE_A, PropertyTranslationKeys.WAVEFLOW_SCALE_A, false));
        propertySpecs.add(this.integerSpec(PROP_SCALE_B, PropertyTranslationKeys.WAVEFLOW_SCALE_B, false));
        propertySpecs.add(this.integerSpec(PROP_SCALE_C, PropertyTranslationKeys.WAVEFLOW_SCALE_C, false));
        propertySpecs.add(this.integerSpec(PROP_SCALE_D, PropertyTranslationKeys.WAVEFLOW_SCALE_D, false));
        propertySpecs.add(this.integerSpec(PROP_MULTIPLIER_A, PropertyTranslationKeys.WAVEFLOW_MULTIPLIER_A, false));
        propertySpecs.add(this.integerSpec(PROP_MULTIPLIER_B, PropertyTranslationKeys.WAVEFLOW_MULTIPLIER_B, false));
        propertySpecs.add(this.integerSpec(PROP_MULTIPLIER_C, PropertyTranslationKeys.WAVEFLOW_MULTIPLIER_C, false));
        propertySpecs.add(this.integerSpec(PROP_MULTIPLIER_D, PropertyTranslationKeys.WAVEFLOW_MULTIPLIER_D, false));
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        setLoadProfileObisCode(ObisCode.fromString(properties.getTypedProperty("LoadProfileObisCode", "0.0.99.1.0.255")));
        correctTime = properties.getTypedProperty(CORRECTTIME.getName(), 0);
        multiFrame = properties.getTypedProperty("EnableMultiFrameMode", 0) == 1;

        verifyProfileInterval = properties.getTypedProperty("verifyProfileInterval", 1) == 1;

        // e.g. USED,4,28740,28800,1,0e514a401f25
        String wavenisBubbleUpInfo = properties.getTypedProperty("WavenisBubbleUpInfo", "USED,1,28800,28800,1,000000000000");
        bubbleUpStartMoment = Integer.parseInt(wavenisBubbleUpInfo.split(",")[2]);
        deviceType = properties.getTypedProperty("ApplicationStatusVariant", 0);

        setInfoTypeTimeoutProperty(Integer.parseInt(properties.getTypedProperty(PROP_TIMEOUT, String.valueOf(DEFAULT_TIMEOUT)).trim()));
        setInfoTypeProtocolRetriesProperty(properties.getTypedProperty(PROP_RETRIES, DEFAULT_RETRIES));

        Integer scaleA = properties.getTypedProperty(PROP_SCALE_A);
        Integer scaleB = properties.getTypedProperty(PROP_SCALE_B);
        Integer scaleC = properties.getTypedProperty(PROP_SCALE_C);
        Integer scaleD = properties.getTypedProperty(PROP_SCALE_D);

        Integer multiplierA = properties.getTypedProperty(PROP_MULTIPLIER_A);
        Integer multiplierB = properties.getTypedProperty(PROP_MULTIPLIER_B);
        Integer multiplierC = properties.getTypedProperty(PROP_MULTIPLIER_C);
        Integer multiplierD = properties.getTypedProperty(PROP_MULTIPLIER_D);

        pulseWeights[0] = createPulseWeight(scaleA, multiplierA, 1);
        pulseWeights[1] = createPulseWeight(scaleB, multiplierB, 2);
        pulseWeights[2] = createPulseWeight(scaleC, multiplierC, 3);
        pulseWeights[3] = createPulseWeight(scaleD, multiplierD, 4);

        initialRFCommand = properties.getTypedProperty("InitialRFCommand", 0);
        roundDownToNearestInterval = properties.getTypedProperty("RoundDownToNearestInterval", 0) == 1;
        connectionMode = properties.getTypedProperty(CONNECTION_PROPERTY, MUC_WAVECELL_CONNECTION);
        String nodeIdString = properties.getTypedProperty(ADDRESS.getName(), "-1");
        try {
            waveFlowId = Integer.parseInt(nodeIdString.trim().length() == 0 ? "-1" : nodeIdString.trim());   //DeviceId
        } catch (NumberFormatException e) {
            waveFlowId = -1;
        }
        if ((waveFlowId == -1) && (connectionMode == 1)) {
            throw new MissingPropertyException("DeviceId property is invalid... It should contain the WaveFlow ID if the connectionMode is set to 1");
        }
    }

    private Integer getIntProperty(String property) {
        if (property == null) {
            return null;
        }
        return Integer.parseInt(property);
    }

    private PulseWeight createPulseWeight(Integer scale, Integer multiplier, int port) {
        if (pulseWeights[port - 1] != null) {
            return pulseWeights[port - 1];     //Don't override cached pulse weights!
        }
        if (scale == null) {
            return null;
        }
        if (multiplier == null) {
            return null;
        }
        return new PulseWeight(new WaveFlowV2(this.getPropertySpecService(), this.getNlsService()), scale, multiplier, port);
    }

    /**
     * Getter for the pulse weight for a specific port
     *
     * @param port            port number, zero based!
     * @param requestsAllowed
     * @return
     * @throws IOException
     */
    public PulseWeight getPulseWeight(int port, boolean requestsAllowed) throws IOException {
        if (port < 0) {
            port = 0;
        }
        if (pulseWeights[port] == null) {
            if (requestsAllowed) {
                pulseWeights[port] = getParameterFactory().readPulseWeight(port + 1);
            } else {
                pulseWeights[port] = new PulseWeight(this, 0, 1, port + 1);
            }
        }
        return pulseWeights[port];
    }

    public void setPulseWeights(PulseWeight[] pulseWeights) {
        this.pulseWeights = pulseWeights;
    }

    public PulseWeight getPulseWeight(int port) throws IOException {
        return getPulseWeight(port, true);
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return "N/A";         //Omit for battery saving purposes.
    }

    public String readFirmwareVersion() throws IOException {
        return "V" + WaveflowProtocolUtils.toHexString(getRadioCommandFactory().readFirmwareVersion().getFirmwareVersion()) + ", Mode of transmission " + getRadioCommandFactory().readFirmwareVersion().getModeOfTransmission();
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: Wed Dec 28 16:35:58 2016 +0100 $";
    }

    @Override
    public Date getTime() throws IOException {
        // If we need to sync the time, then we need to request the RTC in the waveflow device in order to determine the shift.
        // However, if no timesync needs to be done, we're ok with a dummy Date() from the RTU+Server.
        // we do this because we want to limit the roundtrips to the RF device
        if (correctTime == 0) {
            return Calendar.getInstance(getTimeZone()).getTime();
        } else {
            return getParameterFactory().readTimeDateRTC();
        }
    }

    @Override
    public TimeZone getTimeZone() {
        if (super.getTimeZone() == null) {
            return TimeZone.getDefault();
        }
        return super.getTimeZone();
    }

    public final void forceSetTime() throws IOException {
        Calendar cal = Calendar.getInstance(getTimeZone());
        getParameterFactory().writeTimeDateRTC(cal.getTime());
    }

    @Override
    public void setTime() throws IOException {
        forceSetTime();
    }

    /**
     * When verifyProfileInterval is false, the given interval from EiServer is returned.
     * Otherwise, the interval is queried from the meter.
     * <p/>
     * NOTE: when requesting the DAILY profile data, the meter's interval is set to 1 HOUR (special case),
     * so verifyProfileInterval needs to be set to false in order to prevent an interval mismatch.
     */
    @Override
    public int getProfileInterval() throws IOException {
        if (isVerifyProfileInterval()) {
            return getParameterFactory().getProfileIntervalInSeconds();
        } else {
            return super.getProfileInterval();
        }
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        try {
            return getTheProfileData(lastReading, new Date(), includeEvents);
        } catch (WaveFlowException e) {
            getLogger().warning("No profile data available." + "\n\r" + e.getMessage());
            return null;
        }
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        try {
            return getTheProfileData(from, to, includeEvents);
        } catch (WaveFlowException e) {
            getLogger().warning("No profile data available." + "\n\r" + e.getMessage());
            return null;
        }
    }

    public ObisCode getLoadProfileObisCode() {
        return loadProfileObisCode;
    }

    public void setLoadProfileObisCode(ObisCode loadProfileObisCode) {
        this.loadProfileObisCode = loadProfileObisCode;
    }

    @Override
    public void setHalfDuplexController(HalfDuplexController halfDuplexController) {
        // absorb
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return getParameterFactory().readOperatingMode().getNumberOfInputsUsed();
    }

    @Override
    public WaveFlowConnect getWaveFlowConnect() {
        return waveFlowConnect;
    }

    /**
     * This parses the received alarm frames into a list of meter events.
     * The first entry of this list is the alarm status, this is used in the acknowledgement sent back to the module.
     * <p/>
     * The event String is an ASCII representation of all bytes in the alarm frame
     */
    @Deprecated
    public List map2MeterEvent(String event) throws IOException {
        List statusAndEvents = new ArrayList();
        AlarmFrameParser alarmFrame = new AlarmFrameParser(this);
        alarmFrame.parse(ProtocolUtils.convert2ascii(event.getBytes()));
        statusAndEvents.add(alarmFrame.getResponseACK());
        statusAndEvents.add(alarmFrame.getMeterEvents());
        return statusAndEvents;
    }

    /**
     * Same parsing as the map2MeterEvent method, but takes the original byte array (incoming frame) as argument
     */
    @Override
    public List<MeterEvent> parseAlarms(byte[] frame) throws IOException {
        AlarmFrameParser alarmFrame = new AlarmFrameParser(this);
        alarmFrame.parse(frame);
        return alarmFrame.getMeterEvents();
    }

    public int getDeviceType() {
        return deviceType;
    }

    @Override
    public Logger getLogger() {
        return super.getLogger();
    }

    @Override
    public int getInfoTypeProtocolRetriesProperty() {
        return super.getInfoTypeProtocolRetriesProperty();
    }

}