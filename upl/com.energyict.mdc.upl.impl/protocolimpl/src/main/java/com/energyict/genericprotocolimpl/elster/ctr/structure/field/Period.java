package com.energyict.genericprotocolimpl.elster.ctr.structure.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;

/**
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:39:27
 */
public class Period extends AbstractField<Period> {

    public static final int LENGTH = 1;

    private int period;

    public byte[] getBytes() {
        return getBytesFromInt(period, LENGTH);
    }

    public Period parse(byte[] rawData, int offset) throws CTRParsingException {
        this.period = getIntFromBytes(rawData, offset, LENGTH);
        return this;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public String getDescription() {
        switch (period) {
            case 1: return "Quarters of an hour on the day specified";
            case 2: return "The hours on the day specified";
            case 3: return "The daily records on the day specified";
            case 4: return "Monthly records from the month specified";
            case 5: return "Annual records from the given year";
            default: return "";
        }
    }

}
