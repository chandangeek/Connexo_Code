/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.google.common.collect.Range;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link IntervalLength#extend(Range, ZoneId)} method.
 */
public class IntervalLengthExtendTest {

    private static final ZoneId ZONE_ID = ZoneId.of("Antarctica/McMurdo");

    @Test
    public void extendAlreadyAligned15minClosedRange() {
        LocalDateTime start = LocalDateTime.of(2017, Month.FEBRUARY, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2017, Month.FEBRUARY, 1, 9, 15, 0);
        Range<Instant> period = Range.closed(start.atZone(ZONE_ID).toInstant(), end.atZone(ZONE_ID).toInstant());

        // Business method
        Range<Instant> extended = IntervalLength.MINUTE15.extend(period, ZONE_ID);

        // Asserts
        assertThat(extended.lowerBoundType()).isEqualTo(period.lowerBoundType());
        assertThat(extended.upperBoundType()).isEqualTo(period.upperBoundType());
        assertThat(extended.lowerEndpoint()).isEqualTo(period.lowerEndpoint());
        assertThat(extended.upperEndpoint()).isEqualTo(period.upperEndpoint());
    }

    @Test
    public void extendAlreadyAligned15minOpenRange() {
        LocalDateTime start = LocalDateTime.of(2017, Month.FEBRUARY, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2017, Month.FEBRUARY, 1, 9, 15, 0);
        Range<Instant> period = Range.open(start.atZone(ZONE_ID).toInstant(), end.atZone(ZONE_ID).toInstant());

        // Business method
        Range<Instant> extended = IntervalLength.MINUTE15.extend(period, ZONE_ID);

        // Asserts
        assertThat(extended.lowerBoundType()).isEqualTo(period.lowerBoundType());
        assertThat(extended.upperBoundType()).isEqualTo(period.upperBoundType());
        assertThat(extended.lowerEndpoint()).isEqualTo(period.lowerEndpoint());
        assertThat(extended.upperEndpoint()).isEqualTo(period.upperEndpoint());
    }

    @Test
    public void extend15minClosedRange() {
        LocalDateTime start = LocalDateTime.of(2017, Month.FEBRUARY, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2017, Month.FEBRUARY, 1, 7, 11, 47);
        Range<Instant> period = Range.closed(start.atZone(ZONE_ID).toInstant(), end.atZone(ZONE_ID).toInstant());

        // Business method
        Range<Instant> extended = IntervalLength.MINUTE15.extend(period, ZONE_ID);

        // Asserts
        assertThat(extended.lowerBoundType()).isEqualTo(period.lowerBoundType());
        assertThat(extended.upperBoundType()).isEqualTo(period.upperBoundType());
        assertThat(extended.lowerEndpoint()).isEqualTo(period.lowerEndpoint());
        Instant expectedEndPoint = LocalDateTime.of(2017, Month.FEBRUARY, 1, 7, 15, 0).atZone(ZONE_ID).toInstant();
        assertThat(extended.upperEndpoint()).isEqualTo(expectedEndPoint);
    }

    @Test
    public void extend15minOpenRange() {
        LocalDateTime start = LocalDateTime.of(2017, Month.FEBRUARY, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2017, Month.FEBRUARY, 1, 7, 11, 47);
        Range<Instant> period = Range.open(start.atZone(ZONE_ID).toInstant(), end.atZone(ZONE_ID).toInstant());

        // Business method
        Range<Instant> extended = IntervalLength.MINUTE15.extend(period, ZONE_ID);

        // Asserts
        assertThat(extended.lowerBoundType()).isEqualTo(period.lowerBoundType());
        assertThat(extended.upperBoundType()).isEqualTo(period.upperBoundType());
        assertThat(extended.lowerEndpoint()).isEqualTo(period.lowerEndpoint());
        Instant expectedEndPoint = LocalDateTime.of(2017, Month.FEBRUARY, 1, 7, 15, 0).atZone(ZONE_ID).toInstant();
        assertThat(extended.upperEndpoint()).isEqualTo(expectedEndPoint);
    }

    @Test
    public void extendAlreadyAlignedDailyClosedRange() {
        LocalDateTime start = LocalDateTime.of(2017, Month.FEBRUARY, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2017, Month.MAY, 10, 0, 0, 0);
        Range<Instant> period = Range.closed(start.atZone(ZONE_ID).toInstant(), end.atZone(ZONE_ID).toInstant());

        // Business method
        Range<Instant> extended = IntervalLength.DAY1.extend(period, ZONE_ID);

        // Asserts
        assertThat(extended.lowerBoundType()).isEqualTo(period.lowerBoundType());
        assertThat(extended.upperBoundType()).isEqualTo(period.upperBoundType());
        assertThat(extended.lowerEndpoint()).isEqualTo(period.lowerEndpoint());
        assertThat(extended.upperEndpoint()).isEqualTo(period.upperEndpoint());
    }

    @Test
    public void extendAlreadyAlignedDailyOpenRange() {
        LocalDateTime start = LocalDateTime.of(2017, Month.FEBRUARY, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2017, Month.MAY, 10, 0, 0, 0);
        Range<Instant> period = Range.open(start.atZone(ZONE_ID).toInstant(), end.atZone(ZONE_ID).toInstant());

        // Business method
        Range<Instant> extended = IntervalLength.DAY1.extend(period, ZONE_ID);

        // Asserts
        assertThat(extended.lowerBoundType()).isEqualTo(period.lowerBoundType());
        assertThat(extended.upperBoundType()).isEqualTo(period.upperBoundType());
        assertThat(extended.lowerEndpoint()).isEqualTo(period.lowerEndpoint());
        assertThat(extended.upperEndpoint()).isEqualTo(period.upperEndpoint());
    }

    @Test
    public void extendDailyClosedRange() {
        LocalDateTime start = LocalDateTime.of(2017, Month.FEBRUARY, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2017, Month.MAY, 11, 19, 47, 47);
        Range<Instant> period = Range.closed(start.atZone(ZONE_ID).toInstant(), end.atZone(ZONE_ID).toInstant());

        // Business method
        Range<Instant> extended = IntervalLength.DAY1.extend(period, ZONE_ID);

        // Asserts
        assertThat(extended.lowerBoundType()).isEqualTo(period.lowerBoundType());
        assertThat(extended.upperBoundType()).isEqualTo(period.upperBoundType());
        assertThat(extended.lowerEndpoint()).isEqualTo(period.lowerEndpoint());
        Instant expectedEndPoint = LocalDateTime.of(2017, Month.MAY, 12, 0, 0, 0).atZone(ZONE_ID).toInstant();
        assertThat(extended.upperEndpoint()).isEqualTo(expectedEndPoint);
    }

    @Test
    public void extendDailyOpenRange() {
        LocalDateTime start = LocalDateTime.of(2017, Month.FEBRUARY, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2017, Month.MAY, 11, 19, 47, 47);
        Range<Instant> period = Range.open(start.atZone(ZONE_ID).toInstant(), end.atZone(ZONE_ID).toInstant());

        // Business method
        Range<Instant> extended = IntervalLength.DAY1.extend(period, ZONE_ID);

        // Asserts
        assertThat(extended.lowerBoundType()).isEqualTo(period.lowerBoundType());
        assertThat(extended.upperBoundType()).isEqualTo(period.upperBoundType());
        assertThat(extended.lowerEndpoint()).isEqualTo(period.lowerEndpoint());
        Instant expectedEndPoint = LocalDateTime.of(2017, Month.MAY, 12, 0, 0, 0).atZone(ZONE_ID).toInstant();
        assertThat(extended.upperEndpoint()).isEqualTo(expectedEndPoint);
    }

    @Test
    public void extendAlreadyAlignedMonthlyClosedRange() {
        LocalDateTime start = LocalDateTime.of(2017, Month.FEBRUARY, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2017, Month.MAY, 1, 0, 0, 0);
        Range<Instant> period = Range.closed(start.atZone(ZONE_ID).toInstant(), end.atZone(ZONE_ID).toInstant());

        // Business method
        Range<Instant> extended = IntervalLength.MONTH1.extend(period, ZONE_ID);

        // Asserts
        assertThat(extended.lowerBoundType()).isEqualTo(period.lowerBoundType());
        assertThat(extended.upperBoundType()).isEqualTo(period.upperBoundType());
        assertThat(extended.lowerEndpoint()).isEqualTo(period.lowerEndpoint());
        assertThat(extended.upperEndpoint()).isEqualTo(period.upperEndpoint());
    }

    @Test
    public void extendAlreadyAlignedMonthlyOpenRange() {
        LocalDateTime start = LocalDateTime.of(2017, Month.FEBRUARY, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2017, Month.MAY, 1, 0, 0, 0);
        Range<Instant> period = Range.open(start.atZone(ZONE_ID).toInstant(), end.atZone(ZONE_ID).toInstant());

        // Business method
        Range<Instant> extended = IntervalLength.MONTH1.extend(period, ZONE_ID);

        // Asserts
        assertThat(extended.lowerBoundType()).isEqualTo(period.lowerBoundType());
        assertThat(extended.upperBoundType()).isEqualTo(period.upperBoundType());
        assertThat(extended.lowerEndpoint()).isEqualTo(period.lowerEndpoint());
        assertThat(extended.upperEndpoint()).isEqualTo(period.upperEndpoint());
    }

    @Test
    public void extendMonthlyClosedRange() {
        LocalDateTime start = LocalDateTime.of(2017, Month.FEBRUARY, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2017, Month.MAY, 11, 19, 47, 47);
        Range<Instant> period = Range.closed(start.atZone(ZONE_ID).toInstant(), end.atZone(ZONE_ID).toInstant());

        // Business method
        Range<Instant> extended = IntervalLength.MONTH1.extend(period, ZONE_ID);

        // Asserts
        assertThat(extended.lowerBoundType()).isEqualTo(period.lowerBoundType());
        assertThat(extended.upperBoundType()).isEqualTo(period.upperBoundType());
        assertThat(extended.lowerEndpoint()).isEqualTo(period.lowerEndpoint());
        Instant expectedEndPoint = LocalDateTime.of(2017, Month.JUNE, 1, 0, 0, 0).atZone(ZONE_ID).toInstant();
        assertThat(extended.upperEndpoint()).isEqualTo(expectedEndPoint);
    }

    @Test
    public void extendMonthlyOpenRange() {
        LocalDateTime start = LocalDateTime.of(2017, Month.FEBRUARY, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2017, Month.MAY, 11, 19, 47, 47);
        Range<Instant> period = Range.open(start.atZone(ZONE_ID).toInstant(), end.atZone(ZONE_ID).toInstant());

        // Business method
        Range<Instant> extended = IntervalLength.MONTH1.extend(period, ZONE_ID);

        // Asserts
        assertThat(extended.lowerBoundType()).isEqualTo(period.lowerBoundType());
        assertThat(extended.upperBoundType()).isEqualTo(period.upperBoundType());
        assertThat(extended.lowerEndpoint()).isEqualTo(period.lowerEndpoint());
        Instant expectedEndPoint = LocalDateTime.of(2017, Month.JUNE, 1, 0, 0, 0).atZone(ZONE_ID).toInstant();
        assertThat(extended.upperEndpoint()).isEqualTo(expectedEndPoint);
    }

    @Test
    public void extendAlreadyAlignedYearlyClosedRange() {
        LocalDateTime start = LocalDateTime.of(2015, Month.JANUARY, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2017, Month.JANUARY, 1, 0, 0, 0);
        Range<Instant> period = Range.closed(start.atZone(ZONE_ID).toInstant(), end.atZone(ZONE_ID).toInstant());

        // Business method
        Range<Instant> extended = IntervalLength.YEAR1.extend(period, ZONE_ID);

        // Asserts
        assertThat(extended.lowerBoundType()).isEqualTo(period.lowerBoundType());
        assertThat(extended.upperBoundType()).isEqualTo(period.upperBoundType());
        assertThat(extended.lowerEndpoint()).isEqualTo(period.lowerEndpoint());
        assertThat(extended.upperEndpoint()).isEqualTo(period.upperEndpoint());
    }

    @Test
    public void extendAlreadyAlignedYearlyOpenRange() {
        LocalDateTime start = LocalDateTime.of(2015, Month.JANUARY, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2017, Month.JANUARY, 1, 0, 0, 0);
        Range<Instant> period = Range.open(start.atZone(ZONE_ID).toInstant(), end.atZone(ZONE_ID).toInstant());

        // Business method
        Range<Instant> extended = IntervalLength.YEAR1.extend(period, ZONE_ID);

        // Asserts
        assertThat(extended.lowerBoundType()).isEqualTo(period.lowerBoundType());
        assertThat(extended.upperBoundType()).isEqualTo(period.upperBoundType());
        assertThat(extended.lowerEndpoint()).isEqualTo(period.lowerEndpoint());
        assertThat(extended.upperEndpoint()).isEqualTo(period.upperEndpoint());
    }

    @Test
    public void extendYearlyClosedRange() {
        LocalDateTime start = LocalDateTime.of(2015, Month.FEBRUARY, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2017, Month.MAY, 11, 19, 47, 47);
        Range<Instant> period = Range.closed(start.atZone(ZONE_ID).toInstant(), end.atZone(ZONE_ID).toInstant());

        // Business method
        Range<Instant> extended = IntervalLength.YEAR1.extend(period, ZONE_ID);

        // Asserts
        assertThat(extended.lowerBoundType()).isEqualTo(period.lowerBoundType());
        assertThat(extended.upperBoundType()).isEqualTo(period.upperBoundType());
        assertThat(extended.lowerEndpoint()).isEqualTo(period.lowerEndpoint());
        Instant expectedEndPoint = LocalDateTime.of(2018, Month.JANUARY, 1, 0, 0, 0).atZone(ZONE_ID).toInstant();
        assertThat(extended.upperEndpoint()).isEqualTo(expectedEndPoint);
    }

    @Test
    public void extendYearlyOpenRange() {
        LocalDateTime start = LocalDateTime.of(2017, Month.FEBRUARY, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2017, Month.MAY, 11, 19, 47, 47);
        Range<Instant> period = Range.open(start.atZone(ZONE_ID).toInstant(), end.atZone(ZONE_ID).toInstant());

        // Business method
        Range<Instant> extended = IntervalLength.YEAR1.extend(period, ZONE_ID);

        // Asserts
        assertThat(extended.lowerBoundType()).isEqualTo(period.lowerBoundType());
        assertThat(extended.upperBoundType()).isEqualTo(period.upperBoundType());
        assertThat(extended.lowerEndpoint()).isEqualTo(period.lowerEndpoint());
        Instant expectedEndPoint = LocalDateTime.of(2018, Month.JANUARY, 1, 0, 0, 0).atZone(ZONE_ID).toInstant();
        assertThat(extended.upperEndpoint()).isEqualTo(expectedEndPoint);
    }

}