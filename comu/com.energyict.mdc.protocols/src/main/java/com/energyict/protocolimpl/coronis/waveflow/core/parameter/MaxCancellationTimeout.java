/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

import java.io.IOException;

public class MaxCancellationTimeout extends AbstractParameter {

    int seconds;

    MaxCancellationTimeout(WaveFlow waveFlow) {
        super(waveFlow);
    }

    final int getSeconds() {
        return seconds;
    }

    final void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    @Override
    protected ParameterId getParameterId() {
        return ParameterId.MaxCancelTimeout;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        seconds = ProtocolUtils.getInt(data, 0, 1);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) seconds};
    }
}
