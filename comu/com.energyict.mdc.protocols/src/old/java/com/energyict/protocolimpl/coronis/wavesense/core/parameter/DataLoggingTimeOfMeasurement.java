package com.energyict.protocolimpl.coronis.wavesense.core.parameter;

import com.energyict.protocolimpl.coronis.wavesense.WaveSense;

import java.io.IOException;

public class DataLoggingTimeOfMeasurement extends AbstractParameter {

    public DataLoggingTimeOfMeasurement(WaveSense waveSense) {
        super(waveSense);
    }
    private int timeOfMeasurement;

    @Override
    ParameterId getParameterId() {
        return ParameterId.TimeOfMeasurement;
    }

    public void setTimeOfMeasurement(int timeOfMeasurement) {
        this.timeOfMeasurement = timeOfMeasurement;
    }

    public int getTimeOfMeasurement() {
        return timeOfMeasurement;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        timeOfMeasurement = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) timeOfMeasurement};
    }
}