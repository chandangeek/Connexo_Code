package com.energyict.smartmeterprotocolimpl.nta.esmr50.common.loadprofiles;

import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.DSMRProfileIntervalStatusBits;

/**
 * Created by iulian on 8/17/2016.
 */
@Deprecated
public class ESMR5ProfileIntervalStatusBits extends DSMRProfileIntervalStatusBits {

    public ESMR5ProfileIntervalStatusBits() {
        super(false);
    }

    public ESMR5ProfileIntervalStatusBits(boolean ignoreDstStatusCode) {
        super(ignoreDstStatusCode);
    }
}
