package com.energyict.protocolimpl.coronis.waveflow.hydreka;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.coronis.waveflow.core.CommonObisCodeMapper;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.messages.HydrekaMessages;
import com.energyict.protocolimpl.coronis.waveflow.core.messages.WaveFlowMessageParser;
import com.energyict.protocolimpl.coronis.waveflow.hydreka.parameter.ParameterFactoryHydreka;
import com.energyict.protocolimpl.coronis.waveflow.hydreka.radiocommand.RadioCommandFactoryHydreka;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 12/04/12
 * Time: 17:19
 */
public class Hydreka extends WaveFlow implements MessageProtocol {

    @Override
    public String getProtocolDescription() {
        return "Hydreka WaveFlow";
    }

    private ObisCodeMapperHydreka obisCodeMapperHydreka;
    private ParameterFactoryHydreka parameterFactory = null;
    private RadioCommandFactoryHydreka radioCommandFactory;
    private ProfileDataReader profileDataReader = null;

    @Inject
    public Hydreka(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    public List map2MeterEvent(String event) throws IOException {
        List statusAndEvents = new ArrayList();
        AlarmFrameParser alarmFrame = new AlarmFrameParser(this);
        alarmFrame.parse(ProtocolUtils.convert2ascii(event.getBytes()));
        statusAndEvents.add(alarmFrame.getResponseACK());
        statusAndEvents.add(alarmFrame.getMeterEvents());
        return statusAndEvents;
    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapperHydreka.getRegisterInfo(obisCode);
    }

    @Override
    protected void doTheInit() throws IOException {
        obisCodeMapperHydreka = new ObisCodeMapperHydreka(this);
        commonObisCodeMapper = new CommonObisCodeMapper(this);
        profileDataReader = new ProfileDataReader(this);
        parameterFactory = new ParameterFactoryHydreka(this);
        radioCommandFactory = new RadioCommandFactoryHydreka(this);
        setIsV1(false);
        waveFlowMessages = new HydrekaMessages(this);
        verifyProfileInterval = false;  //Don't read out the profile interval, it is not supported
    }

    @Override
    protected void doConnect() throws IOException {
        if ((getInitialRFCommand() == 0x27)) {
            getRadioCommandFactory().readDailyHydrekaDataReading();
        }
    }

    @Override
    public RadioCommandFactoryHydreka getRadioCommandFactory() {
        if (radioCommandFactory == null) {
            radioCommandFactory = new RadioCommandFactoryHydreka(this);
        }
        return radioCommandFactory;
    }

    @Override
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return 0;       //Fixed
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2013-01-11 11:48:08 +0100 (vr, 11 jan 2013) $";
    }

    /**
     * The Hydreka module doesn't support load profiles, so just return the interval that is configured in EiServer
     */
    public int getProfileInterval() throws UnsupportedException, IOException {
        return super.getProfileInterval();
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapperHydreka.getRegisterValue(obisCode);
    }

    @Override
    protected ProfileData getTheProfileData(Date lastReading, Date toDate, boolean includeEvents) throws UnsupportedException, IOException {
        return profileDataReader.readProfileData(lastReading, toDate, includeEvents);
    }

    @Override
    protected WaveFlowMessageParser getWaveFlowMessages() {
        if (waveFlowMessages == null) {
            waveFlowMessages = new HydrekaMessages(this);
        }
        return waveFlowMessages;
    }

    @Override
    public ParameterFactoryHydreka getParameterFactory() {
        if (parameterFactory == null) {
            parameterFactory = new ParameterFactoryHydreka(this);
        }
        return parameterFactory;
    }

}