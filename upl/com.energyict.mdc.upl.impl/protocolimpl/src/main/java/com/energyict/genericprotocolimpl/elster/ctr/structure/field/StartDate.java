package com.energyict.genericprotocolimpl.elster.ctr.structure.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;

/**
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:39:27
 */
public class StartDate extends AbstractField<StartDate> {

    public static final int LENGTH = 4;

    private int startDate;

    public byte[] getBytes() {
        return getBytesFromInt(startDate, LENGTH);
    }

    public StartDate parse(byte[] rawData, int offset) throws CTRParsingException {
        this.startDate = getIntFromBytes(rawData, offset, LENGTH);
        return this;
    }

    public int getStartDate() {
        return startDate;
    }

    public void setStartDate(int startDate) {
        this.startDate = startDate;
    }
}
