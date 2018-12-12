package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

public class WakeUpPeriodForTimeWindow2 extends AbstractParameter {

    int value;

    WakeUpPeriodForTimeWindow2(WaveFlow waveFlow) {
        super(waveFlow);
    }

    @Override
    protected ParameterId getParameterId() {
        return ParameterId.WakeUpPeriodForTimeWindow2;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        value = ProtocolUtils.getInt(data, 0, 1);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) value};
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}