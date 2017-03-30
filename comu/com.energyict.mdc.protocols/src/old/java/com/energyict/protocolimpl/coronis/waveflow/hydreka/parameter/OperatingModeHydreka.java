/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.hydreka.parameter;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.AbstractParameter;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.OperatingMode;

import java.io.IOException;

public class OperatingModeHydreka extends OperatingMode {

    public OperatingModeHydreka(WaveFlow waveFlow) {
        super(waveFlow);
    }

    @Override
    protected AbstractParameter.ParameterId getParameterId() {
        return null;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        operationMode = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) operationMode};
    }
}