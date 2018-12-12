package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.IOException;

public class BackflowDetectionPeriod extends AbstractParameter {

    public BackflowDetectionPeriod(PropertySpecService propertySpecService, RTM rtm, NlsService nlsService) {
        super(propertySpecService, rtm, nlsService);
    }

    public BackflowDetectionPeriod(PropertySpecService propertySpecService, RTM rtm, int input, NlsService nlsService) {
        super(propertySpecService, rtm, nlsService);
        this.input = input;
    }

    private int input = 1; //1 = A, 2 = B
    private int detectionPeriod;

    /**
     * Expressed in multiples of the profile data interval.
     * Time necessary to cause a back flow event.
     *
     * @return
     */
    public int getDetectionPeriod() {
        return detectionPeriod;
    }

    public void setDetectionPeriod(int detectionPeriod) {
        this.detectionPeriod = detectionPeriod;
    }

    @Override
    ParameterId getParameterId() throws WaveFlowException {
        switch (input) {
            case 1:
                return ParameterId.BackflowDetectionPeriodA;
            case 2:
                return ParameterId.BackflowDetectionPeriodB;
        }
        throw new WaveFlowException("Module doesn't support back flow detection.");
    }

    @Override
    public void parse(byte[] data) throws IOException {
        detectionPeriod = WaveflowProtocolUtils.toInt(data[0]);
    }


    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) getDetectionPeriod()};
    }
}