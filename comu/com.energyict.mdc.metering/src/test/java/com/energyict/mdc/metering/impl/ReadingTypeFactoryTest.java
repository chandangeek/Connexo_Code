/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class ReadingTypeFactoryTest {

    private  static final TimeDuration days = TimeDuration.days(1);

    @Test
    public void activeEnergyImportObisCodeTester(){
        ObisCode activeEnergyImportTotal = ObisCode.fromString("1.0.1.8.0.255");
        Unit kiloWattHour = Unit.get("kWh");
        String activeEnergyMrid = ObisCodeToReadingTypeFactory.createMRIDFromObisCodeAndUnit(activeEnergyImportTotal, kiloWattHour);

        assertThat(activeEnergyMrid).isEqualTo("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
    }

    @Test
    public void activeEnergyImportDailyIntervalTest() {
        ObisCode activeEnergyImportTotal = ObisCode.fromString("1.0.1.8.0.255");
        Unit kiloWattHour = Unit.get("kWh");
        String activeEnergyMrid = ObisCodeToReadingTypeFactory.createMRIDFromObisCodeUnitAndInterval(activeEnergyImportTotal, kiloWattHour, days);

        assertThat(activeEnergyMrid).isEqualTo("0.0.4.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
    }

    @Test
    public void reactiveEnergyImportObisCodeTest() {
        ObisCode reActiveEnergyImportTotal = ObisCode.fromString("1.0.3.8.0.255");
        Unit kiloWattHour = Unit.get("kvarh");
        String reActiveEnergyMrid = ObisCodeToReadingTypeFactory.createMRIDFromObisCodeAndUnit(reActiveEnergyImportTotal, kiloWattHour);

        assertThat(reActiveEnergyMrid).isEqualTo("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.73.0");
    }

    @Test
    public void reactiveEnergyImportDailyIntervalTest() {
        ObisCode reActiveEnergyImportTotal = ObisCode.fromString("1.0.3.8.0.255");
        Unit kiloWattHour = Unit.get("kvarh");
        String reActiveEnergyMrid = ObisCodeToReadingTypeFactory.createMRIDFromObisCodeUnitAndInterval(reActiveEnergyImportTotal, kiloWattHour, days);

        assertThat(reActiveEnergyMrid).isEqualTo("0.0.4.1.1.1.12.0.0.0.0.0.0.0.0.3.73.0");
    }

    @Test
    public void instantCurrentPhase1Test() {
        ObisCode instantCurrent = ObisCode.fromString("1.0.31.7.0.255");
        Unit ampere = Unit.get("A");
        String instantCurrentMrid = ObisCodeToReadingTypeFactory.createMRIDFromObisCodeAndUnit(instantCurrent, ampere);

        assertThat(instantCurrentMrid).isEqualTo("0.0.0.6.0.1.4.0.0.0.0.0.0.0.128.0.5.0");
    }

    @Test
    public void instantVoltagePhase3Test() {
        ObisCode instantVoltage = ObisCode.fromString("1.0.32.7.0.255");
        Unit voltage = Unit.get("V");
        String instantVoltageMrid = ObisCodeToReadingTypeFactory.createMRIDFromObisCodeAndUnit(instantVoltage, voltage);

        assertThat(instantVoltageMrid).isEqualTo("0.0.0.6.0.1.158.0.0.0.0.0.0.0.129.0.29.0");
    }

    @Test
    public void lastAverageDemandActiveEnergyImportRate2Test() {
        ObisCode lastAverageActiveImport = ObisCode.fromString("1.0.1.5.2.255");
        Unit kiloWatt = Unit.get("kW");
        String lastAvgActiveImpMrid = ObisCodeToReadingTypeFactory.createMRIDFromObisCodeAndUnit(lastAverageActiveImport, kiloWatt);

        assertThat(lastAvgActiveImpMrid).isEqualTo("0.2.0.4.1.1.8.0.0.0.0.2.0.0.0.3.38.0");
    }

    @Test
    public void reactivePowerExportCurrentAverageTest() {
        ObisCode reactivePowerExpCurrentAvg = ObisCode.fromString("1.0.4.4.0.255");
        Unit kvar = Unit.get("kvar");
        String reactivePowerExpCurrentAvgMrid = ObisCodeToReadingTypeFactory.createMRIDFromObisCodeAndUnit(reactivePowerExpCurrentAvg, kvar);

        assertThat(reactivePowerExpCurrentAvgMrid).isEqualTo("0.2.0.6.19.1.8.0.0.0.0.0.0.0.0.3.63.0");
    }
}
