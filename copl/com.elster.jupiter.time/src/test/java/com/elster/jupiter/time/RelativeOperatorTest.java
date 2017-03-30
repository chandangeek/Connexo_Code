/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time;

import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;
import java.util.Locale;

import static org.fest.assertions.api.Assertions.assertThat;

public class RelativeOperatorTest {

    @Rule
    public TestRule mcMurdo = Using.timeZoneOfMcMurdo();

    public static final Locale LOCALE_WITH_MONDAY_AS_FIRST_DAY_OF_WEEK = new Locale("nl", "BE");
    public static final Locale LOCALE_WITH_SUNDAY_AS_FIRST_DAY_OF_WEEK = new Locale("en", "US");

    @Test
    public void testEqualDayOfWeekWithMondayAsFirstDayOfWeek() {
        withLocale(LOCALE_WITH_MONDAY_AS_FIRST_DAY_OF_WEEK, () -> {
            assertThat(WeekFields.of(Locale.getDefault()).getFirstDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);

            ZonedDateTime monday = ZonedDateTime.of(2015, 10, 5, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
            ZonedDateTime result = RelativeOperator.EQUAL.apply(monday, RelativeField.DAY_OF_WEEK, DayOfWeek.MONDAY.getValue());

            assertThat(result).isEqualTo(monday);
        });
    }

    @Test
    public void testEqualDayOfWeekWithSundayAsFirstDayOfWeek() {
        withLocale(LOCALE_WITH_SUNDAY_AS_FIRST_DAY_OF_WEEK, () -> {
            assertThat(WeekFields.of(Locale.getDefault()).getFirstDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);

            ZonedDateTime sunday = ZonedDateTime.of(2015, 10, 4, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
            ZonedDateTime result = RelativeOperator.EQUAL.apply(sunday, RelativeField.DAY_OF_WEEK, DayOfWeek.SUNDAY.getValue());

            assertThat(result).isEqualTo(sunday);
        });
    }

    @Test
    public void testTuesDayWithMondayAsFirstDayOfWeek() {
        withLocale(LOCALE_WITH_MONDAY_AS_FIRST_DAY_OF_WEEK, () -> {
            assertThat(WeekFields.of(Locale.getDefault()).getFirstDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);

            ZonedDateTime monday = ZonedDateTime.of(2015, 10, 5, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
            ZonedDateTime result = RelativeOperator.EQUAL.apply(monday, RelativeField.DAY_OF_WEEK, DayOfWeek.TUESDAY.getValue());

            ZonedDateTime tuesday = monday.plusDays(1);
            assertThat(result).isEqualTo(tuesday);
        });
    }

    @Test
    public void testTuesDayDayOfWeekWithSundayAsFirstDayOfWeek() {
        withLocale(LOCALE_WITH_SUNDAY_AS_FIRST_DAY_OF_WEEK, () -> {
            assertThat(WeekFields.of(Locale.getDefault()).getFirstDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);

            ZonedDateTime sunday = ZonedDateTime.of(2015, 10, 4, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
            ZonedDateTime result = RelativeOperator.EQUAL.apply(sunday, RelativeField.DAY_OF_WEEK, DayOfWeek.TUESDAY.getValue());

            ZonedDateTime tuesday = sunday.plusDays(2);

            assertThat(result).isEqualTo(tuesday);
        });
    }

    @Test
    public void testPreviousTuesDayWithMondayAsFirstDayOfWeek() {
        withLocale(LOCALE_WITH_MONDAY_AS_FIRST_DAY_OF_WEEK, () -> {
            assertThat(WeekFields.of(Locale.getDefault()).getFirstDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);

            ZonedDateTime thursday = ZonedDateTime.of(2015, 10, 8, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
            ZonedDateTime result = RelativeOperator.EQUAL.apply(thursday, RelativeField.DAY_OF_WEEK, DayOfWeek.TUESDAY.getValue());

            ZonedDateTime tuesday = thursday.minusDays(2);
            assertThat(result).isEqualTo(tuesday);
        });
    }

    @Test
    public void testPreviousTuesDayDayOfWeekWithSundayAsFirstDayOfWeek() {
        withLocale(LOCALE_WITH_SUNDAY_AS_FIRST_DAY_OF_WEEK, () -> {
            assertThat(WeekFields.of(Locale.getDefault()).getFirstDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);

            ZonedDateTime thursday = ZonedDateTime.of(2015, 10, 8, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
            ZonedDateTime result = RelativeOperator.EQUAL.apply(thursday, RelativeField.DAY_OF_WEEK, DayOfWeek.TUESDAY.getValue());

            ZonedDateTime tuesday = thursday.minusDays(2);

            assertThat(result).isEqualTo(tuesday);
        });
    }

    private static void withLocale(Locale locale, Runnable runnable) {
        Locale toRestore = Locale.getDefault();
        Locale.setDefault(locale);
        try {
            runnable.run();
        } finally {
            Locale.setDefault(toRestore);
        }

    }
}