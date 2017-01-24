package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 1-mrt-2011
 * Time: 17:02:08
 */
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