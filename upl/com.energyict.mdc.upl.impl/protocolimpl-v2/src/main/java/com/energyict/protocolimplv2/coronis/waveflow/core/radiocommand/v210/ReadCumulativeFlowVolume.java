package com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210;

import com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;
import com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.AbstractRadioCommand;

/**
 * Copyrights EnergyICT
 * Date: 16-mei-2011
 * Time: 13:16:28
 */
public class ReadCumulativeFlowVolume extends AbstractRadioCommand {

    public ReadCumulativeFlowVolume(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private int[] flow = new int[7];        //Flow per band
                                            //Unit is 1/1000th of billable unit.

    public int[] getFlow() {
        return flow;
    }

    public int getFlow(int period) {
        return flow[period];
    }

    @Override
    protected void parse(byte[] data) {
        int offset = 0;

        for (int i = 0; i < 7; i++) {
            flow[i] = convertBCD(data, offset, 4, false);
            offset += 4;
        }
    }

    @Override
    protected byte[] prepare() {
        return new byte[0];
    }

    @Override
    protected RadioCommandId getSubCommandId() {
        return RadioCommandId.ReadCumulativeFlowVolume;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ExtendedApplicativeCommand;
    }
}