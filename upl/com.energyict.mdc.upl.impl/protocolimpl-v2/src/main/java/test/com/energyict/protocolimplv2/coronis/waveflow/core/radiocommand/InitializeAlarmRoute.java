package test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand;

import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

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
    protected void parse(byte[] data) {
        if ((data[0] & 0xFF) == 0xFF) {
            throw createWaveFlowException("Error initializing the alarm route");
        }
    }

    @Override
    protected byte[] prepare() {
        return new byte[]{(byte) alarmMode};
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.InitializeAlarmRoute;
    }
}