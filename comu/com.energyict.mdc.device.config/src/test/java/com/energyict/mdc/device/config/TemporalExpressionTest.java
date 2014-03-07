package com.energyict.mdc.device.config;

import com.energyict.mdc.common.TimeDuration;
import junit.framework.TestCase;
import org.joda.time.DateTimeConstants;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Test cases for the temporal expression class which is used in the scheduling of jobs.
 *
 * @author alex
 */
public final class TemporalExpressionTest extends TestCase {

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
        final TimeDuration dailyInterval = new TimeDuration(1, TimeDuration.DAYS);
        // At 4 AM...
        final TimeDuration time = new TimeDuration(4, TimeDuration.HOURS);

        DAILY_SCHEDULING_EXPRESSION = new TemporalExpression(dailyInterval, time);

        final TimeDuration weeklyInterval = new TimeDuration(1, TimeDuration.WEEKS);

        WEEKLY_SCHEDULING_EXPRESSION = new TemporalExpression(weeklyInterval, time);
    }

    /**
     * Tests the next occurrence when DST ends using a DST aware time zone.
     */
    public final void testDailyNextOccurrenceAtDSTEndWithDSTAwareTimeZone() {
        doTestScheduling(DST_END_DATE, true, DAILY_SCHEDULING_EXPRESSION);
    }

    /**
     * Tests the next occurrence when DST starts with a DST aware time zone.
     */
    public final void testDailyNextOccurrenceAtDSTStartWithDSTAwareTimeZone() {
        doTestScheduling(DST_START_TIME, true, DAILY_SCHEDULING_EXPRESSION);
    }

    /**
     * Tests the next occurrence when DST starts with a DST unaware time zone.
     */
    public final void testDailyNextOccurrenceAtDSTEndWithDSTUnawareTimeZone() {
        doTestScheduling(DST_END_DATE, false, DAILY_SCHEDULING_EXPRESSION);
    }

    /**
     * Tests the next occurrence scheduling using a timezone that is not DST aware.
     */
    public final void testDailyNextOccurrenceAtDSTStartWithDSTUnawareTimeZone() {
        doTestScheduling(DST_START_TIME, false, DAILY_SCHEDULING_EXPRESSION);
    }

    /**
     * Tests the determination of the next scheduling occurrence inside a DST period using a DST aware timezone.
     */
    public final void testDailyNextOccurrenceInsideDSTPeriodWithDSTAwareTimeZone() {
        doTestScheduling(DATE_INSIDE_DST_PERIOD, true, DAILY_SCHEDULING_EXPRESSION);
    }

    /**
     * Tests the determination of the next scheduling occurrence inside a DST period using a DST unaware timezone.
     */
    public final void testDailyNextOccurrenceInsideDSTPeriodWithDSTUnawareTimeZone() {
        doTestScheduling(DATE_INSIDE_DST_PERIOD, false, DAILY_SCHEDULING_EXPRESSION);
    }

    /**
     * Tests the determination of the next scheduling occurrence inside a DST period using a DST aware timezone.
     */
    public final void testDailyNextOccurrenceOutsideDSTPeriodWithDSTAwareTimeZone() {
        doTestScheduling(DATE_OUTSIDE_DST_PERIOD, true, DAILY_SCHEDULING_EXPRESSION);
    }

    /**
     * Tests the determination of the next scheduling occurrence inside a DST period using a DST aware timezone.
     */
    public final void testDailyNextOccurrenceOutsideDSTPeriodWithDSTUnawareTimeZone() {
        doTestScheduling(DATE_OUTSIDE_DST_PERIOD, false, DAILY_SCHEDULING_EXPRESSION);
    }


    /**
     * Tests the next occurrence when DST ends using a DST aware time zone.
     */
    public final void testWeeklyNextOccurrenceAtDSTEndWithDSTAwareTimeZone() {
        doTestScheduling(DST_END_DATE, true, WEEKLY_SCHEDULING_EXPRESSION);
    }

    /**
     * Tests the next occurrence when DST starts with a DST aware time zone.
     */
    public final void testWeekyNextOccurrenceAtDSTStartWithDSTAwareTimeZone() {
        doTestScheduling(DST_START_TIME, true, WEEKLY_SCHEDULING_EXPRESSION);
    }

    /**
     * Tests the next occurrence when DST starts with a DST unaware time zone.
     */
    public final void testWeeklyNextOccurrenceAtDSTEndWithDSTUnawareTimeZone() {
        doTestScheduling(DST_END_DATE, false, WEEKLY_SCHEDULING_EXPRESSION);
    }

    /**
     * Tests the next occurrence scheduling using a timezone that is not DST aware.
     */
    public final void testWeeklyNextOccurrenceAtDSTStartWithDSTUnawareTimeZone() {
        doTestScheduling(DST_START_TIME, false, WEEKLY_SCHEDULING_EXPRESSION);
    }

    /**
     * Tests the determination of the next scheduling occurrence inside a DST period using a DST aware timezone.
     */
    public final void testWeeklyNextOccurrenceInsideDSTPeriodWithDSTAwareTimeZone() {
        doTestScheduling(DATE_INSIDE_DST_PERIOD, true, WEEKLY_SCHEDULING_EXPRESSION);
    }

    /**
     * Tests the determination of the next scheduling occurrence inside a DST period using a DST unaware timezone.
     */
    public final void testWeeklyNextOccurrenceInsideDSTPeriodWithDSTUnawareTimeZone() {
        doTestScheduling(DATE_INSIDE_DST_PERIOD, false, WEEKLY_SCHEDULING_EXPRESSION);
    }

    /**
     * Tests the determination of the next scheduling occurrence inside a DST period using a DST aware timezone.
     */
    public final void testWeeklyNextOccurrenceOutsideDSTPeriodWithDSTAwareTimeZone() {
        doTestScheduling(DATE_OUTSIDE_DST_PERIOD, true, WEEKLY_SCHEDULING_EXPRESSION);
    }

    /**
     * Tests the determination of the next scheduling occurrence inside a DST period using a DST aware timezone.
     */
    public final void testWeeklyNextOccurrenceOutsideDSTPeriodWithDSTUnawareTimeZone() {
        doTestScheduling(DATE_OUTSIDE_DST_PERIOD, false, WEEKLY_SCHEDULING_EXPRESSION);
    }

    /**
     * Tests winter to summer time transition with 15 min leaps
     */
    public final void testDSTLeapNextOccurrence15min() {
        Calendar cal = Calendar.getInstance(TIMEZONE_WITH_DST);
        cal.clear();
        cal.set(Calendar.YEAR, 2010);
        cal.set(Calendar.MONTH, 2);
        cal.set(Calendar.DATE, 28);
        cal.set(Calendar.HOUR_OF_DAY, 1);
        cal.set(Calendar.MINUTE, 30);
        cal.set(Calendar.MILLISECOND, 0);

        TimeDuration every = new TimeDuration(15, TimeDuration.MINUTES);
        TimeDuration offset = new TimeDuration(0, TimeDuration.MINUTES);

        TemporalExpression expr = new TemporalExpression(every, offset);

        cal.setTime(expr.nextOccurrence(cal));

        assertEquals(1, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(45, cal.get(Calendar.MINUTE));

        cal.setTime(expr.nextOccurrence(cal));

        assertEquals(3, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));

        cal.setTime(expr.nextOccurrence(cal));

        assertEquals(3, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(15, cal.get(Calendar.MINUTE));


    }

    /**
     * Tests winter to summer time transition with 1 hour leaps
     */
    public final void testDSTLeapNextOccurrence1hour() {
        Calendar cal = Calendar.getInstance(TIMEZONE_WITH_DST);
        cal.clear();
        cal.set(Calendar.YEAR, 2010);
        cal.set(Calendar.MONTH, 2);
        cal.set(Calendar.DATE, 28);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);

        TimeDuration every = new TimeDuration(1, TimeDuration.HOURS);
        TimeDuration offset = new TimeDuration(0, TimeDuration.MINUTES);

        TemporalExpression expr = new TemporalExpression(every, offset);

        cal.setTime(expr.nextOccurrence(cal));

        assertEquals(1, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));

        cal.setTime(expr.nextOccurrence(cal));

        assertEquals(3, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));

        cal.setTime(expr.nextOccurrence(cal));

        assertEquals(4, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));

    }

    /**
     * Tests winter to summer time transition with 2 hour leaps
     */
    public final void testDSTLeapNextOccurrence2hour() {


        Calendar cal = Calendar.getInstance(TIMEZONE_WITH_DST);
        cal.clear();
        cal.set(Calendar.YEAR, 2010);
        cal.set(Calendar.MONTH, 2);
        cal.set(Calendar.DATE, 27);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);

        TimeDuration every = new TimeDuration(2, TimeDuration.HOURS);
        TimeDuration offset = new TimeDuration(10, TimeDuration.MINUTES);

        TemporalExpression expr = new TemporalExpression(every, offset);

        cal.setTime(expr.nextOccurrence(cal));

        assertEquals(23, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(10, cal.get(Calendar.MINUTE));

        cal.setTime(expr.nextOccurrence(cal));

        assertEquals(1, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(10, cal.get(Calendar.MINUTE));

        cal.setTime(expr.nextOccurrence(cal));

        assertEquals(4, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(10, cal.get(Calendar.MINUTE));

        cal.setTime(expr.nextOccurrence(cal));

        assertEquals(6, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(10, cal.get(Calendar.MINUTE));

    }

    /**
     * Tests summer to winter time transition with 15 min leaps
     */
    public final void testNonDSTLeapNextOccurrence15min() {

        Calendar cal = Calendar.getInstance(TIMEZONE_WITH_DST);
        cal.clear();
        cal.set(Calendar.YEAR, 2010);
        cal.set(Calendar.MONTH, 9);
        cal.set(Calendar.DATE, 31);
        cal.set(Calendar.HOUR_OF_DAY, 1);
        cal.set(Calendar.MINUTE, 30);
        cal.set(Calendar.MILLISECOND, 0);

        TimeDuration every = new TimeDuration(15, TimeDuration.MINUTES);
        TimeDuration offset = new TimeDuration(0, TimeDuration.MINUTES);

        TemporalExpression expr = new TemporalExpression(every, offset);

        cal.setTime(expr.nextOccurrence(cal));

        assertEquals(1, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(45, cal.get(Calendar.MINUTE));

        cal.setTime(expr.nextOccurrence(cal));

        assertEquals(2, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));

        cal.setTime(expr.nextOccurrence(cal));

        assertEquals(2, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(15, cal.get(Calendar.MINUTE));

        cal.setTime(expr.nextOccurrence(cal));

        assertEquals(2, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));

        cal.setTime(expr.nextOccurrence(cal));

        assertEquals(2, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(45, cal.get(Calendar.MINUTE));

        cal.setTime(expr.nextOccurrence(cal));

        assertEquals(2, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));

        cal.setTime(expr.nextOccurrence(cal));

        assertEquals(2, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(15, cal.get(Calendar.MINUTE));

    }

    /**
     * Tests summer to winter time transition with 1 hour leaps
     */
    public final void testNonDSTLeapNextOccurrence1hour() {
        Calendar cal = Calendar.getInstance(TIMEZONE_WITH_DST);
        cal.clear();
        cal.set(Calendar.YEAR, 2010);
        cal.set(Calendar.MONTH, 9);
        cal.set(Calendar.DATE, 31);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);

        TimeDuration every = new TimeDuration(1, TimeDuration.HOURS);
        TimeDuration offset = new TimeDuration(0, TimeDuration.MINUTES);

        TemporalExpression expr = new TemporalExpression(every, offset);

        cal.setTime(expr.nextOccurrence(cal));

        assertEquals(1, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));

        cal.setTime(expr.nextOccurrence(cal));

        assertEquals(2, cal.get(Calendar.HOUR_OF_DAY));  // 2h SUMMER TIME
        assertEquals(0, cal.get(Calendar.MINUTE));

        cal.setTime(expr.nextOccurrence(cal));

        assertEquals(2, cal.get(Calendar.HOUR_OF_DAY));     // 2h WINTER TIME
        assertEquals(0, cal.get(Calendar.MINUTE));

        cal.setTime(expr.nextOccurrence(cal));

        assertEquals(3, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));

    }

    public void testEiserver2255() {
        TemporalExpression expression = new TemporalExpression(new TimeDuration(12, TimeDuration.MONTHS), new TimeDuration(1, TimeDuration.MONTHS));
        Calendar now = Calendar.getInstance();
        Date date = expression.nextOccurrence(now);
        Calendar returnedDate = Calendar.getInstance();
        returnedDate.setTime(date);
        assertTrue("Calculated date is not correct !", now.get(Calendar.YEAR) < returnedDate.get(Calendar.YEAR));
    }

    public void testOnLastDayOfMonthSpecifiedWithOffset31 () {
        TemporalExpression expression = new TemporalExpression(new TimeDuration(1, TimeDuration.MONTHS), new TimeDuration(31, TimeDuration.DAYS));
        Calendar february1st2013 = Calendar.getInstance();
        february1st2013.set(2013, Calendar.FEBRUARY, 1, 0, 0, 0);
        Date nextOccurrence = expression.nextOccurrence(february1st2013);
        Calendar nextCalendar = Calendar.getInstance();
        nextCalendar.setTime(nextOccurrence);
        assertEquals("Calculated nextOccurrence month is not correct !", Calendar.FEBRUARY, nextCalendar.get(Calendar.MONTH));
        assertEquals("Calculated nextOccurrence day is not correct !", 28, nextCalendar.get(Calendar.DAY_OF_MONTH));
    }

    public void testOnLastDayOfMonthSpecifiedWithOffset31And6PM () {
        int offsetInSeconds = DateTimeConstants.SECONDS_PER_DAY * 31 + DateTimeConstants.SECONDS_PER_HOUR * 18;
        TemporalExpression expression = new TemporalExpression(new TimeDuration(1, TimeDuration.MONTHS), new TimeDuration(offsetInSeconds));
        Calendar february1st2013 = Calendar.getInstance();
        february1st2013.set(2013, Calendar.FEBRUARY, 1, 0, 0, 0);
        Date nextOccurrence = expression.nextOccurrence(february1st2013);
        Calendar nextCalendar = Calendar.getInstance();
        nextCalendar.setTime(nextOccurrence);
        assertEquals("Calculated nextOccurrence month is not correct !", Calendar.FEBRUARY, nextCalendar.get(Calendar.MONTH));
        assertEquals("Calculated nextOccurrence day is not correct !", 28, nextCalendar.get(Calendar.DAY_OF_MONTH));
        assertEquals("Calculated nextOccurrence hour is not correct !", 18, nextCalendar.get(Calendar.HOUR_OF_DAY));
        assertEquals("Calculated nextOccurrence minute is not correct !", 0, nextCalendar.get(Calendar.MINUTE));
        assertEquals("Calculated nextOccurrence second is not correct !", 0, nextCalendar.get(Calendar.SECOND));
    }

    public void testOnLastDayOfMonthSpecifiedWithOffset30 () {
        TemporalExpression expression = new TemporalExpression(new TimeDuration(1, TimeDuration.MONTHS), new TimeDuration(31, TimeDuration.DAYS));
        Calendar february1st2013 = Calendar.getInstance();
        february1st2013.set(2013, Calendar.FEBRUARY, 1, 0, 0, 0);
        Date nextOccurrence = expression.nextOccurrence(february1st2013);
        Calendar nextCalendar = Calendar.getInstance();
        nextCalendar.setTime(nextOccurrence);
        assertEquals("Calculated nextOccurrence month is not correct !", Calendar.FEBRUARY, nextCalendar.get(Calendar.MONTH));
        assertEquals("Calculated nextOccurrence day is not correct !", 28, nextCalendar.get(Calendar.DAY_OF_MONTH));
    }

    public void testOnLastDayOfMonthSpecifiedWithOffset29 () {
        TemporalExpression expression = new TemporalExpression(new TimeDuration(1, TimeDuration.MONTHS), new TimeDuration(31, TimeDuration.DAYS));
        Calendar february1st2013 = Calendar.getInstance();
        february1st2013.set(2013, Calendar.FEBRUARY, 1, 0, 0, 0);
        Date nextOccurrence = expression.nextOccurrence(february1st2013);
        Calendar nextCalendar = Calendar.getInstance();
        nextCalendar.setTime(nextOccurrence);
        assertEquals("Calculated nextOccurrence month is not correct !", Calendar.FEBRUARY, nextCalendar.get(Calendar.MONTH));
        assertEquals("Calculated nextOccurrence day is not correct !", 28, nextCalendar.get(Calendar.DAY_OF_MONTH));
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

        assertEquals("Scheduled hour [" + scheduledHour + "] should be [" + expectedScheduledHour + "]", expectedScheduledHour, scheduledHour);
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
