package test.com.energyict.protocolimplv2.coronis.waveflow.core.parameter;

import com.energyict.protocolimpl.utils.ProtocolTools;
import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

public class StartTimeForTimeWindow2 extends AbstractParameter {

    int value;

    StartTimeForTimeWindow2(WaveFlow waveFlow) {
        super(waveFlow);
    }

    @Override
    protected ParameterId getParameterId() {
        return ParameterId.StartTimeForTimeWindow2;
    }

    @Override
    protected void parse(byte[] data) {
        value = ProtocolTools.getIntFromBytes(data, 0, 1);
    }

    @Override
    protected byte[] prepare() {
        return new byte[]{(byte) value};
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}