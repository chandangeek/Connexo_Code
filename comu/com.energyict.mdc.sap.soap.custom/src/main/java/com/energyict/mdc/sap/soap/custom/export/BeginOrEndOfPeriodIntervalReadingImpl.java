/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.custom.export;

import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ReadingQuality;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

public class BeginOrEndOfPeriodIntervalReadingImpl implements BaseReading {

    private final BaseReading decorated;
    private final Instant timeStamp;

    public static BaseReading intervalReading(BaseReading decorated, Instant timeStamp) {
        return new BeginOrEndOfPeriodIntervalReadingImpl(decorated, timeStamp);
    }

    private BeginOrEndOfPeriodIntervalReadingImpl(BaseReading decorated, Instant timeStamp) {
        this.decorated = decorated;
        this.timeStamp = timeStamp;
    }

    @Override
    public BigDecimal getSensorAccuracy() {
        return decorated.getSensorAccuracy();
    }

    @Override
    public Instant getTimeStamp() {
        return timeStamp;
    }

    @Override
    public Instant getReportedDateTime() {
        return decorated.getReportedDateTime();
    }

    @Override
    public BigDecimal getValue() {
        return decorated.getValue();
    }

    @Override
    public String getSource() {
        return decorated.getSource();
    }

    @Override
    public Optional<Range<Instant>> getTimePeriod() {
        return Optional.of(Range.openClosed(timeStamp.minus(1, ChronoUnit.HOURS), timeStamp));
    }

    @Override
    public List<? extends ReadingQuality> getReadingQualities() {
        return decorated.getReadingQualities();
    }
}
