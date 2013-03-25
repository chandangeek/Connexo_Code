package com.energyict.protocolimpl.coronis.waveflow.hydreka.parameter;

import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.AbstractParameter;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 17/12/12
 * Time: 11:50
 * Author: khe
 */
public class RTCResynchPeriod extends AbstractParameter {

    private int period;

    public RTCResynchPeriod(WaveFlow waveFlow) {
        super(waveFlow);
        hydrekaOnly = true;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    @Override
    protected AbstractParameter.ParameterId getParameterId() throws WaveFlowException {
        return AbstractParameter.ParameterId.RTCResynchPeriod;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        period = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) period};
    }
}