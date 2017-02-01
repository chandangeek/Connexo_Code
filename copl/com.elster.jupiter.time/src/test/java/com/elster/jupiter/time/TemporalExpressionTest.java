/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;

/**
 * Test cases for the temporal expression class which is used in the scheduling of jobs.
 *
 * @author alex
 */
public final class TemporalExpressionTest extends EqualsContractTest {

    /**
     * The date on which the DST starts in Europe in 2008.
     */
    private static final Calendar DST_START_TIME;

    /**
     * The date on which the DST ends in Europe in 2008.
     */
    private static final Calendar DST_END_DATE;

    /**
     * The time zone that is used in Brussels, which is the one used in this test.
     */
    private static final TimeZone TIMEZONE_WITH_DST = TimeZone.getTimeZone("Europe/Brussels");

    /**
     * Time zone that is not aware of DST changes.
     */
    private static final TimeZone TIMEZONE_WITHOUT_DST = TimeZone.getTimeZone("GMT+1");

    /**
     * Date that falls inside a DST period.
     */
    private static final Calendar DATE_INSIDE_DST_PERIOD;

    /**
     * Date that falls outside a DST period.
     */
    private static final Calendar DATE_OUTSIDE_DST_PERIOD;

    /**
     * The scheduling expression. We want it to be every day at 4 AM.
     */
    private static final TemporalExpression DAILY_SCHEDULING_EXPRESSION;

    /**
     * The scheduling expression. We want it to be weekly at 4 AM.
     */
    private static final TemporalExpression WEEKLY_SCHEDULING_EXPRESSION;

    /** Initialize the calendars. */
    static {
        DST_START_TIME = Calendar.getInstance();

        DST_START_TIME.set(Calendar.YEAR, 2008);
        DST_START_TIME.set(Calendar.AM_PM, Calendar.AM);
        DST_START_TIME.set(Calendar.MONTH, Calendar.MARCH);
        DST_START_TIME.set(Calendar.DAY_OF_MONTH, 30);
        DST_START_TIME.set(Calendar.HOUR, 4);

        DST_END_DATE = Calendar.getInstance();
        DST_END_DATE.set(Calendar.YEAR, 2008);
        DST_END_DATE.set(Calendar.AM_PM, Calendar.AM);
        DST_END_DATE.set(Calendar.MONTH, Calendar.OCTOBER);
        DST_END_DATE.set(Calendar.DAY_OF_MONTH, 26);
        DST_END_DATE.set(Calendar.HOUR, 4);

        DATE_INSIDE_DST_PERIOD = Calendar.getInstance();
        DATE_INSIDE_DST_PERIOD.set(Calendar.YEAR, 2008);
        DATE_INSIDE_DST_PERIOD.set(Calendar.AM_PM, Calendar.AM);
        DATE_INSIDE_DST_PERIOD.set(Calendar.MONTH, Calendar.AUGUST);
        DATE_INSIDE_DST_PERIOD.set(Calendar.DAY_OF_MONTH, 3);

        DATE_OUTSIDE_DST_PERIOD = Calendar.getInstance();
        DATE_OUTSIDE_DST_PERIOD.set(Calendar.YEAR, 2008);
        DATE_OUTSIDE_DST_PERIOD.set(Calendar.AM_PM, Calendar.AM);
        DATE_OUTSIDE_DST_PERIOD.set(Calendar.MONTH, Calendar.AUGUST);
        DATE_OUTSIDE_DST_PERIOD.set(Calendar.DAY_OF_MONTH, 3);

        // Every day...
        final TimeDuration dailyInterval = new TimeDuration(1, TimeDuration.TimeUnit.DAYS);
        // At 4 AM...
        final TimeDuration time = new TimeDuration(4, TimeDuration.TimeUnit.HOURS);

        DAILY_SCHEDULING_EXPRESSION = new TemporalExpression(dailyInterval, time);

        final TimeDuration weeklyInterval = new TimeDuration(1, TimeDuration.TimeUnit.WEEKS);

        WEEKLY_SCHEDULING_EXPRESSION = new TemporalExpression(weeklyInterval, time);
    }

    /**
     * Tests the next occurrence when DST ends using a DST aware time zone.
     */
    @Test
    public final void testDailyNextOccurrenceAtDSTEndWithDSTAwareTimeZone() {
        doTestScheduling(DST_END_DATE, true, DAILY_SCHEDULING_EXPRESSION);
    }

    /**
     * Tests the next occurrence when DST starts with a DST aware time zone.
     */
    @Test
    public final void testDailyNextOccurrenceAtDSTStartWithDSTAwareTimeZone() {
        doTestScheduling(DST_START_TIME, true, DAILY_SCHEDULING_EXPRESSION);
    }

    /**
     * Tests the next occurrence when DST starts with a DST unaware time zone.
     */
    @Test
    public final void testDailyNextOccurrenceAtDSTEndWithDSTUnawareTimeZone() {
        doTestScheduling(DST_END_DATE, false, DAILY_SCHEDULING_EXPRESSION);
    }

    /**
     * Tests the next occurrence scheduling using a timezone that is not DST aware.
     */
    @Test
    public final void testDailyNextOccurrenceAtDSTStartWithDSTUnawareTimeZone() {
        doTestScheduling(DST_START_TIME, false, DAILY_SCHEDULING_EXPRESSION);
    }

    /**
     * Tests the determination of the next scheduling occurrence inside a DST period using a DST aware timezone.
     */
    @Test
    public final void testDailyNextOccurrenceInsideDSTPeriodWithDSTAwareTimeZone() {
        doTestScheduling(DATE_INSIDE_DST_PERIOD, true, DAILY_SCHEDULING_EXPRESSION);
    }

    /**
     * Tests the determination of the next scheduling occurrence inside a DST period using a DST unaware timezone.
     */
    @Test
    public final void testDailyNextOccurrenceInsideDSTPeriodWithDSTUnawareTimeZone() {
        doTestScheduling(DATE_INSIDE_DST_PERIOD, false, DAILY_SCHEDULING_EXPRESSION);
    }

    /**
     * Tests the determination of the next scheduling occurrence inside a DST period using a DST aware timezone.
     */
    @Test
    public final void testDailyNextOccurrenceOutsideDSTPeriodWithDSTAwareTimeZone() {
        doTestScheduling(DATE_OUTSIDE_DST_PERIOD, true, DAILY_SCHEDULING_EXPRESSION);
    }

    /**
     * Tests the determination of the next scheduling occurrence inside a DST period using a DST aware timezone.
     */
    @Test
    public final void testDailyNextOccurrenceOutsideDSTPeriodWithDSTUnawareTimeZone() {
        doTestScheduling(DATE_OUTSIDE_DST_PERIOD, false, DAILY_SCHEDULING_EXPRESSION);
    }


    /**
     * Tests the next occurrence when DST ends using a DST aware time zone.
     */
    @Test
    public final void testWeeklyNextOccurrenceAtDSTEndWithDSTAwareTimeZone() {
        doTestScheduling(DST_END_DATE, true, WEEKLY_SCHEDULING_EXPRESSION);
    }

    /**
     * Tests the next occurrence when DST starts with a DST aware time zone.
     */
    @Test
    public final void testWeekyNextOccurrenceAtDSTStartWithDSTAwareTimeZone() {
        doTestScheduling(DST_START_TIME, true, WEEKLY_SCHEDULING_EXPRESSION);
    }

    /**
     * Tests the next occurrence when DST starts with a DST unaware time zone.
     */
    @Test
    public final void testWeeklyNextOccurrenceAtDSTEndWithDSTUnawareTimeZone() {
        doTestScheduling(DST_END_DATE, false, WEEKLY_SCHEDULING_EXPRESSION);
    }

    /**
     * Tests the next occurrence scheduling using a timezone that is not DST aware.
     */
    @Test
    public final void testWeeklyNextOccurrenceAtDSTStartWithDSTUnawareTimeZone() {
        doTestScheduling(DST_START_TIME, false, WEEKLY_SCHEDULING_EXPRESSION);
    }

    /**
     * Tests the determination of the next scheduling occurrence inside a DST period using a DST aware timezone.
     */
    @Test
    public final void testWeeklyNextOccurrenceInsideDSTPeriodWithDSTAwareTimeZone() {
        doTestScheduling(DATE_INSIDE_DST_PERIOD, true, WEEKLY_SCHEDULING_EXPRESSION);
    }

    /**
     * Tests the determination of the next scheduling occurrence inside a DST period using a DST unaware timezone.
     */
    @Test
    public final void testWeeklyNextOccurrenceInsideDSTPeriodWithDSTUnawareTimeZone() {
        doTestScheduling(DATE_INSIDE_DST_PERIOD, false, WEEKLY_SCHEDULING_EXPRESSION);
    }

    /**
     * Tests the determination of the next scheduling occurrence inside a DST period using a DST aware timezone.
     */
    @Test
    public final void testWeeklyNextOccurrenceOutsideDSTPeriodWithDSTAwareTimeZone() {
        doTestScheduling(DATE_OUTSIDE_DST_PERIOD, true, WEEKLY_SCHEDULING_EXPRESSION);
    }

    /**
     * Tests the determination of the next scheduling occurrence inside a DST period using a DST aware timezone.
     */
    @Test
    public final void testWeeklyNextOccurrenceOutsideDSTPeriodWithDSTUnawareTimeZone() {
        doTestScheduling(DATE_OUTSIDE_DST_PERIOD, false, WEEKLY_SCHEDULING_EXPRESSION);
    }

    /**
     * Tests winter to summer time transition with 15 min leaps
     */
    @Test
    public final void testDSTLeapNextOccurrence15min() {
        Calendar cal = Calendar.getInstance(TIMEZONE_WITH_DST);
        cal.clear();
        cal.set(Calendar.YEAR, 2010);
        cal.set(Calendar.MONTH, 2);
        cal.set(Calendar.DATE, 28);
        cal.set(Calendar.HOUR_OF_DAY, 1);
        cal.set(Calendar.MINUTE, 30);
        cal.set(Calendar.MILLISECOND, 0);

        TimeDuration every = new TimeDuration(15, TimeDuration.TimeUnit.MINUTES);
        TimeDuration offset = new TimeDuration(0, TimeDuration.TimeUnit.MINUTES);

        TemporalExpression expr = new TemporalExpression(every, offset);

        cal.setTime(expr.nextOccurrence(cal));

        assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(1);
        assertThat(cal.get(Calendar.MINUTE)).isEqualTo(45);

        cal.setTime(expr.nextOccurrence(cal));

        assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(3);
        assertThat(cal.get(Calendar.MINUTE)).isEqualTo(0);

        cal.setTime(expr.nextOccurrence(cal));

        assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(3);
        assertThat(cal.get(Calendar.MINUTE)).isEqualTo(15);


    }

    /**
     * Tests winter to summer time transition with 1 hour leaps
     */
    @Test
    public final void testDSTLeapNextOccurrence1hour() {
        Calendar cal = Calendar.getInstance(TIMEZONE_WITH_DST);
        cal.clear();
        cal.set(Calendar.YEAR, 2010);
        cal.set(Calendar.MONTH, 2);
        cal.set(Calendar.DATE, 28);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);

        TimeDuration every = new TimeDuration(1, TimeDuration.TimeUnit.HOURS);
        TimeDuration offset = new TimeDuration(0, TimeDuration.TimeUnit.MINUTES);

        TemporalExpression expr = new TemporalExpression(every, offset);

        cal.setTime(expr.nextOccurrence(cal));

        assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(1);
        assertThat(cal.get(Calendar.MINUTE)).isEqualTo(0);

        cal.setTime(expr.nextOccurrence(cal));

        assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(3);
        assertThat(cal.get(Calendar.MINUTE)).isEqualTo(0);

        cal.setTime(expr.nextOccurrence(cal));

        assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(4);
        assertThat(cal.get(Calendar.MINUTE)).isEqualTo(0);

    }

    /**
     * Tests winter to summer time transition with 2 hour leaps
     */
    @Test
    public final void testDSTLeapNextOccurrence2hour() {


        Calendar cal = Calendar.getInstance(TIMEZONE_WITH_DST);
        cal.clear();
        cal.set(Calendar.YEAR, 2010);
        cal.set(Calendar.MONTH, 2);
        cal.set(Calendar.DATE, 27);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);

        TimeDuration every = new TimeDuration(2, TimeDuration.TimeUnit.HOURS);
        TimeDuration offset = new TimeDuration(10, TimeDuration.TimeUnit.MINUTES);

        TemporalExpression expr = new TemporalExpression(every, offset);

        cal.setTime(expr.nextOccurrence(cal));

        assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(23);
        assertThat(cal.get(Calendar.MINUTE)).isEqualTo(10);

        cal.setTime(expr.nextOccurrence(cal));

        assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(1);
        assertThat(cal.get(Calendar.MINUTE)).isEqualTo(10);

        cal.setTime(expr.nextOccurrence(cal));

        assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(4);
        assertThat(cal.get(Calendar.MINUTE)).isEqualTo(10);

        cal.setTime(expr.nextOccurrence(cal));

        assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(6);
        assertThat(cal.get(Calendar.MINUTE)).isEqualTo(10);

    }

    /**
     * Tests summer to winter time transition with 15 min leaps
     */
    @Test
    public final void testNonDSTLeapNextOccurrence15min() {

        Calendar cal = Calendar.getInstance(TIMEZONE_WITH_DST);
        cal.clear();
        cal.set(Calendar.YEAR, 2010);
        cal.set(Calendar.MONTH, 9);
        cal.set(Calendar.DATE, 31);
        cal.set(Calendar.HOUR_OF_DAY, 1);
        cal.set(Calendar.MINUTE, 30);
        cal.set(Calendar.MILLISECOND, 0);

        TimeDuration every = new TimeDuration(15, TimeDuration.TimeUnit.MINUTES);
        TimeDuration offset = new TimeDuration(0, TimeDuration.TimeUnit.MINUTES);

        TemporalExpression expr = new TemporalExpression(every, offset);

        cal.setTime(expr.nextOccurrence(cal));

        assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(1);
        assertThat(cal.get(Calendar.MINUTE)).isEqualTo(45);

        cal.setTime(expr.nextOccurrence(cal));

        assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(2);
        assertThat(cal.get(Calendar.MINUTE)).isEqualTo(0);

        cal.setTime(expr.nextOccurrence(cal));

        assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(2);
        assertThat(cal.get(Calendar.MINUTE)).isEqualTo(15);

        cal.setTime(expr.nextOccurrence(cal));

        assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(2);
        assertThat(cal.get(Calendar.MINUTE)).isEqualTo(30);

        cal.setTime(expr.nextOccurrence(cal));

        assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(2);
        assertThat(cal.get(Calendar.MINUTE)).isEqualTo(45);

        cal.setTime(expr.nextOccurrence(cal));

        assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(2);
        assertThat(cal.get(Calendar.MINUTE)).isEqualTo(0);

        cal.setTime(expr.nextOccurrence(cal));

        assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(2);
        assertThat(cal.get(Calendar.MINUTE)).isEqualTo(15);

    }

    /**
     * Tests summer to winter time transition with 1 hour leaps
     */
    @Test
    public final void testNonDSTLeapNextOccurrence1hour() {
        Calendar cal = Calendar.getInstance(TIMEZONE_WITH_DST);
        cal.clear();
        cal.set(Calendar.YEAR, 2010);
        cal.set(Calendar.MONTH, 9);
        cal.set(Calendar.DATE, 31);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);

        TimeDuration every = new TimeDuration(1, TimeDuration.TimeUnit.HOURS);
        TimeDuration offset = new TimeDuration(0, TimeDuration.TimeUnit.MINUTES);

        TemporalExpression expr = new TemporalExpression(every, offset);

        cal.setTime(expr.nextOccurrence(cal));

        assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(1);
        assertThat(cal.get(Calendar.MINUTE)).isEqualTo(0);

        cal.setTime(expr.nextOccurrence(cal));

        assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(2);  // 2h SUMMER TIME
        assertThat(cal.get(Calendar.MINUTE)).isEqualTo(0);

        cal.setTime(expr.nextOccurrence(cal));

        assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(2);     // 2h WINTER TIME
        assertThat(cal.get(Calendar.MINUTE)).isEqualTo(0);

        cal.setTime(expr.nextOccurrence(cal));

        assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(3);
        assertThat(cal.get(Calendar.MINUTE)).isEqualTo(0);

    }

    @Test
    public void testEiserver2255() {
        TemporalExpression expression = new TemporalExpression(new TimeDuration(12, TimeDuration.TimeUnit.MONTHS), new TimeDuration(1, TimeDuration.TimeUnit.MONTHS));
        Calendar now = Calendar.getInstance();
        Date date = expression.nextOccurrence(now);
        Calendar returnedDate = Calendar.getInstance();
        returnedDate.setTime(date);
        assertThat(now.get(Calendar.YEAR) < returnedDate.get(Calendar.YEAR)).describedAs("Calculated date is not correct !").isTrue();
    }

    @Test
    public void testOnLastDayOfMonthSpecifiedWithOffset31 () {
        TemporalExpression expression = new TemporalExpression(new TimeDuration(1, TimeDuration.TimeUnit.MONTHS), new TimeDuration(31, TimeDuration.TimeUnit.DAYS));
        Calendar february1st2013 = Calendar.getInstance();
        february1st2013.set(2013, Calendar.FEBRUARY, 1, 0, 0, 0);
        Date nextOccurrence = expression.nextOccurrence(february1st2013);
        Calendar nextCalendar = Calendar.getInstance();
        nextCalendar.setTime(nextOccurrence);
        assertThat(nextCalendar.get(Calendar.MONTH)).describedAs("Calculated nextOccurrence month is not correct !").isEqualTo(Calendar.FEBRUARY);
        assertThat(nextCalendar.get(Calendar.DAY_OF_MONTH)).describedAs("Calculated nextOccurrence day is not correct !").isEqualTo(28);
    }

    @Test
    public void testOnLastDayOfMonthSpecifiedWithOffset31And6PM () {
        int offsetInSeconds = 86400 * 31 + 3600 * 18;
        TemporalExpression expression = new TemporalExpression(new TimeDuration(1, TimeDuration.TimeUnit.MONTHS), new TimeDuration(offsetInSeconds));
        Calendar february1st2013 = Calendar.getInstance();
        february1st2013.set(2013, Calendar.FEBRUARY, 1, 0, 0, 0);
        Date nextOccurrence = expression.nextOccurrence(february1st2013);
        Calendar nextCalendar = Calendar.getInstance();
        nextCalendar.setTime(nextOccurrence);
        assertThat(nextCalendar.get(Calendar.MONTH)).describedAs("Calculated nextOccurrence month is not correct !").isEqualTo(Calendar.FEBRUARY);
        assertThat(nextCalendar.get(Calendar.DAY_OF_MONTH)).describedAs("Calculated nextOccurrence day is not correct !").isEqualTo(28);
        assertThat(nextCalendar.get(Calendar.HOUR_OF_DAY)).describedAs("Calculated nextOccurrence hour is not correct !").isEqualTo(18);
        assertThat(nextCalendar.get(Calendar.MINUTE)).describedAs("Calculated nextOccurrence minute is not correct !").isEqualTo(0);
        assertThat(nextCalendar.get(Calendar.SECOND)).describedAs("Calculated nextOccurrence second is not correct !").isEqualTo(0);
    }

    @Test
    public void testOnLastDayOfMonthSpecifiedWithOffset31And6PMWithSetLastDay() {
        int offsetInSeconds = 3600 * 18;
        TemporalExpression expression = new TemporalExpression(new TimeDuration(1, TimeDuration.TimeUnit.MONTHS), new TimeDuration(offsetInSeconds));
        expression.setLastDay();
        Calendar february1st2013 = Calendar.getInstance();
        february1st2013.set(2013, Calendar.FEBRUARY, 1, 0, 0, 0);
        Date nextOccurrence = expression.nextOccurrence(february1st2013);
        Calendar nextCalendar = Calendar.getInstance();
        nextCalendar.setTime(nextOccurrence);
        assertThat(nextCalendar.get(Calendar.MONTH)).describedAs("Calculated nextOccurrence month is not correct !").isEqualTo(Calendar.FEBRUARY);
        assertThat(nextCalendar.get(Calendar.DAY_OF_MONTH)).describedAs("Calculated nextOccurrence day is not correct !").isEqualTo(28);
        assertThat(nextCalendar.get(Calendar.HOUR_OF_DAY)).describedAs("Calculated nextOccurrence hour is not correct !").isEqualTo(18);
        assertThat(nextCalendar.get(Calendar.MINUTE)).describedAs("Calculated nextOccurrence minute is not correct !").isEqualTo(0);
        assertThat(nextCalendar.get(Calendar.SECOND)).describedAs("Calculated nextOccurrence second is not correct !").isEqualTo(0);
    }

    @Test
    public void testGetLastDayNotSet() throws Exception {
        int offsetInSeconds = 3600 * 18;
        TemporalExpression expression = new TemporalExpression(new TimeDuration(1, TimeDuration.TimeUnit.MONTHS), new TimeDuration(offsetInSeconds));
        assertThat(expression.isLastDay()).isFalse();
    }

    @Test
    public void testGetLastDaySet() throws Exception {
        int offsetInSeconds = 3600 * 18;
        TemporalExpression expression = new TemporalExpression(new TimeDuration(1, TimeDuration.TimeUnit.MONTHS), new TimeDuration(offsetInSeconds));
        expression.setLastDay();
        assertThat(expression.isLastDay()).isTrue();
    }

    @Test
    public void testLastOfAfterAprilWithoutOffset() throws Exception {
        TemporalExpression expression = new TemporalExpression(new TimeDuration(1, TimeDuration.TimeUnit.MONTHS));
        expression.setLastDay();
        Calendar april5th2013 = Calendar.getInstance();
        april5th2013.set(2013, Calendar.APRIL, 5, 0, 0, 0);
        Date nextOccurrence = expression.nextOccurrence(april5th2013);
        Calendar nextCalendar = Calendar.getInstance();
        nextCalendar.setTime(nextOccurrence);
        assertThat(nextCalendar.get(Calendar.MONTH)).describedAs("Calculated nextOccurrence month is not correct !").isEqualTo(Calendar.APRIL);
        assertThat(nextCalendar.get(Calendar.DAY_OF_MONTH)).describedAs("Calculated nextOccurrence day is not correct !").isEqualTo(30);
        assertThat(nextCalendar.get(Calendar.HOUR_OF_DAY)).describedAs("Calculated nextOccurrence hour is not correct !").isEqualTo(0);
        assertThat(nextCalendar.get(Calendar.MINUTE)).describedAs("Calculated nextOccurrence minute is not correct !").isEqualTo(0);
        assertThat(nextCalendar.get(Calendar.SECOND)).describedAs("Calculated nextOccurrence second is not correct !").isEqualTo(0);
    }

    @Test
    public void testLastOfAfterJulyWithoutOffset() throws Exception {
        TemporalExpression expression = new TemporalExpression(new TimeDuration(1, TimeDuration.TimeUnit.MONTHS));
        expression.setLastDay();
        Calendar july5th2013 = Calendar.getInstance();
        july5th2013.set(2013, Calendar.JULY, 5, 0, 0, 0);
        Date nextOccurrence = expression.nextOccurrence(july5th2013);
        Calendar nextCalendar = Calendar.getInstance();
        nextCalendar.setTime(nextOccurrence);
        assertThat(nextCalendar.get(Calendar.MONTH)).describedAs("Calculated nextOccurrence month is not correct !").isEqualTo(Calendar.JULY);
        assertThat(nextCalendar.get(Calendar.DAY_OF_MONTH)).describedAs("Calculated nextOccurrence day is not correct !").isEqualTo(31);
        assertThat(nextCalendar.get(Calendar.HOUR_OF_DAY)).describedAs("Calculated nextOccurrence hour is not correct !").isEqualTo(0);
        assertThat(nextCalendar.get(Calendar.MINUTE)).describedAs("Calculated nextOccurrence minute is not correct !").isEqualTo(0);
        assertThat(nextCalendar.get(Calendar.SECOND)).describedAs("Calculated nextOccurrence second is not correct !").isEqualTo(0);
    }

    @Test
    public void testLastOfMonthRoundRobin() throws Exception {
        int[] lastDayIndex = {31,28,31,30,31,30,31,31,30,31,30,31};
        TemporalExpression expression = new TemporalExpression(new TimeDuration(1, TimeDuration.TimeUnit.MONTHS));
        expression.setLastDay();
        Calendar januari5th2013 = Calendar.getInstance();
        januari5th2013.set(2013, Calendar.JANUARY, 5, 0, 0, 0);
        Calendar nextCalendar = januari5th2013;
        for (int month=Calendar.JANUARY; month<=Calendar.DECEMBER; month++) {
            Date nextOccurrence = expression.nextOccurrence(januari5th2013);
            nextCalendar.setTime(nextOccurrence);
            assertThat(nextCalendar.get(Calendar.MONTH)).describedAs("Calculated nextOccurrence month is not correct for month "+month).isEqualTo(month);
            assertThat(nextCalendar.get(Calendar.DAY_OF_MONTH)).describedAs("Calculated nextOccurrence day is not correct for month "+month).isEqualTo(lastDayIndex[month]);
            assertThat(nextCalendar.get(Calendar.HOUR_OF_DAY)).describedAs("Calculated nextOccurrence hour is not correct for month " + month).isEqualTo(0);
            assertThat(nextCalendar.get(Calendar.MINUTE)).describedAs("Calculated nextOccurrence minute is not correct for month " + month).isEqualTo(0);
            assertThat(nextCalendar.get(Calendar.SECOND)).describedAs("Calculated nextOccurrence second is not correct for month " + month).isEqualTo(0);
        }
    }

    @Test
    public void testLastOfMonthRoundRobinWithOffset() throws Exception {
        int[] lastDayIndex = {31,28,31,30,31,30,31,31,30,31,30,31};
        TemporalExpression expression = new TemporalExpression(new TimeDuration(1, TimeDuration.TimeUnit.MONTHS), new TimeDuration(14, TimeDuration.TimeUnit.HOURS));
        expression.setLastDay();
        Calendar januari5th2013 = Calendar.getInstance();
        januari5th2013.set(2013, Calendar.JANUARY, 5, 0, 0, 0);
        Calendar nextCalendar = januari5th2013;
        for (int month=Calendar.JANUARY; month<=Calendar.DECEMBER; month++) {
            Date nextOccurrence = expression.nextOccurrence(januari5th2013);
            nextCalendar.setTime(nextOccurrence);
            assertThat(nextCalendar.get(Calendar.MONTH)).describedAs("Calculated nextOccurrence month is not correct for month "+month).isEqualTo(month);
            assertThat(nextCalendar.get(Calendar.DAY_OF_MONTH)).describedAs("Calculated nextOccurrence day is not correct for month "+month).isEqualTo(lastDayIndex[month]);
//            assertThat(nextCalendar.get(Calendar.HOUR_OF_DAY)).describedAs("Calculated nextOccurrence hour is not correct for month " + month).isEqualTo(14);
            assertThat(nextCalendar.get(Calendar.MINUTE)).describedAs("Calculated nextOccurrence minute is not correct for month " + month).isEqualTo(0);
            assertThat(nextCalendar.get(Calendar.SECOND)).describedAs("Calculated nextOccurrence second is not correct for month " + month).isEqualTo(0);
        }
    }

    @Test
    public void testDailyRoundRobinWithOffset() throws Exception {
        TemporalExpression expression = new TemporalExpression(new TimeDuration(1, TimeDuration.TimeUnit.MONTHS), new TimeDuration(14, TimeDuration.TimeUnit.HOURS));
        expression.setLastDay();
        Calendar januari1th2013 = Calendar.getInstance(TIMEZONE_WITH_DST);
        januari1th2013.set(2013, Calendar.JANUARY, 1, 0, 0, 0);
        Calendar nextCalendar = januari1th2013;
        for (int day=1; day<=365; day++) {
            Date nextOccurrence = expression.nextOccurrence(januari1th2013);
            /* Enable for debugging purposes
            System.out.println(nextOccurrence+"\t"+(nextOccurrence.getTime()-nextCalendar.getTime().getTime()));
            */
            nextCalendar.setTime(nextOccurrence);
//            assertThat(nextCalendar.get(Calendar.DAY_OF_YEAR)).describedAs("Calculated nextOccurrence day is not correct for day " + day).isEqualTo(day);
//            assertThat(nextCalendar.get(Calendar.HOUR_OF_DAY)).describedAs("Calculated nextOccurrence hour is not correct for day " + day).isEqualTo(2);
//            assertThat(nextCalendar.get(Calendar.MINUTE)).describedAs("Calculated nextOccurrence minute is not correct for day " + day).isEqualTo(30);
//            assertThat(nextCalendar.get(Calendar.SECOND)).describedAs("Calculated nextOccurrence second is not correct for day " + day).isEqualTo(0);
        }
    }

    @Test
    public void testOnLastDayOfMonthSpecifiedWithOffset30 () {
        TemporalExpression expression = new TemporalExpression(new TimeDuration(1, TimeDuration.TimeUnit.MONTHS), new TimeDuration(31, TimeDuration.TimeUnit.DAYS));
        Calendar february1st2013 = Calendar.getInstance();
        february1st2013.set(2013, Calendar.FEBRUARY, 1, 0, 0, 0);
        Date nextOccurrence = expression.nextOccurrence(february1st2013);
        Calendar nextCalendar = Calendar.getInstance();
        nextCalendar.setTime(nextOccurrence);
        assertThat(nextCalendar.get(Calendar.MONTH)).describedAs("Calculated nextOccurrence month is not correct !").isEqualTo(Calendar.FEBRUARY);
        assertThat(nextCalendar.get(Calendar.DAY_OF_MONTH)).describedAs("Calculated nextOccurrence day is not correct !").isEqualTo(28);
    }

    @Test
    public void testOnLastDayOfMonthSpecifiedWithOffset29 () {
        TemporalExpression expression = new TemporalExpression(new TimeDuration(1, TimeDuration.TimeUnit.MONTHS), new TimeDuration(31, TimeDuration.TimeUnit.DAYS));
        Calendar february1st2013 = Calendar.getInstance();
        february1st2013.set(2013, Calendar.FEBRUARY, 1, 0, 0, 0);
        Date nextOccurrence = expression.nextOccurrence(february1st2013);
        Calendar nextCalendar = Calendar.getInstance();
        nextCalendar.setTime(nextOccurrence);
        assertThat(nextCalendar.get(Calendar.MONTH)).describedAs("Calculated nextOccurrence month is not correct !").isEqualTo(Calendar.FEBRUARY);
        assertThat(nextCalendar.get(Calendar.DAY_OF_MONTH)).describedAs("Calculated nextOccurrence day is not correct !").isEqualTo(28);
    }

    @Test
    public void testOffsetDuringDST() {
        TemporalExpression temporalExpression = new TemporalExpression(new TimeDuration(1, TimeDuration.TimeUnit.DAYS), new TimeDuration(14, TimeDuration.TimeUnit.HOURS));
        ZoneId zone = ZoneId.of("Europe/Brussels");
        TimeZone.setDefault(TimeZone.getTimeZone(zone));
        ZonedDateTime date = ZonedDateTime.of(2013, 3, 29, 2, 30, 0, 0, zone);

        ZonedDateTime expected1 = ZonedDateTime.of(2013, 3, 29, 14, 0, 0, 0, zone);
        ZonedDateTime expected2 = ZonedDateTime.of(2013, 3, 30, 14, 0, 0, 0, zone);
        ZonedDateTime expected3 = ZonedDateTime.of(2013, 3, 31, 14, 0, 0, 0, zone);
        ZonedDateTime expected4 = ZonedDateTime.of(2013, 4, 1, 14, 0, 0, 0, zone);

        assertThat(temporalExpression.nextOccurrence(date)).contains(expected1);
        assertThat(temporalExpression.nextOccurrence(expected1)).contains(expected2);
        assertThat(temporalExpression.nextOccurrence(expected2)).contains(expected3);
        assertThat(temporalExpression.nextOccurrence(expected3)).contains(expected4);
    }

    @Test
    public void testOffsetDuringDSTTricky() {
        TemporalExpression temporalExpression = new TemporalExpression(new TimeDuration(1, TimeDuration.TimeUnit.DAYS), new TimeDuration(2, TimeDuration.TimeUnit.HOURS));
        ZoneId zone = ZoneId.of("Europe/Brussels");
        TimeZone.setDefault(TimeZone.getTimeZone(zone));
        ZonedDateTime date = ZonedDateTime.of(2013, 3, 29, 1, 0, 0, 0, zone);

        ZonedDateTime expected1 = ZonedDateTime.of(2013, 3, 29, 2, 0, 0, 0, zone);
        ZonedDateTime expected2 = ZonedDateTime.of(2013, 3, 30, 2, 0, 0, 0, zone);
        ZonedDateTime expected3 = ZonedDateTime.of(2013, 3, 31, 2, 0, 0, 0, zone);
        ZonedDateTime expected4 = ZonedDateTime.of(2013, 4, 1, 3, 0, 0, 0, zone);
        ZonedDateTime expected5 = ZonedDateTime.of(2013, 4, 2, 2, 0, 0, 0, zone);

        assertThat(temporalExpression.nextOccurrence(date)).contains(expected1);
        assertThat(temporalExpression.nextOccurrence(expected1)).contains(expected2);
        assertThat(temporalExpression.nextOccurrence(expected2)).contains(expected3);
        assertThat(temporalExpression.nextOccurrence(expected3)).contains(expected4);
        assertThat(temporalExpression.nextOccurrence(expected4)).contains(expected5);
    }

    @Test
    public void testNextOccurrenceWhenCalculatedInBetween() {
        TemporalExpression temporalExpression = new TemporalExpression(new TimeDuration(15, TimeDuration.TimeUnit.MINUTES));
        ZoneId zone = ZoneId.of("Europe/Brussels");
        TimeZone.setDefault(TimeZone.getTimeZone(zone));
        ZonedDateTime date = ZonedDateTime.of(2015, 1, 13, 14, 6, 11, 987, zone);
        ZonedDateTime expectedNextOccurrence = ZonedDateTime.of(2015, 1, 13, 14, 15, 0, 0, zone);

        assertThat(temporalExpression.nextOccurrence(date)).contains(expectedNextOccurrence);
    }

    @Test
    public void everyThreeHoursTest() {
        TemporalExpression temporalExpression = new TemporalExpression(new TimeDuration(3, TimeDuration.TimeUnit.HOURS));
        ZoneId zone = ZoneId.of("Europe/Brussels");
        TimeZone.setDefault(TimeZone.getTimeZone(zone));
        ZonedDateTime date = ZonedDateTime.of(2015, 1, 13, 14, 6, 11, 987, zone);
        ZonedDateTime expectedNextOccurrence = ZonedDateTime.of(2015, 1, 13, 15, 0, 0, 0, zone);

        assertThat(temporalExpression.nextOccurrence(date)).contains(expectedNextOccurrence);
    }

    /**
     * Tests the scheduling on a particular date using a DST aware time zone or not (as specified).
     *
     * @param schedulingDate      The date on which the scheduling should occur.
     * @param useDSTAwareTimeZone True if the test should use a DST aware time zone, false if it does not.
     */
    private static void doTestScheduling (final Calendar schedulingDate, final boolean useDSTAwareTimeZone, final TemporalExpression schedulingExpression) {
        setDSTAwareTimezones(useDSTAwareTimeZone);

        final Date nextScheduledDate = DAILY_SCHEDULING_EXPRESSION.nextOccurrence(schedulingDate);
        final Calendar nextScheduleCalendar = Calendar.getInstance();
        nextScheduleCalendar.setTime(nextScheduledDate);

        final int scheduledHour = nextScheduleCalendar.get(Calendar.HOUR);
        final int expectedScheduledHour = DAILY_SCHEDULING_EXPRESSION.getOffset().getCount();

        assertThat(scheduledHour).describedAs("Scheduled hour [" + scheduledHour + "] should be [" + expectedScheduledHour + "]").isEqualTo(expectedScheduledHour);
    }

    /**
     * Sets up the time zones so that they are DST aware or not. This is just part of the setup of the test case as DST aware time zones have a problem
     * with scheduling and DST unaware don't.
     *
     * @param dstAware
     */
    private static void setDSTAwareTimezones(final boolean dstAware) {
        if (dstAware) {
            TimeZone.setDefault(TIMEZONE_WITH_DST);
            DST_START_TIME.setTimeZone(TIMEZONE_WITH_DST);
            DST_END_DATE.setTimeZone(TIMEZONE_WITH_DST);
            DATE_INSIDE_DST_PERIOD.setTimeZone(TIMEZONE_WITH_DST);
            DATE_OUTSIDE_DST_PERIOD.setTimeZone(TIMEZONE_WITH_DST);
        } else {
            TimeZone.setDefault(TIMEZONE_WITHOUT_DST);
            DST_START_TIME.setTimeZone(TIMEZONE_WITHOUT_DST);
            DST_END_DATE.setTimeZone(TIMEZONE_WITHOUT_DST);
            DATE_INSIDE_DST_PERIOD.setTimeZone(TIMEZONE_WITHOUT_DST);
            DATE_OUTSIDE_DST_PERIOD.setTimeZone(TIMEZONE_WITHOUT_DST);
        }
    }

    @Override
    protected Object getInstanceA() {
        return DAILY_SCHEDULING_EXPRESSION;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new TemporalExpression(TimeDuration.days(1), TimeDuration.hours(4));
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Arrays.asList(
            new TemporalExpression(TimeDuration.days(2), TimeDuration.hours(0)),
            new TemporalExpression(TimeDuration.days(2), TimeDuration.hours(4)),
            new TemporalExpression(TimeDuration.days(1), TimeDuration.hours(2))
        );
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }

    /* public void testStuff() {
      Calendar now = Calendar.getInstance();
      now.clear();
      now.set(Calendar.YEAR, 2010);
      now.set(Calendar.MONTH, Calendar.OCTOBER);
      now.set(Calendar.DAY_OF_MONTH, 8);
      now.set(Calendar.HOUR_OF_DAY, 16);
      now.set(Calendar.MINUTE, 46);
      now.set(Calendar.SECOND, 15);

      TemporalExpression test = new TemporalExpression(new TimeDuration(2, TimeDuration.YEARS));
      assertEquals("every 2 years", test.toString());
      assertDate(test.nextOccurrence(now), 2012, Calendar.JANUARY, 1, 0, 0, 0);
      test = new TemporalExpression(new TimeDuration(11, TimeDuration.MONTHS));
      assertEquals("every 11 months", test.toString());
      assertDate(test.nextOccurrence(now), 2011, Calendar.SEPTEMBER, 1, 0, 0, 0);

      test = new TemporalExpression(new TimeDuration(30, TimeDuration.DAYS));
      assertEquals("every 30 days", test.toString());
      assertDate(test.nextOccurrence(now), 2010, Calendar.NOVEMBER, 7, 0, 0, 0);

      test = new TemporalExpression(new TimeDuration(23, TimeDuration.HOURS));
      assertEquals("every 23 hours", test.toString());
      assertDate(test.nextOccurrence(now), 2010, Calendar.OCTOBER, 9, 15, 0, 0);

      test = new TemporalExpression(new TimeDuration(1, TimeDuration.HOURS));
      assertEquals("every 1 hours", test.toString());
      assertDate(test.nextOccurrence(now), 2010, Calendar.OCTOBER, 8, 17, 0, 0);

      test = new TemporalExpression(new TimeDuration(1, TimeDuration.HOURS), new TimeDuration(30, TimeDuration.MINUTES));
      assertEquals("every 1 hours (offset: 30 minutes)", test.toString());
      assertDate(test.nextOccurrence(now), 2010, Calendar.OCTOBER, 8, 17, 30, 0);

      test = new TemporalExpression(new TimeDuration(59, TimeDuration.MINUTES));
      assertEquals("every 59 minutes", test.toString());
      assertDate(test.nextOccurrence(now), 2010, Calendar.OCTOBER, 8, 16, 48, 0);

      test = new TemporalExpression(new TimeDuration(59, TimeDuration.SECONDS));
      assertEquals("every 59 seconds", test.toString());
      assertDate(test.nextOccurrence(now), 2010, Calendar.OCTOBER, 8, 16, 47, 1);

      test = new TemporalExpression(new TimeDuration(45, TimeDuration.WEEKS));
      assertEquals("every 45 weeks", test.toString());
      assertDate(test.nextOccurrence(now), 2011, Calendar.AUGUST, 15, 0, 0, 0);
      test = new TemporalExpression(3600 * 24 * 31 * 2, 3600 * 24 * 26);
      assertEquals("every 2 months (offset: 26 days)", test.toString());
      assertDate(test.nextOccurrence(now), 2010, Calendar.OCTOBER, 27, 0, 0, 0);
  }

  private static void assertDate(Date testDate, int year, int month, int day, int hour, int minute, int second) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(testDate);
      assertEquals("Year not correct", year, cal.get(Calendar.YEAR));
      assertEquals("Month not correct", month, cal.get(Calendar.MONTH));
      assertEquals("Day not correct", day, cal.get(Calendar.DAY_OF_MONTH));
      assertEquals("Hour not correct", hour, cal.get(Calendar.HOUR_OF_DAY));
      assertEquals("Minute not correct", minute, cal.get(Calendar.MINUTE));
      assertEquals("Second not correct", second, cal.get(Calendar.SECOND));
  }  */
}
