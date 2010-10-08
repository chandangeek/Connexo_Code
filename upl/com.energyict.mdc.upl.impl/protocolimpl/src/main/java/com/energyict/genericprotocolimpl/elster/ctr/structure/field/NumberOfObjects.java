package com.energyict.genericprotocolimpl.elster.ctr.structure.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;

/**
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:39:27
 */
public class NumberOfObjects extends AbstractField<NumberOfObjects> {

    public static final int LENGTH = 1;

    private int numberOfObjects;

    public byte[] getBytes() {
        return getBytesFromInt(numberOfObjects, LENGTH);
    }

    public NumberOfObjects parse(byte[] rawData, int offset) throws CTRParsingException {
        this.numberOfObjects = getIntFromBytes(rawData, offset, LENGTH);
        return this;
    }

    public int getNumberOfObjects() {
        return numberOfObjects;
    }

    public void setNumberOfObjects(int numberOfObjects) {
        this.numberOfObjects = numberOfObjects;
    }
}
