package com.elster.jupiter.metering.impl.test;

import com.elster.jupiter.metering.readings.IntervalReading;

import java.math.BigDecimal;
import java.util.Date;

public class IntervalReadingImpl extends BaseReadingImpl implements IntervalReading {

    public IntervalReadingImpl(Date timeStamp, BigDecimal value) {
        super(timeStamp, value);
    }
}
