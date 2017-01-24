package com.energyict.protocolimpl.coronis.waveflow.hydreka.parameter;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.AbstractParameter;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.OperatingMode;

import java.io.IOException;

/**
 * Bit 4: tamper detection
 * Bit 5: leakage detection
 * Bit 6: probe RTC resynch
 * Bit 7: low battery detection
 * <p/>
 * <p/>
 * Copyrights EnergyICT
 * Date: 17/12/12
 * Time: 11:50
 * Author: khe
 */
public class OperatingModeHydreka extends OperatingMode {

    public OperatingModeHydreka(WaveFlow waveFlow) {
        super(waveFlow);
    }

    @Override
    protected AbstractParameter.ParameterId getParameterId() {
        return null;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        operationMode = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) operationMode};
    }
}