package com.energyict.protocolimpl.coronis.waveflow.waveflowV210;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.coronis.waveflow.core.CommonObisCodeMapper;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.messages.WaveFlowMessageParser;
import com.energyict.protocolimpl.coronis.waveflow.core.messages.WaveFlowV210Messages;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.ParameterFactory;

import java.io.IOException;
import java.util.Date;

public class WaveFlowV210 extends WaveFlow implements MessageProtocol {

    private ObisCodeMapper obisCodeMapper;
    private ProfileDataReaderV210 profileDataReader;
    private CommonObisCodeMapper commonObisCodeMapper = null;
    private ParameterFactory parameterFactory = null;

    @Override
    protected void doTheInit() throws IOException {
        obisCodeMapper = new ObisCodeMapper(this);
        profileDataReader = new ProfileDataReaderV210(this);
        commonObisCodeMapper = new CommonObisCodeMapper(this);
        parameterFactory = new ParameterFactory(this);
        setIsV1(true);     //Boolean indicating this is the V1 protocol, using the legacy v1 commands.
        setIsV210(true);
        waveFlowMessages = new WaveFlowV210Messages(this);
    }

    public WaveFlowMessageParser getWaveFlowMessages() {
        if (waveFlowMessages == null) {
            waveFlowMessages = new WaveFlowV210Messages(this);
        }
        return waveFlowMessages;
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapper.getRegisterValue(obisCode);
    }

    /**
     * Override this method to provide meter specific info for an obiscode mapped register. This method is called outside the communication session. So the info provided is static info in the protocol.
     *
     * @param obisCode obiscode of the register to lookup
     * @return RegisterInfo object
     * @throws java.io.IOException thrown when somethiong goes wrong
     */
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
        throw new UnsupportedException("Waveflow V210 doesn't support the bubble up mechanism");
    }
}