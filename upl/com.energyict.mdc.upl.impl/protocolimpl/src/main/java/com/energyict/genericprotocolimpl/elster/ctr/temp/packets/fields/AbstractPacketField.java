package com.energyict.genericprotocolimpl.elster.ctr.temp.packets.fields;

import com.energyict.genericprotocolimpl.elster.ctr.common.Field;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 12-aug-2010
 * Time: 15:06:57
 */
public abstract class AbstractPacketField implements Field {

    @Override
    public String toString() {
        return getClass().getSimpleName() + " = " + ProtocolTools.getHexStringFromBytes(getBytes());
    }
    
}
