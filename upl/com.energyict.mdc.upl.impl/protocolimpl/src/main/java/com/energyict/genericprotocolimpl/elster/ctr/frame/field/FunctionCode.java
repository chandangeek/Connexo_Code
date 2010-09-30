package com.energyict.genericprotocolimpl.elster.ctr.frame.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;

/**
 * Copyrights EnergyICT
 * Date: 30-sep-2010
 * Time: 9:29:10
 */
public class FunctionCode extends AbstractField<FunctionCode> {

    public static final int LENGTH = 1;
    private int functionCode;

    public byte[] getBytes() {
        return getBytesFromInt(functionCode, LENGTH);
    }

    public FunctionCode parse(byte[] rawData, int offset) {
        functionCode = getIntFromBytes(rawData, offset, LENGTH);
        return this;
    }

}
