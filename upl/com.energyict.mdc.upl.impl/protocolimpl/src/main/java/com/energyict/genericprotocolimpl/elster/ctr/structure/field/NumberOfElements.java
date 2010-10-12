package com.energyict.genericprotocolimpl.elster.ctr.structure.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;

/**
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:39:27
 */
public class NumberOfElements extends AbstractField<NumberOfElements> {

    public static final int LENGTH = 1;

    private int numberOfElements;

    public byte[] getBytes() {
        return getBytesFromInt(numberOfElements, LENGTH);
    }

    public NumberOfElements parse(byte[] rawData, int offset) throws CTRParsingException {
        this.numberOfElements = getIntFromBytes(rawData, offset, LENGTH);
        return this;
    }

    public int getNumberOfElements() {
        return numberOfElements;
    }

    public void setNumberOfElements(int element) {
        this.numberOfElements = element;
    }
}
