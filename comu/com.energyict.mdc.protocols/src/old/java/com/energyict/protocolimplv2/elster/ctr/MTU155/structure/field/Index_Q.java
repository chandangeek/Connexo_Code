package com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

/**
 * Class for the Index_Q field in a CTR Structure Object
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:39:27
 */
public class Index_Q extends AbstractField<Index_Q> {

    private int index_Q;

    public Index_Q() {
        this(0);
    }

    public Index_Q(int index_Q) {
        this.index_Q = index_Q < 0 ? 0 : index_Q;
    }

    public int getLength() {
        return 2;
    }

    public byte[] getBytes() {
        return getBytesFromInt(index_Q, getLength());
    }

    public Index_Q parse(byte[] rawData, int offset) throws CTRParsingException {
        this.index_Q = getIntFromBytes(rawData, offset, getLength());
        return this;
    }

    public int getIndex_Q() {
        return index_Q;
    }

    public void setIndex_Q(int index_Q) {
        this.index_Q = index_Q;
    }
}
