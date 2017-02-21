/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.google.common.collect.Range;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link IntervalLength#toTimeSeries(Range, java.time.ZoneId)} method.
 */
public class IntervalLengthTimeSeriesTest {

    private static final Instant START = Instant.ofEpochMilli(1464739200000L);  // June 1st, 2016 (UTC)
    private static final Instant END = Instant.ofEpochMilli(1464825600000L);  // June 2nd, 2016 (UTC)

    @Test(expected = IllegalArgumentException.class)
    public void needLowerbound() {
        Range<Instant> period = Range.atMost(END);

        // Business method
        IntervalLength.MINUTE15.toTimeSeries(period, ZoneId.of("UTC"));

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void needUpperbound() {
        Range<Instant> period = Range.atLeast(START);

        // Business method
        IntervalLength.MINUTE15.toTimeSeries(period, ZoneId.of("UTC"));

        // Asserts: see expected exception rule
    }

    @Test
    public void closedClosed() {
        Range<Instant> period = Range.closed(START, END);

        // Business method
        List<Instant> timeSeries = IntervalLength.HOUR1.toTimeSeries(period, ZoneId.of("UTC")).collect(Collectors.toList());

        // Asserts
        assertThat(timeSeries).hasSize(25);
        assertThat(timeSeries.get(0)).isEqualTo(START);
        assertThat(timeSeries.get(24)).isEqualTo(END);
    }

    @Test
    public void closedClosedWithOffsetAtStart() {
        Range<Instant> period = Range.closed(START.plusMillis(10), END);

        // Business method
        ZoneId zoneId = ZoneId.of("UTC");
        List<Instant> timeSeries = IntervalLength.HOUR1.toTimeSeries(period, zoneId).collect(Collectors.toList());

        // Asserts
        assertThat(timeSeries).hasSize(24);
        assertThat(timeSeries.get(0)).isEqualTo(IntervalLength.HOUR1.addTo(START, zoneId));
        assertThat(timeSeries.get(23)).isEqualTo(END);
    }

    @Test
    public void closedClosedWithOffsetAtEnd() {
        Range<Instant> period = Range.closed(START, END.minusMillis(10));

        // Business method
        ZoneId zoneId = ZoneId.of("UTC");
        List<Instant> timeSeries = IntervalLength.HOUR1.toTimeSeries(period, zoneId).collect(Collectors.toList());

        // Asserts
        assertThat(timeSeries).hasSize(24);
        assertThat(timeSeries.get(0)).isEqualTo(START);
        assertThat(timeSeries.get(23)).isEqualTo(IntervalLength.HOUR1.subtractFrom(END, zoneId));
    }

    @Test
    public void closedOpen() {
        Range<Instant> period = Range.closedOpen(START, END);

        // Business method
        List<Instant> timeSeries = IntervalLength.HOUR1.toTimeSeries(period, ZoneId.of("UTC")).collect(Collectors.toList());

        // Asserts
        assertThat(timeSeries).hasSize(24);
        assertThat(timeSeries.get(0)).isEqualTo(START);
        assertThat(timeSeries.get(23)).isEqualTo(END.minus(Duration.ofHours(1)));
    }

    @Test
    public void openClosed() {
        Range<Instant> period = Range.openClosed(START, END);

        // Business method
        List<Instant> timeSeries = IntervalLength.HOUR1.toTimeSeries(period, ZoneId.of("UTC")).collect(Collectors.toList());

        // Asserts
        assertThat(timeSeries).hasSize(24);
        assertThat(timeSeries.get(0)).isEqualTo(START.plus(Duration.ofHours(1)));
        assertThat(timeSeries.get(23)).isEqualTo(END);
    }

    @Test
    public void open() {
        Range<Instant> period = Range.open(START, END);

        // Business method
        List<Instant> timeSeries = IntervalLength.HOUR1.toTimeSeries(period, ZoneId.of("UTC")).collect(Collectors.toList());

        // Asserts
        assertThat(timeSeries).hasSize(23);
        assertThat(timeSeries.get(0)).isEqualTo(START.plus(Duration.ofHours(1)));
        assertThat(timeSeries.get(22)).isEqualTo(END.minus(Duration.ofHours(1)));
    }

}