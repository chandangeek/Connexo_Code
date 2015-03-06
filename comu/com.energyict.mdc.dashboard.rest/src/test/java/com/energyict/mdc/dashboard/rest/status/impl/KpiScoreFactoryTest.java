package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.common.rest.ExceptionFactory;
import com.google.common.collect.Range;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
        when(clock.instant()).thenReturn(LocalDateTime.of(2014, 8, 1, 13, 11, 16).toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
        KpiScoreFactory kpiScoreFactory = new KpiScoreFactory(exceptionFactory, clock);
        Range<Instant> range = kpiScoreFactory.getRangeByDisplayRange(Duration.ofHours(1));
        assertThat(range.lowerEndpoint()).isEqualTo(LocalDateTime.of(2014, 8, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        assertThat(range.upperEndpoint()).isEqualTo(LocalDateTime.of(2014, 8, 1, 14, 0, 0).toInstant(ZoneOffset.UTC));
    }

    @Test
    public void testDisplayRangeOneHourAtHour() throws Exception {
        when(clock.instant()).thenReturn(LocalDateTime.of(2014, 8, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
        KpiScoreFactory kpiScoreFactory = new KpiScoreFactory(exceptionFactory, clock);
        Range<Instant> range = kpiScoreFactory.getRangeByDisplayRange(Duration.ofHours(1));
        assertThat(range.lowerEndpoint()).isEqualTo(LocalDateTime.of(2014, 8, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        assertThat(range.upperEndpoint()).isEqualTo(LocalDateTime.of(2014, 8, 1, 14, 0, 0).toInstant(ZoneOffset.UTC));
    }

    @Test
    public void testDisplayRangeOneDay() throws Exception {
        when(clock.instant()).thenReturn(LocalDateTime.of(2014, 8, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
        KpiScoreFactory kpiScoreFactory = new KpiScoreFactory(exceptionFactory, clock);
        Range<Instant> range = kpiScoreFactory.getRangeByDisplayRange(Duration.ofDays(1));
        assertThat(range.lowerEndpoint()).isEqualTo(LocalDateTime.of(2014, 8, 1, 0, 0, 0).toInstant(ZoneOffset.UTC));
        assertThat(range.upperEndpoint()).isEqualTo(LocalDateTime.of(2014, 8, 2, 0, 0, 0).toInstant(ZoneOffset.UTC));
    }

    @Test
    public void testDisplayRangeOneDayAtMidnight() throws Exception {
        when(clock.instant()).thenReturn(LocalDateTime.of(2014, 8, 1, 0, 0, 0).toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
        KpiScoreFactory kpiScoreFactory = new KpiScoreFactory(exceptionFactory, clock);
        Range<Instant> range = kpiScoreFactory.getRangeByDisplayRange(Duration.ofDays(1));
        assertThat(range.lowerEndpoint()).isEqualTo(LocalDateTime.of(2014, 8, 1, 0, 0, 0).toInstant(ZoneOffset.UTC));
        assertThat(range.upperEndpoint()).isEqualTo(LocalDateTime.of(2014, 8, 2, 0, 0, 0).toInstant(ZoneOffset.UTC));
    }

    @Test
    public void testDisplayRangeOneWeek() throws Exception {
        when(clock.instant()).thenReturn(LocalDateTime.of(2014, 8, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
        KpiScoreFactory kpiScoreFactory = new KpiScoreFactory(exceptionFactory, clock);
        Range<Instant> range = kpiScoreFactory.getRangeByDisplayRange(Period.ofWeeks(1));
        assertThat(range.lowerEndpoint()).isEqualTo(LocalDateTime.of(2014, 7, 28, 0, 0, 0).toInstant(ZoneOffset.UTC));
        assertThat(range.upperEndpoint()).isEqualTo(LocalDateTime.of(2014, 8, 4, 0, 0, 0).toInstant(ZoneOffset.UTC));
    }

    @Test
    public void testDisplayRangeTwoWeeks() throws Exception {
        when(clock.instant()).thenReturn(LocalDateTime.of(2014, 8, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
        KpiScoreFactory kpiScoreFactory = new KpiScoreFactory(exceptionFactory, clock);
        Range<Instant> range = kpiScoreFactory.getRangeByDisplayRange(Period.ofWeeks(2));
        assertThat(range.lowerEndpoint()).isEqualTo(LocalDateTime.of(2014, 7, 21, 0, 0, 0).toInstant(ZoneOffset.UTC));
        assertThat(range.upperEndpoint()).isEqualTo(LocalDateTime.of(2014, 8, 4, 0, 0, 0).toInstant(ZoneOffset.UTC));
    }

    @Test
    public void testDisplayRangeOneMonth() throws Exception {
        when(clock.instant()).thenReturn(LocalDateTime.of(2014, 8, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
        KpiScoreFactory kpiScoreFactory = new KpiScoreFactory(exceptionFactory, clock);
        Range<Instant> range = kpiScoreFactory.getRangeByDisplayRange(Period.ofMonths(1));
        assertThat(range.lowerEndpoint()).isEqualTo(LocalDateTime.of(2014, 8, 1, 0, 0, 0).toInstant(ZoneOffset.UTC));
        assertThat(range.upperEndpoint()).isEqualTo(LocalDateTime.of(2014, 9, 1, 0, 0, 0).toInstant(ZoneOffset.UTC));
    }

    @Test
    public void testDisplayRangeOneYear() throws Exception {
        when(clock.instant()).thenReturn(LocalDateTime.of(2014, 8, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
        KpiScoreFactory kpiScoreFactory = new KpiScoreFactory(exceptionFactory, clock);
        Range<Instant> range = kpiScoreFactory.getRangeByDisplayRange(Period.ofYears(1));
        assertThat(range.lowerEndpoint()).isEqualTo(LocalDateTime.of(2014, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC));
        assertThat(range.upperEndpoint()).isEqualTo(LocalDateTime.of(2015, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC));
    }


}
