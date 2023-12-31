package test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand;

import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

/**
 * Copyrights EnergyICT
 * Date: 1-mrt-2011
 * Time: 17:02:08
 */
public class CleanWaterValveCommand extends AbstractRadioCommand {

    private boolean success = false;

    protected CleanWaterValveCommand(WaveFlow waveFlow) {
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
        return new byte[]{(byte) 0x02};                             //Writing byte = 0x02 equals cleaning the water valve.
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ControlWaterValve;
    }
}