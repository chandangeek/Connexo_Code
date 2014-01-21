package com.elster.jupiter.metering.impl.test;

import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;

import java.math.BigDecimal;
import java.util.Date;

public abstract class BaseReadingImpl implements BaseReading {

    private final BigDecimal value;
    private final Date timeStamp;
    private Optional<Interval> interval = Optional.absent();

    public BaseReadingImpl(Date timeStamp, BigDecimal value) {
        this.timeStamp = timeStamp;
        this.value = value;
    }

    @Override
    public BigDecimal getSensorAccuracy() {
        return null;
    }

    @Override
    public String getSource() {
        return null;
    }

    // TODO is this the time the value was logged in the device? (Ex. Maximum Demand)
    @Override
    public Date getTimeStamp() {
        return this.timeStamp;
    }

    @Override
    public Date getReportedDateTime() {
        return new Date();
    }

    @Override
    public BigDecimal getValue() {
        return this.value;
    }

    @Override
    public Interval getTimePeriod() {
        return interval.orNull();
    }

    void setInterval(Date start, Date end) {
        interval = Optional.fromNullable(new Interval(start, end));
    }

}
