package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;

import java.io.IOException;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 11-apr-2011
 * Time: 10:18:24
 */
public class ReadingErrorDetectionDate extends AbstractParameter {

    private int port;
    private Date date;

    ReadingErrorDetectionDate(PropertySpecService propertySpecService, RTM rtm, int port, NlsService nlsService) {
        super(propertySpecService, rtm, nlsService);
        this.port = port;
    }

    @Override
    ParameterId getParameterId() throws WaveFlowException {
        if (port == 1) {
            return ParameterId.ReadingErrorDetectionDateA;
        } else {
            return ParameterId.ReadingErrorDetectionDateB;
        }
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        date = TimeDateRTCParser.parse(data, getRTM().getTimeZone()).getTime();
    }

    @Override
    protected byte[] prepare() throws IOException {
        throw new WaveFlowException("Not allowed to write this parameter");
    }

    public Date getDate() {
        return date;
    }
}