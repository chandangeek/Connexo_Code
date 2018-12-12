/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.rest.util.ExceptionFactory;

import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KpiScoreFactoryTest {
    @Mock
    Clock clock;
    @Mock
    ExceptionFactory exceptionFactory;

    @Test
    public void testDisplayRangeOneHour() throws Exception {
        when(clock.instant()).thenReturn(getInstant(2014, 8, 1, 13, 11, 16));
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        KpiScoreFactory kpiScoreFactory = new KpiScoreFactory(exceptionFactory, clock);
        Range<Instant> range = kpiScoreFactory.getActualRangeByDisplayRange(Duration.ofHours(1));
        assertThat(range.lowerEndpoint()).isEqualTo(getInstant(2014, 8, 1, 13, 0, 0));
        assertThat(range.upperEndpoint()).isEqualTo(getInstant(2014, 8, 1, 14, 0, 0));
    }

    @Test
    public void testDisplayRangeOneHourAtHour() throws Exception {
        when(clock.instant()).thenReturn(getInstant(2014, 8, 1, 13, 0, 0));
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        KpiScoreFactory kpiScoreFactory = new KpiScoreFactory(exceptionFactory, clock);
        Range<Instant> range = kpiScoreFactory.getActualRangeByDisplayRange(Duration.ofHours(1));
        assertThat(range.lowerEndpoint()).isEqualTo(getInstant(2014, 8, 1, 13, 0, 0));
        assertThat(range.upperEndpoint()).isEqualTo(getInstant(2014, 8, 1, 14, 0, 0));
    }

    @Test
    public void testDisplayRangeOneDay() throws Exception {
        when(clock.instant()).thenReturn(getInstant(2014, 8, 1, 13, 0, 0));
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        KpiScoreFactory kpiScoreFactory = new KpiScoreFactory(exceptionFactory, clock);
        Range<Instant> range = kpiScoreFactory.getActualRangeByDisplayRange(Duration.ofDays(1));
        assertThat(range.lowerEndpoint()).isEqualTo(getInstant(2014, 8, 1, 0, 0, 0));
        assertThat(range.upperEndpoint()).isEqualTo(getInstant(2014, 8, 2, 0, 0, 0));
    }

    @Test
    public void testDisplayRangeOneDayAtMidnight() throws Exception {
        when(clock.instant()).thenReturn(getInstant(2014, 8, 1, 0, 0, 0));
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        KpiScoreFactory kpiScoreFactory = new KpiScoreFactory(exceptionFactory, clock);
        Range<Instant> range = kpiScoreFactory.getActualRangeByDisplayRange(Duration.ofDays(1));
        assertThat(range.lowerEndpoint()).isEqualTo(getInstant(2014, 8, 1, 0, 0, 0));
        assertThat(range.upperEndpoint()).isEqualTo(getInstant(2014, 8, 2, 0, 0, 0));
    }

    @Test
    public void testDisplayRangeOneWeek() throws Exception {
        when(clock.instant()).thenReturn(getInstant(2014, 8, 1, 13, 0, 0));
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        KpiScoreFactory kpiScoreFactory = new KpiScoreFactory(exceptionFactory, clock);
        Range<Instant> range = kpiScoreFactory.getActualRangeByDisplayRange(Period.ofWeeks(1));
        assertThat(range.lowerEndpoint()).isEqualTo(getInstant(2014, 7, 28, 0, 0, 0));
        assertThat(range.upperEndpoint()).isEqualTo(getInstant(2014, 8, 4, 0, 0, 0));
    }

    @Test
    public void testDisplayRangeOneWeekOnAMonday() throws Exception {
        when(clock.instant()).thenReturn(getInstant(2015, 3, 9, 8, 51, 0));
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        KpiScoreFactory kpiScoreFactory = new KpiScoreFactory(exceptionFactory, clock);
        Range<Instant> range = kpiScoreFactory.getActualRangeByDisplayRange(Period.ofWeeks(1));
        assertThat(range.lowerEndpoint()).isEqualTo(getInstant(2015, 3, 9, 0, 0, 0));
        assertThat(range.upperEndpoint()).isEqualTo(getInstant(2015, 3, 16, 0, 0, 0));
    }

    @Test
    public void testDisplayRangeTwoWeeks() throws Exception {
        when(clock.instant()).thenReturn(getInstant(2014, 8, 1, 13, 0, 0));
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        KpiScoreFactory kpiScoreFactory = new KpiScoreFactory(exceptionFactory, clock);
        Range<Instant> range = kpiScoreFactory.getActualRangeByDisplayRange(Period.ofWeeks(2));
        assertThat(range.lowerEndpoint()).isEqualTo(getInstant(2014, 7, 21, 0, 0, 0));
        assertThat(range.upperEndpoint()).isEqualTo(getInstant(2014, 8, 4, 0, 0, 0));
    }

    @Test
    public void testDisplayRangeTwoWeeksOnAMonday() throws Exception {
        when(clock.instant()).thenReturn(getInstant(2015, 3, 9, 8, 51, 0));
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        KpiScoreFactory kpiScoreFactory = new KpiScoreFactory(exceptionFactory, clock);
        Range<Instant> range = kpiScoreFactory.getActualRangeByDisplayRange(Period.ofWeeks(2));
        assertThat(range.lowerEndpoint()).isEqualTo(getInstant(2015, 3, 2, 0, 0, 0));
        assertThat(range.upperEndpoint()).isEqualTo(getInstant(2015, 3, 16, 0, 0, 0));
    }

    @Test
    public void testDisplayRangeOneMonth() throws Exception {
        when(clock.instant()).thenReturn(getInstant(2014, 8, 1, 13, 0, 0));
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        KpiScoreFactory kpiScoreFactory = new KpiScoreFactory(exceptionFactory, clock);
        Range<Instant> range = kpiScoreFactory.getActualRangeByDisplayRange(Period.ofMonths(1));
        assertThat(range.lowerEndpoint()).isEqualTo(getInstant(2014, 8, 1, 0, 0, 0));
        assertThat(range.upperEndpoint()).isEqualTo(getInstant(2014, 9, 1, 0, 0, 0));
    }

    @Test
    public void testDisplayRangeOneYear() throws Exception {
        when(clock.instant()).thenReturn(getInstant(2014, 8, 1, 13, 0, 0));
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        KpiScoreFactory kpiScoreFactory = new KpiScoreFactory(exceptionFactory, clock);
        Range<Instant> range = kpiScoreFactory.getActualRangeByDisplayRange(Period.ofYears(1));
        assertThat(range.lowerEndpoint()).isEqualTo(getInstant(2014, 1, 1, 0, 0, 0));
        assertThat(range.upperEndpoint()).isEqualTo(getInstant(2015, 1, 1, 0, 0, 0));
    }

    private Instant getInstant(int year, int month, int day, int hour, int minute, int second) {
        return LocalDateTime.of(year, month, day, hour, minute, second).atZone(ZoneId.systemDefault()).toInstant();
    }


}
