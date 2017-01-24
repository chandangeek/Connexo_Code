package com.energyict.protocolimpl.coronis.wavetherm.core.parameter;


import com.energyict.protocolimpl.coronis.wavetherm.WaveTherm;

import java.io.IOException;

public class LowThresholdAlarmDuration extends AbstractParameter {

    public LowThresholdAlarmDuration(WaveTherm waveTherm) {
        super(waveTherm);
    }

    @Override
    ParameterId getParameterId() {
        if (sensor == 1) {
            return ParameterId.LowThresholdAlarmDurationSensor1;
        } else {
            return ParameterId.LowThresholdAlarmDurationSensor2;
        }
    }

    private int duration;
    private int sensor = 1;

    public void setSensor(int sensor) {
        this.sensor = sensor;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        duration = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) duration};
    }
}