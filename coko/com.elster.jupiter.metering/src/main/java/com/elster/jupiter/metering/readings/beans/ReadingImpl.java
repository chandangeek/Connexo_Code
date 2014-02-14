package com.elster.jupiter.metering.readings.beans;

import com.elster.jupiter.metering.readings.Reading;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Our default implementation of a {@link Reading}.
 * A Reading serves as a single reading for ex. a RegisterValue
 *
 * Copyrights EnergyICT
 * Date: 25/11/13
 * Time: 15:16
 */
public class ReadingImpl extends BaseReadingImpl implements Reading {

    private final String mrid;
    private String reason;

    public ReadingImpl(String mrid, BigDecimal value, Date timeStamp) {
        super(timeStamp, value);
        this.mrid = mrid;
    }

    @Override
    public String getReason() {
        return reason;
    }

    @Override
    public String getReadingTypeCode() {
        return this.mrid;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
