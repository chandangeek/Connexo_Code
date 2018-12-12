/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.time;

import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test the aspects of {@link DayMonthTime} that are not covered yet in
 * <ul>
 * <li>{@link DayMonthTimeEqualsContractTest}</li>
 * <li>{@link DayMonthTimeComparisonTest}</li>
 * <li>{@link DayMonthTimeConverterTest}</li>
 * </ul>
 */
public class DayMonthTimeTest {

    @Test
    public void fromMidnight() {
        // Business method
        DayMonthTime may2nd = DayMonthTime.fromMidnight(MonthDay.of(Month.MAY, 2));

        // Asserts
        assertThat(may2nd.getMonth()).isEqualTo(Month.MAY);
        assertThat(may2nd.getMonthValue()).isEqualTo(5);
        assertThat(may2nd.getDayOfMonth()).isEqualTo(2);
        assertThat(may2nd.getHour()).isEqualTo(0);
        assertThat(may2nd.getMinute()).isEqualTo(0);
        assertThat(may2nd.getSecond()).isEqualTo(0);
        assertThat(may2nd.getNano()).isEqualTo(0);
    }

    @Test
    public void from() {
        // Business method
        DayMonthTime may2nd = DayMonthTime.from(MonthDay.of(Month.MAY, 17), LocalTime.of(11, 23, 31, 916742));

        // Asserts
        assertThat(may2nd.getMonth()).isEqualTo(Month.MAY);
        assertThat(may2nd.getMonthValue()).isEqualTo(5);
        assertThat(may2nd.getDayOfMonth()).isEqualTo(17);
        assertThat(may2nd.getHour()).isEqualTo(11);
        assertThat(may2nd.getMinute()).isEqualTo(23);
        assertThat(may2nd.getSecond()).isEqualTo(31);
        assertThat(may2nd.getNano()).isEqualTo(916742);
    }

    @Test
    public void toStringOnMidnightDoesNotNPE() {
        DayMonthTime midnight = DayMonthTime.fromMidnight(MonthDay.of(Month.MAY, 2));

        // Business method && asserts
        assertThat(midnight.toString()).isNotEmpty();
    }

}