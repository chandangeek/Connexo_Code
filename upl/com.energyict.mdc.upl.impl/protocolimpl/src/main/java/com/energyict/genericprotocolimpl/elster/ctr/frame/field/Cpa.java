package com.energyict.genericprotocolimpl.elster.ctr.frame.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;

/**
 * Copyrights EnergyICT
 * Date: 29-sep-2010
 * Time: 17:23:46
 */
public class Cpa extends AbstractField<Cpa> {

    public static final int LENGTH = 4;

    private int cpa;

    public byte[] getBytes() {
        return getBytesFromInt(cpa, LENGTH);
    }

    public Cpa parse(byte[] rawData, int offset) {
        cpa = getIntFromBytes(rawData, offset, LENGTH);
        return this;
    }

    public int getLength() {
        return LENGTH;
    }

}
