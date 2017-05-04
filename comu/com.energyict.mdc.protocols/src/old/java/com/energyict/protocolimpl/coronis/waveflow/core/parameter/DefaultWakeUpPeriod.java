/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

import java.io.IOException;

public class DefaultWakeUpPeriod extends AbstractParameter {

    int value;

    DefaultWakeUpPeriod(WaveFlow waveFlow) {
        super(waveFlow);
    }

    @Override
    protected ParameterId getParameterId() {
        return ParameterId.DefaultWakeUpPeriod;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        value = ProtocolUtils.getInt(data, 0, 1);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) value};
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}