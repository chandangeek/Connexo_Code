package com.energyict.protocolimpl.coronis.wavetherm.core.parameter;


import com.energyict.protocolimpl.coronis.wavetherm.WaveTherm;

import java.io.IOException;

public class DetectionMeasurementPeriod extends AbstractParameter {

    private int minutes;

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getMinutes() {
        return minutes;
    }

    public DetectionMeasurementPeriod(WaveTherm waveTherm) {
        super(waveTherm);
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.DetectionMeasurementPeriod;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        minutes = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) minutes};
    }
}