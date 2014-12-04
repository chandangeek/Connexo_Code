package com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

/**
 * Class for the NumberOfElements field in a CTR Structure Object
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:39:27
 */
public class NumberOfElements extends AbstractField<NumberOfElements> {

    private int numberOfElements;

    public NumberOfElements() {
        this(0);
    }

    public int getLength() {
        return 1;
    }

    public NumberOfElements(int numberOfElements) {
        this.numberOfElements = numberOfElements;
    }

    public byte[] getBytes() {
        return getBytesFromInt(numberOfElements, getLength());
    }

    public NumberOfElements parse(byte[] rawData, int offset) throws CTRParsingException {
        this.numberOfElements = getIntFromBytes(rawData, offset, getLength());
        return this;
    }

    public int getNumberOfElements() {
        return numberOfElements;
    }

    public void setNumberOfElements(int element) {
        this.numberOfElements = element;
    }
}
