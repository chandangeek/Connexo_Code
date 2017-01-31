/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl;

import com.elster.jupiter.cbo.Phase;
import com.energyict.mdc.common.ObisCode;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class PhaseMappingTest {

    @Test
    public void nonElectricityTest() {
        ObisCode nonElectricityObisCode = ObisCode.fromString("7.0.12.7.0.255");
        assertThat(PhaseMapping.getPhaseFor(nonElectricityObisCode)).isEqualTo(Phase.NOTAPPLICABLE);
    }

    @Test
    public void electricityButNotAPhaseTest() {
        ObisCode electricityButNotAPhase = ObisCode.fromString("1.0.1.8.0.255");
        assertThat(PhaseMapping.getPhaseFor(electricityButNotAPhase)).isEqualTo(Phase.NOTAPPLICABLE);
    }

    @Test
    public void currentAllPhasesTest() {
        ObisCode currentObisCode1 = ObisCode.fromString("1.0.11.7.0.255");
        assertThat(PhaseMapping.getPhaseFor(currentObisCode1)).isEqualTo(Phase.PHASEABC);
        assertThat(PhaseMapping.getPhaseFor(currentObisCode1).getId()).isEqualTo(224);
        ObisCode currentObisCode2 = ObisCode.fromString("1.0.88.7.0.255");
        assertThat(PhaseMapping.getPhaseFor(currentObisCode2)).isEqualTo(Phase.PHASEABC);
        assertThat(PhaseMapping.getPhaseFor(currentObisCode2).getId()).isEqualTo(224);
        ObisCode currentObisCode3 = ObisCode.fromString("1.0.90.7.0.255");
        assertThat(PhaseMapping.getPhaseFor(currentObisCode3)).isEqualTo(Phase.PHASEABC);
        assertThat(PhaseMapping.getPhaseFor(currentObisCode3).getId()).isEqualTo(224);
    }

    @Test
    public void voltageAllPhasesTest() {
        ObisCode voltageObisCode1 = ObisCode.fromString("1.0.12.7.0.255");
        assertThat(PhaseMapping.getPhaseFor(voltageObisCode1)).isEqualTo(Phase.PHASEABCN);
        assertThat(PhaseMapping.getPhaseFor(voltageObisCode1).getId()).isEqualTo(225);
        ObisCode voltageObisCode2 = ObisCode.fromString("1.0.89.7.0.255");
        assertThat(PhaseMapping.getPhaseFor(voltageObisCode2)).isEqualTo(Phase.PHASEABCN);
        assertThat(PhaseMapping.getPhaseFor(voltageObisCode2).getId()).isEqualTo(225);
    }

    @Test
    public void currentPhaseAToNTest() {
        ObisCode currentPhaseAToN = ObisCode.fromString("1.0.31.7.0.255");
        assertThat(PhaseMapping.getPhaseFor(currentPhaseAToN)).isEqualTo(Phase.PHASEA);
        assertThat(PhaseMapping.getPhaseFor(currentPhaseAToN).getId()).isEqualTo(128);
    }

    @Test
    public void voltagePhaseATest() {
        ObisCode voltagePhaseA = ObisCode.fromString("1.0.32.7.0.255");
        assertThat(PhaseMapping.getPhaseFor(voltagePhaseA)).isEqualTo(Phase.PHASEAN);
        assertThat(PhaseMapping.getPhaseFor(voltagePhaseA).getId()).isEqualTo(129);
    }

    @Test
    public void currentPhaseBToNTest() {
        ObisCode currentPhaseBToN = ObisCode.fromString("1.0.51.7.0.255");
        assertThat(PhaseMapping.getPhaseFor(currentPhaseBToN)).isEqualTo(Phase.PHASEB);
        assertThat(PhaseMapping.getPhaseFor(currentPhaseBToN).getId()).isEqualTo(64);
    }

    @Test
    public void voltagePhaseBTest() {
        ObisCode voltagePhaseB = ObisCode.fromString("1.0.52.7.0.255");
        assertThat(PhaseMapping.getPhaseFor(voltagePhaseB)).isEqualTo(Phase.PHASEBN);
        assertThat(PhaseMapping.getPhaseFor(voltagePhaseB).getId()).isEqualTo(65);
    }

    @Test
    public void currentPhaseCToNTest() {
        ObisCode currentPhaseCToN = ObisCode.fromString("1.0.71.7.0.255");
        assertThat(PhaseMapping.getPhaseFor(currentPhaseCToN)).isEqualTo(Phase.PHASEC);
        assertThat(PhaseMapping.getPhaseFor(currentPhaseCToN).getId()).isEqualTo(32);
    }

    @Test
    public void voltagePhaseCTest() {
        ObisCode voltagePhaseC = ObisCode.fromString("1.0.72.7.0.255");
        assertThat(PhaseMapping.getPhaseFor(voltagePhaseC)).isEqualTo(Phase.PHASECN);
        assertThat(PhaseMapping.getPhaseFor(voltagePhaseC).getId()).isEqualTo(33);
    }

    @Test
    public void neutralPhaseTest() {
        ObisCode neutral1 = ObisCode.fromString("1.0.91.7.0.255");
        assertThat(PhaseMapping.getPhaseFor(neutral1)).isEqualTo(Phase.PHASEN);
        assertThat(PhaseMapping.getPhaseFor(neutral1).getId()).isEqualTo(16);
        ObisCode neutral2 = ObisCode.fromString("1.0.92.7.0.255");
        assertThat(PhaseMapping.getPhaseFor(neutral2)).isEqualTo(Phase.PHASEN);
        assertThat(PhaseMapping.getPhaseFor(neutral2).getId()).isEqualTo(16);
    }

    @Test
    public void phaseARelativePhaseATest() {
        ObisCode phaseARelativePhaseA = ObisCode.fromString("1.0.81.7.4.255");
        assertThat(PhaseMapping.getPhaseFor(phaseARelativePhaseA)).isEqualTo(Phase.PHASEAA);
        assertThat(PhaseMapping.getPhaseFor(phaseARelativePhaseA).getId()).isEqualTo(136);
    }

    @Test
    public void phaseBRelativePhaseATest() {
        ObisCode phaseBRelativePhaseA1 = ObisCode.fromString("1.0.81.7.1.255");
        assertThat(PhaseMapping.getPhaseFor(phaseBRelativePhaseA1)).isEqualTo(Phase.PHASEBA);
        assertThat(PhaseMapping.getPhaseFor(phaseBRelativePhaseA1).getId()).isEqualTo(72);
        ObisCode phaseBRelativePhaseA2 = ObisCode.fromString("1.0.81.7.5.255");
        assertThat(PhaseMapping.getPhaseFor(phaseBRelativePhaseA2)).isEqualTo(Phase.PHASEBA);
        assertThat(PhaseMapping.getPhaseFor(phaseBRelativePhaseA2).getId()).isEqualTo(72);
    }

    @Test
    public void phaseCRelativePhaseATest() {
        ObisCode phaseCRelativePhaseA1 = ObisCode.fromString("1.0.81.7.2.255");
        assertThat(PhaseMapping.getPhaseFor(phaseCRelativePhaseA1)).isEqualTo(Phase.PHASECA);
        assertThat(PhaseMapping.getPhaseFor(phaseCRelativePhaseA1).getId()).isEqualTo(40);
        ObisCode phaseCRelativePhaseA2 = ObisCode.fromString("1.0.81.7.6.255");
        assertThat(PhaseMapping.getPhaseFor(phaseCRelativePhaseA2)).isEqualTo(Phase.PHASECA);
        assertThat(PhaseMapping.getPhaseFor(phaseCRelativePhaseA2).getId()).isEqualTo(40);
    }
}
