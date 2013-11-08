package com.energyict.protocolimpl.coronis.wavelog.core.radiocommand;

import com.energyict.protocolimpl.coronis.wavelog.WaveLog;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 1-apr-2011
 * Time: 15:49:53
 */
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
