package com.energyict.genericprotocolimpl.elster.ctr.temp.structures.fields;

import com.energyict.genericprotocolimpl.elster.ctr.common.Field;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 13-aug-2010
 * Time: 10:46:58
 */
public abstract class AbstractStructureField implements Field {

    @Override
    public String toString() {
        return getClass().getSimpleName() + " = " + ProtocolTools.getHexStringFromBytes(getBytes());
    }

}
