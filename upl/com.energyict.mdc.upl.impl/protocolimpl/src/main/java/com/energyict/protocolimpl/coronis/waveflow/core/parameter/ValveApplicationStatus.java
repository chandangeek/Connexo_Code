package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

import java.io.IOException;

public class ValveApplicationStatus extends AbstractParameter {

    int status;

    ValveApplicationStatus(WaveFlow waveFlow) {
        super(waveFlow);
    }

    final int getStatus() {
        return status;
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.ValveApplicationStatus;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        status = ProtocolUtils.getInt(data, 0, 1);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) status};
    }
}