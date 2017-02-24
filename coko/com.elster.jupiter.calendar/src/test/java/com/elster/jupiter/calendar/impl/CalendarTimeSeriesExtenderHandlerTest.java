/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.tasks.TaskOccurrence;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link CalendarTimeSeriesExtenderHandler} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-24 (13:21)
 */
@RunWith(MockitoJUnitRunner.class)
public class CalendarTimeSeriesExtenderHandlerTest {

    private static final long CALENDAR_ID = 97L;
    private static final long TIMESERIES_1_ID = 101L;
    private static final long TIMESERIES_2_ID = 102L;

    @Mock
    private ServerCalendarService calendarService;
    @Mock
    private MessageService messageService;
    @Mock
    private DestinationSpec destinationSpec;
    @Mock
    private MessageBuilder messageBuilder;
    @Mock
    private TaskOccurrence taskOccurrence;
    @Mock
    private ServerCalendar calendar;
    @Mock
    private CalendarTimeSeriesEntity calendarTimeSeries1;
    @Mock
    private TimeSeries timeSeries1;
    @Mock
    private CalendarTimeSeriesEntity calendarTimeSeries2;
    @Mock
    private TimeSeries timeSeries2;

    @Before
    public void setupCalendarService() {
        when(this.calendarService.findAllCalendarsForExtension()).thenReturn(Collections.singletonList(this.calendar));
        when(this.calendarService.findCalendar(CALENDAR_ID)).thenReturn(Optional.of(this.calendar));
        when(this.calendar.getId()).thenReturn(CALENDAR_ID);
        when(this.calendar.getCachedTimeSeries()).thenReturn(Arrays.asList(this.calendarTimeSeries1, calendarTimeSeries2));
        when(this.calendarTimeSeries1.calendar()).thenReturn(this.calendar);
        when(this.calendarTimeSeries1.timeSeries()).thenReturn(this.timeSeries1);
        when(this.timeSeries1.getId()).thenReturn(TIMESERIES_1_ID);
        when(this.calendarTimeSeries2.calendar()).thenReturn(this.calendar);
        when(this.calendarTimeSeries2.timeSeries()).thenReturn(this.timeSeries2);
        when(this.timeSeries2.getId()).thenReturn(TIMESERIES_2_ID);
    }

    @Before
    public void setupMessageService() {
        when(this.messageService.getDestinationSpec(CalendarTimeSeriesExtenderHandlerFactory.TASK_DESTINATION)).thenReturn(Optional.of(this.destinationSpec));
        when(this.destinationSpec.message(anyString())).thenReturn(this.messageBuilder);
    }

    @Test
    public void executeGlobalTask() {
        CalendarTimeSeriesExtenderHandler testInstance = this.getInstance();
        when(this.taskOccurrence.getPayLoad()).thenReturn(CalendarTimeSeriesExtenderHandler.GLOBAL_START_PAYLOAD);

        // Business method
        testInstance.execute(this.taskOccurrence);

        // Asserts
        verify(this.messageService).getDestinationSpec(CalendarTimeSeriesExtenderHandlerFactory.TASK_DESTINATION);
        verify(this.destinationSpec, times(2)).message(anyString());    // Create a message for every cached time series
        verify(this.messageBuilder, times(2)).send();   // Send every messge that was created
    }

    @Test
    public void executeSingleTask() {
        CalendarTimeSeriesExtenderHandler testInstance = this.getInstance();
        when(this.taskOccurrence.getPayLoad()).thenReturn(CALENDAR_ID + "#" + TIMESERIES_1_ID);

        // Business method
        testInstance.execute(this.taskOccurrence);

        // Asserts
        verify(this.calendarService).findCalendar(CALENDAR_ID);
        verify(this.calendar).extend(TIMESERIES_1_ID);
    }

    private CalendarTimeSeriesExtenderHandler getInstance() {
        return new CalendarTimeSeriesExtenderHandler(this.calendarService, this.messageService);
    }
}