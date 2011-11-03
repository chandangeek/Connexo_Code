package com.energyict.protocolimpl.coronis.waveflow.waveflowV1;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.coronis.waveflow.core.CommonObisCodeMapper;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.messages.WaveFlowMessageParser;
import com.energyict.protocolimpl.coronis.waveflow.core.messages.WaveFlowV1Messages;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.ParameterFactory;

import java.io.IOException;
import java.util.Date;

public class WaveFlowV1 extends WaveFlow implements MessageProtocol {

    /**
     * specific obis code mapper
     */
    private ObisCodeMapper obisCodeMapper;

    /**
     * read and build the profiledata
     */
    private ProfileDataReaderV1 profileDataReader;

    /**
     * The common obis code mapper for the waveflow pulse (Waveflow V1)
     */
    private CommonObisCodeMapper commonObisCodeMapper = null;

    /**
     * The parameter factory interface
     */
    private ParameterFactory parameterFactory = null;

    @Override
    protected void doTheInit() throws IOException {
        obisCodeMapper = new ObisCodeMapper(this);
        profileDataReader = new ProfileDataReaderV1(this);
        commonObisCodeMapper = new CommonObisCodeMapper(this);
        parameterFactory = new ParameterFactory(this);
        setIsV1(true);     //Boolean indicating this is the V1 protocol, using the legacy v1 commands.
        waveFlowMessages = new WaveFlowV1Messages(this);
    }

    protected WaveFlowMessageParser getWaveFlowMessages() {
        if (waveFlowMessages == null) {
            waveFlowMessages = new WaveFlowV1Messages(this);
        }
        return waveFlowMessages;
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

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapper.getRegisterValue(obisCode);
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
        if (parameterFactory == null) {
            parameterFactory = new ParameterFactory(this);
        }
        return parameterFactory;
    }

    public BubbleUpObject parseBubbleUpData(byte[] data) throws IOException {
        throw new UnsupportedException("Waveflow V1 doesn't support the bubble up mechanism");
    }
}