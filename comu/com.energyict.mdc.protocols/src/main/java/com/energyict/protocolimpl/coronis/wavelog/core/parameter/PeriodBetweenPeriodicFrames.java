package com.energyict.protocolimpl.coronis.wavelog.core.parameter;

import com.energyict.protocolimpl.coronis.wavelog.WaveLog;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class PeriodBetweenPeriodicFrames extends AbstractParameter {

    public PeriodBetweenPeriodicFrames(WaveLog waveLog) {
        super(waveLog);
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.PeriodicFramePeriod;
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
        time = ProtocolTools.getUnsignedIntFromBytes(data, 0, 2);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return ProtocolTools.getBytesFromInt(time, 2);
    }
}