package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.v210;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.AbstractRadioCommand;

import java.io.IOException;

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
    protected void parse(byte[] data) throws IOException {
        int offset = 0;

        for (int i = 0; i < 7; i++) {
            flow[i] = convertBCD(data, offset, 4, false);
            offset += 4;
        }
    }

    @Override
    protected byte[] prepare() throws IOException {
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