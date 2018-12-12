package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.utils.ProtocolUtils;

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
    protected ParameterId getParameterId() {
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

    /**
     * Resets a flag in the status byte.
     *
     * @param bit: zero based index of the bit that should be set to 0.
     */
    public void resetBit(int bit) {
        status = status & ~(0x01 << bit);
    }
}