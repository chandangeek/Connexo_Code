package com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

/**
 * Class for the NumberOfObjects field in a CTR Structure Object
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
