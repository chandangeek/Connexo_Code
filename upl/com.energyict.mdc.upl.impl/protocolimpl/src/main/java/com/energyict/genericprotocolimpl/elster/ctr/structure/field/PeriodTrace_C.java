package com.energyict.genericprotocolimpl.elster.ctr.structure.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;

/**
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:39:27
 */
public class PeriodTrace_C extends AbstractField<PeriodTrace_C> {

    private int period;
    private static final int SEC_PER_MIN = 60;
    private static final int SEC_PER_HOUR = SEC_PER_MIN * 60;

    public PeriodTrace_C() {
        this(0);
    }

    public PeriodTrace_C(int period) {
        this.period = period;
    }

    public byte[] getBytes() {
        return getBytesFromInt(period, getLength());
    }

    public PeriodTrace_C parse(byte[] rawData, int offset) throws CTRParsingException {
        this.period = getIntFromBytes(rawData, offset, getLength());
        return this;
    }

    public int getLength() {
        return 1;
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

    public int getIntervalInSeconds() {
        switch (period) {
            case 1 : return 15 * SEC_PER_MIN;
            case 2 : return SEC_PER_HOUR;
            case 3 : return 24 * SEC_PER_HOUR;
            default : return 0;
        }
    }

    public int getTraceCIntervalCount() {
        switch (period) {
            case 1 : return 24; // 24 hours
            case 2 : return 15; // 15 Days
            case 3 : return 12; // 12 Months
            default : return 0;
        }
    }
}
