/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.impl.ServerUsagePoint;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link CalendarTimeSeriesCacheHandler} component.
 */
@RunWith(MockitoJUnitRunner.class)
public class CalendarTimeSeriesCacheHandlerTest {

    private static final long USAGEPOINT_ID = 97L;
    private static final long UTC_TIMESTAMP = 1489478196000L;
    private static final String PAYLOAD = Long.toString(USAGEPOINT_ID) + CalendarTimeSeriesCacheHandler.USAGE_POINT_TIMESTAMP_SEPARATOR + Long.toString(UTC_TIMESTAMP);

    @Mock
    private MeteringService meteringService;
    @Mock
    private ServerDataAggregationService dataAggregationService;
    @Mock
    private Message message;
    @Mock
    private ServerUsagePoint usagePoint;
    @Mock
    private ServerDataAggregationService.DetailedCalendarUsage calendarUsage;
    @Mock
    private Calendar calendar;

    @Before
    public void initializeMocks() {
        when(this.message.getPayload()).thenReturn(PAYLOAD.getBytes());
        when(this.meteringService.findUsagePointById(USAGEPOINT_ID)).thenReturn(Optional.of(this.usagePoint));
        when(this.usagePoint.getId()).thenReturn(USAGEPOINT_ID);

        when(this.calendarUsage.getCalendar()).thenReturn(this.calendar);
        when(this.calendarUsage.getIntervalLength()).thenReturn(IntervalLength.MINUTE15);
        when(this.calendarUsage.getZoneId()).thenReturn(ZoneOffset.UTC);
    }

    @Test
    public void executeWithoutUsages() {
        CalendarTimeSeriesCacheHandler instance = this.getInstance();
        when(this.dataAggregationService.introspect(this.usagePoint, Instant.ofEpochMilli(UTC_TIMESTAMP))).thenReturn(Collections.emptyList());

        // Business method
        instance.process(this.message);

        // Asserts
        verify(this.meteringService).findUsagePointById(USAGEPOINT_ID);
        verify(this.dataAggregationService).introspect(this.usagePoint, Instant.ofEpochMilli(UTC_TIMESTAMP));
        verify(this.calendar, never()).toTimeSeries(IntervalLength.MINUTE15.toTemporalAmount(), ZoneOffset.UTC);
    }

    @Test
    public void executeWithSingleUsage() {
        CalendarTimeSeriesCacheHandler instance = this.getInstance();
        when(this.dataAggregationService.introspect(this.usagePoint, Instant.ofEpochMilli(UTC_TIMESTAMP))).thenReturn(Collections.singletonList(this.calendarUsage));

        // Business method
        instance.process(this.message);

        // Asserts
        verify(this.meteringService).findUsagePointById(USAGEPOINT_ID);
        verify(this.dataAggregationService).introspect(this.usagePoint, Instant.ofEpochMilli(UTC_TIMESTAMP));
        verify(this.calendarUsage).getCalendar();
        verify(this.calendarUsage).getIntervalLength();
        verify(this.calendarUsage).getZoneId();
        verify(this.calendar).toTimeSeries(IntervalLength.MINUTE15.toTemporalAmount(), ZoneOffset.UTC);
    }

    @Test
    public void executeWithMultipleUsages() {
        CalendarTimeSeriesCacheHandler instance = this.getInstance();

        Calendar additionalCalendar = mock(Calendar.class);
        ServerDataAggregationService.DetailedCalendarUsage additionalUsage = mock(ServerDataAggregationService.DetailedCalendarUsage.class);
        when(additionalUsage.getCalendar()).thenReturn(additionalCalendar);
        when(additionalUsage.getIntervalLength()).thenReturn(IntervalLength.HOUR1);
        ZoneOffset additionalZoneId = ZoneOffset.ofHours(2);
        when(additionalUsage.getZoneId()).thenReturn(additionalZoneId);
        when(this.dataAggregationService
                .introspect(this.usagePoint, Instant.ofEpochMilli(UTC_TIMESTAMP)))
            .thenReturn(Arrays.asList(this.calendarUsage, additionalUsage));

        // Business method
        instance.process(this.message);

        // Asserts
        verify(this.meteringService).findUsagePointById(USAGEPOINT_ID);
        verify(this.dataAggregationService).introspect(this.usagePoint, Instant.ofEpochMilli(UTC_TIMESTAMP));
        verify(this.calendarUsage).getCalendar();
        verify(this.calendarUsage).getIntervalLength();
        verify(this.calendarUsage).getZoneId();
        verify(this.calendar).toTimeSeries(IntervalLength.MINUTE15.toTemporalAmount(), ZoneOffset.UTC);
        verify(additionalUsage).getCalendar();
        verify(additionalUsage).getIntervalLength();
        verify(additionalUsage).getZoneId();
        verify(additionalCalendar).toTimeSeries(IntervalLength.HOUR1.toTemporalAmount(), additionalZoneId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void executeForUsagePointThatWasDeletedInTheMeantime() {
        CalendarTimeSeriesCacheHandler instance = this.getInstance();
        when(this.meteringService.findUsagePointById(USAGEPOINT_ID)).thenReturn(Optional.empty());

        // Business method
        instance.process(this.message);

        // Asserts: see expected exception rule
        verify(this.meteringService).findUsagePointById(USAGEPOINT_ID);
    }

    @Test
    public void payloadFor() {
        // Business method
        String payload = CalendarTimeSeriesCacheHandler.payloadFor(this.usagePoint, Instant.ofEpochMilli(UTC_TIMESTAMP));

        // Asserts
        verify(this.usagePoint).getId();
        assertThat(payload).contains(Long.toString(USAGEPOINT_ID));
        assertThat(payload).contains(Long.toString(UTC_TIMESTAMP));
    }

    private CalendarTimeSeriesCacheHandler getInstance() {
        return new CalendarTimeSeriesCacheHandler(this.meteringService, this.dataAggregationService);
    }

}