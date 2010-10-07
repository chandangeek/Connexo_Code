package com.energyict.genericprotocolimpl.elster.ctr.structure.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;

/**
 * Copyrights EnergyICT
 * Date: 7-okt-2010
 * Time: 15:45:26
 */
public class NackReason extends AbstractField<NackReason> {

    public static final int LENGTH = 1;

    private int reason;

    public byte[] getBytes() {
        return getBytesFromInt(reason, LENGTH);
    }

    public NackReason parse(byte[] rawData, int offset) throws CTRParsingException {
        reason = getIntFromBytes(rawData, offset, LENGTH);
        return this;
    }

}
