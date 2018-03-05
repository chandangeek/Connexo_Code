/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.master.data.rest.impl;

import com.energyict.mdc.masterdata.rest.impl.MridStringMatcher;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MridStringMatcherTest {

    @Test
    public void whenNoPeriodAndNoTimeThenValidMRID(){
        assertTrue(MridStringMatcher.isValid("0.0.0.0"));
    }

    @Test
    public void whenNoPeriodAndTimeThenInvalidMRID(){
        assertFalse(MridStringMatcher.isValid("0.0.2.0"));
    }

    @Test
    public void whenBillingPeriodAndNoTimeThenValidMRID(){
        assertTrue(MridStringMatcher.isValid("8.0.0.0"));
    }

    @Test
    public void whenBillingPeriodAndTimeThenInvalidMRID(){
        assertFalse(MridStringMatcher.isValid("8.0.2.0"));
    }

    @Test
    public void whenDailyPeriodAndAggregateAndNoTimeThenValidMRID(){
        assertTrue(MridStringMatcher.isValid("11.2.0.0"));
    }

    @Test
    public void whenDailyPeriodAndNoAggregateAndNoTimeThenInvalidMRID(){
        assertFalse(MridStringMatcher.isValid("11.0.0.0"));
    }

    @Test
    public void whenDailyPeriodAndAggregateAndTimeThenInvalidMRID(){
        assertFalse(MridStringMatcher.isValid("11.2.2.0"));
    }

    @Test
    public void whenMonthlyPeriodAndAggregateAndNoTimeThenValidMRID(){
        assertTrue(MridStringMatcher.isValid("13.2.0.0"));
    }

    @Test
    public void whenMonthlyPeriodAndNoAggregateAndNoTimeThenInvalidMRID(){
        assertFalse(MridStringMatcher.isValid("13.0.0.0"));
    }

    @Test
    public void whenMonthlyPeriodAndInvalidAggregateAndNoTimeThenInvalidMRID(){
        assertFalse(MridStringMatcher.isValid("13.25.0.0"));
    }

    @Test
    public void whenWeeklyPeriodAndNoTimeThenInvalidMRID(){
        assertFalse(MridStringMatcher.isValid("24.0.0.0"));
    }
}
