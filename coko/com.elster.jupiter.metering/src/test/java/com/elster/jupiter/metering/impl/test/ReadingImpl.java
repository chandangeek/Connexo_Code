package com.elster.jupiter.metering.impl.test;

import com.elster.jupiter.metering.readings.Reading;

import java.math.BigDecimal;
import java.util.Date;

public class ReadingImpl extends BaseReadingImpl implements Reading {

    private final String mrid;

    public ReadingImpl(String mrid, BigDecimal value, Date timeStamp) {
        super(timeStamp, value);
        this.mrid = mrid;
    }

    @Override
    public String getReason() {
        return null;
    }

    @Override
    public String getReadingTypeCode() {
        return this.mrid;
    }

}
