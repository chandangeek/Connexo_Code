package com.energyict.protocolimpl.coronis.wavelog.core.parameter;

import com.energyict.protocolimpl.coronis.wavelog.WaveLog;

import java.io.IOException;

public class TimeBetweenPeriodicRetries extends AbstractParameter {

    public TimeBetweenPeriodicRetries(WaveLog waveLog) {
        super(waveLog);
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.TimeBetweenPeriodicFrameRetries;
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