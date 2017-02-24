/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.Status;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.StorerStats;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ServerCalendar#extend(long)} and {@link ServerCalendar#bumpEndYear()} methods
 * that are being called from the recurrent task that extends the cached time series of a Calendar.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-24 (12:48)
 */
@RunWith(MockitoJUnitRunner.class)
public class CalendarTimeSeriesExtendTest {

    private static final long TIMESERIES_ID = 97L;

    @Mock
    private OrmService ormService;
    @Mock
    private DataModel dataModel;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Table table;
    @Mock
    private Category category;
    @Mock
    private NlsService nlsService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private IdsService idsService;
    @Mock
    private Vault vault;
    @Mock
    private RecordSpec recordSpec;
    @Mock
    private TimeSeries generatedTimeSeries;
    @Mock
    private TimeSeriesDataStorer storer;
    @Mock
    private StorerStats storerStats;
    @Mock
    private UserService userService;
    @Mock
    private EventService eventService;
    @Mock
    private UpgradeService upgradeService;
    @Mock
    private MessageService messageService;
    @Mock
    private TaskService taskService;
    @Mock
    private Clock clock;

    private Instant timeSeriesCreationTime;
    private Instant recurrentTaskExecutionTime;

    @Before
    public void initializeMocks() {
        this.timeSeriesCreationTime = LocalDateTime.of(2017, Month.FEBRUARY, 6, 8, 50, 0).toInstant(ZoneOffset.UTC);
        this.recurrentTaskExecutionTime = LocalDateTime.of(2017, Month.DECEMBER, 1, 0, 0, 1).toInstant(ZoneOffset.UTC);  // Very shortly after midnight of Dec 1st 2017
        when(this.clock.instant()).thenReturn(this.timeSeriesCreationTime);
        when(this.clock.getZone()).thenReturn(ZoneOffset.UTC);

        when(this.nlsService.getThesaurus(anyString(), any(Layer.class))).thenReturn(this.thesaurus);
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit testing");
        when(this.thesaurus.getFormat(any(TranslationKey.class))).thenReturn(messageFormat);
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);

        when(this.ormService.newDataModel(eq(CalendarService.COMPONENTNAME), anyString())).thenReturn(this.dataModel);
        when(this.dataModel.addTable(anyString(), any(Class.class))).thenReturn(this.table);
        when(this.dataModel.getTable(anyString())).thenReturn(this.table);

        when(this.idsService.getVault(eq(CalendarService.COMPONENTNAME), anyLong())).thenReturn(Optional.of(this.vault));
        when(this.idsService.getRecordSpec(eq(CalendarService.COMPONENTNAME), anyLong())).thenReturn(Optional.of(this.recordSpec));
        when(this.vault.createRegularTimeSeries(eq(this.recordSpec), any(ZoneId.class), any(TemporalAmount.class), anyInt())).thenReturn(this.generatedTimeSeries);
        when(this.generatedTimeSeries
                .toList(any(Range.class)))
                .thenReturn(Collections.emptyList());
        when(this.generatedTimeSeries.getLastDateTime()).thenReturn(timeSeriesCreationTime);
        when(this.generatedTimeSeries.getId()).thenReturn(TIMESERIES_ID);
        when(this.idsService.createNonOverrulingStorer()).thenReturn(this.storer);
        when(this.storer.execute()).thenReturn(this.storerStats);
    }

    private ServerCalendarService getCalendarService() {
        return new CalendarServiceImpl(this.ormService, this.nlsService, this.idsService, this.userService, this.eventService, this.upgradeService, this.messageService, this.taskService, this.clock);
    }

    @Test
    public void extend() {
        ServerCalendarService calendarService = this.getCalendarService();
        when(this.dataModel.getInstance(CalendarTimeSeriesEntityImpl.class)).thenReturn(new CalendarTimeSeriesEntityImpl(calendarService));
        ServerCalendar calendar = this.createSimplePeakOffPeakCalendar(calendarService, "FirstCall");
        calendar.toTimeSeries(Duration.ofHours(1L), ZoneOffset.UTC);
        when(this.clock.instant()).thenReturn(this.recurrentTaskExecutionTime);
        reset(this.vault);
        when(this.vault.createRegularTimeSeries(eq(this.recordSpec), any(ZoneId.class), any(TemporalAmount.class), anyInt())).thenReturn(this.generatedTimeSeries);
        reset(this.storer);
        when(this.storer.execute()).thenReturn(this.storerStats);

        // Business methods
        calendar.extend(TIMESERIES_ID);

        // Asserts
        verify(this.vault, never()).createRegularTimeSeries(this.recordSpec, ZoneOffset.UTC, Duration.ofHours(1), 0);
        verify(this.storer).execute();
        Instant expectedRangeStart = ZonedDateTime.ofInstant(this.recurrentTaskExecutionTime, ZoneOffset.UTC).withSecond(0).withDayOfYear(1).plusYears(1).toInstant();
        Instant expectedRangeEnd = ZonedDateTime.ofInstant(this.recurrentTaskExecutionTime, ZoneOffset.UTC).withSecond(0).withDayOfYear(1).plusYears(2).toInstant();
        verify(this.generatedTimeSeries).toList(Range.closedOpen(expectedRangeStart, expectedRangeEnd));
    }

    @Test
    public void bumpVersion() {
        ServerCalendarService calendarService = this.getCalendarService();
        when(this.dataModel.getInstance(CalendarTimeSeriesEntityImpl.class)).thenReturn(new CalendarTimeSeriesEntityImpl(calendarService));
        ServerCalendar calendar = this.createSimplePeakOffPeakCalendar(calendarService, "FirstCall");
        calendar.toTimeSeries(Duration.ofHours(1L), ZoneOffset.UTC);
        when(this.clock.instant()).thenReturn(this.recurrentTaskExecutionTime);
        reset(this.dataModel);

        // Business methods
        calendar.bumpEndYear();

        // Asserts
        Year expectedEndYear = Year.of(Year.now(this.clock).atDay(1).plusYears(1).getYear());
        assertThat(calendar.getEndYear()).isEqualTo(expectedEndYear);
        verify(this.dataModel).update(calendar, CalendarImpl.Fields.ENDYEAR.fieldName());
    }

    private ServerCalendar createSimplePeakOffPeakCalendar(ServerCalendarService calendarService, String name) {
        CalendarImpl calendar = new CalendarImpl(calendarService, this.eventService, this.clock, this.thesaurus);
        calendar.setName(name);
        calendar.setCategory(this.category);
        calendar.setStartYear(Year.now(this.clock));
        calendar.setStatus(Status.ACTIVE);
        return calendar;
    }

}