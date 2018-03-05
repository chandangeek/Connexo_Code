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
    public void whenDailyPeriodThenInvalidMRID() {
        assertFalse(MridStringMatcher.isValid("11.2.0.0"));
    }

    @Test
    public void whenMonthlyPeriodThenInvalidMRID() {
        assertFalse(MridStringMatcher.isValid("13.2.0.0"));
    }

    @Test
    public void whenWeeklyPeriodThenInvalidMRID() {
        assertFalse(MridStringMatcher.isValid("24.2.0.0"));
    }

    @Test
    public void whenSeasonalPeriodThenInvalidMRID() {
        assertFalse(MridStringMatcher.isValid("22.2.0.0"));
    }
}
