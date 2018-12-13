/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.tools;

import com.elster.jupiter.time.TimeDuration;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test for the {@link com.energyict.mdc.engine.impl.tools.TimeDurations} component
 *
 * @author gna
 * @since 29/05/12 - 13:01
 */
public class TimeDurationsTest {

    @Test
    public void timeUnitCodeCheckTest(){
        TimeDuration tdDays = new TimeDuration(10, TimeDuration.TimeUnit.DAYS);
        TimeDuration tdHours = new TimeDuration(240, TimeDuration.TimeUnit.HOURS);
        TimeDuration tdMinutes = new TimeDuration(14400, TimeDuration.TimeUnit.MINUTES);
        TimeDuration tdMonths = new TimeDuration(13, TimeDuration.TimeUnit.MONTHS);
        TimeDuration tdMonths2 = new TimeDuration(13, TimeDuration.TimeUnit.MONTHS);
        TimeDuration tdYears = new TimeDuration(3, TimeDuration.TimeUnit.YEARS);
        TimeDuration tdYears2 = new TimeDuration(3, TimeDuration.TimeUnit.YEARS);

        assertTrue(TimeDurations.timeUnitCodeCheck(tdDays, tdHours));
        assertTrue(TimeDurations.timeUnitCodeCheck(tdDays, tdMinutes));
        assertTrue(TimeDurations.timeUnitCodeCheck(tdMinutes, tdHours));
        assertTrue(TimeDurations.timeUnitCodeCheck(tdMonths, tdMonths2));
        assertTrue(TimeDurations.timeUnitCodeCheck(tdYears2, tdYears));

        assertFalse(TimeDurations.timeUnitCodeCheck(tdMonths, tdDays));
        assertFalse(TimeDurations.timeUnitCodeCheck(tdMonths, tdHours));
        assertFalse(TimeDurations.timeUnitCodeCheck(tdMonths, tdMinutes));
        assertFalse(TimeDurations.timeUnitCodeCheck(tdMonths, tdYears));
        assertFalse(TimeDurations.timeUnitCodeCheck(tdMinutes, tdMonths));
        assertFalse(TimeDurations.timeUnitCodeCheck(tdHours, tdMonths));
        assertFalse(TimeDurations.timeUnitCodeCheck(tdDays, tdMonths));

        assertFalse(TimeDurations.timeUnitCodeCheck(tdYears, tdDays));
        assertFalse(TimeDurations.timeUnitCodeCheck(tdYears, tdHours));
        assertFalse(TimeDurations.timeUnitCodeCheck(tdYears, tdMinutes));
        assertFalse(TimeDurations.timeUnitCodeCheck(tdYears, tdMonths));
        assertFalse(TimeDurations.timeUnitCodeCheck(tdMinutes, tdYears));
        assertFalse(TimeDurations.timeUnitCodeCheck(tdHours, tdYears));
        assertFalse(TimeDurations.timeUnitCodeCheck(tdDays, tdYears));
    }

    @Test
    public void hasLargerDurationThenTest(){
        TimeDuration tdDays = new TimeDuration(10, TimeDuration.TimeUnit.DAYS);
        TimeDuration tdDaysSmaller = new TimeDuration(1, TimeDuration.TimeUnit.DAYS);
        TimeDuration tdHours = new TimeDuration(240, TimeDuration.TimeUnit.HOURS);
        TimeDuration tdHoursSmaller = new TimeDuration(24, TimeDuration.TimeUnit.HOURS);
        TimeDuration tdMinutes = new TimeDuration(14400, TimeDuration.TimeUnit.MINUTES);
        TimeDuration tdMinutesSmaller = new TimeDuration(1440, TimeDuration.TimeUnit.MINUTES);
        TimeDuration tdMonths = new TimeDuration(13, TimeDuration.TimeUnit.MONTHS);
        TimeDuration tdYears = new TimeDuration(3, TimeDuration.TimeUnit.YEARS);

        assertFalse(TimeDurations.hasLargerDurationThen(tdDays, tdHours, false));
        assertFalse(TimeDurations.hasLargerDurationThen(tdMinutes, tdHours, false));
        assertFalse(TimeDurations.hasLargerDurationThen(tdDays, tdMinutes, false));

        assertFalse(TimeDurations.hasLargerDurationThen(tdDaysSmaller, tdDays, false));
        assertFalse(TimeDurations.hasLargerDurationThen(tdHoursSmaller, tdDays, false));
        assertFalse(TimeDurations.hasLargerDurationThen(tdMinutesSmaller, tdDays, false));
        assertFalse(TimeDurations.hasLargerDurationThen(tdHoursSmaller, tdHours, false));
        assertFalse(TimeDurations.hasLargerDurationThen(tdDaysSmaller, tdHours, false));
        assertFalse(TimeDurations.hasLargerDurationThen(tdMinutesSmaller, tdHours, false));
        assertFalse(TimeDurations.hasLargerDurationThen(tdMinutesSmaller, tdMinutes, false));
        assertFalse(TimeDurations.hasLargerDurationThen(tdHours, tdMinutes, false));
        assertFalse(TimeDurations.hasLargerDurationThen(tdDays, tdMinutes, false));

        assertFalse(TimeDurations.hasLargerDurationThen(tdDays, tdMonths, false));
        assertFalse(TimeDurations.hasLargerDurationThen(tdMonths, tdDays, false));
        assertFalse(TimeDurations.hasLargerDurationThen(tdDays, tdYears, false));
        assertFalse(TimeDurations.hasLargerDurationThen(tdYears, tdDays, false));

        assertTrue(TimeDurations.hasLargerDurationThen(tdDays, tdDaysSmaller, false));
        assertTrue(TimeDurations.hasLargerDurationThen(tdDays, tdHoursSmaller, false));
        assertTrue(TimeDurations.hasLargerDurationThen(tdDays, tdMinutesSmaller, false));
        assertTrue(TimeDurations.hasLargerDurationThen(tdMinutes, tdDaysSmaller, false));
        assertTrue(TimeDurations.hasLargerDurationThen(tdMinutes, tdHoursSmaller, false));
        assertTrue(TimeDurations.hasLargerDurationThen(tdMinutes, tdMinutesSmaller, false));
        assertTrue(TimeDurations.hasLargerDurationThen(tdHours, tdDaysSmaller, false));
        assertTrue(TimeDurations.hasLargerDurationThen(tdHours, tdHoursSmaller, false));
        assertTrue(TimeDurations.hasLargerDurationThen(tdHours, tdMinutesSmaller, false));
    }

    @Test
    public void hasLargerDurationThenWithVariableTimeUnitCodeTest(){
        TimeDuration tdDays = new TimeDuration(10, TimeDuration.TimeUnit.DAYS);
        TimeDuration tdDaysSmaller = new TimeDuration(1, TimeDuration.TimeUnit.DAYS);
        TimeDuration tdHours = new TimeDuration(240, TimeDuration.TimeUnit.HOURS);
        TimeDuration tdHoursSmaller = new TimeDuration(24, TimeDuration.TimeUnit.HOURS);
        TimeDuration tdMinutes = new TimeDuration(14400, TimeDuration.TimeUnit.MINUTES);
        TimeDuration tdMinutesSmaller = new TimeDuration(1440, TimeDuration.TimeUnit.MINUTES);
        TimeDuration tdMonths = new TimeDuration(13, TimeDuration.TimeUnit.MONTHS);
        TimeDuration tdYears = new TimeDuration(3, TimeDuration.TimeUnit.YEARS);

        assertFalse(TimeDurations.hasLargerDurationThen(tdDays, tdHours, true));
        assertFalse(TimeDurations.hasLargerDurationThen(tdMinutes, tdHours, true));
        assertFalse(TimeDurations.hasLargerDurationThen(tdDays, tdMinutes, true));

        assertFalse(TimeDurations.hasLargerDurationThen(tdDaysSmaller, tdDays, true));
        assertFalse(TimeDurations.hasLargerDurationThen(tdHoursSmaller, tdDays, true));
        assertFalse(TimeDurations.hasLargerDurationThen(tdMinutesSmaller, tdDays, true));
        assertFalse(TimeDurations.hasLargerDurationThen(tdHoursSmaller, tdHours, true));
        assertFalse(TimeDurations.hasLargerDurationThen(tdDaysSmaller, tdHours, true));
        assertFalse(TimeDurations.hasLargerDurationThen(tdMinutesSmaller, tdHours, true));
        assertFalse(TimeDurations.hasLargerDurationThen(tdMinutesSmaller, tdMinutes, true));
        assertFalse(TimeDurations.hasLargerDurationThen(tdHours, tdMinutes, true));
        assertFalse(TimeDurations.hasLargerDurationThen(tdDays, tdMinutes, true));

        assertFalse(TimeDurations.hasLargerDurationThen(tdDays, tdMonths, true));
        assertTrue(TimeDurations.hasLargerDurationThen(tdMonths, tdDays, true));
        assertFalse(TimeDurations.hasLargerDurationThen(tdDays, tdYears, true));
        assertTrue(TimeDurations.hasLargerDurationThen(tdYears, tdDays, true));

        assertTrue(TimeDurations.hasLargerDurationThen(tdDays, tdDaysSmaller, true));
        assertTrue(TimeDurations.hasLargerDurationThen(tdDays, tdHoursSmaller, true));
        assertTrue(TimeDurations.hasLargerDurationThen(tdDays, tdMinutesSmaller, true));
        assertTrue(TimeDurations.hasLargerDurationThen(tdMinutes, tdDaysSmaller, true));
        assertTrue(TimeDurations.hasLargerDurationThen(tdMinutes, tdHoursSmaller, true));
        assertTrue(TimeDurations.hasLargerDurationThen(tdMinutes, tdMinutesSmaller, true));
        assertTrue(TimeDurations.hasLargerDurationThen(tdHours, tdDaysSmaller, true));
        assertTrue(TimeDurations.hasLargerDurationThen(tdHours, tdHoursSmaller, true));
        assertTrue(TimeDurations.hasLargerDurationThen(tdHours, tdMinutesSmaller, true));
    }

    @Test
    public void hasLargerOrEqualDurationThenTest(){
        TimeDuration tdDays = new TimeDuration(10, TimeDuration.TimeUnit.DAYS);
        TimeDuration tdDaysSmaller = new TimeDuration(1, TimeDuration.TimeUnit.DAYS);
        TimeDuration tdHours = new TimeDuration(240, TimeDuration.TimeUnit.HOURS);
        TimeDuration tdHoursSmaller = new TimeDuration(24, TimeDuration.TimeUnit.HOURS);
        TimeDuration tdMinutes = new TimeDuration(14400, TimeDuration.TimeUnit.MINUTES);
        TimeDuration tdMinutesSmaller = new TimeDuration(1440, TimeDuration.TimeUnit.MINUTES);
        TimeDuration tdMonths = new TimeDuration(13, TimeDuration.TimeUnit.MONTHS);
        TimeDuration tdYears = new TimeDuration(3, TimeDuration.TimeUnit.YEARS);

        assertTrue(TimeDurations.hasLargerOrEqualDurationThen(tdDays, tdHours, false));
        assertTrue(TimeDurations.hasLargerOrEqualDurationThen(tdMinutes, tdHours, false));
        assertTrue(TimeDurations.hasLargerOrEqualDurationThen(tdDays, tdMinutes, false));

        assertFalse(TimeDurations.hasLargerOrEqualDurationThen(tdDaysSmaller, tdDays, false));
        assertFalse(TimeDurations.hasLargerOrEqualDurationThen(tdHoursSmaller, tdDays, false));
        assertFalse(TimeDurations.hasLargerOrEqualDurationThen(tdMinutesSmaller, tdDays, false));
        assertFalse(TimeDurations.hasLargerOrEqualDurationThen(tdHoursSmaller, tdHours, false));
        assertFalse(TimeDurations.hasLargerOrEqualDurationThen(tdDaysSmaller, tdHours, false));
        assertFalse(TimeDurations.hasLargerOrEqualDurationThen(tdMinutesSmaller, tdHours, false));
        assertFalse(TimeDurations.hasLargerOrEqualDurationThen(tdMinutesSmaller, tdMinutes, false));
        assertTrue(TimeDurations.hasLargerOrEqualDurationThen(tdHours, tdMinutes, false));
        assertTrue(TimeDurations.hasLargerOrEqualDurationThen(tdDays, tdMinutes, false));

        assertFalse(TimeDurations.hasLargerOrEqualDurationThen(tdDays, tdMonths, false));
        assertFalse(TimeDurations.hasLargerOrEqualDurationThen(tdMonths, tdDays, false));
        assertFalse(TimeDurations.hasLargerOrEqualDurationThen(tdDays, tdYears, false));
        assertFalse(TimeDurations.hasLargerOrEqualDurationThen(tdYears, tdDays, false));

        assertTrue(TimeDurations.hasLargerOrEqualDurationThen(tdDays, tdDaysSmaller, false));
        assertTrue(TimeDurations.hasLargerOrEqualDurationThen(tdDays, tdHoursSmaller, false));
        assertTrue(TimeDurations.hasLargerOrEqualDurationThen(tdDays, tdMinutesSmaller, false));
        assertTrue(TimeDurations.hasLargerOrEqualDurationThen(tdMinutes, tdDaysSmaller, false));
        assertTrue(TimeDurations.hasLargerOrEqualDurationThen(tdMinutes, tdHoursSmaller, false));
        assertTrue(TimeDurations.hasLargerOrEqualDurationThen(tdMinutes, tdMinutesSmaller, false));
        assertTrue(TimeDurations.hasLargerOrEqualDurationThen(tdHours, tdDaysSmaller, false));
        assertTrue(TimeDurations.hasLargerOrEqualDurationThen(tdHours, tdHoursSmaller, false));
        assertTrue(TimeDurations.hasLargerOrEqualDurationThen(tdHours, tdMinutesSmaller, false));
    }

    @Test
    public void hasLargerOrEqualDurationThenWithVariableTimeUnitCodeTest(){
        TimeDuration tdDays = new TimeDuration(10, TimeDuration.TimeUnit.DAYS);
        TimeDuration tdDaysSmaller = new TimeDuration(1, TimeDuration.TimeUnit.DAYS);
        TimeDuration tdHours = new TimeDuration(240, TimeDuration.TimeUnit.HOURS);
        TimeDuration tdHoursSmaller = new TimeDuration(24, TimeDuration.TimeUnit.HOURS);
        TimeDuration tdMinutes = new TimeDuration(14400, TimeDuration.TimeUnit.MINUTES);
        TimeDuration tdMinutesSmaller = new TimeDuration(1440, TimeDuration.TimeUnit.MINUTES);
        TimeDuration tdMonths = new TimeDuration(13, TimeDuration.TimeUnit.MONTHS);
        TimeDuration tdMonths2 = new TimeDuration(13, TimeDuration.TimeUnit.MONTHS);
        TimeDuration tdYears = new TimeDuration(3, TimeDuration.TimeUnit.YEARS);
        TimeDuration tdYears2 = new TimeDuration(3, TimeDuration.TimeUnit.YEARS);

        assertTrue(TimeDurations.hasLargerOrEqualDurationThen(tdDays, tdHours, true));
        assertTrue(TimeDurations.hasLargerOrEqualDurationThen(tdMinutes, tdHours, true));
        assertTrue(TimeDurations.hasLargerOrEqualDurationThen(tdDays, tdMinutes, true));

        assertFalse(TimeDurations.hasLargerOrEqualDurationThen(tdDaysSmaller, tdDays, true));
        assertFalse(TimeDurations.hasLargerOrEqualDurationThen(tdHoursSmaller, tdDays, true));
        assertFalse(TimeDurations.hasLargerOrEqualDurationThen(tdMinutesSmaller, tdDays, true));
        assertFalse(TimeDurations.hasLargerOrEqualDurationThen(tdHoursSmaller, tdHours, true));
        assertFalse(TimeDurations.hasLargerOrEqualDurationThen(tdDaysSmaller, tdHours, true));
        assertFalse(TimeDurations.hasLargerOrEqualDurationThen(tdMinutesSmaller, tdHours, true));
        assertFalse(TimeDurations.hasLargerOrEqualDurationThen(tdMinutesSmaller, tdMinutes, true));
        assertTrue(TimeDurations.hasLargerOrEqualDurationThen(tdHours, tdMinutes, true));
        assertTrue(TimeDurations.hasLargerOrEqualDurationThen(tdDays, tdMinutes, true));

        assertFalse(TimeDurations.hasLargerOrEqualDurationThen(tdDays, tdMonths, true));
        assertTrue(TimeDurations.hasLargerOrEqualDurationThen(tdMonths, tdDays, true));
        assertTrue(TimeDurations.hasLargerOrEqualDurationThen(tdMonths, tdMonths2, true));
        assertFalse(TimeDurations.hasLargerOrEqualDurationThen(tdDays, tdYears, true));
        assertTrue(TimeDurations.hasLargerOrEqualDurationThen(tdYears, tdDays, true));
        assertTrue(TimeDurations.hasLargerOrEqualDurationThen(tdYears, tdYears2, true));

        assertTrue(TimeDurations.hasLargerOrEqualDurationThen(tdDays, tdDaysSmaller, true));
        assertTrue(TimeDurations.hasLargerOrEqualDurationThen(tdDays, tdHoursSmaller, true));
        assertTrue(TimeDurations.hasLargerOrEqualDurationThen(tdDays, tdMinutesSmaller, true));
        assertTrue(TimeDurations.hasLargerOrEqualDurationThen(tdMinutes, tdDaysSmaller, true));
        assertTrue(TimeDurations.hasLargerOrEqualDurationThen(tdMinutes, tdHoursSmaller, true));
        assertTrue(TimeDurations.hasLargerOrEqualDurationThen(tdMinutes, tdMinutesSmaller, true));
        assertTrue(TimeDurations.hasLargerOrEqualDurationThen(tdHours, tdDaysSmaller, true));
        assertTrue(TimeDurations.hasLargerOrEqualDurationThen(tdHours, tdHoursSmaller, true));
        assertTrue(TimeDurations.hasLargerOrEqualDurationThen(tdHours, tdMinutesSmaller, true));
    }

    @Test
    public void hasSmallerDurationThenTest(){
        TimeDuration tdDays = new TimeDuration(10, TimeDuration.TimeUnit.DAYS);
        TimeDuration tdDaysSmaller = new TimeDuration(1, TimeDuration.TimeUnit.DAYS);
        TimeDuration tdHours = new TimeDuration(240, TimeDuration.TimeUnit.HOURS);
        TimeDuration tdHoursSmaller = new TimeDuration(24, TimeDuration.TimeUnit.HOURS);
        TimeDuration tdMinutes = new TimeDuration(14400, TimeDuration.TimeUnit.MINUTES);
        TimeDuration tdMinutesSmaller = new TimeDuration(1440, TimeDuration.TimeUnit.MINUTES);
        TimeDuration tdMonths = new TimeDuration(13, TimeDuration.TimeUnit.MONTHS);
        TimeDuration tdYears = new TimeDuration(3, TimeDuration.TimeUnit.YEARS);

        assertFalse(TimeDurations.hasSmallerDurationThen(tdDays, tdHours, false));
        assertFalse(TimeDurations.hasSmallerDurationThen(tdMinutes, tdHours, false));
        assertFalse(TimeDurations.hasSmallerDurationThen(tdDays, tdMinutes, false));

        assertTrue(TimeDurations.hasSmallerDurationThen(tdDaysSmaller, tdDays, false));
        assertTrue(TimeDurations.hasSmallerDurationThen(tdHoursSmaller, tdDays, false));
        assertTrue(TimeDurations.hasSmallerDurationThen(tdMinutesSmaller, tdDays, false));
        assertTrue(TimeDurations.hasSmallerDurationThen(tdHoursSmaller, tdHours, false));
        assertTrue(TimeDurations.hasSmallerDurationThen(tdDaysSmaller, tdHours, false));
        assertTrue(TimeDurations.hasSmallerDurationThen(tdMinutesSmaller, tdHours, false));
        assertTrue(TimeDurations.hasSmallerDurationThen(tdMinutesSmaller, tdMinutes, false));
        assertFalse(TimeDurations.hasSmallerDurationThen(tdHours, tdMinutes, false));
        assertFalse(TimeDurations.hasSmallerDurationThen(tdDays, tdMinutes, false));

        assertFalse(TimeDurations.hasSmallerDurationThen(tdDays, tdMonths, false));
        assertFalse(TimeDurations.hasSmallerDurationThen(tdMonths, tdDays, false));
        assertFalse(TimeDurations.hasSmallerDurationThen(tdDays, tdYears, false));
        assertFalse(TimeDurations.hasSmallerDurationThen(tdYears, tdDays, false));

        assertFalse(TimeDurations.hasSmallerDurationThen(tdDays, tdDaysSmaller, false));
        assertFalse(TimeDurations.hasSmallerDurationThen(tdDays, tdHoursSmaller, false));
        assertFalse(TimeDurations.hasSmallerDurationThen(tdDays, tdMinutesSmaller, false));
        assertFalse(TimeDurations.hasSmallerDurationThen(tdMinutes, tdDaysSmaller, false));
        assertFalse(TimeDurations.hasSmallerDurationThen(tdMinutes, tdHoursSmaller, false));
        assertFalse(TimeDurations.hasSmallerDurationThen(tdMinutes, tdMinutesSmaller, false));
        assertFalse(TimeDurations.hasSmallerDurationThen(tdHours, tdDaysSmaller, false));
        assertFalse(TimeDurations.hasSmallerDurationThen(tdHours, tdHoursSmaller, false));
        assertFalse(TimeDurations.hasSmallerDurationThen(tdHours, tdMinutesSmaller, false));
    }

    @Test
    public void hasSmallerDurationThenWithVariableTimeUnitCodeTest(){
        TimeDuration tdDays = new TimeDuration(10, TimeDuration.TimeUnit.DAYS);
        TimeDuration tdDaysSmaller = new TimeDuration(1, TimeDuration.TimeUnit.DAYS);
        TimeDuration tdHours = new TimeDuration(240, TimeDuration.TimeUnit.HOURS);
        TimeDuration tdHoursSmaller = new TimeDuration(24, TimeDuration.TimeUnit.HOURS);
        TimeDuration tdMinutes = new TimeDuration(14400, TimeDuration.TimeUnit.MINUTES);
        TimeDuration tdMinutesSmaller = new TimeDuration(1440, TimeDuration.TimeUnit.MINUTES);
        TimeDuration tdMonths = new TimeDuration(13, TimeDuration.TimeUnit.MONTHS);
        TimeDuration tdMonthsSmaller = new TimeDuration(12, TimeDuration.TimeUnit.MONTHS);
        TimeDuration tdYears = new TimeDuration(3, TimeDuration.TimeUnit.YEARS);
        TimeDuration tdYearsSmaller = new TimeDuration(1, TimeDuration.TimeUnit.YEARS);

        assertFalse(TimeDurations.hasSmallerDurationThen(tdDays, tdHours, true));
        assertFalse(TimeDurations.hasSmallerDurationThen(tdMinutes, tdHours, true));
        assertFalse(TimeDurations.hasSmallerDurationThen(tdDays, tdMinutes, true));

        assertTrue(TimeDurations.hasSmallerDurationThen(tdDaysSmaller, tdDays, true));
        assertTrue(TimeDurations.hasSmallerDurationThen(tdHoursSmaller, tdDays, true));
        assertTrue(TimeDurations.hasSmallerDurationThen(tdMinutesSmaller, tdDays, true));
        assertTrue(TimeDurations.hasSmallerDurationThen(tdHoursSmaller, tdHours, true));
        assertTrue(TimeDurations.hasSmallerDurationThen(tdDaysSmaller, tdHours, true));
        assertTrue(TimeDurations.hasSmallerDurationThen(tdMinutesSmaller, tdHours, true));
        assertTrue(TimeDurations.hasSmallerDurationThen(tdMinutesSmaller, tdMinutes, true));
        assertFalse(TimeDurations.hasSmallerDurationThen(tdHours, tdMinutes, true));
        assertFalse(TimeDurations.hasSmallerDurationThen(tdDays, tdMinutes, true));

        assertTrue(TimeDurations.hasSmallerDurationThen(tdDays, tdMonths, true));
        assertTrue(TimeDurations.hasSmallerDurationThen(tdMonthsSmaller, tdMonths, true));
        assertFalse(TimeDurations.hasSmallerDurationThen(tdMonths, tdDays, true));
        assertTrue(TimeDurations.hasSmallerDurationThen(tdDays, tdYears, true));
        assertTrue(TimeDurations.hasSmallerDurationThen(tdYearsSmaller, tdYears, true));
        assertFalse(TimeDurations.hasSmallerDurationThen(tdYears, tdDays, true));

        assertFalse(TimeDurations.hasSmallerDurationThen(tdDays, tdDaysSmaller, true));
        assertFalse(TimeDurations.hasSmallerDurationThen(tdDays, tdHoursSmaller, true));
        assertFalse(TimeDurations.hasSmallerDurationThen(tdDays, tdMinutesSmaller, true));
        assertFalse(TimeDurations.hasSmallerDurationThen(tdMinutes, tdDaysSmaller, true));
        assertFalse(TimeDurations.hasSmallerDurationThen(tdMinutes, tdHoursSmaller, true));
        assertFalse(TimeDurations.hasSmallerDurationThen(tdMinutes, tdMinutesSmaller, true));
        assertFalse(TimeDurations.hasSmallerDurationThen(tdHours, tdDaysSmaller, true));
        assertFalse(TimeDurations.hasSmallerDurationThen(tdHours, tdHoursSmaller, true));
        assertFalse(TimeDurations.hasSmallerDurationThen(tdHours, tdMinutesSmaller, true));
    }

    @Test
    public void hasSmallerOrEqualDurationThenWithVariableTimeUnitCodeTest(){
        TimeDuration tdDays = new TimeDuration(10, TimeDuration.TimeUnit.DAYS);
        TimeDuration tdDaysSmaller = new TimeDuration(1, TimeDuration.TimeUnit.DAYS);
        TimeDuration tdHours = new TimeDuration(240, TimeDuration.TimeUnit.HOURS);
        TimeDuration tdHoursSmaller = new TimeDuration(24, TimeDuration.TimeUnit.HOURS);
        TimeDuration tdMinutes = new TimeDuration(14400, TimeDuration.TimeUnit.MINUTES);
        TimeDuration tdMinutesSmaller = new TimeDuration(1440, TimeDuration.TimeUnit.MINUTES);
        TimeDuration tdMonths = new TimeDuration(13, TimeDuration.TimeUnit.MONTHS);
        TimeDuration tdMonthsSmaller = new TimeDuration(12, TimeDuration.TimeUnit.MONTHS);
        TimeDuration tdYears = new TimeDuration(3, TimeDuration.TimeUnit.YEARS);
        TimeDuration tdYearsSmaller = new TimeDuration(1, TimeDuration.TimeUnit.YEARS);

        assertTrue(TimeDurations.hasSmallerOrEqualDurationThen(tdDays, tdHours, true));
        assertTrue(TimeDurations.hasSmallerOrEqualDurationThen(tdMinutes, tdHours, true));
        assertTrue(TimeDurations.hasSmallerOrEqualDurationThen(tdDays, tdMinutes, true));

        assertTrue(TimeDurations.hasSmallerOrEqualDurationThen(tdDaysSmaller, tdDays, true));
        assertTrue(TimeDurations.hasSmallerOrEqualDurationThen(tdHoursSmaller, tdDays, true));
        assertTrue(TimeDurations.hasSmallerOrEqualDurationThen(tdMinutesSmaller, tdDays, true));
        assertTrue(TimeDurations.hasSmallerOrEqualDurationThen(tdHoursSmaller, tdHours, true));
        assertTrue(TimeDurations.hasSmallerOrEqualDurationThen(tdDaysSmaller, tdHours, true));
        assertTrue(TimeDurations.hasSmallerOrEqualDurationThen(tdMinutesSmaller, tdHours, true));
        assertTrue(TimeDurations.hasSmallerOrEqualDurationThen(tdMinutesSmaller, tdMinutes, true));
        assertTrue(TimeDurations.hasSmallerOrEqualDurationThen(tdHours, tdMinutes, true));
        assertTrue(TimeDurations.hasSmallerOrEqualDurationThen(tdDays, tdMinutes, true));

        assertTrue(TimeDurations.hasSmallerOrEqualDurationThen(tdDays, tdMonths, true));
        assertTrue(TimeDurations.hasSmallerOrEqualDurationThen(tdMonthsSmaller, tdMonths, true));
        assertFalse(TimeDurations.hasSmallerOrEqualDurationThen(tdMonths, tdDays, true));
        assertTrue(TimeDurations.hasSmallerOrEqualDurationThen(tdDays, tdYears, true));
        assertTrue(TimeDurations.hasSmallerOrEqualDurationThen(tdYearsSmaller, tdYears, true));
        assertFalse(TimeDurations.hasSmallerOrEqualDurationThen(tdYears, tdDays, true));

        assertFalse(TimeDurations.hasSmallerOrEqualDurationThen(tdDays, tdDaysSmaller, true));
        assertFalse(TimeDurations.hasSmallerOrEqualDurationThen(tdDays, tdHoursSmaller, true));
        assertFalse(TimeDurations.hasSmallerOrEqualDurationThen(tdDays, tdMinutesSmaller, true));
        assertFalse(TimeDurations.hasSmallerOrEqualDurationThen(tdMinutes, tdDaysSmaller, true));
        assertFalse(TimeDurations.hasSmallerOrEqualDurationThen(tdMinutes, tdHoursSmaller, true));
        assertFalse(TimeDurations.hasSmallerOrEqualDurationThen(tdMinutes, tdMinutesSmaller, true));
        assertFalse(TimeDurations.hasSmallerOrEqualDurationThen(tdHours, tdDaysSmaller, true));
        assertFalse(TimeDurations.hasSmallerOrEqualDurationThen(tdHours, tdHoursSmaller, true));
        assertFalse(TimeDurations.hasSmallerOrEqualDurationThen(tdHours, tdMinutesSmaller, true));
    }

    @Test
    public void hasSmallerOrEqualDurationThenTest(){
        TimeDuration tdDays = new TimeDuration(10, TimeDuration.TimeUnit.DAYS);
        TimeDuration tdDaysSmaller = new TimeDuration(1, TimeDuration.TimeUnit.DAYS);
        TimeDuration tdHours = new TimeDuration(240, TimeDuration.TimeUnit.HOURS);
        TimeDuration tdHoursSmaller = new TimeDuration(24, TimeDuration.TimeUnit.HOURS);
        TimeDuration tdMinutes = new TimeDuration(14400, TimeDuration.TimeUnit.MINUTES);
        TimeDuration tdMinutesSmaller = new TimeDuration(1440, TimeDuration.TimeUnit.MINUTES);
        TimeDuration tdMonths = new TimeDuration(13, TimeDuration.TimeUnit.MONTHS);
        TimeDuration tdMonthsSmaller = new TimeDuration(12, TimeDuration.TimeUnit.MONTHS);
        TimeDuration tdYears = new TimeDuration(3, TimeDuration.TimeUnit.YEARS);
        TimeDuration tdYearsSmaller = new TimeDuration(1, TimeDuration.TimeUnit.YEARS);

        assertTrue(TimeDurations.hasSmallerOrEqualDurationThen(tdDays, tdHours, false));
        assertTrue(TimeDurations.hasSmallerOrEqualDurationThen(tdMinutes, tdHours, false));
        assertTrue(TimeDurations.hasSmallerOrEqualDurationThen(tdDays, tdMinutes, false));

        assertTrue(TimeDurations.hasSmallerOrEqualDurationThen(tdDaysSmaller, tdDays, false));
        assertTrue(TimeDurations.hasSmallerOrEqualDurationThen(tdHoursSmaller, tdDays, false));
        assertTrue(TimeDurations.hasSmallerOrEqualDurationThen(tdMinutesSmaller, tdDays, false));
        assertTrue(TimeDurations.hasSmallerOrEqualDurationThen(tdHoursSmaller, tdHours, false));
        assertTrue(TimeDurations.hasSmallerOrEqualDurationThen(tdDaysSmaller, tdHours, false));
        assertTrue(TimeDurations.hasSmallerOrEqualDurationThen(tdMinutesSmaller, tdHours, false));
        assertTrue(TimeDurations.hasSmallerOrEqualDurationThen(tdMinutesSmaller, tdMinutes, false));
        assertTrue(TimeDurations.hasSmallerOrEqualDurationThen(tdHours, tdMinutes, false));
        assertTrue(TimeDurations.hasSmallerOrEqualDurationThen(tdDays, tdMinutes, false));

        assertFalse(TimeDurations.hasSmallerOrEqualDurationThen(tdDays, tdMonths, false));
        assertTrue(TimeDurations.hasSmallerOrEqualDurationThen(tdMonthsSmaller, tdMonths, false));
        assertFalse(TimeDurations.hasSmallerOrEqualDurationThen(tdMonths, tdDays, false));
        assertFalse(TimeDurations.hasSmallerOrEqualDurationThen(tdDays, tdYears, false));
        assertTrue(TimeDurations.hasSmallerOrEqualDurationThen(tdYearsSmaller, tdYears, false));
        assertFalse(TimeDurations.hasSmallerOrEqualDurationThen(tdYears, tdDays, false));

        assertFalse(TimeDurations.hasSmallerOrEqualDurationThen(tdDays, tdDaysSmaller, false));
        assertFalse(TimeDurations.hasSmallerOrEqualDurationThen(tdDays, tdHoursSmaller, false));
        assertFalse(TimeDurations.hasSmallerOrEqualDurationThen(tdDays, tdMinutesSmaller, false));
        assertFalse(TimeDurations.hasSmallerOrEqualDurationThen(tdMinutes, tdDaysSmaller, false));
        assertFalse(TimeDurations.hasSmallerOrEqualDurationThen(tdMinutes, tdHoursSmaller, false));
        assertFalse(TimeDurations.hasSmallerOrEqualDurationThen(tdMinutes, tdMinutesSmaller, false));
        assertFalse(TimeDurations.hasSmallerOrEqualDurationThen(tdHours, tdDaysSmaller, false));
        assertFalse(TimeDurations.hasSmallerOrEqualDurationThen(tdHours, tdHoursSmaller, false));
        assertFalse(TimeDurations.hasSmallerOrEqualDurationThen(tdHours, tdMinutesSmaller, false));
    }
}
