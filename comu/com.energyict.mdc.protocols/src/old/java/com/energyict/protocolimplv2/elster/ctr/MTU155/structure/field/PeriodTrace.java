/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

public class PeriodTrace extends AbstractField<PeriodTrace> {

    private int period;

    public PeriodTrace() {
        this(0);
    }

    public PeriodTrace(int period) {
        this.period = period;
    }

    public byte[] getBytes() {
        return getBytesFromInt(period, getLength());
    }

    public PeriodTrace parse(byte[] rawData, int offset) throws CTRParsingException {
        this.period = getIntFromBytes(rawData, offset, getLength());
        return this;
    }

    public int getPeriod() {
        return period;
    }

    /**
     * @return possible date formats
     */
    public String getDateFormat() {
        switch (period) {
            case 1:
                return "yy, mm, dd, qq";
            case 2:
                return "yy, mm, dd, hh";
            case 3:
                return "yy, mm, dd, 00";
            case 4:
                return "yy, mm, 00, 00";
            case 5:
                return "aa, 00, 00, 00";
            default:
                return "";
        }
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    /**
     * @return description for the period of the trace data
     */
    public String getDescription() {
        switch (period) {
            case 1:
                return "Quarters of an hour on the day specified";
            case 2:
                return "The hours on the day specified";
            case 3:
                return "The daily records on the day specified";
            case 4:
                return "Monthly records from the month specified";
            case 5:
                return "Annual records from the given year";
            default:
                return "";
        }
    }

    public int getLength() {
        return 1;
    }
}
