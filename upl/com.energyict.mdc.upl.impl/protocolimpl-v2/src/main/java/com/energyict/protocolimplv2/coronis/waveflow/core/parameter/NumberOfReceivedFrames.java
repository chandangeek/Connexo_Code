package com.energyict.protocolimplv2.coronis.waveflow.core.parameter;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

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
    protected ParameterId getParameterId() {
        return ParameterId.NumberOfReceivedFrames;
    }

    @Override
    protected void parse(byte[] data) {
        number = ProtocolTools.getUnsignedIntFromBytes(data);
    }

    @Override
    protected byte[] prepare() {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(getClass(), "prepare");
    }
}