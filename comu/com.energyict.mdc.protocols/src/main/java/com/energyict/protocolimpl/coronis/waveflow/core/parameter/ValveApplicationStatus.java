/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

import java.io.IOException;

public class ValveApplicationStatus extends AbstractParameter {

    int status;

    ValveApplicationStatus(WaveFlow waveFlow) {
        super(waveFlow);
    }

    final int getStatus() {
        return status;
    }

    @Override
    protected ParameterId getParameterId() {
        return ParameterId.ValveApplicationStatus;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        status = ProtocolUtils.getInt(data, 0, 1);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) status};
    }

    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Resets a flag in the status byte.
     *
     * @param bit: zero based index of the bit that should be set to 0.
     */
    public void resetBit(int bit) {
        status = status & ~(0x01 << bit);
    }
}