/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.time;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the conversion aspects of {@link DayMonthTime}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-07-15 (14:08)
 */
public class DayMonthTimeConverterTest {

    @Test
    public void atYear() {
        DayMonthTime now = DayMonthTime.from(MonthDay.of(Month.JULY, 15), LocalTime.of(14, 9));

        // Business method
        LocalDateTime localDateTime = now.atYear(2001);

        // Asserts
        assertThat(localDateTime).isEqualTo(LocalDateTime.of(2001, Month.JULY, 15, 14, 9, 0, 0));
    }

    @Test
    public void withMonth() {
        DayMonthTime now = DayMonthTime.from(MonthDay.of(Month.JULY, 15), LocalTime.of(14, 9));

        // Business method
        DayMonthTime other = now.with(Month.APRIL);

        // Asserts
        assertThat(other).isEqualTo(DayMonthTime.from(MonthDay.of(Month.APRIL, 15), LocalTime.of(14, 9)));
    }

    @Test
    public void withIntMonth() {
        DayMonthTime now = DayMonthTime.from(MonthDay.of(Month.JULY, 15), LocalTime.of(14, 9));

        // Business method
        DayMonthTime other = now.withMonth(4);

        // Asserts
        assertThat(other).isEqualTo(DayMonthTime.from(MonthDay.of(Month.APRIL, 15), LocalTime.of(14, 9)));
    }

    @Test
    public void withDayOfMonth() {
        DayMonthTime now = DayMonthTime.from(MonthDay.of(Month.JULY, 15), LocalTime.of(14, 9));

        // Business method
        DayMonthTime other = now.withDayOfMonth(4);

        // Asserts
        assertThat(other).isEqualTo(DayMonthTime.from(MonthDay.of(Month.JULY, 4), LocalTime.of(14, 9)));
    }

    @Test
    public void withHour() {
        DayMonthTime now = DayMonthTime.from(MonthDay.of(Month.JULY, 15), LocalTime.of(14, 9));

        // Business method
        DayMonthTime other = now.withHour(16);

        // Asserts
        assertThat(other).isEqualTo(DayMonthTime.from(MonthDay.of(Month.JULY, 15), LocalTime.of(16, 9)));
    }

    @Test
    public void withMinute() {
        DayMonthTime now = DayMonthTime.from(MonthDay.of(Month.JULY, 15), LocalTime.of(14, 9));

        // Business method
        DayMonthTime other = now.withMinute(16);

        // Asserts
        assertThat(other).isEqualTo(DayMonthTime.from(MonthDay.of(Month.JULY, 15), LocalTime.of(14, 16)));
    }

    @Test
    public void withSecond() {
        DayMonthTime now = DayMonthTime.from(MonthDay.of(Month.JULY, 15), LocalTime.of(14, 9));

        // Business method
        DayMonthTime other = now.withSecond(16);

        // Asserts
        assertThat(other).isEqualTo(DayMonthTime.from(MonthDay.of(Month.JULY, 15), LocalTime.of(14, 9, 16)));
    }

    @Test
    public void withNano() {
        DayMonthTime now = DayMonthTime.from(MonthDay.of(Month.JULY, 15), LocalTime.of(14, 9));

        // Business method
        DayMonthTime other = now.withNano(16);

        // Asserts
        assertThat(other).isEqualTo(DayMonthTime.from(MonthDay.of(Month.JULY, 15), LocalTime.of(14, 9, 0, 16)));
    }

}