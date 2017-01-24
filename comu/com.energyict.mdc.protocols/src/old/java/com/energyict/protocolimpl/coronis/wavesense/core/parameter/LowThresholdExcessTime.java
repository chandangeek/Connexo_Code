package com.energyict.protocolimpl.coronis.wavesense.core.parameter;


import com.energyict.protocolimpl.coronis.wavesense.WaveSense;

import java.io.IOException;

public class LowThresholdExcessTime extends AbstractParameter {

    public LowThresholdExcessTime(WaveSense waveSense) {
        super(waveSense);
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.LowThresholdExcessTime;
    }

    private int time;

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        time = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) time};
    }
}