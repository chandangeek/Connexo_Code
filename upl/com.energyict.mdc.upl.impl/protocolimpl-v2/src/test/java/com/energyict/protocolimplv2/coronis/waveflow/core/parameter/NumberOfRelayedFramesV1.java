package com.energyict.protocolimplv2.coronis.waveflow.core.parameter;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;
import com.energyict.protocolimplv2.coronis.waveflow.core.ParameterType;

/**
 * Copyrights EnergyICT
 * Date: 5-apr-2011
 * Time: 14:13:24
 */
public class NumberOfRelayedFramesV1 extends AbstractParameter {

    NumberOfRelayedFramesV1(WaveFlow waveFlow) {
        super(waveFlow);
        parameterType = ParameterType.WaveFlowV1_433MHz;
    }

    private int number = 0;

    public int getNumber() {
        return number;
    }

    @Override
    protected ParameterId getParameterId() {
        return ParameterId.NumberOfRelayedFramesV1;
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