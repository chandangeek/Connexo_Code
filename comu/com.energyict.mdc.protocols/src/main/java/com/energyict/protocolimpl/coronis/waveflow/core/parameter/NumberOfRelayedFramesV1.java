/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.mdc.protocol.api.UnsupportedException;

import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.waveflow.core.ParameterType;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class NumberOfRelayedFramesV1 extends AbstractParameter {

    NumberOfRelayedFramesV1(WaveFlow waveFlow) {
        super(waveFlow);
        parameterType = ParameterType.WaveFlowV1_433MHz;
    }

    private int number = 0;

    public int getNumber() {
        return number;
    }

    @Override
    protected ParameterId getParameterId() throws WaveFlowException {
        return ParameterId.NumberOfRelayedFramesV1;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        number = ProtocolTools.getUnsignedIntFromBytes(data);
    }

    @Override
    protected byte[] prepare() throws IOException {
        throw new UnsupportedException("Not allowed to write parameter 0xE9");
    }
}