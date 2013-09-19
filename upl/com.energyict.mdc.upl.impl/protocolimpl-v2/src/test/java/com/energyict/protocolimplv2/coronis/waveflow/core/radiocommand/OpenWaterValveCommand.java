package com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand;

import com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

/**
 * Copyrights EnergyICT
 * Date: 1-mrt-2011
 * Time: 16:27:00
 */
public class OpenWaterValveCommand extends AbstractRadioCommand {

    protected OpenWaterValveCommand(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private boolean success = false;

    public boolean isSuccess() {
        return success;
    }

    @Override
    protected void parse(byte[] data) {
        success = (data[0] == 0x00);
    }

    @Override
    protected byte[] prepare() {
        return new byte[]{(byte) 0x00};                             //Writing byte = 0x00 equals opening the water valve.
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ControlWaterValve;
    }
}