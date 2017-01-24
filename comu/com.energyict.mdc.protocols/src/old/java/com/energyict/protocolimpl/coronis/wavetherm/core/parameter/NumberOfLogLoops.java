package com.energyict.protocolimpl.coronis.wavetherm.core.parameter;

import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.protocolimpl.coronis.wavetherm.WaveTherm;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 29-mrt-2011
 * Time: 14:57:50
 */
public class NumberOfLogLoops extends AbstractParameter {

    NumberOfLogLoops(WaveTherm waveTherm) {
        super(waveTherm);
    }

    private int loops;

    public int getLoops() {
        return loops;
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.NumberOfLogLoops;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        loops = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        throw new UnsupportedException();
    }
}