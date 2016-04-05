package com.energyict.protocolimpl.coronis.waveflow.waveflowV2;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.BubbleUpObject;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.coronis.waveflow.core.BubbleUpFrameParser;
import com.energyict.protocolimpl.coronis.waveflow.core.CommonObisCodeMapper;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.messages.WaveFlowMessageParser;
import com.energyict.protocolimpl.coronis.waveflow.core.messages.WaveFlowV2Messages;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.ParameterFactory;

import java.io.IOException;
import java.util.Date;

public class WaveFlowV2 extends WaveFlow implements MessageProtocol {

    private ObisCodeMapper obisCodeMapper;
    private ProfileDataReader profileDataReader;

    @Override
    protected void doTheInit() throws IOException {
        obisCodeMapper = new ObisCodeMapper(this);
        profileDataReader = new ProfileDataReader(this);
        int numberOfInputs = calcNumberOfInputs();
        profileDataReader.setNumberOfInputsUsed(numberOfInputs);
        getLogger().info("Module has " + numberOfInputs + " channel(s), based on the pulseweight properties");
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
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    @Override
    protected ProfileData getTheProfileData(Date lastReading, Date toDate, boolean includeEvents) throws UnsupportedException, IOException {
        return profileDataReader.getProfileData(lastReading, toDate, includeEvents);
    }

    public BubbleUpObject parseBubbleUpData(byte[] data) throws IOException {
        return BubbleUpFrameParser.parse(data, this);
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }
}