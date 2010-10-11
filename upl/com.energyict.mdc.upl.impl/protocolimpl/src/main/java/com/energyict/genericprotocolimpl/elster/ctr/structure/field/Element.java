package com.energyict.genericprotocolimpl.elster.ctr.structure.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;

/**
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:39:27
 */
public class Element extends AbstractField<Element> {

    public static final int LENGTH = 1;

    private int element;

    public byte[] getBytes() {
        return getBytesFromInt(element, LENGTH);
    }

    public Element parse(byte[] rawData, int offset) throws CTRParsingException {
        this.element = getIntFromBytes(rawData, offset, LENGTH);
        return this;
    }

    public int getElement() {
        return element;
    }

    public void setElement(int element) {
        this.element = element;
    }
}
