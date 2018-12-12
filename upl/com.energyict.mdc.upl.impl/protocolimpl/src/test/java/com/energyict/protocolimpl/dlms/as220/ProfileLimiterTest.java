package com.energyict.protocolimpl.dlms.as220;

import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Copyrights EnergyICT
 * Date: 17-aug-2010
 * Time: 14:16:18
 */
public class ProfileLimiterTest {

    private static final Date FROM_DATE = ProtocolTools.createCalendar(2010, 7, 15, 7, 15, 0, 0).getTime();
    private static final Date TO_DATE = ProtocolTools.createCalendar(2010, 8, 10, 10, 30, 0, 0).getTime();

    @Test
    public void testGetOldFromToDate() throws Exception {
        assertEquals(FROM_DATE, new ProfileLimiter(FROM_DATE, TO_DATE, 0).getOldFromDate());
        assertEquals(FROM_DATE, new ProfileLimiter(FROM_DATE, TO_DATE, -20).getOldFromDate());
        assertEquals(FROM_DATE, new ProfileLimiter(FROM_DATE, TO_DATE, 20).getOldFromDate());

        assertEquals(TO_DATE, new ProfileLimiter(FROM_DATE, TO_DATE, 0).getOldToDate());
        assertEquals(TO_DATE, new ProfileLimiter(FROM_DATE, TO_DATE, -20).getOldToDate());
        assertEquals(TO_DATE, new ProfileLimiter(FROM_DATE, TO_DATE, 20).getOldToDate());
    }

    @Test
    public void testGetFromToDateZero() throws Exception {
        assertEquals(FROM_DATE, new ProfileLimiter(FROM_DATE, TO_DATE, 0).getFromDate());
        assertEquals(TO_DATE, new ProfileLimiter(FROM_DATE, TO_DATE, 0).getToDate());
    }

    @Test
    public void testGetFromToDatePositive() throws Exception {
        assertEquals(FROM_DATE, new ProfileLimiter(FROM_DATE, TO_DATE, 15).getFromDate());
        assertEquals(ProtocolTools.createCalendar(2010, 7, 16, 7, 15, 0, 0).getTime(), new ProfileLimiter(FROM_DATE, TO_DATE, 1).getToDate());
        assertEquals(ProtocolTools.createCalendar(2010, 7, 30, 7, 15, 0, 0).getTime(), new ProfileLimiter(FROM_DATE, TO_DATE, 15).getToDate());
        assertEquals(ProtocolTools.createCalendar(2010, 8, 4, 7, 15, 0, 0).getTime(), new ProfileLimiter(FROM_DATE, TO_DATE, 20).getToDate());
        assertEquals(TO_DATE, new ProfileLimiter(FROM_DATE, TO_DATE, 30).getToDate());
    }

    @Test
    public void testGetFromToDateNegative() throws Exception {
        assertEquals(TO_DATE, new ProfileLimiter(FROM_DATE, TO_DATE, -15).getToDate());
        assertEquals(ProtocolTools.createCalendar(2010, 8, 9, 10, 30, 0, 0).getTime(), new ProfileLimiter(FROM_DATE, TO_DATE, -1).getFromDate());
        assertEquals(ProtocolTools.createCalendar(2010, 7, 26, 10, 30, 0, 0).getTime(), new ProfileLimiter(FROM_DATE, TO_DATE, -15).getFromDate());
        assertEquals(ProtocolTools.createCalendar(2010, 7, 21, 10, 30, 0, 0).getTime(), new ProfileLimiter(FROM_DATE, TO_DATE, -20).getFromDate());
        assertEquals(FROM_DATE, new ProfileLimiter(FROM_DATE, TO_DATE, -30).getFromDate());
    }

}
