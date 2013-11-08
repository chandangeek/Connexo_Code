package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand;

import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 1-apr-2011
 * Time: 15:49:53
 */
public class InitializeAlarmRoute extends AbstractRadioCommand {

    private int alarmMode = 0;

    public void setAlarmMode(int alarmMode) {
        this.alarmMode = alarmMode;
    }

    protected InitializeAlarmRoute(WaveFlow waveFlow) {
        super(waveFlow);
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        if ((data[0] & 0xFF) == 0xFF) {
            throw new WaveFlowException("Error initializing the alarm route");
        }
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) alarmMode};
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.InitializeAlarmRoute;
    }
}