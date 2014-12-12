package com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

/**
 * Class for the Coda field in a CTR Structure Object
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:39:27
 */
public class Coda extends AbstractField<Coda> {

    private int coda;

    public Coda() {
        this(0);
    }

    public Coda(int coda) {
        this.coda = coda;
    }

    public int getLength() {
        return 2;
    }

    public byte[] getBytes() {
        return getBytesFromInt(coda, getLength());
    }

    public Coda parse(byte[] rawData, int offset) throws CTRParsingException {
        this.coda = getIntFromBytes(rawData, offset, getLength());
        return this;
    }

    public int getCoda() {
        return coda;
    }

    public void setCoda(int coda) {
        this.coda = coda;
    }
}
