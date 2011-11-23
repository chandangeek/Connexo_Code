package com.energyict.protocolimpl.coronis.waveflow.core;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocolimpl.coronis.core.*;
import com.energyict.protocolimpl.coronis.waveflow.core.messages.WaveFlowMessageParser;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.ParameterFactory;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.PulseWeight;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.RadioCommandFactory;
import com.energyict.protocolimpl.coronis.waveflow.waveflowV2.WaveFlowV2;

import java.io.*;
import java.util.*;

abstract public class WaveFlow extends AbstractProtocol implements ProtocolLink, EventMapper, BubbleUp {

    private static final String PROP_SCALE_A = "ScaleA";
    private static final String PROP_SCALE_B = "ScaleB";
    private static final String PROP_SCALE_C = "ScaleC";
    private static final String PROP_SCALE_D = "ScaleD";
    private static final String PROP_MULTIPLIER_A = "MultiplierA";
    private static final String PROP_MULTIPLIER_B = "MultiplierB";
    private static final String PROP_MULTIPLIER_C = "MultiplierC";
    private static final String PROP_MULTIPLIER_D = "MultiplierD";

    abstract protected void doTheInit() throws IOException;

    abstract protected ProfileData getTheProfileData(Date lastReading, Date toDate, boolean includeEvents) throws UnsupportedException, IOException;

    private boolean multiFrame;         //Custom property enabling multiframe mode. This mode is not available when using repeaters to reach the waveflow module.
    private boolean verifyProfileInterval = true;
    private boolean roundDownToNearestInterval = false;
    private int initialRFCommand = 0;
    private boolean isV1 = false;
    private int bubbleUpStartMoment;
    private int deviceType;
    private PulseWeight[] pulseWeights = new PulseWeight[4];
    private boolean isV210 = false;
    protected WaveFlowMessageParser waveFlowMessages;

    public boolean usesInitialRFCommand() {
        return getInitialRFCommand() == 0x06 || getInitialRFCommand() == 0x27;
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

    public void setIsV210(boolean v210) {
        isV210 = v210;
    }

    public final boolean isVerifyProfileInterval() {
        return verifyProfileInterval;
    }

    public int getInitialRFCommand() {
        return initialRFCommand;
    }

    public int getBubbleUpStartMoment() {
        return bubbleUpStartMoment;
    }

    protected abstract WaveFlowMessageParser getWaveFlowMessages();

    public void applyMessages(List messageEntries) throws IOException {
        getWaveFlowMessages().applyMessages(messageEntries);
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return getWaveFlowMessages().queryMessage(messageEntry);
    }

    public List getMessageCategories() {
        return getWaveFlowMessages().getMessageCategories();
    }

    public String writeMessage(Message msg) {
        return getWaveFlowMessages().writeMessage(msg);
    }

    public String writeTag(MessageTag tag) {
        return getWaveFlowMessages().writeTag(tag);
    }

    public String writeValue(MessageValue value) {
        return getWaveFlowMessages().writeValue(value);
    }

    /**
     * reference to the lower connect latyers of the wavenis stack
     */
    private WaveFlowConnect waveFlowConnect;

    /**
     * reference to the radio commands factory
     */
    private RadioCommandFactory radioCommandFactory;

    abstract public CommonObisCodeMapper getCommonObisCodeMapper();

    abstract public ParameterFactory getParameterFactory();

    /**
     * the correcttime property. this property is set from the protocolreader in order to allow to sync the time...
     */
    private int correctTime;

    /**
     * The obiscode for the load profile.
     */
    ObisCode loadProfileObisCode;

    final public RadioCommandFactory getRadioCommandFactory() {
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

    private boolean isV2() {
        return (!isV1() && !isV210());
    }

    @Override
    protected void doDisConnect() throws IOException {
    }

    @Override
    protected ProtocolConnection doInit(InputStream inputStream,
                                        OutputStream outputStream, int timeoutProperty,
                                        int protocolRetriesProperty, int forcedDelay, int echoCancelling,
                                        int protocolCompatible, Encryptor encryptor,
                                        HalfDuplexController halfDuplexController) throws IOException {

        radioCommandFactory = new RadioCommandFactory(this);
        waveFlowConnect = new WaveFlowConnect(inputStream, outputStream, timeoutProperty, getLogger(), forcedDelay, getInfoTypeProtocolRetriesProperty());
        doTheInit();

        return waveFlowConnect;

    }

    @Override
    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setLoadProfileObisCode(ObisCode.fromString(properties.getProperty("LoadProfileObisCode", "0.0.99.1.0.255")));
        correctTime = Integer.parseInt(properties.getProperty(MeterProtocol.CORRECTTIME, "0"));
        multiFrame = Integer.parseInt(properties.getProperty("EnableMultiFrameMode", "0")) == 1;

        //Important to set this to false (don't check the interval) when requesting the DAILY load profile.
        //This is because the meter's interval needs to be set to 60 minutes in order to generate daily values...
        verifyProfileInterval = Integer.parseInt(properties.getProperty("verifyProfileInterval", "1")) == 1;

        // e.g. USED,4,28740,28800,1,0e514a401f25
        bubbleUpStartMoment = Integer.parseInt(properties.getProperty("WavenisBubbleUpInfo", "USED,1,28800,28800,1,000000000000").split(",")[2]);
        deviceType = Integer.parseInt(properties.getProperty("ApplicationStatusVariant", "0"));

        setInfoTypeTimeoutProperty(Integer.parseInt(properties.getProperty(PROP_TIMEOUT, "5000").trim()));
        setInfoTypeProtocolRetriesProperty(Integer.parseInt(properties.getProperty(PROP_RETRIES, "2").trim()));

        int scaleA = Integer.parseInt(properties.getProperty(PROP_SCALE_A, "0"));
        int scaleB = Integer.parseInt(properties.getProperty(PROP_SCALE_B, "0"));
        int scaleC = Integer.parseInt(properties.getProperty(PROP_SCALE_C, "0"));
        int scaleD = Integer.parseInt(properties.getProperty(PROP_SCALE_D, "0"));

        int multiplierA = Integer.parseInt(properties.getProperty(PROP_MULTIPLIER_A, "1"));
        int multiplierB = Integer.parseInt(properties.getProperty(PROP_MULTIPLIER_B, "1"));
        int multiplierC = Integer.parseInt(properties.getProperty(PROP_MULTIPLIER_C, "1"));
        int multiplierD = Integer.parseInt(properties.getProperty(PROP_MULTIPLIER_D, "1"));

        WaveFlow waveFlow = new WaveFlowV2();
        PulseWeight pulseWeightA = new PulseWeight(waveFlow, scaleA, multiplierA, 1);
        PulseWeight pulseWeightB = new PulseWeight(waveFlow, scaleB, multiplierB, 2);
        PulseWeight pulseWeightC = new PulseWeight(waveFlow, scaleC, multiplierC, 3);
        PulseWeight pulseWeightD = new PulseWeight(waveFlow, scaleD, multiplierD, 4);
        initialRFCommand = Integer.parseInt(properties.getProperty("InitialRFCommand", "0").trim());
        roundDownToNearestInterval = Integer.parseInt(properties.getProperty("RoundDownToNearestInterval", "0").trim()) == 1;

        pulseWeights = new PulseWeight[]{pulseWeightA, pulseWeightB, pulseWeightC, pulseWeightD};
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
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return "N/A";         //Omit for battery saving purposes.
    }

    public String readFirmwareVersion() throws IOException, UnsupportedException {
        return "V" + WaveflowProtocolUtils.toHexString(getRadioCommandFactory().readFirmwareVersion().getFirmwareVersion()) + ", Mode of transmission " + getRadioCommandFactory().readFirmwareVersion().getModeOfTransmission();
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2011-10-25 16:18:44 +0200 (di, 25 okt 2011) $";
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
        Calendar now = Calendar.getInstance();
        Calendar cal = Calendar.getInstance(getTimeZone());
        cal.set(Calendar.YEAR, now.get(Calendar.YEAR));
        cal.set(Calendar.MONTH, now.get(Calendar.MONTH));
        cal.set(Calendar.DATE, now.get(Calendar.DATE));
        cal.set(Calendar.DAY_OF_WEEK, now.get(Calendar.DAY_OF_WEEK));
        cal.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.SECOND, now.get(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, now.get(Calendar.MILLISECOND));
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
    public int getProfileInterval() throws UnsupportedException, IOException {
        if (isVerifyProfileInterval()) {
            return getParameterFactory().getProfileIntervalInSeconds();
        } else {
            return super.getProfileInterval();
        }
    }

    /**
     * Override this method to request the load profile from the meter starting at lastreading until now.
     *
     * @param lastReading   request from
     * @param includeEvents enable or disable tht reading of meterevents
     * @return All load profile data in the meter from lastReading
     * @throws java.io.IOException When something goes wrong
     */
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        try {
            return getTheProfileData(lastReading, new Date(), includeEvents);
        } catch (WaveFlowException e) {
            getLogger().warning("No profile data available." + "\n\r" + e.getMessage());
            return null;
        }
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
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
    protected List doGetOptionalKeys() {
        List result = new ArrayList();
        result.add("EnableMultiFrameMode");
        result.add("verifyProfileInterval");
        result.add("InitialRFCommand");
        result.add("RoundDownToNearestInterval");
        result.add("LoadProfileObisCode");
        result.add("ApplicationStatusVariant");
        result.add(PROP_SCALE_A);
        result.add(PROP_SCALE_B);
        result.add(PROP_SCALE_C);
        result.add(PROP_SCALE_D);
        result.add(PROP_MULTIPLIER_A);
        result.add(PROP_MULTIPLIER_B);
        result.add(PROP_MULTIPLIER_C);
        result.add(PROP_MULTIPLIER_D);
        return result;
    }

    public void setHalfDuplexController(HalfDuplexController halfDuplexController) {
        // absorb
    }

    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return getParameterFactory().readOperatingMode().getNumberOfInputsUsed();
    }

    public WaveFlowConnect getWaveFlowConnect() {
        return waveFlowConnect;
    }

    /**
     * This parses the received alarm frames into a list of meter events.
     * The first entry of this list is the alarm status, this is used in the acknowledgement sent back to the module.
     */
    public List map2MeterEvent(String event) throws IOException {
        List statusAndEvents = new ArrayList();
        AlarmFrameParser alarmFrame = new AlarmFrameParser(this);
        alarmFrame.parse(event.getBytes());
        statusAndEvents.add(alarmFrame.getResponseACK());
        statusAndEvents.add(alarmFrame.getMeterEvents());
        return statusAndEvents;
    }

    public int getDeviceType() {
        return deviceType;
    }
}