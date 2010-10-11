package com.energyict.genericprotocolimpl.elster.ctr.structure.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;

/**
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:39:27
 */
public class Coda extends AbstractField<Coda> {

    public static final int LENGTH = 2;

    private int coda;

    public byte[] getBytes() {
        return getBytesFromInt(coda, LENGTH);
    }

    public Coda parse(byte[] rawData, int offset) throws CTRParsingException {
        this.coda = getIntFromBytes(rawData, offset, LENGTH);
        return this;
    }

    public int getCoda() {
        return coda;
    }

    public void setCoda(int coda) {
        this.coda = coda;
    }
}
