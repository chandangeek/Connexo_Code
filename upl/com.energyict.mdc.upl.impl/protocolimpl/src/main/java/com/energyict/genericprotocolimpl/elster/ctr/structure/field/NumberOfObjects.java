package com.energyict.genericprotocolimpl.elster.ctr.structure.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;

/**
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:39:27
 */
public class NumberOfObjects extends AbstractField<NumberOfObjects> {

    private int numberOfObjects;

    public NumberOfObjects() {
        this(0);
    }

    public NumberOfObjects(int numberOfObjects) {
        this.numberOfObjects = numberOfObjects;
    }

    public int getLength() {
        return 1;
    }

    public byte[] getBytes() {
        return getBytesFromInt(numberOfObjects, getLength());
    }

    public NumberOfObjects parse(byte[] rawData, int offset) throws CTRParsingException {
        this.numberOfObjects = getIntFromBytes(rawData, offset, getLength());
        return this;
    }

    public int getNumberOfObjects() {
        return numberOfObjects;
    }

    public void setNumberOfObjects(int numberOfObjects) {
        this.numberOfObjects = numberOfObjects;
    }

}
