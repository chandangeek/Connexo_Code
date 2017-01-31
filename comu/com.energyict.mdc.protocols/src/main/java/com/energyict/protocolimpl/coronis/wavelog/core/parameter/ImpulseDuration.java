/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavelog.core.parameter;


import com.energyict.protocolimpl.coronis.wavelog.WaveLog;

import java.io.IOException;

public class ImpulseDuration extends AbstractParameter {

    public ImpulseDuration(WaveLog waveLog) {
        super(waveLog);
    }

    public ImpulseDuration(WaveLog waveLog, int input) {
        super(waveLog);
        this.input = input;
    }

    @Override
    ParameterId getParameterId() {
        switch (input) {
            case 1:
                return ParameterId.ImpulseDurationOutput1;
            case 2:
                return ParameterId.ImpulseDurationOutput2;
            case 3:
                return ParameterId.ImpulseDurationOutput3;
            case 4:
                return ParameterId.ImpulseDurationOutput4;
            default:
                return ParameterId.ImpulseDurationOutput1;
        }
    }

    private int duration;
    private int input;

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