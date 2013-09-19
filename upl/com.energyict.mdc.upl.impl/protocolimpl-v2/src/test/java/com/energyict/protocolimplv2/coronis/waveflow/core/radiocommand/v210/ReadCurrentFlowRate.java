package com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210;

import com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;
import com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.AbstractRadioCommand;

/**
 * Copyrights EnergyICT
 * Date: 16-mei-2011
 * Time: 11:46:52
 */
public class ReadCurrentFlowRate extends AbstractRadioCommand {

    public ReadCurrentFlowRate(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private int currentFlowRate;    //Range 0 - 9.999.999

    public int getCurrentFlowRate() {
        return currentFlowRate;
    }

    @Override
    protected void parse(byte[] data) {
        currentFlowRate = convertBCD(data, true);
    }

    @Override
    protected byte[] prepare() {
        return new byte[0];
    }

    @Override
    protected RadioCommandId getSubCommandId() {
        return RadioCommandId.ReadCurrentFlowRate;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ExtendedApplicativeCommand;
    }
}