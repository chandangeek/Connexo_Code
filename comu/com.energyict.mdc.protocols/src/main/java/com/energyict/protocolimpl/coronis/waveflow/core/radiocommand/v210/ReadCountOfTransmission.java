/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.v210;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.AbstractRadioCommand;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class ReadCountOfTransmission extends AbstractRadioCommand {

    public ReadCountOfTransmission(WaveFlow waveFlow) {
        super(waveFlow);
    }

    int count;

    public int getCount() {
        return count;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        count = ProtocolTools.getUnsignedIntFromBytes(data, 0, 4);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[0];
    }

    @Override
    protected RadioCommandId getSubCommandId() {
        return RadioCommandId.ReadCountOfTransmission;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ExtendedApplicativeCommand;
    }
}