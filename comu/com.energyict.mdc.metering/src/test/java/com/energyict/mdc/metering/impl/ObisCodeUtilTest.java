/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl;

import com.energyict.mdc.common.ObisCode;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class ObisCodeUtilTest {

    @Test
    public void isElectricityObisCodeTest() {
        String obisCodeEnd = ".0.1.8.0.255";
        for (int i = 0; i <= 255; i++) {
            ObisCode obisCode = ObisCode.fromString(i + obisCodeEnd);
            if(i == 1){
                assertThat(ObisCodeUtil.isElectricity(obisCode)).isTrue();
            } else {
                assertThat(ObisCodeUtil.isElectricity(obisCode)).isFalse();
            }
        }
    }

    @Test
    public void isNotEnergyObisCodeBecauseItIsNotElectricityTest(){
        ObisCode nonElectricityObisCode = ObisCode.fromString("7.0.1.8.0.255");

        assertThat(ObisCodeUtil.isEnergy(nonElectricityObisCode)).isFalse();
    }

    @Test
    public void isNotEnergyObisCodeBecauseCFieldIsNotInProperRangeTest() {
        ObisCode nonElectricityObisCode = ObisCode.fromString("1.0.0.8.0.255");
        assertThat(ObisCodeUtil.isEnergy(nonElectricityObisCode)).isFalse();

        for (int i = 11; i <= 80; i+=20) {
            for (int j = 0; j < 4; j++) {
                assertThat(ObisCodeUtil.isEnergy(ObisCode.fromString("1.0." + (i+j) + ".8.0.255")))
                        .describedAs("For i = " + i + " and j = " + j)
                        .isFalse();
            }
        }
    }

    @Test
    public void isEnergyObisCodeTest() {
        ObisCode nonElectricityObisCode = ObisCode.fromString("1.0.1.8.0.255");
        assertThat(ObisCodeUtil.isEnergy(nonElectricityObisCode)).isTrue();
    }

    @Test
    public void isVoltageObisCode(){
        ObisCode voltageObisCode1 = ObisCode.fromString("1.0.12.8.0.255");
        assertThat(ObisCodeUtil.isVoltage(voltageObisCode1)).isTrue();
        ObisCode voltageObisCode2 = ObisCode.fromString("1.0.32.8.0.255");
        assertThat(ObisCodeUtil.isVoltage(voltageObisCode2)).isTrue();
        ObisCode voltageObisCode3 = ObisCode.fromString("1.0.52.8.0.255");
        assertThat(ObisCodeUtil.isVoltage(voltageObisCode3)).isTrue();
        ObisCode voltageObisCode4 = ObisCode.fromString("1.0.72.8.0.255");
        assertThat(ObisCodeUtil.isVoltage(voltageObisCode4)).isTrue();
        ObisCode voltageObisCode5 = ObisCode.fromString("1.0.89.8.0.255");
        assertThat(ObisCodeUtil.isVoltage(voltageObisCode5)).isTrue();
        ObisCode voltageObisCode6 = ObisCode.fromString("1.0.92.8.0.255");
        assertThat(ObisCodeUtil.isVoltage(voltageObisCode6)).isTrue();
    }

    @Test
    public void isNotVoltageBecauseItIsNotElectricityTest() {
        ObisCode voltageObisCode1 = ObisCode.fromString("0.0.12.8.0.255");
        assertThat(ObisCodeUtil.isVoltage(voltageObisCode1)).isFalse();
    }

    @Test
    public void isCurrentObisCode(){
        ObisCode currentObisCode1 = ObisCode.fromString("1.0.11.8.0.255");
        assertThat(ObisCodeUtil.isCurrent(currentObisCode1)).isTrue();
        ObisCode currentObisCode2 = ObisCode.fromString("1.0.31.8.0.255");
        assertThat(ObisCodeUtil.isCurrent(currentObisCode2)).isTrue();
        ObisCode currentObisCode3 = ObisCode.fromString("1.0.51.8.0.255");
        assertThat(ObisCodeUtil.isCurrent(currentObisCode3)).isTrue();
        ObisCode currentObisCode4 = ObisCode.fromString("1.0.71.8.0.255");
        assertThat(ObisCodeUtil.isCurrent(currentObisCode4)).isTrue();
        ObisCode currentObisCode5 = ObisCode.fromString("1.0.88.8.0.255");
        assertThat(ObisCodeUtil.isCurrent(currentObisCode5)).isTrue();
        ObisCode currentObisCode6 = ObisCode.fromString("1.0.90.8.0.255");
        assertThat(ObisCodeUtil.isCurrent(currentObisCode6)).isTrue();
        ObisCode currentObisCode7 = ObisCode.fromString("1.0.91.8.0.255");
        assertThat(ObisCodeUtil.isCurrent(currentObisCode7)).isTrue();
    }

    @Test
    public void isNotCurrentBecauseItIsNotElectricityTest() {
        ObisCode currentObisCode1 = ObisCode.fromString("0.0.11.8.0.255");
        assertThat(ObisCodeUtil.isCurrent(currentObisCode1)).isFalse();
    }
}