/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time;

import java.time.Duration;
import java.time.Period;
import java.util.Calendar;
import java.util.Date;
import org.junit.Test;

import static com.elster.jupiter.util.Checks.is;
import static junit.framework.Assert.assertEquals;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class TimeDurationTest {

    @Test
    public void test_truncate() {
        TimeDuration td = new TimeDuration(5, TimeDuration.TimeUnit.MINUTES);
        Calendar cal = Calendar.getInstance();
        cal.set(2010, Calendar.JULY, 12, 8, 17, 23);
        cal.set(Calendar.MILLISECOND, 0);
        td.truncate(cal);

        Calendar result = Calendar.getInstance();
        result.set(2010, Calendar.JULY, 12, 8, 15, 0);
        result.set(Calendar.MILLISECOND, 0);
        assertTrue("Test_truncate 1", is(cal.getTime()).equalTo(result.getTime()));

        // Truncate with an 'empty' TimeDuration
        td = new TimeDuration(0, TimeDuration.TimeUnit.MINUTES);
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
        TimeDuration duration = new TimeDuration(75, TimeDuration.TimeUnit.SECONDS);
        duration.truncate(cal);
        assertTime(cal.getTime(), 12, 31, 15);
        cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 31);
        cal.set(Calendar.SECOND, 25);
        duration = new TimeDuration(15, TimeDuration.TimeUnit.SECONDS);
        duration.truncate(cal);
        assertTime(cal.getTime(), 12, 31, 15);
    }

    @Test
    public void testConversionToTemporalAmountForMinutes() throws Exception {
        TimeDuration td = new TimeDuration(5, TimeDuration.TimeUnit.MINUTES);
        assertThat(td.asTemporalAmount()).isEqualTo(Duration.ofMinutes(5));
    }

    @Test
    public void testConversionToTemporalAmountForSeconds() throws Exception {
        TimeDuration td = new TimeDuration(10, TimeDuration.TimeUnit.SECONDS);
        assertThat(td.asTemporalAmount()).isEqualTo(Duration.ofSeconds(10));
    }

    @Test
    public void testConversionToTemporalAmountForYears() throws Exception {
        TimeDuration td = new TimeDuration("3 years");
        assertThat(td.asTemporalAmount()).isEqualTo(Period.ofYears(3));
    }

    @Test
    public void testConversionToTemporalAmountForDays() throws Exception {
        TimeDuration td = new TimeDuration("9 days");
        assertThat(td.asTemporalAmount()).isEqualTo(Period.ofDays(9));
    }

    @Test
    public void testConversionToTemporalAmountForMillis() throws Exception {
        TimeDuration td = new TimeDuration("310 milliseconds");
        assertThat(td.asTemporalAmount()).isEqualTo(Duration.ofMillis(310));
    }

    @Test
    public void testConversionToTemporalAmountForHours() throws Exception {
        TimeDuration td = new TimeDuration("5 hours");
        assertThat(td.asTemporalAmount()).isEqualTo(Duration.ofHours(5));
    }

    @Test
    public void testConversionToTemporalAmountForWeeks() throws Exception {
        TimeDuration td = new TimeDuration("1 weeks");
        assertThat(td.asTemporalAmount()).isEqualTo(Period.ofWeeks(1));
    }

    @Test
    public void testConversionToTemporalAmountFor2Weeks() throws Exception {
        TimeDuration td = new TimeDuration("2 weeks");
        assertThat(td.asTemporalAmount()).isEqualTo(Period.ofWeeks(2));
        assertThat(td.asTemporalAmount()).isEqualTo(Period.ofDays(14));
    }

    private static void assertTime(Date testDate, int hour, int minute, int second) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(testDate);
        assertEquals("Hour not correct", hour, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals("Minute not correct", minute, cal.get(Calendar.MINUTE));
        assertEquals("Second not correct", second, cal.get(Calendar.SECOND));
    }

}
