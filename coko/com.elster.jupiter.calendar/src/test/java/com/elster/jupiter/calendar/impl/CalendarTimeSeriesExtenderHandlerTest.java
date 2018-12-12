/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.tasks.TaskLogHandler;
import com.elster.jupiter.tasks.TaskOccurrence;

import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link CalendarTimeSeriesExtenderHandler} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-24 (13:21)
 */
@RunWith(MockitoJUnitRunner.class)
public class CalendarTimeSeriesExtenderHandlerTest {

    private static final long CALENDAR_1_ID = 97L;
    private static final long TIMESERIES_1_1_ID = 101L;
    private static final long TIMESERIES_1_2_ID = 102L;
    private static final long CALENDAR_2_ID = 103L;
    private static final long TIMESERIES_2_1_ID = 104L;
    private static final long TIMESERIES_2_2_ID = 105L;

    @Mock
    private ServerCalendarService calendarService;
    @Mock
    private DestinationSpec destinationSpec;
    @Mock
    private MessageBuilder messageBuilder;
    @Mock
    private TaskOccurrence taskOccurrence;
    @Mock
    private TaskLogHandler taskLogHandler;
    @Mock
    private Handler handler;
    @Mock
    private ServerCalendar calendar1;
    @Mock
    private CalendarTimeSeriesEntity calendarTimeSeries1_1;
    @Mock
    private TimeSeries timeSeries1_1;
    @Mock
    private CalendarTimeSeriesEntity calendarTimeSeries1_2;
    @Mock
    private TimeSeries timeSeries1_2;
    @Mock
    private ServerCalendar calendar2;
    @Mock
    private CalendarTimeSeriesEntity calendarTimeSeries2_1;
    @Mock
    private TimeSeries timeSeries2_1;
    @Mock
    private CalendarTimeSeriesEntity calendarTimeSeries2_2;
    @Mock
    private TimeSeries timeSeries2_2;

    private FakeTransactionService transactionService = new FakeTransactionService();

    @Before
    public void setupCalendarService() {
        when(this.calendarService.findAllCalendarsForExtension()).thenReturn(Arrays.asList(this.calendar1, this.calendar2));
        when(this.calendarService.findCalendar(CALENDAR_1_ID)).thenReturn(Optional.of(this.calendar1));
        when(this.calendar1.getId()).thenReturn(CALENDAR_1_ID);
        when(this.calendar1.getCachedTimeSeries()).thenReturn(Arrays.asList(this.calendarTimeSeries1_1, calendarTimeSeries1_2));
        when(this.calendarTimeSeries1_1.calendar()).thenReturn(this.calendar1);
        when(this.calendarTimeSeries1_1.timeSeries()).thenReturn(this.timeSeries1_1);
        when(this.timeSeries1_1.getId()).thenReturn(TIMESERIES_1_1_ID);
        when(this.calendarTimeSeries1_2.calendar()).thenReturn(this.calendar1);
        when(this.calendarTimeSeries1_2.timeSeries()).thenReturn(this.timeSeries1_2);
        when(this.timeSeries1_2.getId()).thenReturn(TIMESERIES_1_2_ID);
        when(this.calendarService.findCalendar(CALENDAR_2_ID)).thenReturn(Optional.of(this.calendar2));
        when(this.calendar2.getId()).thenReturn(CALENDAR_2_ID);
        when(this.calendar2.getCachedTimeSeries()).thenReturn(Arrays.asList(this.calendarTimeSeries2_1, calendarTimeSeries2_2));
        when(this.calendarTimeSeries2_2.calendar()).thenReturn(this.calendar2);
        when(this.calendarTimeSeries2_2.timeSeries()).thenReturn(this.timeSeries2_1);
        when(this.timeSeries2_2.getId()).thenReturn(TIMESERIES_2_1_ID);
        when(this.calendarTimeSeries2_2.calendar()).thenReturn(this.calendar2);
        when(this.calendarTimeSeries2_2.timeSeries()).thenReturn(this.timeSeries2_2);
        when(this.timeSeries2_2.getId()).thenReturn(TIMESERIES_2_2_ID);
        when(this.taskOccurrence.createTaskLogHandler()).thenReturn(this.taskLogHandler);
        when(this.taskLogHandler.asHandler()).thenReturn(this.handler);
    }

    @Test
    public void noWorkIsDoneInExecute() {
        CalendarTimeSeriesExtenderHandler testInstance = this.getInstance();
        when(this.taskOccurrence.getPayLoad()).thenReturn(CalendarTimeSeriesExtenderHandler.GLOBAL_START_PAYLOAD);

        // Business method
        testInstance.execute(this.taskOccurrence);

        // Asserts
        assertThat(this.transactionService.getContexts()).isEmpty();
        verifyZeroInteractions(this.calendar1);
        verifyZeroInteractions(this.calendar2);
    }

    @Test
    public void executeGlobalTask() {
        CalendarTimeSeriesExtenderHandler testInstance = this.getInstance();
        when(this.taskOccurrence.getPayLoad()).thenReturn(CalendarTimeSeriesExtenderHandler.GLOBAL_START_PAYLOAD);

        // Business method
        testInstance.postExecute(this.taskOccurrence);

        // Asserts
        assertThat(this.transactionService.getContexts()).hasSize(3);   // Two calendars so two transactions + one for the final log entries
        verify(this.calendar1).extendAllTimeSeries();
        verify(this.calendar2).extendAllTimeSeries();
        verify(this.taskOccurrence).createTaskLogHandler();
        verify(this.taskLogHandler).asHandler();
    }

    private CalendarTimeSeriesExtenderHandler getInstance() {
        return new CalendarTimeSeriesExtenderHandler(this.transactionService, this.calendarService);
    }

}