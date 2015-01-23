package com.energyict.protocolimplv2.nta.dsmr50.elster.am540.profiles;

import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.dsmr40.landisgyr.profiles.LGLoadProfileBuilder;

/**
 * Provides functionality to fetch and create ProfileData objects for the AM540 protocol
 *
 * @author sva
 * @since 23/01/2015 - 14:16
 */
public class AM540LoadProfileBuilder extends LGLoadProfileBuilder {

    /**
     * Default constructor
     *
     * @param meterProtocol the {@link #meterProtocol}
     */
    public AM540LoadProfileBuilder(AbstractDlmsProtocol meterProtocol) {
        super(meterProtocol);
    }
}