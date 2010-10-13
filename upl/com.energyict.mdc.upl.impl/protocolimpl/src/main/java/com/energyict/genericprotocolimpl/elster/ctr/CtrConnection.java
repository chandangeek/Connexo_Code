package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRConnectionException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.Frame;

/**
 * Copyrights EnergyICT
 * Date: 13-okt-2010
 * Time: 10:27:24
 */
public interface CtrConnection<F extends Frame> {

    F sendFrameGetResponse(F frame) throws CTRConnectionException;

}
