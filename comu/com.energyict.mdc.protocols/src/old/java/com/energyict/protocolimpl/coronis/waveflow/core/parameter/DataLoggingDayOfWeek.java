/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

import java.io.IOException;

public class DataLoggingDayOfWeek extends AbstractParameter {

    public DataLoggingDayOfWeek(WaveFlow waveFlow) {
        super(waveFlow);
    }

    @Override
    protected ParameterId getParameterId() {
        return ParameterId.DayOfWeek;
    }

    private int dayOfWeek;

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    @Override
    public void parse(byte[] data) throws IOException {
        dayOfWeek = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) dayOfWeek};
    }
}