package com.elster.jupiter.metering.readings.beans;

import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/11/13
 * Time: 10:36
 */
public abstract class BaseReadingImpl implements BaseReading {

    private final BigDecimal value;
    private final Date timeStamp;
    private Optional<Interval> timePeriod = Optional.absent();
    private String source;
    private BigDecimal sensorAccuracy;
    private final List<ReadingQualityImpl> qualities = new ArrayList<>();

    BaseReadingImpl(Date timeStamp, BigDecimal value) {
        this.timeStamp = timeStamp;
        this.value = value;
    }

    @Override
    public BigDecimal getSensorAccuracy() {
        return sensorAccuracy;
    }

    @Override
    public String getSource() {
        return source;
    }

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
        return timePeriod.orNull();
    }

    public void setTimePeriod(Date start, Date end) {
        timePeriod = Optional.of(new Interval(start, end));
    }

    public void setTimePeriod(Interval interval) {
        this.timePeriod = Optional.fromNullable(interval);
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setSensorAccuracy(BigDecimal sensorAccuracy) {
        this.sensorAccuracy = sensorAccuracy;
    }
    
    public void addQuality(String typeCode, String comment) {
    	qualities.add(new ReadingQualityImpl(typeCode, comment));
    }
    
    public void addQuality(String typeCode) {
    	addQuality(typeCode,null);
    }
    
    public List<? extends ReadingQuality> getQualities() {
    	return Collections.unmodifiableList(qualities);
    }
}
