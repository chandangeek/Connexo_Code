package com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210;

import com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;
import com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.AbstractRadioCommand;

/**
 * Copyrights EnergyICT
 * Date: 16-mei-2011
 * Time: 12:05:50
 */
public class ReadCumulativeFlowDaily extends AbstractRadioCommand {

    public ReadCumulativeFlowDaily(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private int[] flow = new int[4];  //Flow values per daily segment.
                                      //Remark: unit is 1/1000th of the billable unit.
    public int[] getFlow() {
        return flow;
    }

    @Override
    protected void parse(byte[] data) {
        int offset = 0;

        for (int i = 0; i < 4; i++) {
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
        return RadioCommandId.ReadCumulativeFlowDaily;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ExtendedApplicativeCommand;
    }
}