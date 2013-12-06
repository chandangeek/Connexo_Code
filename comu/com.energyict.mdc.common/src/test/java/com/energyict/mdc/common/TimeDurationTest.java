package com.energyict.mdc.common;

import org.junit.*;

import java.util.Calendar;
import java.util.Date;

import static com.elster.jupiter.util.Checks.is;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

public class TimeDurationTest {

    @Test
    public void test_truncate() {
        TimeDuration td = new TimeDuration(5, TimeDuration.MINUTES);
        Calendar cal = Calendar.getInstance();
        cal.set(2010, Calendar.JULY, 12, 8, 17, 23);
        cal.set(Calendar.MILLISECOND, 0);
        td.truncate(cal);

        Calendar result = Calendar.getInstance();
        result.set(2010, Calendar.JULY, 12, 8, 15, 0);
        result.set(Calendar.MILLISECOND, 0);
        assertTrue("Test_truncate 1", is(cal.getTime()).equalTo(result.getTime()));

        // Truncate with an 'empty' TimeDuration
        td = new TimeDuration(0, TimeDuration.MINUTES);
        cal.set(2010, Calendar.JULY, 12, 8, 17, 23);
        cal.set(Calendar.MILLISECOND, 0);
        td.truncate(cal);
        result.set(2010, Calendar.JULY, 12, 8, 17, 23);
        result.set(Calendar.MILLISECOND, 0);
        assertTrue("Test_truncate 2", is(cal.getTime()).equalTo(result.getTime()));
    }

    @Test
    public void testStuff() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 31);
        cal.set(Calendar.SECOND, 25);
        TimeDuration duration = new TimeDuration(75, TimeDuration.SECONDS);
        duration.truncate(cal);
        assertTime(cal.getTime(), 12, 31, 15);
        cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 31);
        cal.set(Calendar.SECOND, 25);
        duration = new TimeDuration(15, TimeDuration.SECONDS);
        duration.truncate(cal);
        assertTime(cal.getTime(), 12, 31, 15);
    }

    private static void assertTime(Date testDate, int hour, int minute, int second) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(testDate);
        assertEquals("Hour not correct", hour, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals("Minute not correct", minute, cal.get(Calendar.MINUTE));
        assertEquals("Second not correct", second, cal.get(Calendar.SECOND));
    }

}
