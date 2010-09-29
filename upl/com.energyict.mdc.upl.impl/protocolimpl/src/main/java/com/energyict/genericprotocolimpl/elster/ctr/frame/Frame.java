package com.energyict.genericprotocolimpl.elster.ctr.frame;

import com.energyict.genericprotocolimpl.elster.ctr.common.Field;

/**
 * Copyrights EnergyICT
 * Date: 27-sep-2010
 * Time: 8:32:29
 */
public interface Frame extends Field {

    void parse(byte[] rawFrame, int offset);

}
