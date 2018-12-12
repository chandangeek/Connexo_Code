package com.energyict.protocolimpl.iec1107.vdew;

import junit.framework.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.TimeZone;

/**
 * @author sva
 * @since 10/04/2015 - 10:59
 */
public class VDEWTimeStampTest {

    @Test
    public void testParseDateGivenInWinterTimeUsingDstTimeZone() throws Exception {
        VDEWTimeStamp vts = new VDEWTimeStamp(TimeZone.getTimeZone("Europe/Brussels"));
        String dateStr = "0150410060500";   // Fri Apr 10 06:05:00 GMT +1

        // Business methods
        vts.parse(dateStr);
        Date date = vts.getCalendar().getTime();

        // Asserts
        Assert.assertEquals(1428642300000l, date.getTime());
    }

    @Test
    public void testParseDateGivenInWinterTimeUsingNonDstTimeZone() throws Exception {
        VDEWTimeStamp vts = new VDEWTimeStamp(TimeZone.getTimeZone("GMT+1"));
        String dateStr = "0150410060500";   // Fri Apr 10 06:05:00 GMT +1

        // Business methods
        vts.parse(dateStr);
        Date date = vts.getCalendar().getTime();

        // Asserts
        Assert.assertEquals(1428642300000l, date.getTime());
    }

    @Test
    public void testParseDateGivenInSummerTimeUsingDstTimeZone() throws Exception {
        VDEWTimeStamp vts = new VDEWTimeStamp(TimeZone.getTimeZone("Europe/Brussels"));
        String dateStr = "1150410060500";   // Fri Apr 10 06:05:00 GMT+2

        // Business methods
        vts.parse(dateStr);
        Date date = vts.getCalendar().getTime();

        // Asserts
        Assert.assertEquals(1428638700000l, date.getTime());
    }
}