/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.mdc.protocol.api.UnsupportedException;

import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

import java.io.IOException;

public class NumberOfFrameInRxAndTx extends AbstractParameter {

    NumberOfFrameInRxAndTx(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private int numberOfFrameRx = 0;
    private int numberOfFrameTx = 0;

    public int getNumberOfFrameTx() {
        return numberOfFrameTx;
    }

    public int getNumberOfFrameRx() {
        return numberOfFrameRx;
    }

    @Override
    protected ParameterId getParameterId() throws WaveFlowException {
        return ParameterId.NumberOfFrameRxAndTx;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        numberOfFrameRx = data[0] & 0xFF;
        numberOfFrameTx = data[1] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        throw new UnsupportedException("Not allowed to write parameter 0xEB");
    }
}
