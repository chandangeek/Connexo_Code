package test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand;

import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

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
    protected void parse(byte[] data) {
        //not used
    }

    @Override
    protected byte[] prepare() {
        return new byte[]{(byte) status};
    }
}