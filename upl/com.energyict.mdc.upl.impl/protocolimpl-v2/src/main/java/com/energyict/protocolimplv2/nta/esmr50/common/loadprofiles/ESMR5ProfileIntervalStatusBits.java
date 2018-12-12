package com.energyict.protocolimplv2.nta.esmr50.common.loadprofiles;


import com.energyict.protocolimplv2.nta.abstractnta.DSMRProfileIntervalStatusBits;

/**
 * Created by iulian on 8/17/2016.
 */
public class ESMR5ProfileIntervalStatusBits extends DSMRProfileIntervalStatusBits {

    public ESMR5ProfileIntervalStatusBits() {
        super(false);
    }

    public ESMR5ProfileIntervalStatusBits(boolean ignoreDstStatusCode) {
        super(ignoreDstStatusCode);
    }
}
