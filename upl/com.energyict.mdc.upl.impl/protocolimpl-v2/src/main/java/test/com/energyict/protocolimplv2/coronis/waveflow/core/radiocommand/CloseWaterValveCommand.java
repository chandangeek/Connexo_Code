package test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand;

import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

/**
 * Copyrights EnergyICT
 * Date: 1-mrt-2011
 * Time: 17:02:01
 */
public class CloseWaterValveCommand extends AbstractRadioCommand {

    private boolean success = false;

    protected CloseWaterValveCommand(WaveFlow waveFlow) {
        super(waveFlow);
    }

    public boolean isSuccess() {
        return success;
    }

    @Override
    protected void parse(byte[] data) {
        success = (data[0] == 0x00);
    }

    @Override
    protected byte[] prepare() {
        return new byte[]{(byte) 0x01};                             //Writing byte = 0x01 equals closing the water valve.
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ControlWaterValve;
    }
}