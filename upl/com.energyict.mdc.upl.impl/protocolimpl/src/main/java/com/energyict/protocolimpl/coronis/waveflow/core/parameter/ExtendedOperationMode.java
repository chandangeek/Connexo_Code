package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

public class ExtendedOperationMode extends AbstractParameter {

    int status;

    public ExtendedOperationMode(WaveFlow waveFlow) {
        super(waveFlow);
    }

    public ExtendedOperationMode(WaveFlow waveFlow, int status) {
        super(waveFlow);
        this.status = status;
    }

    // The LSB of this status byte indicates the method of back flow detection.
    public final int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean usingVolumeMethodForBackFlowDetection() {
        return ((status & 0x01) == 0);
    }

    public boolean usingFlowRateMethodForBackFlowDetection() {
        return ((status & 0x01) == 1);
    }

    public boolean isPushFrameEnabled() {
        return ((status & 0x08) >> 3) == 1;
    }

    public void enablePushFrames() {
        status = status | 0x08; //Set bit4 to 1
    }

    public void enableVolumeMethod() {
        status = status & 0xFE; //Set bit0 to 0
    }

    public void enableFlowRateMethod() {
        status = status | 0x01; //Set bit0 to 1
    }

    @Override
    protected ParameterId getParameterId() {
        return ParameterId.ExtendedOperationMode;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        status = ProtocolUtils.getInt(data, 0, 1);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) status};
    }

    public void disablePushFrames() {
        status = status & 0xF7; //Set bit4 to 0
    }
}