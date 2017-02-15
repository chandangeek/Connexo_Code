package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;

import java.io.IOException;
import java.util.Date;

public class BackflowDetectionDate extends AbstractParameter {

    public BackflowDetectionDate(PropertySpecService propertySpecService, RTM rtm, NlsService nlsService) {
        super(propertySpecService, rtm, nlsService);
    }

    public BackflowDetectionDate(PropertySpecService propertySpecService, RTM rtm, int input, NlsService nlsService) {
        super(propertySpecService, rtm, nlsService);
        this.input = input;
    }

    private int input = 1; //1 = A, 2 = B,...
    private Date date;

    public Date getDate() {
        return date;
    }

    public void setPort(int port) {
        this.input = port;
    }

    @Override
    ParameterId getParameterId() throws WaveFlowException {
        switch (input) {
            case 1:
                return ParameterId.BackflowDetectionDateA;
            case 2:
                return ParameterId.BackflowDetectionDateB;
        }
        throw new WaveFlowException("Module doesn't support back flow detection.");
    }

    @Override
    public void parse(byte[] data) throws IOException {
        date = TimeDateRTCParser.parse(data, 0, 7, getRTM().getTimeZone()).getTime();
    }

    protected byte[] prepare() throws IOException {
        throw new WaveFlowException("Not allowed to write this parameter.");
    }
}