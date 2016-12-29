package com.energyict.protocolimpl.coronis.amco.rtm;

import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.BubbleUp;
import com.energyict.protocol.BubbleUpObject;
import com.energyict.protocol.EventMapper;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.coronis.amco.rtm.core.alarmframe.AlarmFrameParser;
import com.energyict.protocolimpl.coronis.amco.rtm.core.alarmframe.BubbleUpFrameParser;
import com.energyict.protocolimpl.coronis.amco.rtm.core.parameter.ParameterFactory;
import com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand.RadioCommandFactory;
import com.energyict.protocolimpl.coronis.core.ProtocolLink;
import com.energyict.protocolimpl.coronis.core.WaveFlowConnect;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.CORRECTTIME;

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

    public RTM(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

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
        return initialRFCommand == 0x07 ||initialRFCommand == 0x01;
    }

    public ParameterFactory getParameterFactory() {
        if (parameterFactory == null) {
            parameterFactory = new ParameterFactory(this);
        }
        return parameterFactory;
    }

    public final RadioCommandFactory getRadioCommandFactory() {
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
        if (getInitialRFCommand() == 0x01) {
            getRadioCommandFactory().readCurrentRegister();
        }
    }

    @Override
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
    protected void doDisconnect() throws IOException {
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        try {
            return profileDataReader.getProfileData(lastReading, new Date(), includeEvents);
        } catch (WaveFlowException e) {
            getLogger().warning("No profile data available." + "\n\r" + e.getMessage());
            return null;
        }
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
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

    @Override
    public int getProfileInterval() throws IOException {
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
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        propertySpecs.add(this.integerSpec(CORRECTTIME.getName(), false));
        propertySpecs.add(this.integerSpec("verifyProfileInterval", false));
        propertySpecs.add(this.integerSpec("EnableMultiFrameMode", false));
        propertySpecs.add(this.stringSpec("WavenisBubbleUpInfo", false));
        propertySpecs.add(this.integerSpec("InitialRFCommand", false));
        propertySpecs.add(this.integerSpec("RoundDownToNearestInterval", false));
        return propertySpecs;
    }

    @Override
    public void setProperties(TypedProperties properties) throws PropertyValidationException {
        super.setProperties(properties);
        setInfoTypeTimeoutProperty(Integer.parseInt(properties.getTypedProperty("Timeout", "40000").trim()));
        correctTime = Integer.parseInt(properties.getTypedProperty(CORRECTTIME.getName(), "0"));
        verifyProfileInterval = Integer.parseInt(properties.getTypedProperty("verifyProfileInterval", "1")) == 1;
        multiFrame = Integer.parseInt(properties.getTypedProperty("EnableMultiFrameMode", "0")) == 1;

        // e.g. USED,4,28740,28800,1,0e514a401f25
        String[] wavenisBubbleUpInfo = properties.getTypedProperty("WavenisBubbleUpInfo", "USED,1,-1,-1,1,000000000000").split(",");
        bubbleUpStartMoment = Integer.parseInt(wavenisBubbleUpInfo[2]);

        // e.g. USED,4,28740,28800,1,0e514a401f25
        bubbleUpEndHour = Integer.parseInt(wavenisBubbleUpInfo[3]);
        initialRFCommand = Integer.parseInt(properties.getTypedProperty("InitialRFCommand", "0").trim());
        roundDownToNearestInterval = Integer.parseInt(properties.getTypedProperty("RoundDownToNearestInterval", "0").trim()) == 1;
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
        return "$Date: 2011-11-29 10:33:55 +0100 (di, 29 nov 2011) $";
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

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapper.getRegisterValue(obisCode);
    }

    @Override
    public void applyMessages(List messageEntries) throws IOException {
        waveLogMessages.applyMessages(messageEntries);
    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return waveLogMessages.queryMessage(messageEntry);
    }

    @Override
    public List getMessageCategories() {
        return waveLogMessages.getMessageCategories();
    }

    @Override
    public String writeMessage(Message msg) {
        return waveLogMessages.writeMessage(msg);
    }

    @Override
    public String writeTag(MessageTag tag) {
        return waveLogMessages.writeTag(tag);
    }

    @Override
    public String writeValue(MessageValue value) {
        return waveLogMessages.writeValue(value);
    }

    @Override
    public void setHalfDuplexController(HalfDuplexController halfDuplexController) {
        // absorb
    }

    @Override
    public WaveFlowConnect getWaveFlowConnect() {
        return rtmConnect;
    }

    @Override
    public List map2MeterEvent(String event) throws IOException {
        List statusAndEvents = new ArrayList();
        AlarmFrameParser alarmFrame = new AlarmFrameParser(this, this.getPropertySpecService());
        alarmFrame.parse(ProtocolUtils.convert2ascii(event.getBytes()));
        statusAndEvents.add(alarmFrame.getResponse());
        statusAndEvents.add(alarmFrame.getMeterEvents());
        return statusAndEvents;
    }

    @Override
    public BubbleUpObject parseBubbleUpData(byte[] data) throws IOException {
        return BubbleUpFrameParser.parse(data, this, this.getPropertySpecService());
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