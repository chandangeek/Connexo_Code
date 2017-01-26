package com.energyict.protocolimpl.coronis.waveflow.hydreka;

import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.BubbleUpObject;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.coronis.waveflow.core.CommonObisCodeMapper;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.messages.HydrekaMessages;
import com.energyict.protocolimpl.coronis.waveflow.core.messages.WaveFlowMessageParser;
import com.energyict.protocolimpl.coronis.waveflow.hydreka.parameter.ParameterFactoryHydreka;
import com.energyict.protocolimpl.coronis.waveflow.hydreka.radiocommand.RadioCommandFactoryHydreka;
import com.energyict.protocolimpl.utils.ProtocolUtils;

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

    private ObisCodeMapperHydreka obisCodeMapperHydreka;
    private ParameterFactoryHydreka parameterFactory = null;
    private RadioCommandFactoryHydreka radioCommandFactory;
    private ProfileDataReader profileDataReader = null;

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

    @Override
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
    public int getNumberOfChannels() throws IOException {
        return 0;       //Fixed
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: Tue Nov 8 16:41:03 2016 +0100 $";
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapperHydreka.getRegisterValue(obisCode);
    }

    @Override
    protected ProfileData getTheProfileData(Date lastReading, Date toDate, boolean includeEvents) throws IOException {
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

    @Override
    public BubbleUpObject parseBubbleUpData(byte[] data) throws IOException {
        return BubbleUpFrameParser.parseFrame(data, this);
    }

    @Override
    public void applyMessages(List messageEntries) throws IOException {
        getWaveFlowMessages().applyMessages(messageEntries);
    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return getWaveFlowMessages().queryMessage(messageEntry);
    }

    @Override
    public List getMessageCategories() {
        return getWaveFlowMessages().getMessageCategories();
    }

    @Override
    public String writeMessage(Message msg) {
        return getWaveFlowMessages().writeMessage(msg);
    }

    @Override
    public String writeTag(MessageTag tag) {
        return getWaveFlowMessages().writeTag(tag);
    }

    @Override
    public String writeValue(MessageValue value) {
        return getWaveFlowMessages().writeValue(value);
    }

}