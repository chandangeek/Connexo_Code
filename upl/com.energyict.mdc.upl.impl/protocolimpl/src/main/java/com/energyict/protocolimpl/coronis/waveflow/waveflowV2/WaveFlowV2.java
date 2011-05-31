package com.energyict.protocolimpl.coronis.waveflow.waveflowV2;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.coronis.waveflow.core.*;
import com.energyict.protocolimpl.coronis.waveflow.core.messages.WaveFlowMessageParser;
import com.energyict.protocolimpl.coronis.waveflow.core.messages.WaveFlowV2Messages;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.ParameterFactory;

import java.io.IOException;
import java.util.Date;

public class WaveFlowV2 extends WaveFlow implements MessageProtocol {

    private ObisCodeMapper obisCodeMapper;
    private ProfileDataReader profileDataReader;
    private CommonObisCodeMapper commonObisCodeMapper = null;
    private ParameterFactory parameterFactory = null;

    @Override
    protected void doTheInit() throws IOException {
        obisCodeMapper = new ObisCodeMapper(this);
        profileDataReader = new ProfileDataReader(this);
        commonObisCodeMapper = new CommonObisCodeMapper(this);
        parameterFactory = new ParameterFactory(this);
        setIsV1(false);
        waveFlowMessages = new WaveFlowV2Messages(this);
    }

    public WaveFlowMessageParser getWaveFlowMessages() {
        if (waveFlowMessages == null) {
            waveFlowMessages = new WaveFlowV2Messages(this);
        }
        return waveFlowMessages;
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapper.getRegisterValue(obisCode);
    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapper.getRegisterInfo(obisCode);
    }

    @Override
    protected ProfileData getTheProfileData(Date lastReading, Date toDate, boolean includeEvents) throws UnsupportedException, IOException {
        return profileDataReader.getProfileData(lastReading, toDate, includeEvents);
    }

    @Override
    public CommonObisCodeMapper getCommonObisCodeMapper() {
        return commonObisCodeMapper;
    }

    @Override
    public ParameterFactory getParameterFactory() {
        return parameterFactory;
    }

    public BubbleUpObject parseBubbleUpData(byte[] data) throws IOException {
        return BubbleUpFrameParser.parse(data, this);
    }
}