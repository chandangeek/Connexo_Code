package com.energyict.protocolimpl.coronis.wavetherm.core.parameter;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.coronis.wavetherm.WaveTherm;

import java.io.IOException;

public class NumberOfRepeaters extends AbstractParameter {

    int number;

    NumberOfRepeaters(WaveTherm waveTherm) {
        super(waveTherm);
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.NumberOfRepeaters;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        number = ProtocolUtils.getInt(data, 0, 1);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) number};
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
