package com.energyict.genericprotocolimpl.elster.ctr.structure.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;

/**
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:39:27
 */
public class PeriodTrace_C extends AbstractField<PeriodTrace_C> {

    public static final int LENGTH = 1;

    private int period;

    public byte[] getBytes() {
        return getBytesFromInt(period, LENGTH);
    }

    public PeriodTrace_C parse(byte[] rawData, int offset) throws CTRParsingException {
        this.period = getIntFromBytes(rawData, offset, LENGTH);
        return this;
    }

    public int getPeriod() {
        return period;
    }

    public String getDateFormat() {
        switch (period) {
            case 1:
                return "yy, mm, dd";
            case 2:
                return "yy, mm, dd";
            case 3:
                return "yy, mm, 00";
            default:
                return "";
        }
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public String getDescription() {
        switch (period) {
            case 1:
                return "All 1h traces on the specified day";
            case 2:
                return "The 1-day traces for the last 15 days (that specified included)";
            case 3:
                return "The 1-month traces for the last 12 months (that specified included)";
            default:
                return "";
        }
    }
}
