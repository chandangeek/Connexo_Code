package com.energyict.smartmeterprotocolimpl.actaris.sl7000;

import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static junit.framework.Assert.assertEquals;

/**
 * @author sva
 * @since 23/04/2014 - 17:11
 */
public class ProfileLimiterTest {
    private static final Date FROM_DATE = ProtocolTools.createCalendar(2010, 7, 15, 7, 15, 0, 0).getTime(); // 15/07/2010 07:15
    private static final Date FROM_DATE_ROUNDED_DOWN_TO_MIDNIGHT = ProtocolTools.createCalendar(2010, 7, 15, 0, 0, 0, 0).getTime(); // 15/07/2010 07:15
    private static final Date FROM_DATE_ROUNDED_UP_TO_MIDNIGHT = ProtocolTools.createCalendar(2010, 7, 16, 0, 0, 0, 0).getTime(); // 15/07/2010 07:15
    private static final Date TO_DATE = ProtocolTools.createCalendar(2010, 8, 10, 10, 30, 0, 0).getTime();  // 10/08/2010 10:30
    private static final Date TO_DATE_ROUNDED_DOWN_TO_MIDNIGHT = ProtocolTools.createCalendar(2010, 8, 10, 0, 0, 0, 0).getTime();  // 10/08/2010 10:30
    private static final Date TO_DATE_ROUNDED_UP_TO_MIDNIGHT = ProtocolTools.createCalendar(2010, 8, 11, 0, 0, 0, 0).getTime();  // 10/08/2010 10:30
    TimeZone timeZone = TimeZone.getDefault();
    @Test
    public void testGetOldFromToDate() throws Exception {
        assertEquals(FROM_DATE, new ProfileLimiter(FROM_DATE, TO_DATE, 0, timeZone).getOriginalFromDate());
        assertEquals(FROM_DATE, new ProfileLimiter(FROM_DATE, TO_DATE, -20, timeZone).getOriginalFromDate());
        assertEquals(FROM_DATE, new ProfileLimiter(FROM_DATE, TO_DATE, 20, timeZone).getOriginalFromDate());

        assertEquals(TO_DATE, new ProfileLimiter(FROM_DATE, TO_DATE, 0, timeZone).getOriginalToDate());
        assertEquals(TO_DATE, new ProfileLimiter(FROM_DATE, TO_DATE, -20, timeZone).getOriginalToDate());
        assertEquals(TO_DATE, new ProfileLimiter(FROM_DATE, TO_DATE, 20, timeZone).getOriginalToDate());
    }

    @Test
    public void testGetFromToDateZero() throws Exception {
        assertEquals(FROM_DATE_ROUNDED_DOWN_TO_MIDNIGHT, new ProfileLimiter(FROM_DATE, TO_DATE, 0, timeZone).getFromDate());
        assertEquals(TO_DATE_ROUNDED_UP_TO_MIDNIGHT, new ProfileLimiter(FROM_DATE, TO_DATE, 0, timeZone).getToDate());
    }

    @Test
    public void testGetFromToDatePositive() throws Exception {
        assertEquals(FROM_DATE_ROUNDED_DOWN_TO_MIDNIGHT, new ProfileLimiter(FROM_DATE, TO_DATE, 15, timeZone).getFromDate());
        assertEquals(ProtocolTools.createCalendar(2010, 7, 16, 0, 0, 0, 0).getTime(), new ProfileLimiter(FROM_DATE, TO_DATE, 1, timeZone).getToDate());
        assertEquals(ProtocolTools.createCalendar(2010, 7, 30, 0, 0, 0, 0).getTime(), new ProfileLimiter(FROM_DATE, TO_DATE, 15, timeZone).getToDate());
        assertEquals(ProtocolTools.createCalendar(2010, 8, 4, 0, 0, 0, 0).getTime(), new ProfileLimiter(FROM_DATE, TO_DATE, 20, timeZone).getToDate());
        assertEquals(TO_DATE_ROUNDED_UP_TO_MIDNIGHT, new ProfileLimiter(FROM_DATE, TO_DATE, 50, timeZone).getToDate());
    }

    @Test
    public void testGetFromToDateNegative() throws Exception {
        assertEquals(TO_DATE_ROUNDED_UP_TO_MIDNIGHT, new ProfileLimiter(FROM_DATE, TO_DATE, -15, timeZone).getToDate());
        assertEquals(ProtocolTools.createCalendar(2010, 8, 10, 0, 0, 0, 0).getTime(), new ProfileLimiter(FROM_DATE, TO_DATE, -1, timeZone).getFromDate());
        assertEquals(ProtocolTools.createCalendar(2010, 7, 27, 0, 0, 0, 0).getTime(), new ProfileLimiter(FROM_DATE, TO_DATE, -15, timeZone).getFromDate());
        assertEquals(ProtocolTools.createCalendar(2010, 7, 22, 00, 00, 0, 0).getTime(), new ProfileLimiter(FROM_DATE, TO_DATE, -20, timeZone).getFromDate());
        assertEquals(FROM_DATE_ROUNDED_DOWN_TO_MIDNIGHT, new ProfileLimiter(FROM_DATE, TO_DATE, -50, timeZone).getFromDate());
    }

    @Test
    public void testRoundCalendarToMidnight() throws Exception {
        Calendar calRoundDown = ProfileLimiter.roundCalendarToMidnight(Calendar.getInstance(), false);
        Calendar calRoundUp = ProfileLimiter.roundCalendarToMidnight(Calendar.getInstance(), true);

        long difference = calRoundUp.getTimeInMillis() - calRoundDown.getTimeInMillis();

        long expectedDifference = (long) 3600 * 24 * 1000;  // Nbr of milliseconds in 1 day
        assertEquals(expectedDifference, difference);
    }
}