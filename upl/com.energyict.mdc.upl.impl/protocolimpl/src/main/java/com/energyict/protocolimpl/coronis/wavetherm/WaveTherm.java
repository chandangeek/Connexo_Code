package com.energyict.protocolimpl.coronis.wavetherm;

import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.EventMapper;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.coronis.core.ProtocolLink;
import com.energyict.protocolimpl.coronis.core.WaveFlowConnect;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.wavetherm.core.AlarmFrameParser;
import com.energyict.protocolimpl.coronis.wavetherm.core.ObisCodeMapper;
import com.energyict.protocolimpl.coronis.wavetherm.core.parameter.ParameterFactory;
import com.energyict.protocolimpl.coronis.wavetherm.core.radiocommand.RadioCommandFactory;
import com.energyict.protocolimpl.dlms.common.ObisCodePropertySpec;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.CORRECTTIME;

public class WaveTherm extends AbstractProtocol implements MessageProtocol, ProtocolLink, EventMapper {

    private boolean verifyProfileInterval = false;
    private ObisCodeMapper obisCodeMapper;
    private WaveFlowConnect waveLogConnect;
    private RadioCommandFactory radioCommandFactory;
    private ParameterFactory parameterFactory = null;
    private WaveThermMessages waveSenseMessages = new WaveThermMessages(this);
    private int correctTime;
    private ObisCode loadProfileObisCode;
    private ProfileDataReader profileDataReader;
    private int numberOfChannels = -1;

    private boolean isVerifyProfileInterval() {
        return verifyProfileInterval;
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
            numberOfChannels = getParameterFactory().readApplicationStatus().getNumberOfSensors();
        }
        return numberOfChannels;
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
        waveLogConnect = new WaveFlowConnect(inputStream, outputStream, timeoutProperty, getLogger(), forcedDelay, getInfoTypeProtocolRetriesProperty());
        obisCodeMapper = new ObisCodeMapper(this);
        profileDataReader = new ProfileDataReader(this);
        parameterFactory = new ParameterFactory(this);
        return waveLogConnect;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        propertySpecs.add(new ObisCodePropertySpec("LoadProfileObisCode", false));
        propertySpecs.add(UPLPropertySpecFactory.integer(CORRECTTIME.getName(), false));
        propertySpecs.add(UPLPropertySpecFactory.integer("verifyProfileInterval", false));
        return propertySpecs;
    }

    @Override
    public void setProperties(Properties properties) throws PropertyValidationException {
        super.setProperties(properties);
        try {
            setInfoTypeTimeoutProperty(Integer.parseInt(properties.getProperty(PROP_TIMEOUT, "40000").trim()));
            setLoadProfileObisCode(ObisCode.fromString(properties.getProperty("LoadProfileObisCode", "0.0.99.1.0.255")));
            correctTime = Integer.parseInt(properties.getProperty(CORRECTTIME.getName(), "0"));
            verifyProfileInterval = Integer.parseInt(properties.getProperty("verifyProfileInterval", "1")) == 1;
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, this.getClass().getSimpleName() + ": validation of properties failed before");
        }
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
        return "$Date: 2011-05-24 15:37:24 +0200 (di, 24 mei 2011) $";
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
    public int getProfileInterval() throws IOException {
        if (isVerifyProfileInterval()) {
            return getParameterFactory().getProfileIntervalInSeconds();
        } else {
            return super.getProfileInterval();
        }
    }

    @Override
    public ProfileData getProfileData(Date lastReading, Date toDate, boolean includeEvents) throws IOException {
        try {
            return profileDataReader.getProfileData(lastReading, toDate, includeEvents);
        }
        catch (WaveFlowException e) {
            getLogger().warning("No profile data available. " + e.getMessage());
            return null;
        }
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
        waveSenseMessages.applyMessages(messageEntries);
    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return waveSenseMessages.queryMessage(messageEntry);
    }

    @Override
    public List getMessageCategories() {
        return waveSenseMessages.getMessageCategories();
    }

    @Override
    public String writeMessage(Message msg) {
        return waveSenseMessages.writeMessage(msg);
    }

    @Override
    public String writeTag(MessageTag tag) {
        return waveSenseMessages.writeTag(tag);
    }

    @Override
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
		statusAndEvents.add(alarmFrame.getResponse());
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