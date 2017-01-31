/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.metering.ReadingTypeInformation;

import org.junit.Ignore;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class ReadingTypeToObisCodeFactoryTest {

    private String forwardActiveEnergyReadingType = ReadingTypeCodeBuilder
            .of(Commodity.ELECTRICITY_SECONDARY_METERED)
            .accumulate(Accumulation.BULKQUANTITY)
            .flow(FlowDirection.FORWARD)
            .measure(MeasurementKind.ENERGY)
            .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).code();
    private String forwardActiveEnergyTou1ReadingType = ReadingTypeCodeBuilder
            .of(Commodity.ELECTRICITY_SECONDARY_METERED)
            .accumulate(Accumulation.BULKQUANTITY)
            .flow(FlowDirection.FORWARD)
            .measure(MeasurementKind.ENERGY)
            .tou(1)
            .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).code();
    private String reverseActiveEnergyReadingType = ReadingTypeCodeBuilder
            .of(Commodity.ELECTRICITY_SECONDARY_METERED)
            .accumulate(Accumulation.BULKQUANTITY)
            .flow(FlowDirection.REVERSE)
            .measure(MeasurementKind.ENERGY)
            .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).code();
    private String forwardActiveEnergyReadingType15MinDelta = ReadingTypeCodeBuilder
            .of(Commodity.ELECTRICITY_SECONDARY_METERED)
            .accumulate(Accumulation.DELTADELTA)
            .period(TimeAttribute.MINUTE15)
            .flow(FlowDirection.FORWARD)
            .measure(MeasurementKind.ENERGY)
            .tou(0)
            .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).code();
    private String forwardActiveEnergyReadingTypeDailyDelta = ReadingTypeCodeBuilder
            .of(Commodity.ELECTRICITY_SECONDARY_METERED)
            .accumulate(Accumulation.DELTADELTA)
            .period(TimeAttribute.HOUR24)
            .flow(FlowDirection.FORWARD)
            .measure(MeasurementKind.ENERGY)
            .tou(0)
            .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).code();
    private String forwardActiveEnergyTOU1ReadingType15MinDelta = ReadingTypeCodeBuilder
            .of(Commodity.ELECTRICITY_SECONDARY_METERED)
            .accumulate(Accumulation.DELTADELTA)
            .period(TimeAttribute.MINUTE15)
            .flow(FlowDirection.FORWARD)
            .measure(MeasurementKind.ENERGY)
            .tou(1)
            .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).code();
    private String forwardActiveEnergyTOU1ReadingTypeDailyDelta = ReadingTypeCodeBuilder
            .of(Commodity.ELECTRICITY_SECONDARY_METERED)
            .accumulate(Accumulation.DELTADELTA)
            .period(TimeAttribute.HOUR24)
            .flow(FlowDirection.FORWARD)
            .measure(MeasurementKind.ENERGY)
            .tou(1)
            .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).code();
    private String reverseActiveEnergyReadingType15MinDelta = ReadingTypeCodeBuilder
            .of(Commodity.ELECTRICITY_SECONDARY_METERED)
            .accumulate(Accumulation.DELTADELTA)
            .period(TimeAttribute.MINUTE15)
            .flow(FlowDirection.REVERSE)
            .measure(MeasurementKind.ENERGY)
            .tou(0)
            .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).code();
    private String reverseActiveEnergyTou2ReadingType = ReadingTypeCodeBuilder
            .of(Commodity.ELECTRICITY_SECONDARY_METERED)
            .accumulate(Accumulation.BULKQUANTITY)
            .flow(FlowDirection.REVERSE)
            .measure(MeasurementKind.ENERGY)
            .tou(2)
            .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).code();
    private String reverseActiveEnergyTOU2ReadingType15MinDelta = ReadingTypeCodeBuilder
            .of(Commodity.ELECTRICITY_SECONDARY_METERED)
            .accumulate(Accumulation.DELTADELTA)
            .period(TimeAttribute.MINUTE15)
            .flow(FlowDirection.REVERSE)
            .measure(MeasurementKind.ENERGY)
            .tou(2)
            .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).code();
    private String currentTotalAmpere = ReadingTypeCodeBuilder
            .of(Commodity.ELECTRICITY_SECONDARY_METERED)
            .accumulate(Accumulation.INDICATING)
            .measure(MeasurementKind.CURRENT)
            .in(ReadingTypeUnit.AMPERE)
            .code();
    private String currentPhase1Ampere = ReadingTypeCodeBuilder
            .of(Commodity.ELECTRICITY_SECONDARY_METERED)
            .accumulate(Accumulation.INDICATING)
            .measure(MeasurementKind.CURRENT)
            .in(ReadingTypeUnit.AMPERE)
            .phase(Phase.PHASEA)
            .code();
    private String currentTotalVoltage = ReadingTypeCodeBuilder
            .of(Commodity.ELECTRICITY_SECONDARY_METERED)
            .accumulate(Accumulation.INDICATING)
            .measure(MeasurementKind.VOLTAGE)
            .in(ReadingTypeUnit.VOLT)
            .code();
    private String currentPhase2Voltage = ReadingTypeCodeBuilder
            .of(Commodity.ELECTRICITY_SECONDARY_METERED)
            .accumulate(Accumulation.INDICATING)
            .measure(MeasurementKind.VOLTAGE)
            .in(ReadingTypeUnit.VOLT)
            .phase(Phase.PHASEBN)
            .code();

    private ObisCode forwardActiveEnergyObisCode = ObisCode.fromString("1.0.1.8.0.255");
    private ObisCode forwardActiveEnergyTou1ObisCode = ObisCode.fromString("1.0.1.8.1.255");
    private ObisCode reverseActiveEnergyObisCode = ObisCode.fromString("1.0.2.8.0.255");
    private ObisCode reverseActiveEnergyTou2ObisCode = ObisCode.fromString("1.0.2.8.2.255");
    private ObisCode forwardActiveEnergyDeltaObisCode = ObisCode.fromString("1.0.1.6.0.255");
    private ObisCode reverseActiveEnergyDeltaObisCode = ObisCode.fromString("1.0.2.6.0.255");
    private ObisCode forwardActiveEnergyTou1DeltaObisCode = ObisCode.fromString("1.0.1.6.1.255");
    private ObisCode reverseActiveEnergyTou2DeltaObisCode = ObisCode.fromString("1.0.2.6.2.255");
    private ObisCode totalCurrentObisCode = ObisCode.fromString("1.0.11.7.0.255");
    private ObisCode phase1CurrentObisCode = ObisCode.fromString("1.0.31.7.0.255");
    private ObisCode totalVoltageObisCode = ObisCode.fromString("1.0.12.7.0.255");
    private ObisCode phase2VoltageObisCode = ObisCode.fromString("1.0.52.7.0.255");

    @Test
    public void forwardActiveEnergyRegisterTest() {
        ReadingTypeInformation readingTypeInformation = ReadingTypeToObisCodeFactory.from(forwardActiveEnergyReadingType);

        assertThat(readingTypeInformation.getObisCode()).isEqualTo(forwardActiveEnergyObisCode);
        assertThat(readingTypeInformation.getUnit()).isEqualTo(Unit.get("kWh"));
        assertThat(readingTypeInformation.getTimeDuration()).isNull();
    }

    @Test
    public void forwardActiveEnergyTou1RegisterTest() {
        ReadingTypeInformation readingTypeInformation = ReadingTypeToObisCodeFactory.from(forwardActiveEnergyTou1ReadingType);

        assertThat(readingTypeInformation.getObisCode()).isEqualTo(forwardActiveEnergyTou1ObisCode);
        assertThat(readingTypeInformation.getUnit()).isEqualTo(Unit.get("kWh"));
        assertThat(readingTypeInformation.getTimeDuration()).isNull();
    }

    @Test
    public void reverseActiveEnergyRegisterTest() {
        ReadingTypeInformation readingTypeInformation = ReadingTypeToObisCodeFactory.from(reverseActiveEnergyReadingType);

        assertThat(readingTypeInformation.getObisCode()).isEqualTo(reverseActiveEnergyObisCode);
        assertThat(readingTypeInformation.getUnit()).isEqualTo(Unit.get("kWh"));
        assertThat(readingTypeInformation.getTimeDuration()).isNull();
    }

    @Test
    public void reverseActiveEnergyTou2RegisterTest() {
        ReadingTypeInformation readingTypeInformation = ReadingTypeToObisCodeFactory.from(reverseActiveEnergyTou2ReadingType);

        assertThat(readingTypeInformation.getObisCode()).isEqualTo(reverseActiveEnergyTou2ObisCode);
        assertThat(readingTypeInformation.getUnit()).isEqualTo(Unit.get("kWh"));
        assertThat(readingTypeInformation.getTimeDuration()).isNull();
    }

    @Test
    public void forwardActiveEnergy15MinDeltaTest() {
        ReadingTypeInformation readingTypeInformation = ReadingTypeToObisCodeFactory.from(forwardActiveEnergyReadingType15MinDelta);

        assertThat(readingTypeInformation.getObisCode()).isEqualTo(forwardActiveEnergyDeltaObisCode);
        assertThat(readingTypeInformation.getUnit()).isEqualTo(Unit.get("kWh"));
        assertThat(readingTypeInformation.getTimeDuration()).isEqualTo(TimeDuration.minutes(15));
    }

    @Test
    public void forwardActiveEnergyTou115MinDeltaTest() {
        ReadingTypeInformation readingTypeInformation = ReadingTypeToObisCodeFactory.from(forwardActiveEnergyTOU1ReadingType15MinDelta);

        assertThat(readingTypeInformation.getObisCode()).isEqualTo(forwardActiveEnergyTou1DeltaObisCode);
        assertThat(readingTypeInformation.getUnit()).isEqualTo(Unit.get("kWh"));
        assertThat(readingTypeInformation.getTimeDuration()).isEqualTo(TimeDuration.minutes(15));
    }

    @Test
    public void forwardActiveEnergyTou1DailyDeltaTest() {
        ReadingTypeInformation readingTypeInformation = ReadingTypeToObisCodeFactory.from(forwardActiveEnergyTOU1ReadingTypeDailyDelta);

        assertThat(readingTypeInformation.getObisCode()).isEqualTo(forwardActiveEnergyTou1DeltaObisCode);
        assertThat(readingTypeInformation.getUnit()).isEqualTo(Unit.get("kWh"));
        assertThat(readingTypeInformation.getTimeDuration()).isEqualTo(TimeDuration.days(1));
    }

    @Test
    public void reverseActiveEnergy15minDeltaTest() {
        ReadingTypeInformation readingTypeInformation = ReadingTypeToObisCodeFactory.from(reverseActiveEnergyReadingType15MinDelta);

        assertThat(readingTypeInformation.getObisCode()).isEqualTo(reverseActiveEnergyDeltaObisCode);
        assertThat(readingTypeInformation.getUnit()).isEqualTo(Unit.get("kWh"));
        assertThat(readingTypeInformation.getTimeDuration()).isEqualTo(TimeDuration.minutes(15));
    }

    @Test
    public void reverseActiveEnergyTou215minDeltaTest() {
        ReadingTypeInformation readingTypeInformation = ReadingTypeToObisCodeFactory.from(reverseActiveEnergyTOU2ReadingType15MinDelta);

        assertThat(readingTypeInformation.getObisCode()).isEqualTo(reverseActiveEnergyTou2DeltaObisCode);
        assertThat(readingTypeInformation.getUnit()).isEqualTo(Unit.get("kWh"));
        assertThat(readingTypeInformation.getTimeDuration()).isEqualTo(TimeDuration.minutes(15));
    }

    @Test
    public void forwardActiveEnergyDailyDeltaTest() {
        ReadingTypeInformation readingTypeInformation = ReadingTypeToObisCodeFactory.from(forwardActiveEnergyReadingTypeDailyDelta);

        assertThat(readingTypeInformation.getObisCode()).isEqualTo(forwardActiveEnergyDeltaObisCode);
        assertThat(readingTypeInformation.getUnit()).isEqualTo(Unit.get("kWh"));
        assertThat(readingTypeInformation.getTimeDuration()).isEqualTo(TimeDuration.days(1));
    }

    @Test
    public void currentTotalAmpereAnyPhaseTest() {
        ReadingTypeInformation readingTypeInformation = ReadingTypeToObisCodeFactory.from(currentTotalAmpere);

        assertThat(readingTypeInformation.getObisCode()).isEqualTo(totalCurrentObisCode);
        assertThat(readingTypeInformation.getUnit()).isEqualTo(Unit.get("A"));
        assertThat(readingTypeInformation.getTimeDuration()).isNull();
    }

    @Test
    public void currentPhase1AmpereTest() {
        ReadingTypeInformation readingTypeInformation = ReadingTypeToObisCodeFactory.from(currentPhase1Ampere);

        assertThat(readingTypeInformation.getObisCode()).isEqualTo(phase1CurrentObisCode);
        assertThat(readingTypeInformation.getUnit()).isEqualTo(Unit.get("A"));
        assertThat(readingTypeInformation.getTimeDuration()).isNull();
    }

    @Test
    public void currentTotalVoltageAnyPhaseTest() {
        ReadingTypeInformation readingTypeInformation = ReadingTypeToObisCodeFactory.from(currentTotalVoltage);

        assertThat(readingTypeInformation.getObisCode()).isEqualTo(totalVoltageObisCode);
        assertThat(readingTypeInformation.getUnit()).isEqualTo(Unit.get("V"));
        assertThat(readingTypeInformation.getTimeDuration()).isNull();
    }

    @Test
    public void currentVoltagePhase2Test() {
        ReadingTypeInformation readingTypeInformation = ReadingTypeToObisCodeFactory.from(currentPhase2Voltage);

        assertThat(readingTypeInformation.getObisCode()).isEqualTo(phase2VoltageObisCode);
        assertThat(readingTypeInformation.getUnit()).isEqualTo(Unit.get("V"));
        assertThat(readingTypeInformation.getTimeDuration()).isNull();
    }

    @Ignore
    @Test
    public void exceptionTest() {
        //TODO complete test where the UnableToExtractUniqueObisCodeFromReadingTypeException is tested
    }
}
