/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.mdc.protocol.api.UnsupportedException;

import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class TimeDurationInRxAndTx extends AbstractParameter {

    TimeDurationInRxAndTx(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private int timeRx = 0;
    private int timeTx = 0;

    public int getTimeRx() {
        return timeRx;
    }

    public int getTimeTx() {
        return timeTx;
    }

    @Override
    protected ParameterId getParameterId() throws WaveFlowException {
        return ParameterId.TimeDurationRxAndTx;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        byte[] rxBytes = ProtocolTools.getSubArray(data, 0, 2);
        rxBytes  = ProtocolTools.reverseByteArray(rxBytes);
        timeRx = ProtocolTools.getUnsignedIntFromBytes(rxBytes);

        byte[] txBytes = ProtocolTools.getSubArray(data, 2, 4);
        txBytes = ProtocolTools.reverseByteArray(txBytes);
        timeTx = ProtocolTools.getUnsignedIntFromBytes(txBytes);
    }

    @Override
    protected byte[] prepare() throws IOException {
        throw new UnsupportedException("Not allowed to write parameter 0xEA");
    }
}
