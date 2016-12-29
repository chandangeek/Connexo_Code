package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.mdc.io.NestedIOException;
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
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.coronis.core.ProtocolLink;
import com.energyict.protocolimpl.coronis.core.RegisterCache;
import com.energyict.protocolimpl.coronis.core.WaveFlowConnect;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.CORRECTTIME;

public abstract class WaveFlow100mW extends AbstractProtocol implements MessageProtocol, ProtocolLink, EventMapper, RegisterCache {

    private static final int WAVEFLOW_NR_OF_CHANNELS = 2;
    private static final String READ_LOAD_PROFILE_PROPERTY = "ReadLoadProfile";
    private static final String LOAD_PROFILE_OBIS_CODE_PROPERTY = "LoadProfileObisCode";
    private static final String VERIFY_PROFILE_INTERVAL_PROPERTY = "verifyProfileInterval";
    private static final String SERIAL_NUMBER_A = "SerialNumberA";
    private static final String SERIAL_NUMBER_B = "SerialNumberB";

    public WaveFlow100mW(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    protected abstract void doTheConnect() throws IOException;

    protected abstract void doTheInit() throws IOException;

    protected abstract void doTheDisConnect() throws IOException;

    protected abstract ProfileData getTheProfileData(Date lastReading, int portId, boolean includeEvents) throws IOException;

    protected abstract MeterProtocolType getMeterProtocolType();

    protected enum MeterProtocolType {
        SM150E(0),
        ECHODIS(1);

        int type;

        MeterProtocolType(int type) {
            this.type = type;
        }
    }

    private boolean readLoadProfile = true;

    public boolean isReadLoadProfile() {
        return readLoadProfile;
    }

    /**
     * reference to the lower connect latyers of the wavenis stack
     */
    private WaveFlowConnect waveFlowConnect;

    /**
     * reference to the parameter factory
     */
    private ParameterFactory parameterFactory;

    /**
     * reference to the radio commands factory
     */
    private RadioCommandFactory radioCommandFactory;

    /**
     * reference to the obiscode mapper.
     */
    private CommonObisCodeMapper commonObisCodeMapper;

    private boolean verifyProfileInterval = false;
    private String serialNumberA = null;
    private String serialNumberB = null;

    private boolean isVerifyProfileInterval() {
        return verifyProfileInterval;
    }

    public final CommonObisCodeMapper getCommonObisCodeMapper() {
        return commonObisCodeMapper;
    }

    /**
     * reference to the message protocol parser
     */
    private WaveFlow100mWMessages waveFlow100mWMessages = new WaveFlow100mWMessages(this);

    /**
     * the correcttime property. this property is set from the protocolreader in order to allow to sync the time...
     */
    private int correctTime;

    /**
     * cached generic header...
     */
    private GenericHeader cachedGenericHeader = null;

    public final GenericHeader getCachedGenericHeader() throws IOException {
        if (cachedGenericHeader == null) {
            radioCommandFactory.readInternalData();
        }
        return cachedGenericHeader;
    }

    final void setCachedGenericHeader(GenericHeader cachedGenericHeader) {
        this.cachedGenericHeader = cachedGenericHeader;
    }

    /**
     * The obiscode for the load profile. Since the Waveflow100mw can connect 2 watermeters, there are 2 independent load profiles.
     */
    ObisCode loadProfileObisCode;

    public final ParameterFactory getParameterFactory() {
        return parameterFactory;
    }

    public final RadioCommandFactory getRadioCommandFactory() {
        return radioCommandFactory;
    }

    @Override
    protected void doConnect() throws IOException {
        doTheConnect();
        //Validate serial number(s) of the connected meter(s)
        if (serialNumberA != null) {
            InternalData[] internalDatas = readInternalDatas();
            if (internalDatas.length == 0 || internalDatas[0] == null) {
                IOException e = new IOException("Expected a meter with serial number '" + serialNumberA + "' but no meter was connected on port 'A'.");
                throw new NestedIOException(e);
            }
            if (!serialNumberA.equalsIgnoreCase(internalDatas[0].getSerialNumber())) {
                IOException e = new IOException("Serial number mismatch. Configured serial number for meter on port 'A' is '" + serialNumberA + "', while the actual serial number is '" + internalDatas[0].getSerialNumber() + "'");
                throw new NestedIOException(e);
            }
        }
        if (serialNumberB != null) {
            InternalData[] internalDatas = readInternalDatas();
            if (internalDatas.length < 2 || internalDatas[1] == null) {
                IOException e = new IOException("Expected a meter with serial number '" + serialNumberB + "' but no meter was connected on port 'B'.");
                throw new NestedIOException(e);
            }
            if (!serialNumberB.equalsIgnoreCase(internalDatas[1].getSerialNumber())) {
                IOException e = new IOException("Serial number mismatch. Configured serial number for meter on port 'B' is '" + serialNumberB + "', while the actual serial number is '" + internalDatas[1].getSerialNumber() + "'");
                throw new NestedIOException(e);
            }
        }
    }

    @Override
    protected void doDisconnect() throws IOException {
        doTheDisConnect();
    }

    @Override
    protected ProtocolConnection doInit(InputStream inputStream,
                                        OutputStream outputStream, int timeoutProperty,
                                        int protocolRetriesProperty, int forcedDelay, int echoCancelling,
                                        int protocolCompatible, Encryptor encryptor,
                                        HalfDuplexController halfDuplexController) throws IOException {

        parameterFactory = new ParameterFactory(this);
        radioCommandFactory = new RadioCommandFactory(this, propertySpecService);
        waveFlowConnect = new WaveFlowConnect(inputStream, outputStream, timeoutProperty, getLogger(), forcedDelay, getInfoTypeProtocolRetriesProperty());
        commonObisCodeMapper = new CommonObisCodeMapper(this);

        doTheInit();

        return waveFlowConnect;

    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        propertySpecs.add(this.stringSpec(LOAD_PROFILE_OBIS_CODE_PROPERTY, false));
        propertySpecs.add(this.stringSpec(READ_LOAD_PROFILE_PROPERTY, false));
        propertySpecs.add(this.integerSpec(CORRECTTIME.getName(), false));
        propertySpecs.add(this.stringSpec(VERIFY_PROFILE_INTERVAL_PROPERTY, false));
        propertySpecs.add(this.stringSpec(SERIAL_NUMBER_A, false));
        propertySpecs.add(this.stringSpec(SERIAL_NUMBER_B, false));
        return propertySpecs;
    }

    @Override
    public void setProperties(TypedProperties properties) throws PropertyValidationException {
        super.setProperties(properties);
        setInfoTypeTimeoutProperty(Integer.parseInt(properties.getTypedProperty(PROP_TIMEOUT, "40000").trim()));
        setLoadProfileObisCode(ObisCode.fromString(properties.getTypedProperty(LOAD_PROFILE_OBIS_CODE_PROPERTY, "0.0.99.1.0.255")));
        readLoadProfile = "1".equals(properties.getTypedProperty(READ_LOAD_PROFILE_PROPERTY, "1"));
        correctTime = Integer.parseInt(properties.getTypedProperty(CORRECTTIME.getName(), "0"));
        verifyProfileInterval = Boolean.parseBoolean(properties.getTypedProperty(VERIFY_PROFILE_INTERVAL_PROPERTY, "false"));
        serialNumberA = properties.getTypedProperty(SERIAL_NUMBER_A);
        serialNumberB = properties.getTypedProperty(SERIAL_NUMBER_B);
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return "N/A";
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2013-06-10 10:15:31 +0200 (Mon, 10 Jun 2013) $";
    }

    @Override
    public Date getTime() throws IOException {
        // If we need to sync the time, then we need to request the RTC in the waveflow device in order to determine the shift.
        // However, if no timesync needs to be done, we're ok with the current RTC from the cached generic header.
        // we do this because we want to limit the roudtrips to the RF device
        if ((correctTime == 0) && (getCachedGenericHeader() != null)) {
            return cachedGenericHeader.getCurrentRTC();
        } else {
            return parameterFactory.readTimeDateRTC();
        }

    }

    final void forceSetTime() throws IOException {
        parameterFactory.writeTimeDateRTC(new Date());
    }

    final void setWaveFlowTime() throws IOException {
        parameterFactory.writeTimeDateRTC(new Date());
    }

    @Override
    public void setTime() throws IOException {
        if (correctTime > 0) {
            parameterFactory.writeTimeDateRTC(new Date());
            getLogger().warning("Datalogging is restarted because of the time sync!");
            restartDataLogging();
        }
    }

    public final void restartDataLogging() throws IOException {
        int om = parameterFactory.readOperatingMode(); // read current operating mode
        parameterFactory.writeOperatingMode(om & 0xFFF3, 0x000C); // disable logging
        parameterFactory.writeSamplingActivationNextHour();
        parameterFactory.writeOperatingMode(om, 0x000C); // enable logging
        getLogger().warning("Restart the datalogging, current operating mode is [" + WaveflowProtocolUtils.toHexString(om) + "]");

        //int om = parameterFactory.readOperatingMode();
        //parameterFactory.writeOperatingMode(0,0x000C);
        //parameterFactory.writeSamplingActivationNextHour();
        //parameterFactory.writeOperatingMode(0x0004,0x000C);
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
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        int portId;
        if (getLoadProfileObisCode().getD() == 1) {
            portId = 0; // port A
        } else if (getLoadProfileObisCode().getD() == 2) {
            portId = 1; // port B
        } else {
            portId = 2; // port A & B
        }
        try {
            return getTheProfileData(lastReading, portId, includeEvents);
        } catch (WaveFlow100mwEncoderException e) {
            getLogger().warning("No profile data available. Probably datalogging restarted...[" + e.getMessage() + "]");
            return null;
        }
    }

    @Override
    public void applyMessages(List messageEntries) throws IOException {
        waveFlow100mWMessages.applyMessages(messageEntries);
    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return waveFlow100mWMessages.queryMessage(messageEntry);
    }

    @Override
    public List getMessageCategories() {
        return waveFlow100mWMessages.getMessageCategories();
    }

    @Override
    public String writeMessage(Message msg) {
        return waveFlow100mWMessages.writeMessage(msg);
    }

    @Override
    public String writeTag(MessageTag tag) {
        return waveFlow100mWMessages.writeTag(tag);
    }

    @Override
    public String writeValue(MessageValue value) {
        return waveFlow100mWMessages.writeValue(value);
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

    public InternalData[] readInternalDatas() throws IOException {
        return getRadioCommandFactory().readInternalData().getInternalDatas();
    }


    @Override
    public int getNumberOfChannels() throws IOException {
        return WAVEFLOW_NR_OF_CHANNELS;
    }

    @Override
    public WaveFlowConnect getWaveFlowConnect() {
        return waveFlowConnect;
    }

    @Override
    public List map2MeterEvent(String event) throws IOException {
        AlarmFrameParser alarmFrame = new AlarmFrameParser(ProtocolUtils.convert2ascii(event.getBytes()), this);
        List statusAndEvents = new ArrayList();
        statusAndEvents.add(alarmFrame.getResponseACK());
        statusAndEvents.add(alarmFrame.getMeterEvents());
        return statusAndEvents;
    }

    @Override
    public void cacheRegisters(List<ObisCode> obisCodes) throws IOException {
        getLogger().info("Cache internal data reading (0x0B command) if not already done");
        readInternalDatas();
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