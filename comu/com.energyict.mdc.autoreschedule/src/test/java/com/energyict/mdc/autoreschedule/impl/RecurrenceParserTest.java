package com.energyict.mdc.autoreschedule.impl;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class RecurrenceParserTest {

    /**
     * See {@link com.elster.jupiter.time.TimeDuration#getSeconds()}
     */
    private RecurrenceParser recurrenceParser;
    private static final int SECONDS_PER_MINUTE = 60;
    private static final int SECONDS_PER_HOUR = 60 * SECONDS_PER_MINUTE;
    private static final int SECONDS_PER_DAY = 24 * SECONDS_PER_HOUR;
    private static final int SECONDS_PER_WEEK = 7 * SECONDS_PER_DAY;
    private static final int SECONDS_PER_MONTH = 31 * SECONDS_PER_DAY;
    private static final int SECONDS_PER_YEAR = 365 * SECONDS_PER_DAY;

    @Test
    public void unknownExpression() {
        assertEquals(-1, RecurrenceParser.getSeconds("UNKNOWN"));
    }

    @Test
    public void neverSayNever() {
        assertEquals(-1, RecurrenceParser.getSeconds("NEVER"));
    }

    @Test
    public void testEveryFiveSeconds() {
        assertEquals(5, RecurrenceParser.getSeconds("P[5,SECOND,0,50]"));
    }

    @Test
    public void testEveryFiveMinutes() {
        assertEquals(5 * SECONDS_PER_MINUTE, RecurrenceParser.getSeconds("P[5,MINUTE,0,50]"));
    }

    @Test
    public void testEveryHour() {
        assertEquals(SECONDS_PER_HOUR, RecurrenceParser.getSeconds("P[1,HOUR,0,50]"));
    }

    @Test
    public void testEveryTwoHours() {
        assertEquals(2 * SECONDS_PER_HOUR, RecurrenceParser.getSeconds("P[2,HOUR,0,50]"));
    }

    @Test
    public void testEveryDay() {
        assertEquals(SECONDS_PER_DAY, RecurrenceParser.getSeconds("P[1,DAY,0,50]"));
    }

    @Test
    public void testEveryTwoDays() {
        assertEquals(2 * SECONDS_PER_DAY, RecurrenceParser.getSeconds("P[2,DAY,0,50]"));
    }

    @Test
    public void testEveryWeek() {
        assertEquals(SECONDS_PER_WEEK, RecurrenceParser.getSeconds("P[1,WEEK,0,50]"));
    }

    @Test
    public void testEveryTwoWeek() {
        assertEquals(2 * SECONDS_PER_WEEK, RecurrenceParser.getSeconds("P[2,WEEK,0,50]"));
    }

    @Test
    public void testEveryMonth() {
        assertEquals(SECONDS_PER_MONTH, RecurrenceParser.getSeconds("P[1,MONTH,0,50]"));
    }

    @Test
    public void testEveryTwoMonths() {
        assertEquals(2 * SECONDS_PER_MONTH, RecurrenceParser.getSeconds("P[2,MONTH,0,50]"));
    }

    @Test
    public void testEveryYear() {
        assertEquals(SECONDS_PER_YEAR, RecurrenceParser.getSeconds("P[1,YEAR,0,50]"));
    }

    @Test
    public void testEveryTwoYears() {
        assertEquals(2 * SECONDS_PER_YEAR, RecurrenceParser.getSeconds("P[2,YEAR,0,50]"));
    }
}
