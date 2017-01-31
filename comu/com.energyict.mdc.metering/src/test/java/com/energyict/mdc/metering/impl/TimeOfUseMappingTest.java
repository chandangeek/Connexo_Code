/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl;

import com.energyict.mdc.common.ObisCode;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class TimeOfUseMappingTest {

    @Test
    public void nonElectricityOrGasObisCodeTest() {
        ObisCode nonElectricityOrGasObisCode = ObisCode.fromString("69.0.1.8.1.255");
        int timeOfUseFor = TimeOfUseMapping.getTimeOfUseFor(nonElectricityOrGasObisCode);

        assertThat(timeOfUseFor).isEqualTo(TimeOfUseMapping.FIXED_NON_TOU_CODE);
    }

    @Test
    public void electricityButNotTOUTest() {
        ObisCode electricityButNotTOU = ObisCode.fromString("1.0.1.8.0.255");
        int timeOfUseFor = TimeOfUseMapping.getTimeOfUseFor(electricityButNotTOU);

        assertThat(timeOfUseFor).isEqualTo(TimeOfUseMapping.FIXED_NON_TOU_CODE);
    }

    @Test
    public void gasButNotTOUTest() {
        ObisCode gasButNotTOU = ObisCode.fromString("7.0.1.8.0.255");
        int timeOfUseFor = TimeOfUseMapping.getTimeOfUseFor(gasButNotTOU);

        assertThat(timeOfUseFor).isEqualTo(TimeOfUseMapping.FIXED_NON_TOU_CODE);
    }

    @Test
    public void properEFieldTest() {
        String obisCode = "1.0.1.8.x.255";
        for (int i = 1; i <= 255; i++) {
            int x = TimeOfUseMapping.getTimeOfUseFor(ObisCode.fromString(obisCode.replaceAll("x", String.valueOf(i))));
            if (i <= 63) {
                assertThat(x).describedAs("" + x + " should be equal to " + i).isEqualTo(i);
            } else {
                assertThat(x).isEqualTo(TimeOfUseMapping.FIXED_NON_TOU_CODE);
            }
        }
    }

    @Test
    public void nullSafeTest() {
        ObisCode nullSafe = null;
        int timeOfUseFor = TimeOfUseMapping.getTimeOfUseFor(nullSafe);

        assertThat(timeOfUseFor).isEqualTo(TimeOfUseMapping.FIXED_NON_TOU_CODE);
    }
}
