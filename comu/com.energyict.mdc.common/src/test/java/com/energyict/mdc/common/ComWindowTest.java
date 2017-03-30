/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.interval.PartialTime;

import org.joda.time.DateTimeConstants;

import java.util.Calendar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertFalse;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link ComWindow} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-17 (15:50)
 */
@RunWith(MockitoJUnitRunner.class)
public class ComWindowTest {

    private static final int START_SECONDS = 3650;  // One hour and 50 seconds
    private static final int END_SECONDS = 7500;    // Two hours and 5 minutes
    private static final int TEN_PM = DateTimeConstants.SECONDS_PER_HOUR * 22;
    private static final int TWO_AM = DateTimeConstants.SECONDS_PER_HOUR * 2;

    @Test
    public void testConstructorWithSeconds() {
        ComWindow comWindow = new ComWindow(START_SECONDS, END_SECONDS);
        assertNotNull(comWindow.getStart());
        assertEquals(START_SECONDS * 1000, comWindow.getStart().getMillis());
        assertNotNull(comWindow.getEnd());
        assertEquals(END_SECONDS * 1000, comWindow.getEnd().getMillis());
    }

    @Test
    public void testConstructorWithPartialTime() {
        ComWindow comWindow = new ComWindow(PartialTime.fromSeconds(START_SECONDS), PartialTime.fromSeconds(END_SECONDS));
        assertNotNull(comWindow.getStart());
        assertEquals(START_SECONDS * 1000, comWindow.getStart().getMillis());
        assertNotNull(comWindow.getEnd());
        assertEquals(END_SECONDS * 1000, comWindow.getEnd().getMillis());
    }

    @Test
    public void testEqualityWithSame() {
        ComWindow comWindow = new ComWindow(START_SECONDS, END_SECONDS);
        assertTrue("Was expecting comWindow.equals(comWindow) to return true", comWindow.equals(comWindow));
    }

    @Test
    public void testEquality() {
        ComWindow comWindow = new ComWindow(START_SECONDS, END_SECONDS);
        ComWindow clone = new ComWindow(START_SECONDS, END_SECONDS);
        assertTrue(comWindow.equals(clone));
    }

    @Test
    public void testInEqualityWithString() {
        ComWindow comWindow = new ComWindow(START_SECONDS, END_SECONDS);
        assertFalse(comWindow.equals(comWindow.toString()));
    }

    @Test
    public void testInEqualityWithNull() {
        ComWindow comWindow = new ComWindow(START_SECONDS, END_SECONDS);
        assertFalse(comWindow.equals(null));
    }

    @Test
    public void testEqualObjectsShouldHaveEqualHashCodes() {
        ComWindow comWindow = new ComWindow(START_SECONDS, END_SECONDS);
        ComWindow clone = new ComWindow(START_SECONDS, END_SECONDS);
        assertTrue("Was expecting equal objects to have an equal hashCode", comWindow.hashCode() == clone.hashCode());
    }

    @Test
    public void testIncludesTimeDuration() {
        ComWindow comWindow = new ComWindow(START_SECONDS, END_SECONDS);
        assertTrue(comWindow.includes(new TimeDuration(2, TimeDuration.TimeUnit.HOURS)));
    }

    @Test
    public void testIncludesTimeDurationThatRunsAcrossMidnight () {
        ComWindow comWindow = new ComWindow(TEN_PM, TWO_AM);
        assertTrue(comWindow.includes(new TimeDuration(23, TimeDuration.TimeUnit.HOURS)));
    }

    @Test
    public void testIncludesTimeDurationThatRunsAcrossMidnightAtStart () {
        ComWindow comWindow = new ComWindow(TEN_PM, TWO_AM);
        assertTrue(comWindow.includes(new TimeDuration(22, TimeDuration.TimeUnit.HOURS)));
    }

    @Test
    public void testIncludesTimeDurationThatRunsAcrossMidnightAtEnd () {
        ComWindow comWindow = new ComWindow(TEN_PM, TWO_AM);
        assertTrue(comWindow.includes(new TimeDuration(2, TimeDuration.TimeUnit.HOURS)));
    }

    @Test
    public void testIncludesTimeDurationAtStart() {
        ComWindow comWindow = new ComWindow(START_SECONDS, END_SECONDS);
        assertTrue(comWindow.includes(new TimeDuration(START_SECONDS, TimeDuration.TimeUnit.SECONDS)));
    }

    @Test
    public void testIncludesTimeDurationAtEnd() {
        ComWindow comWindow = new ComWindow(START_SECONDS, END_SECONDS);
        assertTrue(comWindow.includes(new TimeDuration(END_SECONDS, TimeDuration.TimeUnit.SECONDS)));
    }

    @Test
    public void testDoesNotIncludeTimeDurationAfterEnd () {
        ComWindow comWindow = new ComWindow(START_SECONDS, END_SECONDS);
        assertFalse(comWindow.includes(new TimeDuration(12, TimeDuration.TimeUnit.HOURS)));
    }

    @Test
    public void testDoesNotIncludeTimeDurationBeforeStart () {
        ComWindow comWindow = new ComWindow(START_SECONDS, END_SECONDS);
        assertFalse(comWindow.includes(new TimeDuration(1, TimeDuration.TimeUnit.HOURS)));
    }

    @Test
    public void testIncludesCalendarAtStart () {
        ComWindow comWindow = new ComWindow(START_SECONDS, END_SECONDS);
        Calendar calendar = this.getCalendar();
        calendar.add(Calendar.SECOND, START_SECONDS);
        assertTrue(comWindow.includes(calendar));
    }

    @Test
    public void testIncludesCalendarInMiddle () {
        ComWindow comWindow = new ComWindow(START_SECONDS, END_SECONDS);
        Calendar calendar = this.getCalendar();
        calendar.add(Calendar.SECOND, START_SECONDS + 1);
        assertTrue(comWindow.includes(calendar));
    }

    @Test
    public void testIncludesCalendarAtEnd () {
        ComWindow comWindow = new ComWindow(START_SECONDS, END_SECONDS);
        Calendar calendar = this.getCalendar();
        calendar.add(Calendar.SECOND, END_SECONDS);
        assertTrue(comWindow.includes(calendar));
    }

    @Test
    public void testDoesNotIncludeCalendarBeforeStart() {
        ComWindow comWindow = new ComWindow(START_SECONDS, END_SECONDS);
        Calendar calendar = this.getCalendar();
        assertFalse(comWindow.includes(calendar));
    }

    @Test
    public void testDoesNotIncludeCalendarAfterEnd() {
        ComWindow comWindow = new ComWindow(START_SECONDS, END_SECONDS);
        Calendar calendar = this.getCalendar();
        calendar.add(Calendar.SECOND, END_SECONDS + 1);
        assertFalse(comWindow.includes(calendar));
    }

    private Calendar getCalendar () {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2013);
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    @Test
    public void testIsNotEmpty () {
        ComWindow comWindow = new ComWindow(START_SECONDS, END_SECONDS);
        assertThat(comWindow.isEmpty()).isFalse();
    }

    @Test
    public void testIsEmptyWithStartAndEndAtNulls () {
        ComWindow comWindow = new ComWindow(null, null);
        assertThat(comWindow.isEmpty()).isTrue();
    }

    @Test
    public void testIsEmptyWithStartAndEndAtMidnight () {
        ComWindow comWindow = new ComWindow(PartialTime.fromSeconds(0), PartialTime.fromSeconds(0));
        assertThat(comWindow.isEmpty()).isTrue();
    }

    @Test
    public void testIsEmptyWithStartAndEndAtNoon () {
        ComWindow comWindow = new ComWindow(PartialTime.fromSeconds(DateTimeConstants.SECONDS_PER_HOUR * 12), PartialTime.fromSeconds(DateTimeConstants.SECONDS_PER_HOUR * 12));
        assertThat(comWindow.isEmpty()).isTrue();
    }

}