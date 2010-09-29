package com.energyict.genericprotocolimpl.elster.ctr.frame;

import com.energyict.genericprotocolimpl.elster.ctr.common.Field;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 29-sep-2010
 * Time: 16:46:07
 */
public abstract class AbstractCTRFrame implements Frame {

    private Field address;
    private Field profi;
    private Field functionCode;
    private Field structureCode;
    private Field channel;

    private Field cpa;
    private Field crc;

    @Override
    public String toString() {
        return getClass().getSimpleName() + " = " + ProtocolTools.getHexStringFromBytes(getBytes());
    }

}
