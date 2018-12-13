package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 11-apr-2011
 * Time: 16:23:06
 */
public class MeasurementPeriodMultiplier extends AbstractParameter {

    MeasurementPeriodMultiplier(PropertySpecService propertySpecService, RTM rtm, NlsService nlsService) {
        super(propertySpecService, rtm, nlsService);
    }

    private int multiplier;

    public int getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    ParameterId getParameterId() throws WaveFlowException {
        return ParameterId.MeasurementPeriodMultiplier;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        multiplier = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) multiplier};
    }
}