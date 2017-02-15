package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 7-apr-2011
 * Time: 16:45:00
 */
public class MeterModelParameter extends AbstractParameter {

    public MeterModelParameter(PropertySpecService propertySpecService, RTM rtm, NlsService nlsService) {
        super(propertySpecService, rtm, nlsService);
    }

    private int port = 1;
    private int meterModel = 0;

    public int getMeterModel() {
        return meterModel;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setMeterModel(int meterModel) {
        this.meterModel = meterModel;
    }

    @Override
    ParameterId getParameterId() {
        switch (port) {
            case 1:
                return ParameterId.MeterModelA;
            case 2:
                return ParameterId.MeterModelB;
            case 3:
                return ParameterId.MeterModelC;
            case 4:
                return ParameterId.MeterModelD;
            default:
                return ParameterId.MeterModelA;
        }
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        meterModel = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) meterModel};
    }
}