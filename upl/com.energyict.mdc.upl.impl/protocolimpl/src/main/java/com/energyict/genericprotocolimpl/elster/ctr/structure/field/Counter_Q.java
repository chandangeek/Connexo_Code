package com.energyict.genericprotocolimpl.elster.ctr.structure.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;

/**
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:39:27
 */
public class Counter_Q extends AbstractField<Counter_Q> {

    public static final int LENGTH = 1;

    private int counter_Q;

    public byte[] getBytes() {
        return getBytesFromInt(counter_Q, LENGTH);
    }

    public Counter_Q parse(byte[] rawData, int offset) throws CTRParsingException {
        this.counter_Q = getIntFromBytes(rawData, offset, LENGTH);
        return this;
    }

    public int getCounter_Q() {
        return counter_Q;
    }

    public void setCounter_Q(int counter_Q) {
        this.counter_Q = counter_Q;
    }
}
