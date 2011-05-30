package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

import java.io.IOException;

public class ApplicationStatus extends AbstractParameter {

    int status;

    ApplicationStatus(WaveFlow waveFlow) {
        super(waveFlow);
    }

    final int getStatus() {
        return status;
    }

    final void setStatus(int status) {
        this.status = status;
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.ApplicationStatus;
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
