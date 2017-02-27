/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.EventSet;
import com.elster.jupiter.calendar.OutOfTheBoxCategory;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.ProgrammableClock;
import com.elster.jupiter.nls.Thesaurus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.TimeZone;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the {@link ServerCalendar#forZone(ZoneId, Year)} method
 * with Clock being fixed to May 2nd, 2016 at 01:40:00 (UTC).
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-03 (13:34)
 */
@RunWith(MockitoJUnitRunner.class)
public class CalendarTimeSeriesZoneTest {

    public static final long OFF_PEAK_EVENT_CODE = 5L;
    public static final long PEAK_EVENT_CODE = 3L;
    private static CalendarInMemoryBootstrapModule inMemoryBootstrapModule =
            new CalendarInMemoryBootstrapModule(
                    new ProgrammableClock(
                            ZoneOffset.UTC,
                            LocalDateTime.of(2016, Month.MAY, 2, 1, 40, 0).toInstant(ZoneOffset.UTC)));

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
    public void simplePeakOffPeakCalendar_UTC() {
        ServerCalendar calendar = this.createSimplePeakOffPeakCalendar("SimpleUTC");

        // Business methods
        ServerCalendar.ZonedView zonedView = calendar.forZone(ZoneOffset.UTC, Year.of(2016));

        // Asserts for a recurring holiday
        Instant jan1st2016_06_59_59_UTC = LocalDate.of(2016, Month.JANUARY, 1).atTime(6, 59, 59).atZone(ZoneOffset.UTC).toInstant();
        assertThat(zonedView.eventFor(jan1st2016_06_59_59_UTC).getCode()).isEqualTo(OFF_PEAK_EVENT_CODE);
        Instant jan1st2016_07_0_0_UTC = jan1st2016_06_59_59_UTC.plusSeconds(1);
        assertThat(zonedView.eventFor(jan1st2016_07_0_0_UTC).getCode()).isEqualTo(OFF_PEAK_EVENT_CODE);
        Instant jan1st2016_21_0_1_UTC = LocalDate.of(2016, Month.JANUARY, 1).atTime(21, 0, 1).atZone(ZoneOffset.UTC).toInstant();
        assertThat(zonedView.eventFor(jan1st2016_21_0_1_UTC).getCode()).isEqualTo(OFF_PEAK_EVENT_CODE);

        // Asserts for normal day
        Instant feb1st2016_06_59_59_UTC = LocalDate.of(2016, Month.FEBRUARY, 1).atTime(6, 59, 59).atZone(ZoneOffset.UTC).toInstant();
        assertThat(zonedView.eventFor(feb1st2016_06_59_59_UTC).getCode()).isEqualTo(OFF_PEAK_EVENT_CODE);
        Instant feb1st2016_07_00_00_UTC = feb1st2016_06_59_59_UTC.plusSeconds(1);
        assertThat(zonedView.eventFor(feb1st2016_07_00_00_UTC).getCode()).isEqualTo(PEAK_EVENT_CODE);
        Instant feb1st2016_21_00_01_UTC = LocalDate.of(2016, Month.FEBRUARY, 1).atTime(21, 0, 1).atZone(ZoneOffset.UTC).toInstant();
        assertThat(zonedView.eventFor(feb1st2016_21_00_01_UTC).getCode()).isEqualTo(OFF_PEAK_EVENT_CODE);

        // Asserts for fixed holiday day
        Instant feb15th2016_06_59_59_UTC = LocalDate.of(2016, Month.FEBRUARY, 15).atTime(6, 59, 59).atZone(ZoneOffset.UTC).toInstant();
        assertThat(zonedView.eventFor(feb15th2016_06_59_59_UTC).getCode()).isEqualTo(OFF_PEAK_EVENT_CODE);
        Instant feb15th2016_07_00_00_UTC = feb15th2016_06_59_59_UTC.plusSeconds(1);
        assertThat(zonedView.eventFor(feb15th2016_07_00_00_UTC).getCode()).isEqualTo(OFF_PEAK_EVENT_CODE);
        Instant feb15th2016_21_00_01_UTC = LocalDate.of(2016, Month.FEBRUARY, 15).atTime(6, 59, 59).atZone(ZoneOffset.UTC).toInstant();
        assertThat(zonedView.eventFor(feb15th2016_21_00_01_UTC).getCode()).isEqualTo(OFF_PEAK_EVENT_CODE);
    }

    @Test
    @Transactional
    public void simplePeakOffPeakCalendar_ESTView_ESTRequest() {
        ServerCalendar calendar = this.createSimplePeakOffPeakCalendar("SimpleEST");

        // Business methods
        ZoneId zoneId = TimeZone.getTimeZone("EST").toZoneId();
        ServerCalendar.ZonedView zonedView = calendar.forZone(zoneId, Year.of(2016));

        // Asserts for a recurring holiday
        Instant jan1st2016_06_59_59 = LocalDate.of(2016, Month.JANUARY, 1).atTime(6, 59, 59).atZone(zoneId).toInstant();
        assertThat(zonedView.eventFor(jan1st2016_06_59_59).getCode()).isEqualTo(OFF_PEAK_EVENT_CODE);
        Instant jan1st2016_07_0_0 = jan1st2016_06_59_59.plusSeconds(1);
        assertThat(zonedView.eventFor(jan1st2016_07_0_0).getCode()).isEqualTo(OFF_PEAK_EVENT_CODE);
        Instant jan1st2016_21_0_1 = LocalDate.of(2016, Month.JANUARY, 1).atTime(21, 0, 1).atZone(zoneId).toInstant();
        assertThat(zonedView.eventFor(jan1st2016_21_0_1).getCode()).isEqualTo(OFF_PEAK_EVENT_CODE);

        // Asserts for normal day
        Instant feb1st2016_06_59_59 = LocalDate.of(2016, Month.FEBRUARY, 1).atTime(6, 59, 59).atZone(zoneId).toInstant();
        assertThat(zonedView.eventFor(feb1st2016_06_59_59).getCode()).isEqualTo(OFF_PEAK_EVENT_CODE);
        Instant feb1st2016_07_00_00 = feb1st2016_06_59_59.plusSeconds(1);
        assertThat(zonedView.eventFor(feb1st2016_07_00_00).getCode()).isEqualTo(PEAK_EVENT_CODE);
        Instant feb1st2016_21_00_01 = LocalDate.of(2016, Month.FEBRUARY, 1).atTime(21, 0, 1).atZone(zoneId).toInstant();
        assertThat(zonedView.eventFor(feb1st2016_21_00_01).getCode()).isEqualTo(OFF_PEAK_EVENT_CODE);

        // Asserts for fixed holiday day
        Instant feb15th2016_06_59_59 = LocalDate.of(2016, Month.FEBRUARY, 15).atTime(6, 59, 59).atZone(zoneId).toInstant();
        assertThat(zonedView.eventFor(feb15th2016_06_59_59).getCode()).isEqualTo(OFF_PEAK_EVENT_CODE);
        Instant feb15th2016_07_00_00 = feb15th2016_06_59_59.plusSeconds(1);
        assertThat(zonedView.eventFor(feb15th2016_07_00_00).getCode()).isEqualTo(OFF_PEAK_EVENT_CODE);
        Instant feb15th2016_21_00_01 = LocalDate.of(2016, Month.FEBRUARY, 15).atTime(6, 59, 59).atZone(zoneId).toInstant();
        assertThat(zonedView.eventFor(feb15th2016_21_00_01).getCode()).isEqualTo(OFF_PEAK_EVENT_CODE);

        /* Asserts for DST winter to summer which was on March 13 for timezone EST in year 2016,
         * which is also configured as a fixed holiday in the calendar. */
        Instant justBeforeDST_W2S = LocalDate.of(2016, Month.FEBRUARY, 15).atTime(1, 59, 59).atZone(zoneId).toInstant();
        assertThat(zonedView.eventFor(justBeforeDST_W2S).getCode()).isEqualTo(OFF_PEAK_EVENT_CODE);
        Instant onDST_W2S = justBeforeDST_W2S.plusSeconds(1);
        assertThat(zonedView.eventFor(onDST_W2S).getCode()).isEqualTo(OFF_PEAK_EVENT_CODE);
        Instant justAfterDST_W2S = onDST_W2S.plusSeconds(1);
        assertThat(zonedView.eventFor(justAfterDST_W2S).getCode()).isEqualTo(OFF_PEAK_EVENT_CODE);

        /* Asserts for DST summer to winter which was on Nov 6 for timezone EST in year 2016,
         * which is configured as a normal day in the calendar. */
        Instant justBeforeDST_S2W = LocalDate.of(2016, Month.NOVEMBER, 6).atTime(2, 59, 59).atZone(zoneId).toInstant();
        assertThat(zonedView.eventFor(justBeforeDST_S2W).getCode()).isEqualTo(OFF_PEAK_EVENT_CODE);
        Instant onDST_S2W = justBeforeDST_S2W.plusSeconds(1);
        assertThat(zonedView.eventFor(onDST_S2W).getCode()).isEqualTo(OFF_PEAK_EVENT_CODE);
        Instant justAfterDST_S2W = onDST_S2W.plusSeconds(1);
        assertThat(zonedView.eventFor(justAfterDST_S2W).getCode()).isEqualTo(OFF_PEAK_EVENT_CODE);
    }

    @Test
    @Transactional
    public void simplePeakOffPeakCalendar_ESTView_UTCRequest() {
        ServerCalendar calendar = this.createSimplePeakOffPeakCalendar("SimpleEST");

        // Business methods
        ZoneId zoneId = TimeZone.getTimeZone("EST").toZoneId();
        ServerCalendar.ZonedView zonedView = calendar.forZone(zoneId, Year.of(2016));

        // Asserts for a recurring holiday
        Instant jan1st2016_21_00_01_UTC = LocalDate.of(2016, Month.MAY, 1).atTime(21, 0, 1).atZone(ZoneOffset.UTC).toInstant();
        assertThat(zonedView.eventFor(jan1st2016_21_00_01_UTC).getCode()).isEqualTo(OFF_PEAK_EVENT_CODE);

        // Asserts for normal day
        Instant feb1st2016_21_00_01_UTC = LocalDate.of(2016, Month.FEBRUARY, 1).atTime(21, 0, 1).atZone(ZoneOffset.UTC).toInstant();
        assertThat(zonedView.eventFor(feb1st2016_21_00_01_UTC).getCode()).isEqualTo(PEAK_EVENT_CODE);

        // Asserts for fixed holiday day
        Instant feb15th2016_21_00_01_UTC = LocalDate.of(2016, Month.FEBRUARY, 15).atTime(21, 0, 1).atZone(ZoneOffset.UTC).toInstant();
        assertThat(zonedView.eventFor(feb15th2016_21_00_01_UTC).getCode()).isEqualTo(OFF_PEAK_EVENT_CODE);

        /* Asserts for DST winter to summer which was on March 13 for timezone EST in year 2016,
         * which is also configured as a fixed holiday in the calendar.
         * During winter, EST is 5 hours behind UTC so 01:59 in UTC is 20:59 of day before in EST
         * and should still be PEAK. */
        Instant justBeforeDST_W2S_UTC = LocalDate.of(2016, Month.FEBRUARY, 15).atTime(1, 59, 59).atZone(ZoneOffset.UTC).toInstant();
        assertThat(zonedView.eventFor(justBeforeDST_W2S_UTC).getCode()).isEqualTo(PEAK_EVENT_CODE);
        Instant onDST_W2S_UTC = justBeforeDST_W2S_UTC.plusSeconds(1);
        assertThat(zonedView.eventFor(onDST_W2S_UTC).getCode()).isEqualTo(OFF_PEAK_EVENT_CODE); // 21:00 in EST on previous day is switch over to OFF_PEAK
        Instant justAfterDST_W2S_UTC = onDST_W2S_UTC.plusSeconds(1);
        assertThat(zonedView.eventFor(justAfterDST_W2S_UTC).getCode()).isEqualTo(OFF_PEAK_EVENT_CODE);  // 21:00 in EST on previous day is OFF_PEAK

        /* Asserts for DST summer to winter which was on Nov 6 for timezone EST in year 2016,
         * which is configured as a normal day in the calendar.
         * During summer, EST (which is then actually called EDT) is 4 hours behind UTC
         * so 02:59:59 in UTC is 22:59:59 of day before in EST and should be OFF_PEAK. */
        Instant justBeforeDST_S2W_UTC = LocalDate.of(2016, Month.NOVEMBER, 6).atTime(2, 59, 59).atZone(ZoneOffset.UTC).toInstant();
        assertThat(zonedView.eventFor(justBeforeDST_S2W_UTC).getCode()).isEqualTo(OFF_PEAK_EVENT_CODE);
        Instant onDST_S2W_UTC = justBeforeDST_S2W_UTC.plusSeconds(1);
        assertThat(zonedView.eventFor(onDST_S2W_UTC).getCode()).isEqualTo(OFF_PEAK_EVENT_CODE);
        Instant justAfterDST_S2W_UTC = onDST_S2W_UTC.plusSeconds(1);
        assertThat(zonedView.eventFor(justAfterDST_S2W_UTC).getCode()).isEqualTo(OFF_PEAK_EVENT_CODE);
    }

    private ServerCalendar createSimplePeakOffPeakCalendar(String name) {
        Category category = getCalendarService().findCategoryByName(OutOfTheBoxCategory.TOU.name()).orElseThrow(() -> new IllegalStateException("Setup failed because out of the box category TOU is missing"));
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
