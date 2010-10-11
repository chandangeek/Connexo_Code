package com.energyict.genericprotocolimpl.elster.ctr.structure.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;

/**
 * Copyrights EnergyICT
 * Date: 7-okt-2010
 * Time: 15:45:26
 */
public class AckCode extends AbstractField<AckCode> {

    public static final int LENGTH = 1;

    private int ackCode;

    public byte[] getBytes() {
        return getBytesFromInt(ackCode, LENGTH);
    }

    public AckCode parse(byte[] rawData, int offset) throws CTRParsingException {
        ackCode = getIntFromBytes(rawData, offset, LENGTH);
        return this;
    }

}
