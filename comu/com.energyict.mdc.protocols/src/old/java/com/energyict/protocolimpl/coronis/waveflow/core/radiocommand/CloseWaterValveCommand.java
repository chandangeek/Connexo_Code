/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

import java.io.IOException;

public class CloseWaterValveCommand extends AbstractRadioCommand {

    private boolean success = false;

    protected CloseWaterValveCommand(WaveFlow waveFlow) {
        super(waveFlow);
    }

    public boolean isSuccess() {
        return success;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        success = (data[0] == 0x00);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) 0x01};                             //Writing byte = 0x01 equals closing the water valve.
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ControlWaterValve;
    }
}