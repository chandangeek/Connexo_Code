/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.CalendarTimeSeries;
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
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAmount;
import java.util.Collections;
import java.util.Optional;
import java.util.TimeZone;

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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ServerCalendar#toTimeSeries(TemporalAmount, ZoneId)} method.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-06 (08:45)
 */
@RunWith(MockitoJUnitRunner.class)
public class CalendarTimeSeriesGenerateTest {

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
    private Clock clock;

    @Before
    public void initializeMocks() {
        when(this.clock.instant()).thenReturn(LocalDateTime.of(2017, Month.FEBRUARY, 6, 8, 50, 0).toInstant(ZoneOffset.UTC));
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
        when(this.idsService.createNonOverrulingStorer()).thenReturn(this.storer);
        when(this.storer.execute()).thenReturn(this.storerStats);
    }

    private ServerCalendarService getCalendarService() {
        return new CalendarServiceImpl(this.ormService, this.nlsService, this.idsService, this.userService, this.eventService, this.upgradeService, this.messageService, this.clock);
    }

    @Test
    public void firstCallCreatesTimeSeries() {
        ServerCalendarService calendarService = this.getCalendarService();
        when(this.dataModel.getInstance(CalendarTimeSeriesEntityImpl.class)).thenReturn(new CalendarTimeSeriesEntityImpl(calendarService));
        ServerCalendar calendar = this.createSimplePeakOffPeakCalendar(calendarService, "FirstCall");

        // Business methods
        calendar.toTimeSeries(Duration.ofHours(1L), ZoneOffset.UTC);

        // Asserts
        verify(this.vault).createRegularTimeSeries(this.recordSpec, ZoneOffset.UTC, Duration.ofHours(1), 0);
        verify(this.storer).execute();
    }

    @Test
    public void secondCallReusesTimeSeries() {
        ServerCalendarService calendarService = this.getCalendarService();
        when(this.dataModel.getInstance(CalendarTimeSeriesEntityImpl.class)).thenReturn(new CalendarTimeSeriesEntityImpl(calendarService));
        ServerCalendar calendar = this.createSimplePeakOffPeakCalendar(calendarService, "SecondCall");
        CalendarTimeSeries hourly = calendar.toTimeSeries(Duration.ofHours(1L), ZoneOffset.UTC);
        reset(this.vault);
        reset(this.storer);

        // Business methods
        CalendarTimeSeries sameAsHourly = calendar.toTimeSeries(Duration.ofHours(1L), ZoneOffset.UTC);

        // Asserts
        verify(this.vault, never()).createRegularTimeSeries(this.recordSpec, ZoneOffset.UTC, Duration.ofHours(1), 0);
        verify(this.storer, never()).execute();
        assertThat(sameAsHourly).isEqualTo(hourly);
    }

    @Test
    public void sameIntervalDifferentTimeZoneCreatesTimeSeries() {
        ServerCalendarService calendarService = this.getCalendarService();
        when(this.dataModel.getInstance(CalendarTimeSeriesEntityImpl.class)).thenReturn(new CalendarTimeSeriesEntityImpl(calendarService));
        ServerCalendar calendar = this.createSimplePeakOffPeakCalendar(calendarService, "SecondCall");
        calendar.toTimeSeries(Duration.ofHours(1L), ZoneOffset.UTC);

        // Business methods
        ZoneId zoneId = TimeZone.getTimeZone("EST").toZoneId();
        calendar.toTimeSeries(Duration.ofHours(1L), zoneId);

        // Asserts
        verify(this.vault).createRegularTimeSeries(this.recordSpec, ZoneOffset.UTC, Duration.ofHours(1), 0);
        verify(this.vault).createRegularTimeSeries(this.recordSpec, zoneId, Duration.ofHours(1), 0);
        verify(this.storer, times(2)).execute();
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
