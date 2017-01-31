/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl;

import com.elster.jupiter.cbo.MeasurementKind;
import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class MeasurementKindMappingTest {

    private static final Unit unitLess = Unit.get(BaseUnit.UNITLESS);

    @Test
    public void nullSafeObisCodeTest() {
        ObisCode nullSafe = null;
        Unit safeUnit = Unit.get(BaseUnit.UNITLESS);
        MeasurementKind measurementKind = MeasurementKindMapping.getMeasurementKindFor(nullSafe, safeUnit);

        assertThat(measurementKind).isEqualTo(MeasurementKind.NOTAPPLICABLE);
    }

    @Test
    public void nullSafeUnitTest() {
        ObisCode safeObisCode = ObisCode.fromString("1.0.1.8.0.255");
        Unit nulLSafeUnit = null;
        MeasurementKind measurementKind = MeasurementKindMapping.getMeasurementKindFor(safeObisCode, nulLSafeUnit);

        assertThat(measurementKind).isEqualTo(MeasurementKind.NOTAPPLICABLE);
    }

    @Test
    public void volumeTestTest() {
        ObisCode volume = ObisCode.fromString("7.0.1.8.0.255");
        MeasurementKind measurementKind = MeasurementKindMapping.getMeasurementKindFor(volume, unitLess);

        assertThat(measurementKind).isEqualTo(MeasurementKind.VOLUME);
    }

    @Test
    public void currentTest() {
        ObisCode current1 = ObisCode.fromString("1.0.11.7.0.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(current1, unitLess);
        assertThat(measurementKind1).isEqualTo(MeasurementKind.CURRENT);
        ObisCode current2 = ObisCode.fromString("1.0.31.7.0.255");
        MeasurementKind measurementKind2 = MeasurementKindMapping.getMeasurementKindFor(current2, unitLess);
        assertThat(measurementKind2).isEqualTo(MeasurementKind.CURRENT);
        ObisCode current3 = ObisCode.fromString("1.0.51.7.0.255");
        MeasurementKind measurementKind3 = MeasurementKindMapping.getMeasurementKindFor(current3, unitLess);
        assertThat(measurementKind3).isEqualTo(MeasurementKind.CURRENT);
        ObisCode current4 = ObisCode.fromString("1.0.71.7.0.255");
        MeasurementKind measurementKind4 = MeasurementKindMapping.getMeasurementKindFor(current4, unitLess);
        assertThat(measurementKind4).isEqualTo(MeasurementKind.CURRENT);
        ObisCode current5 = ObisCode.fromString("1.0.90.7.0.255");
        MeasurementKind measurementKind5 = MeasurementKindMapping.getMeasurementKindFor(current5, unitLess);
        assertThat(measurementKind5).isEqualTo(MeasurementKind.CURRENT);
        ObisCode current6 = ObisCode.fromString("1.0.91.7.0.255");
        MeasurementKind measurementKind6 = MeasurementKindMapping.getMeasurementKindFor(current6, unitLess);
        assertThat(measurementKind6).isEqualTo(MeasurementKind.CURRENT);
    }

    @Test
    public void certainlyNotACurrentTest() {
        ObisCode current1 = ObisCode.fromString("1.0.2.7.0.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(current1, unitLess);
        assertThat(measurementKind1).isNotEqualTo(MeasurementKind.CURRENT);
    }

    @Test
    public void currentAngleTest() {
        ObisCode currentAngle1 = ObisCode.fromString("1.0.81.7.4.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(currentAngle1, unitLess);
        assertThat(measurementKind1).isEqualTo(MeasurementKind.CURRENTANGLE);
        ObisCode currentAngle2 = ObisCode.fromString("1.0.81.7.7.255");
        MeasurementKind measurementKind2 = MeasurementKindMapping.getMeasurementKindFor(currentAngle2, unitLess);
        assertThat(measurementKind2).isEqualTo(MeasurementKind.CURRENTANGLE);
        ObisCode currentAngle3 = ObisCode.fromString("1.0.81.7.15.255");
        MeasurementKind measurementKind3 = MeasurementKindMapping.getMeasurementKindFor(currentAngle3, unitLess);
        assertThat(measurementKind3).isEqualTo(MeasurementKind.CURRENTANGLE);
        ObisCode currentAngle4 = ObisCode.fromString("1.0.81.7.26.255");
        MeasurementKind measurementKind4 = MeasurementKindMapping.getMeasurementKindFor(currentAngle4, unitLess);
        assertThat(measurementKind4).isEqualTo(MeasurementKind.CURRENTANGLE);
        ObisCode currentAngle5 = ObisCode.fromString("1.0.81.7.45.255");
        MeasurementKind measurementKind5 = MeasurementKindMapping.getMeasurementKindFor(currentAngle5, unitLess);
        assertThat(measurementKind5).isEqualTo(MeasurementKind.CURRENTANGLE);
        ObisCode currentAngle6 = ObisCode.fromString("1.0.81.7.74.255");
        MeasurementKind measurementKind6 = MeasurementKindMapping.getMeasurementKindFor(currentAngle6, unitLess);
        assertThat(measurementKind6).isEqualTo(MeasurementKind.CURRENTANGLE);
    }

    @Test
    public void certainlyNotACurrentAngleTest() {
        ObisCode currentAngle1 = ObisCode.fromString("1.0.82.7.4.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(currentAngle1, unitLess);
        assertThat(measurementKind1).isNotEqualTo(MeasurementKind.CURRENTANGLE);
    }

    @Test
    public void demandTest() {
        ObisCode demand1 = ObisCode.fromString("1.0.1.4.0.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(demand1, Unit.get(BaseUnit.WATTHOUR));
        assertThat(measurementKind1).isEqualTo(MeasurementKind.DEMAND);
        ObisCode demand2 = ObisCode.fromString("1.0.2.5.0.255");
        MeasurementKind measurementKind2 = MeasurementKindMapping.getMeasurementKindFor(demand2, unitLess);
        assertThat(measurementKind2).isEqualTo(MeasurementKind.DEMAND);
        ObisCode demand3 = ObisCode.fromString("1.0.3.14.0.255");
        MeasurementKind measurementKind3 = MeasurementKindMapping.getMeasurementKindFor(demand3, unitLess);
        assertThat(measurementKind3).isEqualTo(MeasurementKind.DEMAND);
        ObisCode demand4 = ObisCode.fromString("1.0.4.15.0.255");
        MeasurementKind measurementKind4 = MeasurementKindMapping.getMeasurementKindFor(demand4, unitLess);
        assertThat(measurementKind4).isEqualTo(MeasurementKind.DEMAND);
        ObisCode demand5 = ObisCode.fromString("1.0.5.24.0.255");
        MeasurementKind measurementKind5 = MeasurementKindMapping.getMeasurementKindFor(demand5, unitLess);
        assertThat(measurementKind5).isEqualTo(MeasurementKind.DEMAND);
        ObisCode demand6 = ObisCode.fromString("1.0.6.25.0.255");
        MeasurementKind measurementKind6 = MeasurementKindMapping.getMeasurementKindFor(demand6, unitLess);
        assertThat(measurementKind6).isEqualTo(MeasurementKind.DEMAND);
        ObisCode demand7 = ObisCode.fromString("1.0.7.27.0.255");
        MeasurementKind measurementKind7 = MeasurementKindMapping.getMeasurementKindFor(demand7, unitLess);
        assertThat(measurementKind7).isEqualTo(MeasurementKind.DEMAND);
        ObisCode demand8 = ObisCode.fromString("1.0.8.28.0.255");
        MeasurementKind measurementKind8 = MeasurementKindMapping.getMeasurementKindFor(demand8, unitLess);
        assertThat(measurementKind8).isEqualTo(MeasurementKind.DEMAND);
    }

    @Test
    public void certainlyNotADemandTest() {
        ObisCode notDemand1 = ObisCode.fromString("1.0.0.4.0.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(notDemand1, unitLess);
        assertThat(measurementKind1).isNotEqualTo(MeasurementKind.DEMAND);
        ObisCode notDemand2 = ObisCode.fromString("1.0.1.1.0.255");
        MeasurementKind measurementKind2 = MeasurementKindMapping.getMeasurementKindFor(notDemand2, unitLess);
        assertThat(measurementKind2).isNotEqualTo(MeasurementKind.DEMAND);
    }

    @Test
    public void energyTest() {
        Unit wattHour = Unit.get(BaseUnit.WATTHOUR);
        ObisCode energy1 = ObisCode.fromString("1.0.1.8.0.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(energy1, wattHour);
        assertThat(measurementKind1).isEqualTo(MeasurementKind.ENERGY);
        ObisCode energy2 = ObisCode.fromString("1.0.2.8.0.255");
        MeasurementKind measurementKind2 = MeasurementKindMapping.getMeasurementKindFor(energy2, wattHour);
        assertThat(measurementKind2).isEqualTo(MeasurementKind.ENERGY);
        ObisCode energy3 = ObisCode.fromString("1.0.15.8.0.255");
        MeasurementKind measurementKind3 = MeasurementKindMapping.getMeasurementKindFor(energy3, wattHour);
        assertThat(measurementKind3).isEqualTo(MeasurementKind.ENERGY);
        ObisCode energy4 = ObisCode.fromString("1.0.27.8.0.255");
        MeasurementKind measurementKind4 = MeasurementKindMapping.getMeasurementKindFor(energy4, wattHour);
        assertThat(measurementKind4).isEqualTo(MeasurementKind.ENERGY);
        ObisCode energy5 = ObisCode.fromString("1.0.36.8.0.255");
        MeasurementKind measurementKind5 = MeasurementKindMapping.getMeasurementKindFor(energy5, wattHour);
        assertThat(measurementKind5).isEqualTo(MeasurementKind.ENERGY);
        ObisCode energy6 = ObisCode.fromString("1.0.49.8.0.255");
        MeasurementKind measurementKind6 = MeasurementKindMapping.getMeasurementKindFor(energy6, wattHour);
        assertThat(measurementKind6).isEqualTo(MeasurementKind.ENERGY);
        ObisCode energy7 = ObisCode.fromString("1.0.57.8.0.255");
        MeasurementKind measurementKind7 = MeasurementKindMapping.getMeasurementKindFor(energy7, wattHour);
        assertThat(measurementKind7).isEqualTo(MeasurementKind.ENERGY);
        ObisCode energy8 = ObisCode.fromString("1.0.69.8.0.255");
        MeasurementKind measurementKind8 = MeasurementKindMapping.getMeasurementKindFor(energy8, wattHour);
        assertThat(measurementKind8).isEqualTo(MeasurementKind.ENERGY);
    }

    @Test
    public void notEnergyBecauseWrongUnitTest() {
        Unit watt = Unit.get(BaseUnit.WATT);
        ObisCode energy1 = ObisCode.fromString("1.0.1.8.0.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(energy1, watt);
        assertThat(measurementKind1).isNotEqualTo(MeasurementKind.ENERGY);
        ObisCode energy2 = ObisCode.fromString("1.0.2.8.0.255");
        MeasurementKind measurementKind2 = MeasurementKindMapping.getMeasurementKindFor(energy2, watt);
        assertThat(measurementKind2).isNotEqualTo(MeasurementKind.ENERGY);
    }

    @Test
    public void certainlyNotEnergyTest() {
        Unit wattHour = Unit.get(BaseUnit.WATTHOUR);
        ObisCode notEnergy1 = ObisCode.fromString("1.0.0.8.0.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(notEnergy1, wattHour);
        assertThat(measurementKind1).isNotEqualTo(MeasurementKind.ENERGY);
        ObisCode notEnergy2 = ObisCode.fromString("1.0.11.8.0.255");
        MeasurementKind measurementKind2 = MeasurementKindMapping.getMeasurementKindFor(notEnergy2, wattHour);
        assertThat(measurementKind2).isNotEqualTo(MeasurementKind.ENERGY);
        ObisCode notEnergy3 = ObisCode.fromString("1.0.12.8.0.255");
        MeasurementKind measurementKind3 = MeasurementKindMapping.getMeasurementKindFor(notEnergy3, wattHour);
        assertThat(measurementKind3).isNotEqualTo(MeasurementKind.ENERGY);
        ObisCode notEnergy4 = ObisCode.fromString("1.0.33.8.0.255");
        MeasurementKind measurementKind4 = MeasurementKindMapping.getMeasurementKindFor(notEnergy4, wattHour);
        assertThat(measurementKind4).isNotEqualTo(MeasurementKind.ENERGY);
        ObisCode notEnergy5 = ObisCode.fromString("1.0.51.8.0.255");
        MeasurementKind measurementKind5 = MeasurementKindMapping.getMeasurementKindFor(notEnergy5, wattHour);
        assertThat(measurementKind5).isNotEqualTo(MeasurementKind.ENERGY);
        ObisCode notEnergy6 = ObisCode.fromString("1.0.72.8.0.255");
        MeasurementKind measurementKind6 = MeasurementKindMapping.getMeasurementKindFor(notEnergy6, wattHour);
        assertThat(measurementKind6).isNotEqualTo(MeasurementKind.ENERGY);
        ObisCode notEnergy7 = ObisCode.fromString("1.0.88.8.0.255");
        MeasurementKind measurementKind7 = MeasurementKindMapping.getMeasurementKindFor(notEnergy7, wattHour);
        assertThat(measurementKind7).isNotEqualTo(MeasurementKind.ENERGY);
        ObisCode notEnergy8 = ObisCode.fromString("1.0.99.8.0.255");
        MeasurementKind measurementKind8 = MeasurementKindMapping.getMeasurementKindFor(notEnergy8, wattHour);
        assertThat(measurementKind8).isNotEqualTo(MeasurementKind.ENERGY);
    }

    @Test
    public void frequencyTest() {
        ObisCode frequency1 = ObisCode.fromString("1.0.14.8.0.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(frequency1, unitLess);
        assertThat(measurementKind1).isEqualTo(MeasurementKind.FREQUENCY);
        ObisCode frequency2 = ObisCode.fromString("1.0.34.8.0.255");
        MeasurementKind measurementKind2 = MeasurementKindMapping.getMeasurementKindFor(frequency2, unitLess);
        assertThat(measurementKind2).isEqualTo(MeasurementKind.FREQUENCY);
        ObisCode frequency3 = ObisCode.fromString("1.0.54.9.0.255");
        MeasurementKind measurementKind3 = MeasurementKindMapping.getMeasurementKindFor(frequency3, unitLess);
        assertThat(measurementKind3).isEqualTo(MeasurementKind.FREQUENCY);
        ObisCode frequency4 = ObisCode.fromString("1.0.74.7.0.255");
        MeasurementKind measurementKind4 = MeasurementKindMapping.getMeasurementKindFor(frequency4, unitLess);
        assertThat(measurementKind4).isEqualTo(MeasurementKind.FREQUENCY);
    }

    @Test
    public void certainlyNotAFrequencyTest() {
        ObisCode frequency1 = ObisCode.fromString("1.0.15.8.0.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(frequency1, unitLess);
        assertThat(measurementKind1).isNotEqualTo(MeasurementKind.FREQUENCY);
    }

    @Test
    public void lineLossesTest() {
        ObisCode lineLoss1 = ObisCode.fromString("1.0.83.8.2.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(lineLoss1, unitLess);
        assertThat(measurementKind1).isEqualTo(MeasurementKind.LINELOSSES);
        ObisCode lineLoss2 = ObisCode.fromString("1.0.83.7.12.255");
        MeasurementKind measurementKind2 = MeasurementKindMapping.getMeasurementKindFor(lineLoss2, unitLess);
        assertThat(measurementKind2).isEqualTo(MeasurementKind.LINELOSSES);
        ObisCode lineLoss3 = ObisCode.fromString("1.0.83.8.20.255");
        MeasurementKind measurementKind3 = MeasurementKindMapping.getMeasurementKindFor(lineLoss3, unitLess);
        assertThat(measurementKind3).isEqualTo(MeasurementKind.LINELOSSES);
        ObisCode lineLoss4 = ObisCode.fromString("1.0.83.62.62.255");
        MeasurementKind measurementKind4 = MeasurementKindMapping.getMeasurementKindFor(lineLoss4, unitLess);
        assertThat(measurementKind4).isEqualTo(MeasurementKind.LINELOSSES);
    }

    @Test
    public void certainlyNotALineLossTest() {
        ObisCode notALineLoss1 = ObisCode.fromString("1.0.83.8.8.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(notALineLoss1, unitLess);
        assertThat(measurementKind1).isNotEqualTo(MeasurementKind.LINELOSSES);
        ObisCode notALineLoss2 = ObisCode.fromString("1.0.83.7.49.255");
        MeasurementKind measurementKind2 = MeasurementKindMapping.getMeasurementKindFor(notALineLoss2, unitLess);
        assertThat(measurementKind2).isNotEqualTo(MeasurementKind.LINELOSSES);
        ObisCode notALineLoss3 = ObisCode.fromString("1.0.83.8.90.255");
        MeasurementKind measurementKind3 = MeasurementKindMapping.getMeasurementKindFor(notALineLoss3, unitLess);
        assertThat(measurementKind3).isNotEqualTo(MeasurementKind.LINELOSSES);
        ObisCode notALineLoss4 = ObisCode.fromString("1.0.1.8.0.255");
        MeasurementKind measurementKind4 = MeasurementKindMapping.getMeasurementKindFor(notALineLoss4, unitLess);
        assertThat(measurementKind4).isNotEqualTo(MeasurementKind.LINELOSSES);
    }

    @Test
    public void lossesTest() {
        ObisCode losses1 = ObisCode.fromString("1.0.83.8.8.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(losses1, unitLess);
        assertThat(measurementKind1).isEqualTo(MeasurementKind.LOSSES);
        ObisCode losses2 = ObisCode.fromString("1.0.83.8.38.255");
        MeasurementKind measurementKind2 = MeasurementKindMapping.getMeasurementKindFor(losses2, unitLess);
        assertThat(measurementKind2).isEqualTo(MeasurementKind.LOSSES);
        ObisCode losses3 = ObisCode.fromString("1.0.83.8.50.255");
        MeasurementKind measurementKind3 = MeasurementKindMapping.getMeasurementKindFor(losses3, unitLess);
        assertThat(measurementKind3).isEqualTo(MeasurementKind.LOSSES);
        ObisCode losses4 = ObisCode.fromString("1.0.83.7.89.255");
        MeasurementKind measurementKind4 = MeasurementKindMapping.getMeasurementKindFor(losses4, unitLess);
        assertThat(measurementKind4).isEqualTo(MeasurementKind.LOSSES);
    }

    @Test
    public void certainlyNotALossTest() {
        ObisCode notALoss1 = ObisCode.fromString("1.0.83.8.1.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(notALoss1, unitLess);
        assertThat(measurementKind1).isNotEqualTo(MeasurementKind.LOSSES);
        ObisCode notALoss2 = ObisCode.fromString("1.0.83.8.20.255");
        MeasurementKind measurementKind2 = MeasurementKindMapping.getMeasurementKindFor(notALoss2, unitLess);
        assertThat(measurementKind2).isNotEqualTo(MeasurementKind.LOSSES);
        ObisCode notALoss3 = ObisCode.fromString("1.0.83.8.40.255");
        MeasurementKind measurementKind3 = MeasurementKindMapping.getMeasurementKindFor(notALoss3, unitLess);
        assertThat(measurementKind3).isNotEqualTo(MeasurementKind.LOSSES);
        ObisCode notALoss4 = ObisCode.fromString("1.0.1.8.0.255");
        MeasurementKind measurementKind4 = MeasurementKindMapping.getMeasurementKindFor(notALoss4, unitLess);
        assertThat(measurementKind4).isNotEqualTo(MeasurementKind.LOSSES);
    }

    @Test
    public void powerTest() {
        Unit watt = Unit.get(BaseUnit.WATT);
        ObisCode power1 = ObisCode.fromString("1.0.1.8.0.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(power1, watt);
        assertThat(measurementKind1).isEqualTo(MeasurementKind.POWER);
        ObisCode power2 = ObisCode.fromString("1.0.2.8.0.255");
        MeasurementKind measurementKind2 = MeasurementKindMapping.getMeasurementKindFor(power2, watt);
        assertThat(measurementKind2).isEqualTo(MeasurementKind.POWER);
        ObisCode power3 = ObisCode.fromString("1.0.15.8.0.255");
        MeasurementKind measurementKind3 = MeasurementKindMapping.getMeasurementKindFor(power3, watt);
        assertThat(measurementKind3).isEqualTo(MeasurementKind.POWER);
        ObisCode power4 = ObisCode.fromString("1.0.27.8.0.255");
        MeasurementKind measurementKind4 = MeasurementKindMapping.getMeasurementKindFor(power4, watt);
        assertThat(measurementKind4).isEqualTo(MeasurementKind.POWER);
        ObisCode power5 = ObisCode.fromString("1.0.36.8.0.255");
        MeasurementKind measurementKind5 = MeasurementKindMapping.getMeasurementKindFor(power5, watt);
        assertThat(measurementKind5).isEqualTo(MeasurementKind.POWER);
        ObisCode power6 = ObisCode.fromString("1.0.49.8.0.255");
        MeasurementKind measurementKind6 = MeasurementKindMapping.getMeasurementKindFor(power6, watt);
        assertThat(measurementKind6).isEqualTo(MeasurementKind.POWER);
        ObisCode power7 = ObisCode.fromString("1.0.57.8.0.255");
        MeasurementKind measurementKind7 = MeasurementKindMapping.getMeasurementKindFor(power7, watt);
        assertThat(measurementKind7).isEqualTo(MeasurementKind.POWER);
        ObisCode power8 = ObisCode.fromString("1.0.69.8.0.255");
        MeasurementKind measurementKind8 = MeasurementKindMapping.getMeasurementKindFor(power8, watt);
        assertThat(measurementKind8).isEqualTo(MeasurementKind.POWER);
    }

    @Test
    public void notPowerBecauseNoFlowUnitTest() {
        Unit wattHour = Unit.get(BaseUnit.WATTHOUR);
        ObisCode notPower1 = ObisCode.fromString("1.0.1.8.0.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(notPower1, wattHour);
        assertThat(measurementKind1).isNotEqualTo(MeasurementKind.POWER);
        ObisCode notPower2 = ObisCode.fromString("1.0.2.8.0.255");
        MeasurementKind measurementKind2 = MeasurementKindMapping.getMeasurementKindFor(notPower2, wattHour);
        assertThat(measurementKind2).isNotEqualTo(MeasurementKind.POWER);
        ObisCode notPower3 = ObisCode.fromString("1.0.15.8.0.255");
        MeasurementKind measurementKind3 = MeasurementKindMapping.getMeasurementKindFor(notPower3, wattHour);
        assertThat(measurementKind3).isNotEqualTo(MeasurementKind.POWER);
        ObisCode notPower4 = ObisCode.fromString("1.0.27.8.0.255");
        MeasurementKind measurementKind4 = MeasurementKindMapping.getMeasurementKindFor(notPower4, wattHour);
        assertThat(measurementKind4).isNotEqualTo(MeasurementKind.POWER);
        ObisCode notPower5 = ObisCode.fromString("1.0.36.8.0.255");
        MeasurementKind measurementKind5 = MeasurementKindMapping.getMeasurementKindFor(notPower5, wattHour);
        assertThat(measurementKind5).isNotEqualTo(MeasurementKind.POWER);
        ObisCode notPower6 = ObisCode.fromString("1.0.49.8.0.255");
        MeasurementKind measurementKind6 = MeasurementKindMapping.getMeasurementKindFor(notPower6, wattHour);
        assertThat(measurementKind6).isNotEqualTo(MeasurementKind.POWER);
        ObisCode notPower7 = ObisCode.fromString("1.0.57.8.0.255");
        MeasurementKind measurementKind7 = MeasurementKindMapping.getMeasurementKindFor(notPower7, wattHour);
        assertThat(measurementKind7).isNotEqualTo(MeasurementKind.POWER);
        ObisCode notPower8 = ObisCode.fromString("1.0.69.8.0.255");
        MeasurementKind measurementKind8 = MeasurementKindMapping.getMeasurementKindFor(notPower8, wattHour);
        assertThat(measurementKind8).isNotEqualTo(MeasurementKind.POWER);
    }

    @Test
    public void certainlyNotPowerTest() {
        Unit watt = Unit.get(BaseUnit.WATT);
        ObisCode power1 = ObisCode.fromString("1.0.11.8.0.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(power1, watt);
        assertThat(measurementKind1).isNotEqualTo(MeasurementKind.POWER);
        ObisCode power2 = ObisCode.fromString("1.0.32.8.0.255");
        MeasurementKind measurementKind2 = MeasurementKindMapping.getMeasurementKindFor(power2, watt);
        assertThat(measurementKind2).isNotEqualTo(MeasurementKind.POWER);
        ObisCode power3 = ObisCode.fromString("1.0.53.8.0.255");
        MeasurementKind measurementKind3 = MeasurementKindMapping.getMeasurementKindFor(power3, watt);
        assertThat(measurementKind3).isNotEqualTo(MeasurementKind.POWER);
    }

    @Test
    public void powerFactorTest() {
        ObisCode powerFactor1 = ObisCode.fromString("1.0.13.8.0.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(powerFactor1, unitLess);
        assertThat(measurementKind1).isEqualTo(MeasurementKind.POWERFACTOR);
        ObisCode powerFactor2 = ObisCode.fromString("1.0.33.7.0.255");
        MeasurementKind measurementKind2 = MeasurementKindMapping.getMeasurementKindFor(powerFactor2, unitLess);
        assertThat(measurementKind2).isEqualTo(MeasurementKind.POWERFACTOR);
        ObisCode powerFactor3 = ObisCode.fromString("1.0.53.7.0.255");
        MeasurementKind measurementKind3 = MeasurementKindMapping.getMeasurementKindFor(powerFactor3, unitLess);
        assertThat(measurementKind3).isEqualTo(MeasurementKind.POWERFACTOR);
        ObisCode powerFactor4 = ObisCode.fromString("1.0.73.7.0.255");
        MeasurementKind measurementKind4 = MeasurementKindMapping.getMeasurementKindFor(powerFactor4, unitLess);
        assertThat(measurementKind4).isEqualTo(MeasurementKind.POWERFACTOR);
    }

    @Test
    public void certainlyNotAPowerFactorTest() {
        ObisCode notAPowerFactor1 = ObisCode.fromString("1.0.14.8.0.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(notAPowerFactor1, unitLess);
        assertThat(measurementKind1).isNotEqualTo(MeasurementKind.POWERFACTOR);
        ObisCode notAPowerFactor2 = ObisCode.fromString("1.0.1.8.0.255");
        MeasurementKind measurementKind2 = MeasurementKindMapping.getMeasurementKindFor(notAPowerFactor2, unitLess);
        assertThat(measurementKind2).isNotEqualTo(MeasurementKind.POWERFACTOR);
        ObisCode notAPowerFactor3 = ObisCode.fromString("1.0.2.7.0.255");
        MeasurementKind measurementKind3 = MeasurementKindMapping.getMeasurementKindFor(notAPowerFactor3, Unit.get(BaseUnit.LITER));
        assertThat(measurementKind3).isNotEqualTo(MeasurementKind.POWERFACTOR);
        ObisCode notAPowerFactor4 = ObisCode.fromString("1.0.99.7.0.255");
        MeasurementKind measurementKind4 = MeasurementKindMapping.getMeasurementKindFor(notAPowerFactor4, unitLess);
        assertThat(measurementKind4).isNotEqualTo(MeasurementKind.POWERFACTOR);
    }

    @Test
    public void voltageSagTest() {
        Unit voltage = Unit.get(BaseUnit.VOLT);
        ObisCode sag1 = ObisCode.fromString("1.0.32.31.0.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(sag1, voltage);
        assertThat(measurementKind1).isEqualTo(MeasurementKind.SAG);
        ObisCode sag2 = ObisCode.fromString("1.0.52.32.0.255");
        MeasurementKind measurementKind2 = MeasurementKindMapping.getMeasurementKindFor(sag2, voltage);
        assertThat(measurementKind2).isEqualTo(MeasurementKind.SAG);
        ObisCode sag3 = ObisCode.fromString("1.0.72.33.0.255");
        MeasurementKind measurementKind3 = MeasurementKindMapping.getMeasurementKindFor(sag3, voltage);
        assertThat(measurementKind3).isEqualTo(MeasurementKind.SAG);
        ObisCode sag4 = ObisCode.fromString("1.0.32.34.0.255");
        MeasurementKind measurementKind4 = MeasurementKindMapping.getMeasurementKindFor(sag4, voltage);
        assertThat(measurementKind4).isEqualTo(MeasurementKind.SAG);
        ObisCode sag5 = ObisCode.fromString("1.0.32.43.0.255");
        MeasurementKind measurementKind5 = MeasurementKindMapping.getMeasurementKindFor(sag5, voltage);
        assertThat(measurementKind5).isEqualTo(MeasurementKind.SAG);
    }

    @Test
    public void certainlyNotAVoltageSagTest() {
        Unit voltage = Unit.get(BaseUnit.VOLT);
        ObisCode sag1 = ObisCode.fromString("1.0.32.30.0.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(sag1, voltage);
        assertThat(measurementKind1).isNotEqualTo(MeasurementKind.SAG);
        ObisCode sag2 = ObisCode.fromString("1.0.52.35.0.255");
        MeasurementKind measurementKind2 = MeasurementKindMapping.getMeasurementKindFor(sag2, voltage);
        assertThat(measurementKind2).isNotEqualTo(MeasurementKind.SAG);
        ObisCode sag3 = ObisCode.fromString("1.0.94.31.0.255");
        MeasurementKind measurementKind3 = MeasurementKindMapping.getMeasurementKindFor(sag3, voltage);
        assertThat(measurementKind3).isNotEqualTo(MeasurementKind.SAG);
        ObisCode sag4 = ObisCode.fromString("1.0.32.44.0.255");
        MeasurementKind measurementKind4 = MeasurementKindMapping.getMeasurementKindFor(sag4, voltage);
        assertThat(measurementKind4).isNotEqualTo(MeasurementKind.SAG);
    }

    @Test
    public void voltageSwellTest() {
        Unit voltage = Unit.get(BaseUnit.VOLT);
        ObisCode swell1 = ObisCode.fromString("1.0.32.35.0.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(swell1, voltage);
        assertThat(measurementKind1).isEqualTo(MeasurementKind.SWELL);
        ObisCode swell2 = ObisCode.fromString("1.0.52.36.0.255");
        MeasurementKind measurementKind2 = MeasurementKindMapping.getMeasurementKindFor(swell2, voltage);
        assertThat(measurementKind2).isEqualTo(MeasurementKind.SWELL);
        ObisCode swell3 = ObisCode.fromString("1.0.72.37.0.255");
        MeasurementKind measurementKind3 = MeasurementKindMapping.getMeasurementKindFor(swell3, voltage);
        assertThat(measurementKind3).isEqualTo(MeasurementKind.SWELL);
        ObisCode swell4 = ObisCode.fromString("1.0.32.38.0.255");
        MeasurementKind measurementKind4 = MeasurementKindMapping.getMeasurementKindFor(swell4, voltage);
        assertThat(measurementKind4).isEqualTo(MeasurementKind.SWELL);
        ObisCode swell5 = ObisCode.fromString("1.0.32.44.0.255");
        MeasurementKind measurementKind5 = MeasurementKindMapping.getMeasurementKindFor(swell5, voltage);
        assertThat(measurementKind5).isEqualTo(MeasurementKind.SWELL);
    }

    @Test
    public void certainlyNotAVoltageSwellTest() {
        Unit voltage = Unit.get(BaseUnit.VOLT);
        ObisCode swell1 = ObisCode.fromString("1.0.99.35.0.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(swell1, voltage);
        assertThat(measurementKind1).isNotEqualTo(MeasurementKind.SWELL);
        ObisCode swell2 = ObisCode.fromString("1.0.52.31.0.255");
        MeasurementKind measurementKind2 = MeasurementKindMapping.getMeasurementKindFor(swell2, voltage);
        assertThat(measurementKind2).isNotEqualTo(MeasurementKind.SWELL);
        ObisCode swell3 = ObisCode.fromString("1.0.72.8.0.255");
        MeasurementKind measurementKind3 = MeasurementKindMapping.getMeasurementKindFor(swell3, voltage);
        assertThat(measurementKind3).isNotEqualTo(MeasurementKind.SWELL);
        ObisCode swell4 = ObisCode.fromString("1.0.1.8.0.255");
        MeasurementKind measurementKind4 = MeasurementKindMapping.getMeasurementKindFor(swell4, voltage);
        assertThat(measurementKind4).isNotEqualTo(MeasurementKind.SWELL);
    }

    @Test
    public void totalHarmonicDistortionTest() {
        ObisCode thd1 = ObisCode.fromString("1.0.12.7.124.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(thd1, unitLess);
        assertThat(measurementKind1).isEqualTo(MeasurementKind.TOTALHARMONICDISTORTION);
        ObisCode thd2 = ObisCode.fromString("1.0.52.24.124.255");
        MeasurementKind measurementKind2 = MeasurementKindMapping.getMeasurementKindFor(thd2, unitLess);
        assertThat(measurementKind2).isEqualTo(MeasurementKind.TOTALHARMONICDISTORTION);
        ObisCode thd3 = ObisCode.fromString("1.0.71.7.124.255");
        MeasurementKind measurementKind3 = MeasurementKindMapping.getMeasurementKindFor(thd3, unitLess);
        assertThat(measurementKind3).isEqualTo(MeasurementKind.TOTALHARMONICDISTORTION);
        ObisCode thd4 = ObisCode.fromString("1.0.91.7.124.255");
        MeasurementKind measurementKind4 = MeasurementKindMapping.getMeasurementKindFor(thd4, unitLess);
        assertThat(measurementKind4).isEqualTo(MeasurementKind.TOTALHARMONICDISTORTION);
    }

    @Test
    public void certainlyNotTotalHarmonicDistortionTest() {
        ObisCode notThd1 = ObisCode.fromString("1.0.13.7.124.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(notThd1, unitLess);
        assertThat(measurementKind1).isNotEqualTo(MeasurementKind.TOTALHARMONICDISTORTION);
        ObisCode notThd2 = ObisCode.fromString("1.0.1.8.0.255");
        MeasurementKind measurementKind2 = MeasurementKindMapping.getMeasurementKindFor(notThd2, unitLess);
        assertThat(measurementKind2).isNotEqualTo(MeasurementKind.TOTALHARMONICDISTORTION);
        ObisCode notThd3 = ObisCode.fromString("1.0.71.1.124.255");
        MeasurementKind measurementKind3 = MeasurementKindMapping.getMeasurementKindFor(notThd3, unitLess);
        assertThat(measurementKind3).isNotEqualTo(MeasurementKind.TOTALHARMONICDISTORTION);
        ObisCode notThd4 = ObisCode.fromString("1.0.91.7.1.255");
        MeasurementKind measurementKind4 = MeasurementKindMapping.getMeasurementKindFor(notThd4, unitLess);
        assertThat(measurementKind4).isNotEqualTo(MeasurementKind.TOTALHARMONICDISTORTION);
    }

    @Test
    public void transformerLossesTest() {
        ObisCode transformerLoss1 = ObisCode.fromString("1.0.83.7.5.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(transformerLoss1, unitLess);
        assertThat(measurementKind1).isEqualTo(MeasurementKind.TRANSFORMERLOSSES);
        ObisCode transformerLoss2 = ObisCode.fromString("1.0.83.8.55.255");
        MeasurementKind measurementKind2 = MeasurementKindMapping.getMeasurementKindFor(transformerLoss2, unitLess);
        assertThat(measurementKind2).isEqualTo(MeasurementKind.TRANSFORMERLOSSES);
        ObisCode transformerLoss3 = ObisCode.fromString("1.0.83.1.63.255");
        MeasurementKind measurementKind3 = MeasurementKindMapping.getMeasurementKindFor(transformerLoss3, unitLess);
        assertThat(measurementKind3).isEqualTo(MeasurementKind.TRANSFORMERLOSSES);
        ObisCode transformerLoss4 = ObisCode.fromString("1.0.83.24.45.255");
        MeasurementKind measurementKind4 = MeasurementKindMapping.getMeasurementKindFor(transformerLoss4, unitLess);
        assertThat(measurementKind4).isEqualTo(MeasurementKind.TRANSFORMERLOSSES);
    }

    @Test
    public void certainlyNotTransformerLossesTest() {
        ObisCode notATransformerLoss1 = ObisCode.fromString("1.0.81.7.5.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(notATransformerLoss1, unitLess);
        assertThat(measurementKind1).isNotEqualTo(MeasurementKind.TRANSFORMERLOSSES);
        ObisCode notATransformerLoss2 = ObisCode.fromString("1.0.83.8.20.255");
        MeasurementKind measurementKind2 = MeasurementKindMapping.getMeasurementKindFor(notATransformerLoss2, unitLess);
        assertThat(measurementKind2).isNotEqualTo(MeasurementKind.TRANSFORMERLOSSES);
        ObisCode notATransformerLoss3 = ObisCode.fromString("7.0.83.1.40.255");
        MeasurementKind measurementKind3 = MeasurementKindMapping.getMeasurementKindFor(notATransformerLoss3, unitLess);
        assertThat(measurementKind3).isNotEqualTo(MeasurementKind.TRANSFORMERLOSSES);
        ObisCode notATransformerLoss4 = ObisCode.fromString("1.0.83.24.90.255");
        MeasurementKind measurementKind4 = MeasurementKindMapping.getMeasurementKindFor(notATransformerLoss4, unitLess);
        assertThat(measurementKind4).isNotEqualTo(MeasurementKind.TRANSFORMERLOSSES);
    }

    @Test
    public void voltageTest() {
        ObisCode voltage1 = ObisCode.fromString("1.0.12.7.0.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(voltage1, unitLess);
        assertThat(measurementKind1).isEqualTo(MeasurementKind.VOLTAGE);
        ObisCode voltage2 = ObisCode.fromString("1.0.32.7.0.255");
        MeasurementKind measurementKind2 = MeasurementKindMapping.getMeasurementKindFor(voltage2, unitLess);
        assertThat(measurementKind2).isEqualTo(MeasurementKind.VOLTAGE);
        ObisCode voltage3 = ObisCode.fromString("1.0.52.7.1.255");
        MeasurementKind measurementKind3 = MeasurementKindMapping.getMeasurementKindFor(voltage3, unitLess);
        assertThat(measurementKind3).isEqualTo(MeasurementKind.VOLTAGE);
        ObisCode voltage4 = ObisCode.fromString("1.0.72.7.12.255");
        MeasurementKind measurementKind4 = MeasurementKindMapping.getMeasurementKindFor(voltage4, unitLess);
        assertThat(measurementKind4).isEqualTo(MeasurementKind.VOLTAGE);
    }

    @Test
    public void certainlyNotVoltageTest() {
        ObisCode notVoltage1 = ObisCode.fromString("1.0.12.7.124.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(notVoltage1, unitLess);
        assertThat(measurementKind1).isNotEqualTo(MeasurementKind.VOLTAGE);
        ObisCode notVoltage2 = ObisCode.fromString("1.0.31.7.0.255");
        MeasurementKind measurementKind2 = MeasurementKindMapping.getMeasurementKindFor(notVoltage2, unitLess);
        assertThat(measurementKind2).isNotEqualTo(MeasurementKind.VOLTAGE);
        ObisCode notVoltage3 = ObisCode.fromString("1.0.1.8.1.255");
        MeasurementKind measurementKind3 = MeasurementKindMapping.getMeasurementKindFor(notVoltage3, unitLess);
        assertThat(measurementKind3).isNotEqualTo(MeasurementKind.VOLTAGE);
        ObisCode notVoltage4 = ObisCode.fromString("1.0.2.4.12.255");
        MeasurementKind measurementKind4 = MeasurementKindMapping.getMeasurementKindFor(notVoltage4, unitLess);
        assertThat(measurementKind4).isNotEqualTo(MeasurementKind.VOLTAGE);
    }

    @Test
    public void voltageAngleTest() {
        ObisCode voltageAngle1 = ObisCode.fromString("1.0.81.7.1.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(voltageAngle1, unitLess);
        assertThat(measurementKind1).isEqualTo(MeasurementKind.VOLTAGEANGLE);
        ObisCode voltageAngle2 = ObisCode.fromString("1.0.81.7.11.255");
        MeasurementKind measurementKind2 = MeasurementKindMapping.getMeasurementKindFor(voltageAngle2, unitLess);
        assertThat(measurementKind2).isEqualTo(MeasurementKind.VOLTAGEANGLE);
        ObisCode voltageAngle3 = ObisCode.fromString("1.0.81.7.20.255");
        MeasurementKind measurementKind3 = MeasurementKindMapping.getMeasurementKindFor(voltageAngle3, unitLess);
        assertThat(measurementKind3).isEqualTo(MeasurementKind.VOLTAGEANGLE);
        ObisCode voltageAngle4 = ObisCode.fromString("1.0.81.7.32.255");
        MeasurementKind measurementKind4 = MeasurementKindMapping.getMeasurementKindFor(voltageAngle4, unitLess);
        assertThat(measurementKind4).isEqualTo(MeasurementKind.VOLTAGEANGLE);
        ObisCode voltageAngle5 = ObisCode.fromString("1.0.81.7.50.255");
        MeasurementKind measurementKind5 = MeasurementKindMapping.getMeasurementKindFor(voltageAngle5, unitLess);
        assertThat(measurementKind5).isEqualTo(MeasurementKind.VOLTAGEANGLE);
        ObisCode voltageAngle6 = ObisCode.fromString("1.0.81.7.71.255");
        MeasurementKind measurementKind6 = MeasurementKindMapping.getMeasurementKindFor(voltageAngle6, unitLess);
        assertThat(measurementKind6).isEqualTo(MeasurementKind.VOLTAGEANGLE);
    }

    @Test
    public void certainlyNotAVoltageAngleTest() {
        ObisCode notVoltageAngle1 = ObisCode.fromString("1.0.81.7.4.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(notVoltageAngle1, unitLess);
        assertThat(measurementKind1).isNotEqualTo(MeasurementKind.VOLTAGEANGLE);
        ObisCode notVoltageAngle2 = ObisCode.fromString("1.0.80.7.11.255");
        MeasurementKind measurementKind2 = MeasurementKindMapping.getMeasurementKindFor(notVoltageAngle2, unitLess);
        assertThat(measurementKind2).isNotEqualTo(MeasurementKind.VOLTAGEANGLE);
        ObisCode notVoltageAngle3 = ObisCode.fromString("1.0.81.6.20.255");
        MeasurementKind measurementKind3 = MeasurementKindMapping.getMeasurementKindFor(notVoltageAngle3, unitLess);
        assertThat(measurementKind3).isNotEqualTo(MeasurementKind.VOLTAGEANGLE);
        ObisCode notVoltageAngle4 = ObisCode.fromString("1.0.81.7.29.255");
        MeasurementKind measurementKind4 = MeasurementKindMapping.getMeasurementKindFor(notVoltageAngle4, unitLess);
        assertThat(measurementKind4).isNotEqualTo(MeasurementKind.VOLTAGEANGLE);
        ObisCode notVoltageAngle5 = ObisCode.fromString("1.0.81.7.5.255");
        MeasurementKind measurementKind5 = MeasurementKindMapping.getMeasurementKindFor(notVoltageAngle5, unitLess);
        assertThat(measurementKind5).isNotEqualTo(MeasurementKind.VOLTAGEANGLE);
        ObisCode notVoltageAngle6 = ObisCode.fromString("1.0.1.8.1.255");
        MeasurementKind measurementKind6 = MeasurementKindMapping.getMeasurementKindFor(notVoltageAngle6, unitLess);
        assertThat(measurementKind6).isNotEqualTo(MeasurementKind.VOLTAGEANGLE);
    }

    @Test
    public void volumeTest() {
        ObisCode volume1 = ObisCode.fromString("7.0.3.0.0.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(volume1, unitLess);
        assertThat(measurementKind1).isEqualTo(MeasurementKind.VOLUME);
        ObisCode volume2 = ObisCode.fromString("7.0.13.0.0.255");
        MeasurementKind measurementKind2 = MeasurementKindMapping.getMeasurementKindFor(volume2, unitLess);
        assertThat(measurementKind2).isEqualTo(MeasurementKind.VOLUME);
    }

    @Test
    public void certainlyNotAVolumeTest() {
        ObisCode notAVolume1 = ObisCode.fromString("1.0.3.0.0.255");
        MeasurementKind measurementKind1 = MeasurementKindMapping.getMeasurementKindFor(notAVolume1, unitLess);
        assertThat(measurementKind1).isNotEqualTo(MeasurementKind.VOLUME);
        ObisCode notAVolume2 = ObisCode.fromString("9.0.13.0.0.255");
        MeasurementKind measurementKind2 = MeasurementKindMapping.getMeasurementKindFor(notAVolume2, unitLess);
        assertThat(measurementKind2).isNotEqualTo(MeasurementKind.VOLUME);
    }
}
