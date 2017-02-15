/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.hydreka.parameter;

import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.waveflow.core.ParameterType;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.AbstractParameter;

import java.io.IOException;

public class ReadingHourHistogram extends AbstractParameter {

    private int hour;

    public ReadingHourHistogram(WaveFlow waveFlow) {
        super(waveFlow);
        parameterType = ParameterType.Hydreka;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    @Override
    protected AbstractParameter.ParameterId getParameterId() throws WaveFlowException {
        return AbstractParameter.ParameterId.ReadingHourHistogram;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        hour = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) hour};
    }
}