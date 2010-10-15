package com.energyict.genericprotocolimpl.elster.ctr.structure.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;

/**
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:39:27
 * Contains the used format in the data array. E.g.: qlf (1 byte), val (128 bytes)
 */
public class Type extends AbstractField<Type> {

    private int type;

    public Type() {
        this(0);
    }

    public Type(int type) {
        this.type = type;
    }

    public int getLength() {
        return 1;
    }

    public byte[] getBytes() {
        return getBytesFromInt(type, getLength());
    }

    public Type parse(byte[] rawData, int offset) throws CTRParsingException {
        this.type = getIntFromBytes(rawData, offset, getLength());
        return this;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int[] getDataStructure() {
        switch (type) {
            case 1:
                return new int[]{1, 5, 128};
            case 2:
                return new int[]{1, 128};
            case 3:
                return new int[]{128};
            case 4:
                return new int[]{5, 128};
            default:
                return new int[]{128};
        }

    }
}
