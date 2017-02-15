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
 * Time: 10:25:00
 */
public class BatteryLowDetectionDate extends AbstractParameter {

    BatteryLowDetectionDate(PropertySpecService propertySpecService, RTM rtm, NlsService nlsService) {
        super(propertySpecService, rtm, nlsService);
    }

    private Date date;

    public Date getDate() {
        return date;
    }

    @Override
    ParameterId getParameterId() throws WaveFlowException {
        return ParameterId.BatteryLowDetectionDate;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        date = TimeDateRTCParser.parse(data, getRTM().getTimeZone()).getTime();
    }

    @Override
    protected byte[] prepare() throws IOException {
        throw new WaveFlowException("Not allowed to write this parameter");
    }
}