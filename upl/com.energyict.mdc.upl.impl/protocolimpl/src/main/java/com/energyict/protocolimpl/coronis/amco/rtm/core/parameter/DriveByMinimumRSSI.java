package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 7-apr-2011
 * Time: 16:45:00
 */
public class DriveByMinimumRSSI extends AbstractParameter {

    DriveByMinimumRSSI(PropertySpecService propertySpecService, RTM rtm, NlsService nlsService) {
        super(propertySpecService, rtm, nlsService);
    }

    private int minimumRSSI;

    public int getMinimumRSSI() {
        return minimumRSSI;
    }

    public void setMinimumRSSI(int minimumRSSI) {
        this.minimumRSSI = minimumRSSI;
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.DriveByMinimumRSSI;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        minimumRSSI = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        throw new UnsupportedException("Cannot set this parameter");
    }
}
