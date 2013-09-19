package test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand;

import com.energyict.protocolimpl.utils.ProtocolTools;
import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

public class CurrentIndexReading extends AbstractRadioCommand {

    public CurrentIndexReading(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private int[] readings;  // indexes for input A and B

    public final int[] getReadings() {
        return readings;
    }

    public final int getReadings(int i) {
        return readings[i];
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.CurrentIndexReading;
    }

    @Override
    public void parse(byte[] data) {
        readings = new int[2];
        operationMode = data[0] & 0xFF;
        applicationStatus = data[1] & 0xFF;
        int offset = 2;            //Skip the operation mode and application status

        readings[0] = ProtocolTools.getIntFromBytes(data, offset, 4);           //The indexes are signed values!
        offset += 4;

        readings[1] = ProtocolTools.getIntFromBytes(data, offset, 4);
    }

    protected byte[] prepare() {
        return new byte[0];
    }
}