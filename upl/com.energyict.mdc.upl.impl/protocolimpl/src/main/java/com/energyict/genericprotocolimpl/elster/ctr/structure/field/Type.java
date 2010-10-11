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

    public static final int LENGTH = 1;

    private int type;

    public byte[] getBytes() {
        return getBytesFromInt(type, LENGTH);
    }

    public Type parse(byte[] rawData, int offset) throws CTRParsingException {
        this.type = getIntFromBytes(rawData, offset, LENGTH);
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
