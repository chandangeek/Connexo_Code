package com.energyict.genericprotocolimpl.elster.ctr.frame.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;

/**
 * Copyrights EnergyICT
 * Date: 30-sep-2010
 * Time: 9:29:10
 */
public class StructureCode extends AbstractField<StructureCode> {

    public static final int LENGTH = 1;
    private int structureCode;

    public byte[] getBytes() {
        return getBytesFromInt(structureCode, LENGTH);
    }

    public StructureCode parse(byte[] rawData, int offset) {
        structureCode = getIntFromBytes(rawData, offset, LENGTH);
        return this;
    }

}
