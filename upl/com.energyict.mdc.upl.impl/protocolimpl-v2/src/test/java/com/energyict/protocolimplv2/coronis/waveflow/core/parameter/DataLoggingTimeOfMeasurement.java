package com.energyict.protocolimplv2.coronis.waveflow.core.parameter;

import com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

public class DataLoggingTimeOfMeasurement extends AbstractParameter {

    public DataLoggingTimeOfMeasurement(WaveFlow waveFlow) {
        super(waveFlow);
    }
    private int timeOfMeasurement;

    @Override
    protected ParameterId getParameterId() {
        return ParameterId.TimeOfMeasurement;
    }

    public void setTimeOfMeasurement(int timeOfMeasurement) {
        this.timeOfMeasurement = timeOfMeasurement;
    }

    public int getTimeOfMeasurement() {
        return timeOfMeasurement;
    }

    @Override
    public void parse(byte[] data) {
        timeOfMeasurement = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() {
        return new byte[]{(byte) timeOfMeasurement};
    }
}