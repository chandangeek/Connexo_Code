package com.energyict.genericprotocolimpl.elster.ctr.structure.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;

/**
 * Copyrights EnergyICT
 * Date: 7-okt-2010
 * Time: 15:45:26
 */
public class NackReason extends AbstractField<NackReason> {

    private int reason;

    public NackReason() {
        this(0);
    }

    public NackReason(int reason) {
        this.reason = reason;
    }

    public int getLength() {
        return 1;
    }

    public byte[] getBytes() {
        return getBytesFromInt(reason, getLength());
    }

    public NackReason parse(byte[] rawData, int offset) throws CTRParsingException {
        reason = getIntFromBytes(rawData, offset, getLength());
        return this;
    }

}
