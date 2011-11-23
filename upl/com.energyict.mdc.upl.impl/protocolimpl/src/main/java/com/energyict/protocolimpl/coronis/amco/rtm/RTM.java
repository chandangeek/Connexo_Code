package com.energyict.protocolimpl.coronis.amco.rtm;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocolimpl.coronis.amco.rtm.core.alarmframe.AlarmFrameParser;
import com.energyict.protocolimpl.coronis.amco.rtm.core.alarmframe.BubbleUpFrameParser;
import com.energyict.protocolimpl.coronis.amco.rtm.core.parameter.ParameterFactory;
import com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand.RadioCommandFactory;
import com.energyict.protocolimpl.coronis.core.*;

import java.io.*;
import java.util.*;

public class RTM extends AbstractProtocol implements MessageProtocol, ProtocolLink, EventMapper, BubbleUp {

    private ObisCodeMapper obisCodeMapper;
    private WaveFlowConnect rtmConnect;
    private RadioCommandFactory radioCommandFactory;
    private ParameterFactory parameterFactory = null;
    private RtmMessages waveLogMessages = new RtmMessages(this);
    private int correctTime;
    private int numberOfChannels = -1;
    private ProfileDataReader profileDataReader;
    private boolean verifyProfileInterval = false;
    private boolean multiFrame;         //Custom property enabling multiframe mode. This mode is not available when using repeaters to reach the waveflow module.
    private int bubbleUpStartMoment;
    private int bubbleUpEndHour;
    private int initialRFCommand = 0;
    private boolean roundDownToNearestInterval = false;

    public ObisCodeMapper getObisCodeMapper() {
        return obisCodeMapper;
    }

    public boolean isRoundDownToNearestInterval() {
        return roundDownToNearestInterval;
    }

    public int getInitialRFCommand() {
        return initialRFCommand;
    }

    public boolean usesInitialRFCommand() {
        return initialRFCommand == 0x07;
    }

    public ParameterFactory getParameterFactory() {
        if (parameterFactory == null) {
            parameterFactory = new ParameterFactory(this);
        }
        return parameterFactory;
    }

    final public RadioCommandFactory getRadioCommandFactory() {
        if (radioCommandFactory == null) {
            radioCommandFactory = new RadioCommandFactory(this);
        }
        return radioCommandFactory;
    }

    public int getBubbleUpEndHour() {
        return bubbleUpEndHour;
    }

    public int getBubbleUpStartMoment() {
        return bubbleUpStartMoment;
    }

    @Override
    protected void doConnect() throws IOException {
        if (getExtendedLogging() >= 1) {
            getObisCodeMapper().getRegisterExtendedLogging();
        }
        if (getInitialRFCommand() == 0x07) {
            getRadioCommandFactory().readExtendedDataloggingTable(((int) Math.pow(2, super.getNumberOfChannels())) - 1, 24 / super.getNumberOfChannels());
        }
    }

    public int getNumberOfChannels() throws IOException {
        if (numberOfChannels <= 0) {
            numberOfChannels = getParameterFactory().readOperatingMode().readNumberOfPorts();
        }
        return numberOfChannels;
    }

    public boolean isMultiFrame() {
        return multiFrame;
    }

    @Override
    protected void doDisConnect() throws IOException {
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        try {
            return profileDataReader.getProfileData(lastReading, new Date(), includeEvents);
        } catch (WaveFlowException e) {
            getLogger().warning("No profile data available." + "\n\r" + e.getMessage());
            return null;
        }
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
        try {
            return profileDataReader.getProfileData(from, to, includeEvents);
        } catch (WaveFlowException e) {
            getLogger().warning("No profile data available." + "\n\r" + e.getMessage());
            return null;
        }
    }

    public final boolean isVerifyProfileInterval() {
        return verifyProfileInterval;
    }

    public int getProfileInterval() throws UnsupportedException, IOException {
        if (isVerifyProfileInterval()) {
            return getParameterFactory().getProfileIntervalInSeconds();
        } else {
            return super.getProfileInterval();
        }
    }

    @Override
    protected ProtocolConnection doInit(InputStream inputStream,
                                        OutputStream outputStream, int timeoutProperty,
                                        int protocolRetriesProperty, int forcedDelay, int echoCancelling,
                                        int protocolCompatible, Encryptor encryptor,
                                        HalfDuplexController halfDuplexController) throws IOException {

        radioCommandFactory = new RadioCommandFactory(this);
        rtmConnect = new WaveFlowConnect(inputStream, outputStream, timeoutProperty, getLogger(), forcedDelay, getInfoTypeProtocolRetriesProperty());
        profileDataReader = new ProfileDataReader(this);
        obisCodeMapper = new ObisCodeMapper(this);
        parameterFactory = new ParameterFactory(this);
        return rtmConnect;
    }

    @Override
    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setInfoTypeTimeoutProperty(Integer.parseInt(properties.getProperty("Timeout", "40000").trim()));
        correctTime = Integer.parseInt(properties.getProperty(MeterProtocol.CORRECTTIME, "0"));
        verifyProfileInterval = Integer.parseInt(properties.getProperty("verifyProfileInterval", "1")) == 1;
        multiFrame = Integer.parseInt(properties.getProperty("EnableMultiFrameMode", "0")) == 1;

        // e.g. USED,4,28740,28800,1,0e514a401f25
        bubbleUpStartMoment = Integer.parseInt(properties.getProperty("WavenisBubbleUpInfo", "USED,1,-1,-1,1,000000000000").split(",")[2]);

        // e.g. USED,4,28740,28800,1,0e514a401f25
        bubbleUpEndHour = Integer.parseInt(properties.getProperty("WavenisBubbleUpInfo", "USED,1,-1,-1,1,000000000000").split(",")[3]);
        initialRFCommand = Integer.parseInt(properties.getProperty("InitialRFCommand", "0").trim());
        roundDownToNearestInterval = Integer.parseInt(properties.getProperty("RoundDownToNearestInterval", "0").trim()) == 1;
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return "N/A";         //Omit for battery saving purposes.
    }

    public String readFirmwareVersion() throws IOException {
        return "V" + WaveflowProtocolUtils.toHexString(getRadioCommandFactory().readFirmwareVersion().getFirmwareVersion()) + ", Mode of transmission " + getRadioCommandFactory().readFirmwareVersion().getCommunicationMode();
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2011-11-03 14:29:32 +0100 (do, 03 nov 2011) $";
    }

    @Override
    public Date getTime() throws IOException {
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

    @Override
    public void setTime() throws IOException {
        Calendar now = Calendar.getInstance();
        GregorianCalendar cal = new GregorianCalendar(getTimeZone());
        cal.set(Calendar.YEAR, now.get(Calendar.YEAR));
        cal.set(Calendar.MONTH, now.get(Calendar.MONTH));
        cal.set(Calendar.DATE, now.get(Calendar.DATE));
        cal.set(Calendar.DAY_OF_WEEK, now.get(Calendar.DAY_OF_WEEK));
        cal.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.SECOND, now.get(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, now.get(Calendar.MILLISECOND));
        getParameterFactory().writeTimeDateRTC(cal.getTime());
    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapper.getRegisterInfo(obisCode);
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapper.getRegisterValue(obisCode);
    }

    public void applyMessages(List messageEntries) throws IOException {
        waveLogMessages.applyMessages(messageEntries);
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return waveLogMessages.queryMessage(messageEntry);
    }

    public List getMessageCategories() {
        return waveLogMessages.getMessageCategories();
    }

    public String writeMessage(Message msg) {
        return waveLogMessages.writeMessage(msg);
    }

    public String writeTag(MessageTag tag) {
        return waveLogMessages.writeTag(tag);
    }

    public String writeValue(MessageValue value) {
        return waveLogMessages.writeValue(value);
    }

    @Override
    protected List doGetOptionalKeys() {
        List result = new ArrayList();
        result.add("EnableMultiFrameMode");
        result.add("verifyProfileInterval");
        result.add("InitialRFCommand");
        result.add("RoundDownToNearestInterval");
        return result;
    }

    public void setHalfDuplexController(HalfDuplexController halfDuplexController) {
        // absorb
    }

    public WaveFlowConnect getWaveFlowConnect() {
        return rtmConnect;
    }

    public List map2MeterEvent(String event) throws IOException {
        List statusAndEvents = new ArrayList();
        AlarmFrameParser alarmFrame = new AlarmFrameParser(this);
        alarmFrame.parse(event.getBytes());
        statusAndEvents.add(alarmFrame.getResponse());
        statusAndEvents.add(alarmFrame.getMeterEvents());
        return statusAndEvents;
    }

    public BubbleUpObject parseBubbleUpData(byte[] data) throws IOException {
        return BubbleUpFrameParser.parse(data, this);
    }
}