package com.energyict.protocolimpl.coronis.wavesense.core.parameter;


import com.energyict.protocolimpl.coronis.wavesense.WaveSense;

import java.io.IOException;

public class DetectionMeasurementPeriod extends AbstractParameter {

    public DetectionMeasurementPeriod(WaveSense waveSense) {
        super(waveSense);
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.DetectionMeasurementPeriod;
    }

    private int minutes;

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
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