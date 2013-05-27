package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.v210;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.AbstractRadioCommand;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 16-mei-2011
 * Time: 11:46:52
 */
public class ReadCurrentFlowRate extends AbstractRadioCommand {

    public ReadCurrentFlowRate(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private int currentFlowRate;    //Range 0 � 9.999.999

    public int getCurrentFlowRate() {
        return currentFlowRate;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        currentFlowRate = convertBCD(data, true);
    }

    @Override
    protected byte[] prepare() throws IOException {
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