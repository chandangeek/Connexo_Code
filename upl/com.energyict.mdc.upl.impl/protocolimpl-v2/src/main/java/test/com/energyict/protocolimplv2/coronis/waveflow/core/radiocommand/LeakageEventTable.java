package test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand;

import com.energyict.protocolimpl.utils.ProtocolTools;
import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

public class LeakageEventTable extends AbstractRadioCommand {

    LeakageEvent[] leakageEvents = new LeakageEvent[5];

    public final LeakageEvent[] getLeakageEvents() {
        return leakageEvents;
    }

    LeakageEventTable(WaveFlow waveFlow) {
        super(waveFlow);
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.LeakageEventTable;
    }

    @Override
    protected void parse(byte[] data) {
        int offset = 0;
        for (int i = 0; i < leakageEvents.length; i++) {
            int status = data[offset++] & 0xFF;
            int consumptionRate = ProtocolTools.getIntFromBytes(data, offset, 2);
            offset += 2;
            leakageEvents[i] = new LeakageEvent(status, consumptionRate, ProtocolTools.getSubArray(data, offset, 6), getWaveFlow());
            offset += 6;
        }
    }

    @Override
    protected byte[] prepare() {
        return new byte[0];
    }
}