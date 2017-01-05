package com.energyict.protocolimpl.coronis.wavelog;

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
import com.energyict.protocolimpl.coronis.core.ProtocolLink;
import com.energyict.protocolimpl.coronis.core.WaveFlowConnect;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.wavelog.core.AlarmFrameParser;
import com.energyict.protocolimpl.coronis.wavelog.core.ObisCodeMapper;
import com.energyict.protocolimpl.coronis.wavelog.core.parameter.ParameterFactory;
import com.energyict.protocolimpl.coronis.wavelog.core.radiocommand.RadioCommandFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.CORRECTTIME;

public class WaveLog extends AbstractProtocol implements MessageProtocol, ProtocolLink, EventMapper {

    private ObisCodeMapper obisCodeMapper;
    private WaveFlowConnect waveLogConnect;
    private RadioCommandFactory radioCommandFactory;
    private ParameterFactory parameterFactory = null;
    private WaveLogMessages waveLogMessages = new WaveLogMessages(this);
    private int correctTime;
    private int numberOfChannels = -1;
    private ProfileDataReader profileDataReader;
    private static final int CHANNELS = 8;

    public WaveLog(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    public ObisCodeMapper getObisCodeMapper() {
        return obisCodeMapper;
    }

    public ParameterFactory getParameterFactory() {
        return parameterFactory;
    }

    public final RadioCommandFactory getRadioCommandFactory() {
        return radioCommandFactory;
    }

    @Override
    protected void doConnect() throws IOException {
        if (getExtendedLogging() >= 1) {
            getObisCodeMapper().getRegisterExtendedLogging();
        }
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        if (numberOfChannels <= 0) {
            numberOfChannels = CHANNELS;
        }
        return numberOfChannels;
    }

    @Override
    protected void doDisconnect() throws IOException {
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        try {
            return profileDataReader.getProfileData(lastReading, new Date(), includeEvents);
        }
        catch(WaveFlowException e) {
            getLogger().warning("No profile data available." + "\n\r" + e.getMessage());
            return null;
        }
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        try {
            return profileDataReader.getProfileData(from, to, includeEvents);
        }
        catch(WaveFlowException e) {
            getLogger().warning("No profile data available." + "\n\r" + e.getMessage());
            return null;
        }
    }

    @Override
    protected ProtocolConnection doInit(InputStream inputStream,
                                        OutputStream outputStream, int timeoutProperty,
                                        int protocolRetriesProperty, int forcedDelay, int echoCancelling,
                                        int protocolCompatible, Encryptor encryptor,
                                        HalfDuplexController halfDuplexController) throws IOException {

        radioCommandFactory = new RadioCommandFactory(this);
        waveLogConnect = new WaveFlowConnect(inputStream, outputStream, timeoutProperty, getLogger(), forcedDelay, getInfoTypeProtocolRetriesProperty());
        profileDataReader = new ProfileDataReader(this);
        obisCodeMapper = new ObisCodeMapper(this);
        parameterFactory = new ParameterFactory(this);
        return waveLogConnect;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(this.integerSpec(CORRECTTIME.getName(), false));
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        setInfoTypeTimeoutProperty(Integer.parseInt(properties.getTypedProperty(PROP_TIMEOUT, "40000").trim()));
        correctTime = Integer.parseInt(properties.getTypedProperty(CORRECTTIME.getName(), "0"));
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        try {
            return "V" + WaveflowProtocolUtils.toHexString(getRadioCommandFactory().readFirmwareVersion().getFirmwareVersion()) + ", Mode of transmission " + getRadioCommandFactory().readFirmwareVersion().getCommunicationMode();
        } catch (IOException e) {
            return "Error requesting firmware version";
        }
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2011-05-30 14:06:22 +0200 (ma, 30 mei 2011) $";
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
        return waveLogConnect;
    }

    @Override
    public List map2MeterEvent(String event) throws IOException {
        List statusAndEvents = new ArrayList();
        AlarmFrameParser alarmFrame = new AlarmFrameParser(this);
        alarmFrame.parse(ProtocolUtils.convert2ascii(event.getBytes()));
        statusAndEvents.add(alarmFrame.getResponseAck());
        statusAndEvents.add(alarmFrame.getMeterEvents());
        return statusAndEvents;
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