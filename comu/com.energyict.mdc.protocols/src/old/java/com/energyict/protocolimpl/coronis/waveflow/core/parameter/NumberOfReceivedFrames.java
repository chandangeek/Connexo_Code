package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 5-apr-2011
 * Time: 14:13:24
 */
public class NumberOfReceivedFrames extends AbstractParameter {

    NumberOfReceivedFrames(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private int number = 0;

    public int getNumber() {
        return number;
    }

    @Override
    protected ParameterId getParameterId() throws WaveFlowException {
        return ParameterId.NumberOfReceivedFrames;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        number = ProtocolTools.getUnsignedIntFromBytes(data);
    }

    @Override
    protected byte[] prepare() throws IOException {
        throw new UnsupportedException("Not allowed to write parameter 0xE8");
    }
}