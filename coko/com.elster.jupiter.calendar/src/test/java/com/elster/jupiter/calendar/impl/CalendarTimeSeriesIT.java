/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.EventSet;
import com.elster.jupiter.calendar.OutOfTheBoxCategory;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.nls.Thesaurus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Period;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAmount;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the {@link ServerCalendar#toTimeSeries(TemporalAmount, ZoneId)} method.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-06 (08:37)
 */
@RunWith(MockitoJUnitRunner.class)
public class CalendarTimeSeriesIT {

    public static final long OFF_PEAK_EVENT_CODE = 5L;
    public static final long PEAK_EVENT_CODE = 3L;
    private static CalendarInMemoryBootstrapModule inMemoryBootstrapModule = new CalendarInMemoryBootstrapModule();

    @Mock
    private Thesaurus thesaurus;
    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    private ServerCalendarService getCalendarService() {
        return inMemoryBootstrapModule.getCalendarService();
    }

    @Test
    @Transactional
    public void createMonthlyView() {
        ServerCalendar calendar = this.createSimplePeakOffPeakCalendar("Monthly");

        // Business methods
        TimeSeries monthly = calendar.toTimeSeries(Period.ofMonths(1), ZoneOffset.UTC);

        // Asserts
        assertThat(monthly).isNotNull();
        assertThat(monthly.getId()).isGreaterThan(0);
        assertThat(monthly.interval()).isEqualTo(Period.ofMonths(1));
    }

    private ServerCalendar createSimplePeakOffPeakCalendar(String name) {
        Category category = getCalendarService().findCategoryByName(OutOfTheBoxCategory.TOU.getDefaultDisplayName()).orElseThrow(() -> new IllegalStateException("Setup failed because out of the box category TOU is missing"));
        EventSet testEventSet = createTestEventSet();
        return (ServerCalendar) getCalendarService()
            .newCalendar(name, Year.of(2016), testEventSet)
                .category(category)
                .description("Description remains to be completed :-)")
                .mRID(name + "-mrid")
                .newDayType("Every day")
                    .event("On peak").startsFrom(LocalTime.of(07, 0, 0))
                    .event("Off peak").startsFrom(LocalTime.of(21, 0, 0))
                    .add()
                .newDayType("Holiday")
                    .event("Off peak").startsFrom(LocalTime.MIDNIGHT)
                    .add()
                .addPeriod("Always", "Every day", "Every day", "Every day", "Every day", "Every day", "Every day", "Every day")
                    .on(MonthDay.of(1, 1)).transitionTo("Always")
                .except("Holiday")
                    .occursAlwaysOn(MonthDay.of(1, 1))
                    .occursOnceOn(LocalDate.of(2016, 2, 15))
                    .occursOnceOn(LocalDate.of(2016, 3, 12))    // DST switch from winter to summer in EST
                    .occursAlwaysOn(MonthDay.of(5, 1))
                    .add()
                .add();
    }

    private EventSet createTestEventSet() {
        return getCalendarService().newEventSet("eventset")
                .addEvent("On peak").withCode(PEAK_EVENT_CODE)
                .addEvent("Off peak").withCode(OFF_PEAK_EVENT_CODE)
                .add();
    }

}
