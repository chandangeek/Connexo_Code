/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl;

import com.elster.jupiter.cbo.RationalNumber;
import com.energyict.mdc.common.ObisCode;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class InterHarmonicMappingTest {

    @Test
    public void nonElectricityObisCodeTest() {
        ObisCode nonElectricityObisCode = ObisCode.fromString("69.0.12.7.1.255");
        RationalNumber interHarmonicFor = InterHarmonicMapping.getInterHarmonicFor(nonElectricityObisCode);

        assertThat(interHarmonicFor).isEqualTo(RationalNumber.NOTAPPLICABLE);
    }

    @Test
    public void electricityObisCodeButTotalHarmonicTest() {
        ObisCode totalHarmonic = ObisCode.fromString("1.0.12.7.0.255");
        RationalNumber interHarmonicFor = InterHarmonicMapping.getInterHarmonicFor(totalHarmonic);

        assertThat(interHarmonicFor).isEqualTo(RationalNumber.NOTAPPLICABLE);
    }

    @Test
    public void voltageFirstHarmonicTest() {
        ObisCode voltageFirstHarmonic = ObisCode.fromString("1.0.12.7.1.255");
        RationalNumber interHarmonicFor = InterHarmonicMapping.getInterHarmonicFor(voltageFirstHarmonic);

        assertThat(interHarmonicFor).isEqualTo(new RationalNumber(1, 1));
    }

    @Test
    public void voltageLastDlmsHarmonicTest() {
        ObisCode voltageLastHarmonic = ObisCode.fromString("1.0.12.7.120.255");
        RationalNumber interHarmonicFor = InterHarmonicMapping.getInterHarmonicFor(voltageLastHarmonic);

        assertThat(interHarmonicFor).isEqualTo(new RationalNumber(120, 1));
    }

    @Test
    public void voltageLastDlmsHarmonicPlusOneTest() {
        ObisCode voltageLastHarmonicPlusOne = ObisCode.fromString("1.0.12.7.121.255");
        RationalNumber interHarmonicFor = InterHarmonicMapping.getInterHarmonicFor(voltageLastHarmonicPlusOne);

        assertThat(interHarmonicFor).isEqualTo(RationalNumber.NOTAPPLICABLE);
    }
}
