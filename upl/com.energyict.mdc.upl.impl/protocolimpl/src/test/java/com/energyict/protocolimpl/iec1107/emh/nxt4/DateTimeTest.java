package com.energyict.protocolimpl.iec1107.emh.nxt4;

import org.junit.Test;

import java.util.Date;
import java.util.TimeZone;

import static junit.framework.Assert.assertEquals;

/**
 * @author sva
 * @since 7/04/2015 - 14:57
 */
public class DateTimeTest {

    private static final String DATE_FORMAT = "yyMMddHHmmsswwnz";

    @Test
    public void testFormatSummerDateTime_DST_Timezone() throws Exception {
        DateTime dateTime = new DateTime(TimeZone.getTimeZone("Europe/Brussels"), DATE_FORMAT);

        // Business method
        Date date = new Date(1428413222000l);   // 15:27:02 07/04/15 CEST
        String formattedDate = dateTime.formatDateTime(date);

        // Asserts
        assertEquals("1504071527021501", formattedDate);
    }

    @Test
    public void testFormatWinterDateTime_DST_TimeZone() throws Exception {
        DateTime dateTime = new DateTime(TimeZone.getTimeZone("Europe/Brussels"), DATE_FORMAT);

        // Business method
        Date date = new Date(1425557771000l);   // 13:16:11 05/03/15 CET
        String formattedDate = dateTime.formatDateTime(date);

        // Asserts
        assertEquals("1503051316111000", formattedDate);
    }

    @Test
    public void testFormatSummerDateTime_GMT_Timezone() throws Exception {
        DateTime dateTime = new DateTime(TimeZone.getTimeZone("GMT"), DATE_FORMAT);

        // Business method
        Date date = new Date(1428413222000l);   // 15:27:02 07/04/15 CEST
        String formattedDate = dateTime.formatDateTime(date);

        // Asserts
        assertEquals("1504071327021500", formattedDate);
    }

    @Test
    public void testFormatWinterDateTime_GMT_TimeZone() throws Exception {
        DateTime dateTime = new DateTime(TimeZone.getTimeZone("GMT"), DATE_FORMAT);

        // Business method
        Date date = new Date(1425557771000l);   // 13:16:11 05/03/15 CET
        String formattedDate = dateTime.formatDateTime(date);

        // Asserts
        assertEquals("1503051216111000", formattedDate);
    }

    @Test
    public void testParseSummerDate_DST_Timezone() throws Exception {
        String dateTimeString = "1504071527021501";     // 15:27:02 07/04/15 CEST
        DateTime dateTime = new DateTime(TimeZone.getTimeZone("Europe/Brussels"), DATE_FORMAT);

        // Business method
        Date date = dateTime.parseDate(dateTimeString);

        // Asserts
        assertEquals(new Date(1428413222000l).toString(), date.toString());
    }

    @Test
    public void testParseWinterDate_DST_Timezone() throws Exception {
        String dateTimeString = "1503051316111000";    // 13:16:11 05/03/15 CET
        DateTime dateTime = new DateTime(TimeZone.getTimeZone("Europe/Brussels"), DATE_FORMAT);

        // Business method
        Date date = dateTime.parseDate(dateTimeString);

        // Asserts
        assertEquals(new Date(1425557771000l).toString(), date.toString());
    }

    @Test
    public void testParseSummerDate_GMT_Timezone() throws Exception {
        String dateTimeString = "1504071327021501";     // 15:27:02 07/04/15 CEST
        DateTime dateTime = new DateTime(TimeZone.getTimeZone("GMT"), DATE_FORMAT);

        // Business method
        Date date = dateTime.parseDate(dateTimeString);

        // Asserts
        assertEquals(new Date(1428413222000l).toString(), date.toString());
    }

    @Test
    public void testParseWinterDate_GMT_Timezone() throws Exception {
        String dateTimeString = "1503051216111000";    // 13:16:11 05/03/15 CET
        DateTime dateTime = new DateTime(TimeZone.getTimeZone("GMT"), DATE_FORMAT);

        // Business method
        Date date = dateTime.parseDate(dateTimeString);

        // Asserts
        assertEquals(new Date(1425557771000l).toString(), date.toString());
    }
}