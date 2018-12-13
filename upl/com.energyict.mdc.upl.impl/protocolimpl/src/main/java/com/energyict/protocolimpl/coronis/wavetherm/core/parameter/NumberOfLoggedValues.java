package com.energyict.protocolimpl.coronis.wavetherm.core.parameter;

import com.energyict.mdc.upl.UnsupportedException;

import com.energyict.protocolimpl.coronis.wavetherm.WaveTherm;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 29-mrt-2011
 * Time: 14:57:50
 */
public class NumberOfLoggedValues extends AbstractParameter {

    NumberOfLoggedValues(WaveTherm waveTherm) {
        super(waveTherm);
    }

    private int number;

    public int getNumber() {
        return number;
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.NumberOfLoggedValues;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        byte[] bytes = ProtocolTools.getSubArray(data, 0, 2);
        number = ProtocolTools.getUnsignedIntFromBytes(ProtocolTools.reverseByteArray(bytes));      //It is sent LSB first!
    }

    @Override
    protected byte[] prepare() throws IOException {
        throw new UnsupportedException();
    }
}