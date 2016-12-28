package com.energyict.protocolimpl.coronis.waveflow.waveflowV210;

import com.energyict.mdc.upl.UnsupportedException;
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
import com.energyict.protocolimpl.coronis.waveflow.core.messages.WaveFlowMessageParser;
import com.energyict.protocolimpl.coronis.waveflow.core.messages.WaveFlowV210Messages;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.ParameterFactory;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class WaveFlowV210 extends WaveFlow implements MessageProtocol {

    private ObisCodeMapper obisCodeMapper;
    private ProfileDataReaderV210 profileDataReader;

    public WaveFlowV210(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    protected void doTheInit() throws IOException {
        obisCodeMapper = new ObisCodeMapper(this);
        profileDataReader = new ProfileDataReaderV210(this);
        int numberOfInputs = calcNumberOfInputs();
        profileDataReader.setNumberOfInputs(numberOfInputs);
        getLogger().info("Module has " + numberOfInputs + " channel(s), based on the pulseweight properties");
        commonObisCodeMapper = new CommonObisCodeMapper(this);
        parameterFactory = new ParameterFactory(this);
        setIsV1(true);     //Boolean indicating this is the V1 protocol, using the legacy v1 commands.
        setIsV210(true);
        waveFlowMessages = new WaveFlowV210Messages(this);
    }

    @Override
    public WaveFlowMessageParser getWaveFlowMessages() {
        if (waveFlowMessages == null) {
            waveFlowMessages = new WaveFlowV210Messages(this);
        }
        return waveFlowMessages;
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapper.getRegisterValue(obisCode);
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2013-03-21 10:44:10 +0100 (do, 21 mrt 2013) $";
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    @Override
    protected ProfileData getTheProfileData(Date lastReading, Date toDate, boolean includeEvents) throws IOException {
        return profileDataReader.getProfileData(lastReading, toDate, includeEvents);
    }

    @Override
    public BubbleUpObject parseBubbleUpData(byte[] data) throws IOException {
        throw new UnsupportedException("Waveflow V210 doesn't support the bubble up mechanism");
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