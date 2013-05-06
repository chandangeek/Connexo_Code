package com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex.profiles;

import com.energyict.protocolimpl.base.ProfileIntervalStatusBits;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.profiles.LGLoadProfileBuilder;

/**
 * @author sva
 * @since 3/05/13 - 17:01
 */
public class XemexLoadProfileBuilder extends LGLoadProfileBuilder {

    /**
     * Default constructor
     *
     * @param meterProtocol the {@link #meterProtocol}
     */
    public XemexLoadProfileBuilder(AbstractSmartNtaProtocol meterProtocol) {
        super(meterProtocol);
    }

    @Override
    public ProfileIntervalStatusBits getProfileIntervalStatusBits() {
        return new XemexDSMRProfileIntervalStatusBits();
    }
}