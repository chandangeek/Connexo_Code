package com.energyict.protocolimpl.coronis.waveflow.waveflowV1;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.protocolimpl.coronis.waveflow.core.CommonObisCodeMapper;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.messages.WaveFlowMessageParser;
import com.energyict.protocolimpl.coronis.waveflow.core.messages.WaveFlowV1Messages;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.ParameterFactory;

import javax.inject.Inject;
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

    @Inject
    public WaveFlowV1(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    protected void doTheInit() throws IOException {
        obisCodeMapper = new ObisCodeMapper(this);
        profileDataReader = new ProfileDataReaderV1(this);
        int numberOfInputs = calcNumberOfInputs();
        profileDataReader.setNumberOfInputs(numberOfInputs);
        getLogger().info("Module has " + numberOfInputs + " channel(s), based on the pulseweight properties");
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

    @Override
    public String getProtocolVersion() {
        return "$Date: 2013-03-21 10:44:10 +0100 (do, 21 mrt 2013) $";
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapper.getRegisterValue(obisCode);
    }

    @Override
    protected ProfileData getTheProfileData(Date lastReading, Date toDate, boolean includeEvents) throws UnsupportedException, IOException {
        return profileDataReader.getProfileData(lastReading, toDate, includeEvents);
    }

}