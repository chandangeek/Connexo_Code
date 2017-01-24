package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

import java.io.IOException;

public class AcknowledgeAlarmFrame extends AbstractRadioCommand {

    AcknowledgeAlarmFrame(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private int status;

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.AcknowledgeAlarmFrame;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        //not used
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) status};
    }
}