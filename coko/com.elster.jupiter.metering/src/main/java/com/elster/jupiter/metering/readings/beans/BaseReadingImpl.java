/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.readings.beans;

import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ReadingQuality;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class BaseReadingImpl implements BaseReading {

    private final BigDecimal value;
    private final Instant timeStamp;
    private Optional<Range<Instant>> timePeriod = Optional.empty();
    private String source;
    private BigDecimal sensorAccuracy;
    private final List<ReadingQuality> readingQualities = new ArrayList<>();

    BaseReadingImpl(Instant timeStamp, BigDecimal value) {
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
    public Instant getTimeStamp() {
        return this.timeStamp;
    }

    @Override
    public Instant getReportedDateTime() {
        return Instant.now();
    }

    @Override
    public BigDecimal getValue() {
        return this.value;
    }

    @Override
    public Optional<Range<Instant>> getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(Instant start, Instant end) {
        timePeriod = Optional.of(Range.openClosed(start, end));
    }

    public void setTimePeriod(Range<Instant> range) {
        this.timePeriod = Optional.ofNullable(range);
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setSensorAccuracy(BigDecimal sensorAccuracy) {
        this.sensorAccuracy = sensorAccuracy;
    }

    public void addQuality(String typeCode, String comment) {
    	readingQualities.add(new ReadingQualityImpl(typeCode, comment));
    }

    public void addQuality(ReadingQuality readingQuality) {
        readingQualities.add(readingQuality);
    }

    public void addQuality(String typeCode) {
    	addQuality(typeCode,null);
    }

    public void addQuality(ReadingQualityType readingQualityType) {
        addQuality(readingQualityType.getCode());
    }

    public List<? extends ReadingQuality> getReadingQualities() {
    	return Collections.unmodifiableList(readingQualities);
    }
}
