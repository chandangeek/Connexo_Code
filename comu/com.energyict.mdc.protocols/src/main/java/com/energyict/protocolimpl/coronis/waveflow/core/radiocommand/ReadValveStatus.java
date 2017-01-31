/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

import java.io.IOException;

public class ReadValveStatus extends AbstractRadioCommand {

    private int status;

    protected ReadValveStatus(WaveFlow waveFlow) {
        super(waveFlow);
    }

    public int getStatus() {
        return status;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        status = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[0];
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ReadValveStatus;
    }
}