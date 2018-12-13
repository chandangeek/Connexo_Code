package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.IOException;

public class BackflowBeforeIndication extends AbstractParameter {

    public BackflowBeforeIndication(PropertySpecService propertySpecService, RTM rtm, NlsService nlsService) {
        super(propertySpecService, rtm, nlsService);
    }

    public BackflowBeforeIndication(PropertySpecService propertySpecService, RTM rtm, int input, NlsService nlsService) {
        super(propertySpecService, rtm, nlsService);
        this.input = input;
    }

    private int input = 1; //1 = A, 2 = B
    private int number;

    /**
     * This is the number of back flow presences before indication of real detection
     */
    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }


    @Override
    ParameterId getParameterId() throws WaveFlowException {
        switch (input) {
            case 1:
                return ParameterId.BackflowDetectionBeforeIndicationA;
            case 2:
                return ParameterId.BackflowDetectionBeforeIndicationB;
        }
        throw new WaveFlowException("Module doesn't support back flow detection.");
    }

    @Override
    public void parse(byte[] data) throws IOException {
        number = WaveflowProtocolUtils.toInt(data[0]); //1 byte long.
    }

    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) getNumber()};
    }
}