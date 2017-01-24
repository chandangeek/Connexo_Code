package com.energyict.protocolimpl.coronis.wavelog.core.parameter;


import com.energyict.protocolimpl.coronis.wavelog.WaveLog;

import java.io.IOException;

public class NumberOfRetries extends AbstractParameter {

    public NumberOfRetries(WaveLog waveLog) {
        super(waveLog);
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.NumberOfRetriesAlarmFrames;
    }

    private int number;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        number = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) number};
    }
}