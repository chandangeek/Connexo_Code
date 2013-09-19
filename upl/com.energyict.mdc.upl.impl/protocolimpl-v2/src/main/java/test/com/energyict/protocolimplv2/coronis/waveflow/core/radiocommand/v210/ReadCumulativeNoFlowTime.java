package test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210;

import com.energyict.protocolimpl.utils.ProtocolTools;
import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.AbstractRadioCommand;

/**
 * Copyrights EnergyICT
 * Date: 16-mei-2011
 * Time: 11:52:00
 */
public class ReadCumulativeNoFlowTime extends AbstractRadioCommand {

    public ReadCumulativeNoFlowTime(WaveFlow waveFlow) {
        super(waveFlow);
    }

    int days;         //Range: 0 - 9999

    public int getDays() {
        return days;
    }

    @Override
    protected void parse(byte[] data) {
        days = ProtocolTools.getUnsignedIntFromBytes(data, 0, 2);
    }

    @Override
    protected byte[] prepare() {
        return new byte[0];
    }

    @Override
    protected RadioCommandId getSubCommandId() {
        return RadioCommandId.ReadCumulativeNoFlowTime;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ExtendedApplicativeCommand;
    }
}
