package com.energyict.protocolimpl.coronis.wavesense;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocolimpl.coronis.core.*;
import com.energyict.protocolimpl.coronis.wavesense.core.AlarmFrameParser;
import com.energyict.protocolimpl.coronis.wavesense.core.ObisCodeMapper;
import com.energyict.protocolimpl.coronis.wavesense.core.parameter.ParameterFactory;
import com.energyict.protocolimpl.coronis.wavesense.core.radiocommand.RadioCommandFactory;

import java.io.*;
import java.util.*;

public class WaveSense extends AbstractProtocol implements MessageProtocol, ProtocolLink, EventMapper {

    private boolean verifyProfileInterval = false;
    private ObisCodeMapper obisCodeMapper;
    private WaveFlowConnect waveSenseConnect;
    private RadioCommandFactory radioCommandFactory;
    private ParameterFactory parameterFactory = null;
    private WaveSenseMessages waveSenseMessages = new WaveSenseMessages(this);


    private int correctTime;
    private ObisCode loadProfileObisCode;
    private ProfileDataReader profileDataReader;

    private static final int WAVESENSE_NUMBER_OF_CHANNELS = 1;

    final boolean isVerifyProfileInterval() {
        return verifyProfileInterval;
    }

    public ObisCodeMapper getObisCodeMapper() {
        return obisCodeMapper;
    }

    public ParameterFactory getParameterFactory() {
        return parameterFactory;
    }

    final public RadioCommandFactory getRadioCommandFactory() {
        return radioCommandFactory;
    }

    @Override
    protected void doConnect() throws IOException {
        if (getExtendedLogging() >= 1) {
            getObisCodeMapper().getRegisterExtendedLogging();
        }
    }

    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return WAVESENSE_NUMBER_OF_CHANNELS;
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
        waveSenseConnect = new WaveFlowConnect(inputStream, outputStream, timeoutProperty, getLogger(), forcedDelay, getInfoTypeProtocolRetriesProperty());
        obisCodeMapper = new ObisCodeMapper(this);
        profileDataReader = new ProfileDataReader(this);
        parameterFactory = new ParameterFactory(this);
        waveSenseMessages.setModuleType(getRadioCommandFactory().readModuleType());
        return waveSenseConnect;
    }

    @Override
    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setInfoTypeTimeoutProperty(Integer.parseInt(properties.getProperty("Timeout", "40000").trim()));
        setLoadProfileObisCode(ObisCode.fromString(properties.getProperty("LoadProfileObisCode", "0.0.99.1.0.255")));
        correctTime = Integer.parseInt(properties.getProperty(MeterProtocol.CORRECTTIME, "0"));
        verifyProfileInterval = Integer.parseInt(properties.getProperty("verifyProfileInterval", "1")) == 1;
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        try {
            return "V" + WaveflowProtocolUtils.toHexString(getRadioCommandFactory().readFirmwareVersion().getFirmwareVersion()) + ", Mode of transmission " + getRadioCommandFactory().readFirmwareVersion().getModeOfTransmission();
        } catch (IOException e) {
            return "Error requesting firmware version";
        }
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2011-05-24 15:41:26 +0200 (di, 24 mei 2011) $";
    }

    @Override
    public Date getTime() throws IOException {
        if (!(correctTime == 0)) {
            return new GregorianCalendar(getTimeZone()).getTime();
        } else {
            return getRadioCommandFactory().readTimeDateRTC();
        }
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
        getRadioCommandFactory().writeTimeDateRTC(cal.getTime());
    }

    final public void writeSamplingRate() throws IOException {
        getParameterFactory().writeSamplingPeriod(getProfileInterval());
    }

    public int getProfileInterval() throws UnsupportedException, IOException {
        if (isVerifyProfileInterval()) {
            return getParameterFactory().getProfileIntervalInSeconds();
        } else {
            return super.getProfileInterval();
        }
    }

    public ProfileData getProfileData(Date lastReading, Date toDate, boolean includeEvents) throws IOException, UnsupportedException {
        try {
            return profileDataReader.getProfileData(lastReading, toDate, includeEvents);
        }
        catch (WaveFlowException e) {
            getLogger().warning("No profile data available. " + e.getMessage());
            return null;
        }
    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapper.getRegisterInfo(obisCode);
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapper.getRegisterValue(obisCode);
    }

    public void applyMessages(List messageEntries) throws IOException {
        waveSenseMessages.applyMessages(messageEntries);
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return waveSenseMessages.queryMessage(messageEntry);
    }

    public List getMessageCategories() {
        return waveSenseMessages.getMessageCategories();
    }

    public String writeMessage(Message msg) {
        return waveSenseMessages.writeMessage(msg);
    }

    public String writeTag(MessageTag tag) {
        return waveSenseMessages.writeTag(tag);
    }

    public String writeValue(MessageValue value) {
        return waveSenseMessages.writeValue(value);
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
        result.add("verifyProfileInterval");
        result.add("LoadProfileObisCode");
        return result;
    }

    public void setHalfDuplexController(HalfDuplexController halfDuplexController) {
        // absorb
    }

    public WaveFlowConnect getWaveFlowConnect() {
        return waveSenseConnect;
    }

    public List map2MeterEvent(String event) throws IOException {
        List statusAndEvents = new ArrayList();
        AlarmFrameParser alarmFrame = new AlarmFrameParser(this);
        alarmFrame.parse(event.getBytes());
        statusAndEvents.add(alarmFrame.getResponseACK());
        statusAndEvents.add(alarmFrame.getMeterEvents());
        return statusAndEvents;
    }
}