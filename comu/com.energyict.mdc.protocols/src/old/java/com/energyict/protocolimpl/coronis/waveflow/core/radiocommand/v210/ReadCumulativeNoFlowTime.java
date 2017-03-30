/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.v210;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.AbstractRadioCommand;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class ReadCumulativeNoFlowTime extends AbstractRadioCommand {

    public ReadCumulativeNoFlowTime(WaveFlow waveFlow) {
        super(waveFlow);
    }

    int days;         //Range: 0 - 9999

    public int getDays() {
        return days;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        days = ProtocolTools.getUnsignedIntFromBytes(data, 0, 2);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[0];
    }

    @Override
    protected RadioCommandId getSubCommandId() {
        return RadioCommandId.ReadCumulativeNoFlowTime;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ExtendedApplicativeCommand;
    }
}
