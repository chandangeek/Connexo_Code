package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

import java.io.IOException;

public class DataLoggingMinuteOfMeasurement extends AbstractParameter {

    public DataLoggingMinuteOfMeasurement(WaveFlow waveFlow) {
        super(waveFlow);
    }
    private int minuteOfMeasurement;

    @Override
    protected ParameterId getParameterId() {
        return ParameterId.MinuteOfMeasurement;
    }

    public void setMinuteOfMeasurement(int minuteOfMeasurement) {
        this.minuteOfMeasurement = minuteOfMeasurement;
    }

    public int getMinuteOfMeasurement() {
        return minuteOfMeasurement;
    }

    @Override
    public void parse(byte[] data) throws IOException {
        minuteOfMeasurement = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) minuteOfMeasurement};
    }
}