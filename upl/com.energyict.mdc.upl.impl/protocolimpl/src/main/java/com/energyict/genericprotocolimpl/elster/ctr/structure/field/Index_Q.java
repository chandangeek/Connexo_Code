package com.energyict.genericprotocolimpl.elster.ctr.structure.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;

/**
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:39:27
 */
public class Index_Q extends AbstractField<Index_Q> {

    public static final int LENGTH = 2;

    private int index_Q;

    public byte[] getBytes() {
        return getBytesFromInt(index_Q, LENGTH);
    }

    public Index_Q parse(byte[] rawData, int offset) throws CTRParsingException {
        this.index_Q = getIntFromBytes(rawData, offset, LENGTH);
        return this;
    }

    public int getIndex_Q() {
        return index_Q;
    }

    public void setIndex_Q(int index_Q) {
        this.index_Q = index_Q;
    }
}
