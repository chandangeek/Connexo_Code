package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

import java.io.IOException;

public class HourOfDailyIndexStorage extends AbstractParameter {

    HourOfDailyIndexStorage(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private int hour = 0;

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.HourOfDailyLogging;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        hour = WaveflowProtocolUtils.toInt(data[0]);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) getHour()};
    }
}