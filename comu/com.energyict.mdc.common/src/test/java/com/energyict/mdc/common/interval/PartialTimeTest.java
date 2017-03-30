/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.interval;

import org.joda.time.DateTimeConstants;

import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the {@link PartialTime} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-16 (13:18)
 */
public class PartialTimeTest {

    private static final int MILLIS_IN_HOUR = 3600000;
    private static final int MILLIS_IN_MINUTE = 60000;

    @Test
    public void testFromMilliseconds() {
        assertThat(PartialTime.fromMilliSeconds(MILLIS_IN_HOUR).getMillis()).isEqualTo(MILLIS_IN_HOUR);
    }

    @Test
    public void testFromMinutes() {
        assertThat(PartialTime.fromMinutes(23).getMillis()).isEqualTo(23 * MILLIS_IN_MINUTE);
    }

    @Test
    public void testFromHours() {
        assertThat(PartialTime.fromHours(23).getMillis()).isEqualTo(23 * MILLIS_IN_HOUR);
    }

    @Test
    public void testFromHoursAndMinutes() {
        int expected = 23 * MILLIS_IN_HOUR + 23 * MILLIS_IN_MINUTE;
        assertThat(PartialTime.fromHoursAndMinutes(23, 23).getMillis()).isEqualTo(expected);
    }

    @Test
    public void testFromHoursMinutesAndSeconds() {
        int expected = 23 * MILLIS_IN_HOUR + 23 * MILLIS_IN_MINUTE + 23000;
        assertThat(PartialTime.fromHoursMinutesAndSeconds(23, 23, 23).getMillis()).isEqualTo(expected);
    }

    @Test
    public void testOverflow() {
        assertThat(PartialTime.fromHours(20).plus(PartialTime.fromHours(7))).isEqualTo(PartialTime.fromHours(3));
    }

    @Test
    public void testEqualToIdentical() {
        PartialTime partialTime = PartialTime.fromSeconds(23);
        assertThat(partialTime.equals(partialTime)).isTrue();
    }

    @Test
    public void testEqualToSame() {
        PartialTime partialTime = PartialTime.fromSeconds(23);
        PartialTime clone = PartialTime.fromSeconds(23);
        assertThat(partialTime.equals(clone)).isTrue();
    }

    @Test
    public void testEqualObjectsShouldHaveSameHashCode() {
        PartialTime partialTime = PartialTime.fromSeconds(23);
        PartialTime clone = PartialTime.fromSeconds(23);
        assertThat(partialTime.hashCode() == clone.hashCode()).as("Equal objects should have the same hashCode").isTrue();
    }

    @Test
    public void testNotEqualToDifferent() {
        PartialTime partialTime = PartialTime.fromSeconds(23);
        PartialTime other = PartialTime.fromHours(23);
        assertThat(partialTime.equals(other)).isFalse();
    }

    @Test
    public void testNotEqualToString() {
        PartialTime partialTime = PartialTime.fromSeconds(23);
        assertThat(partialTime.equals("String")).isFalse();
    }

    @Test
    public void testNotEqualToNull() {
        PartialTime partialTime = PartialTime.fromSeconds(23);
        assertThat(partialTime.equals(null)).isFalse();
    }

    @Test
    public void testCompareToIdentical() {
        PartialTime partialTime = PartialTime.fromSeconds(23);
        assertThat(partialTime.compareTo(partialTime)).isZero();
    }

    @Test
    public void testCompareToSame() {
        PartialTime partialTime = PartialTime.fromSeconds(23);
        PartialTime clone = PartialTime.fromSeconds(23);
        assertThat(partialTime.compareTo(clone)).isZero();
    }

    @Test
    public void testCompareToLater() {
        PartialTime before = PartialTime.fromHours(15);
        PartialTime after = PartialTime.fromHours(23);
        assertThat(before.compareTo(after)).isNegative();
    }

    @Test
    public void testCompareToAfter() {
        PartialTime before = PartialTime.fromHours(15);
        PartialTime after = PartialTime.fromHours(23);
        assertThat(after.compareTo(before)).isPositive();
    }

    @Test
    public void testBefore() {
        PartialTime before = PartialTime.fromHours(15);
        PartialTime after = PartialTime.fromHours(23);
        assertThat(before.before(after)).isTrue();
    }

    @Test
    public void testNotBeforeEqual() {
        PartialTime partialTime = PartialTime.fromHours(15);
        assertThat(partialTime.before(partialTime)).isFalse();
    }

    @Test
    public void testAfter() {
        PartialTime before = PartialTime.fromHours(15);
        PartialTime after = PartialTime.fromHours(23);
        assertThat(after.after(before)).isTrue();
    }

    @Test
    public void testNotAfterEqual() {
        PartialTime partialTime = PartialTime.fromHours(23);
        assertThat(partialTime.after(partialTime)).isFalse();
    }

    @Test
    public void testToStringIsNotNull() {
        assertThat(PartialTime.fromHours(23).toString()).isNotNull();
    }

    @Test
    public void testToStringDoesNotIncludeSecondsWhenZero () {
        assertThat(PartialTime.fromMinutes(90).toString()).isEqualTo("01:30");
    }

    @Test
    public void testToStringUsesTwoDigitsForAllParts () {
        int seconds = DateTimeConstants.SECONDS_PER_HOUR + DateTimeConstants.SECONDS_PER_MINUTE * 7 + 3;
        assertThat(PartialTime.fromSeconds(seconds).toString()).isEqualTo("01:07:03");
    }

    @Test
    public void testCopyTo () {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.YEAR, 2013);
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 9);
        calendar.set(Calendar.HOUR_OF_DAY, 14);
        calendar.set(Calendar.MINUTE, 4);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        PartialTime partialTime = PartialTime.fromHours(3);

        Calendar expectedCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expectedCalendar.set(Calendar.YEAR, 2013);
        expectedCalendar.set(Calendar.MONTH, Calendar.JANUARY);
        expectedCalendar.set(Calendar.DAY_OF_MONTH, 9);
        expectedCalendar.set(Calendar.HOUR_OF_DAY, 3);
        expectedCalendar.set(Calendar.MINUTE, 0);
        expectedCalendar.set(Calendar.SECOND, 0);
        expectedCalendar.set(Calendar.MILLISECOND, 0);

        // Business method
        partialTime.copyTo(calendar);

        // Asserts
        assertThat(calendar).isEqualTo(expectedCalendar);
    }

}
