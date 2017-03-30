/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavelog.core.radiocommand;

import com.energyict.protocolimpl.coronis.wavelog.WaveLog;

import java.io.IOException;

public class ResetEventTable extends AbstractRadioCommand {

    protected ResetEventTable(WaveLog waveLog) {
        super(waveLog);
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[0];
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ResetEventTable;
    }
}
