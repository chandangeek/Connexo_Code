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
 * Tests the comparable aspect of {@link DayMonthTime}.
 * Note that the equality aspects are already covered in the {@link DayMonthTimeEqualsContractTest}.
 */
public class DayMonthTimeComparisonTest {

    @Test
    public void equalsIsNotBefore() {
        DayMonthTime myBirthDay = DayMonthTime.from(MonthDay.of(Month.MAY, 2), LocalTime.of(1, 40));

        assertThat(myBirthDay.isBefore(myBirthDay)).isFalse();
    }

    @Test
    public void equalsIsNotAfter() {
        DayMonthTime myBirthDay = DayMonthTime.from(MonthDay.of(Month.MAY, 2), LocalTime.of(1, 40));

        assertThat(myBirthDay.isAfter(myBirthDay)).isFalse();
    }

    @Test
    public void differentMonthDayButSameTime() {
        DayMonthTime jan1st = DayMonthTime.from(MonthDay.of(Month.JANUARY, 1), LocalTime.of(11, 0));
        DayMonthTime feb1st = DayMonthTime.from(MonthDay.of(Month.FEBRUARY, 1), LocalTime.of(11, 0));

        assertThat(jan1st.isBefore(feb1st)).isTrue();
        assertThat(jan1st.isAfter(feb1st)).isFalse();
        assertThat(feb1st.isAfter(jan1st)).isTrue();
        assertThat(feb1st.isBefore(jan1st)).isFalse();
        assertThat(jan1st).usingDefaultComparator().isLessThan(feb1st);
        assertThat(feb1st).usingDefaultComparator().isGreaterThan(jan1st);
    }

    @Test
    public void sameMonthDayDifferentTime() {
        DayMonthTime jan1st11AM = DayMonthTime.from(MonthDay.of(Month.JANUARY, 1), LocalTime.of(11, 0));
        DayMonthTime jan1st11PM = DayMonthTime.from(MonthDay.of(Month.JANUARY, 1), LocalTime.of(23, 0));

        assertThat(jan1st11AM.isBefore(jan1st11PM)).isTrue();
        assertThat(jan1st11AM.isAfter(jan1st11PM)).isFalse();
        assertThat(jan1st11PM.isAfter(jan1st11AM)).isTrue();
        assertThat(jan1st11PM.isBefore(jan1st11AM)).isFalse();
        assertThat(jan1st11AM).usingDefaultComparator().isLessThan(jan1st11PM);
        assertThat(jan1st11PM).usingDefaultComparator().isGreaterThan(jan1st11AM);
    }

    @Test
    public void compareEquals() {
        DayMonthTime myBirthDay = DayMonthTime.from(MonthDay.of(Month.MAY, 2), LocalTime.of(1, 40));

        assertThat(myBirthDay).isEqualByComparingTo(myBirthDay);
    }

}