/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class MacroPeriodMappingTest {

    @Test
    public void nullSafeTest() {
        ObisCode nullSafe = null;
        TimeDuration noTimeDuration = null;
        MacroPeriod macroPeriod = MacroPeriodMapping.getMacroPeriodFor(nullSafe, noTimeDuration);

        assertThat(macroPeriod).isEqualTo(MacroPeriod.NOTAPPLICABLE);
    }

    @Test
    public void billingPeriodTest() {
        ObisCode billing = ObisCode.fromString("1.0.1.8.1.1");
        TimeDuration noTimeDuration = null;
        MacroPeriod macroPeriod = MacroPeriodMapping.getMacroPeriodFor(billing, noTimeDuration);

        assertThat(macroPeriod).isEqualTo(MacroPeriod.BILLINGPERIOD);
    }

    @Test
    public void noBillingPeriodForNoTimeDurationTest() {
        ObisCode billing = ObisCode.fromString("1.0.1.8.1.255");
        TimeDuration noTimeDuration = null;
        MacroPeriod macroPeriod = MacroPeriodMapping.getMacroPeriodFor(billing, noTimeDuration);

        assertThat(macroPeriod).isEqualTo(MacroPeriod.NOTAPPLICABLE);
    }

    @Test
    public void touNonBillingForDailyShouldBeDailyTest() {
        ObisCode totalActiveEnergy = ObisCode.fromString("1.0.1.8.1.255");
        TimeDuration daily = TimeDuration.days(1);
        MacroPeriod macroPeriod = MacroPeriodMapping.getMacroPeriodFor(totalActiveEnergy, daily);

        assertThat(macroPeriod).isEqualTo(MacroPeriod.DAILY);
    }

    @Test
    public void touBillingPeriodForDailyShouldBeDailyTest() {
        ObisCode billing = ObisCode.fromString("1.0.1.8.1.1");
        TimeDuration daily = TimeDuration.days(1);
        MacroPeriod macroPeriod = MacroPeriodMapping.getMacroPeriodFor(billing, daily);

        assertThat(macroPeriod).isEqualTo(MacroPeriod.DAILY);
    }

    @Test
    public void nonTouForDailyShouldBeNotApplicableTest() {
        ObisCode totalActiveEnergy = ObisCode.fromString("1.0.1.8.0.255");
        TimeDuration daily = TimeDuration.hours(24);
        MacroPeriod macroPeriod = MacroPeriodMapping.getMacroPeriodFor(totalActiveEnergy, daily);

        assertThat(macroPeriod).isEqualTo(MacroPeriod.NOTAPPLICABLE);
    }

    @Test
    public void monthlyTimeDurationTest() {
        ObisCode totalActiveEnergy = ObisCode.fromString("1.0.1.8.0.255");
        TimeDuration monthly = TimeDuration.months(1);
        MacroPeriod macroPeriod = MacroPeriodMapping.getMacroPeriodFor(totalActiveEnergy, monthly);

        assertThat(macroPeriod).isEqualTo(MacroPeriod.MONTHLY);
    }

    @Test
    public void monthlyTimeDurationForTOUTest() {
        ObisCode totalActiveEnergy = ObisCode.fromString("1.0.1.8.1.255");
        TimeDuration monthly = TimeDuration.months(1);
        MacroPeriod macroPeriod = MacroPeriodMapping.getMacroPeriodFor(totalActiveEnergy, monthly);

        assertThat(macroPeriod).isEqualTo(MacroPeriod.MONTHLY);
    }

    @Test
    public void weeklyTimeDurationTest() {
        ObisCode totalActiveEnergy = ObisCode.fromString("1.0.1.8.0.255");
        TimeDuration weekly = TimeDuration.weeks(1);
        MacroPeriod macroPeriod = MacroPeriodMapping.getMacroPeriodFor(totalActiveEnergy, weekly);

        assertThat(macroPeriod).isEqualTo(MacroPeriod.WEEKLYS);
    }

    @Test
    public void weeklyTimeDurationForTOUTest() {
        ObisCode totalActiveEnergy = ObisCode.fromString("1.0.1.8.1.255");
        TimeDuration weekly = TimeDuration.weeks(1);
        MacroPeriod macroPeriod = MacroPeriodMapping.getMacroPeriodFor(totalActiveEnergy, weekly);

        assertThat(macroPeriod).isEqualTo(MacroPeriod.WEEKLYS);
    }
}
