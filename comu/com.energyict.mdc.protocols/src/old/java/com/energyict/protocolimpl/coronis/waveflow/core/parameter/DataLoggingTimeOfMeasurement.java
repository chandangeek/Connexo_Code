/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

import java.io.IOException;

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
    public void parse(byte[] data) throws IOException {
        timeOfMeasurement = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) timeOfMeasurement};
    }
}