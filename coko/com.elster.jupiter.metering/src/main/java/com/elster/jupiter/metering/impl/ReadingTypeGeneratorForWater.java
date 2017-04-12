/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.metering.MeteringService;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Generates Water specific ReadingTypes<br/>
 * The generator contains several enums which serve as a sort of artificial <i>grouping</i>.
 * Each enum implements the {@link ReadingTypeTemplate} so the actual generator can generate ReadingTypes based on the templates.
 * Each template can indicate if it needs expansion for a certain attribute.
 */
class ReadingTypeGeneratorForWater extends AbstractReadingTypeGenerator {

    private enum ReadingTypesForPossibleLoadProfileUsage implements ReadingTypeTemplate {

        WATER_VOLUME_M3("Water volume", ReadingTypeCodeBuilder.of(Commodity.POTABLEWATER).flow(FlowDirection.FORWARD).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.CUBICMETER)),
        WATER_VOLUME_LITRE("Water volume", ReadingTypeCodeBuilder.of(Commodity.POTABLEWATER).flow(FlowDirection.FORWARD).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.LITRE)),
        WATER_VOLUME_BACKFLOW_M3("Water volume backflow", ReadingTypeCodeBuilder.of(Commodity.POTABLEWATER).flow(FlowDirection.REVERSE).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.CUBICMETERCOMPENSATED)),
        WATER_VOLUME_BACKFLOW_LITRE("Water volume backflow", ReadingTypeCodeBuilder.of(Commodity.POTABLEWATER).flow(FlowDirection.REVERSE).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.LITRECOMPENSATED)),
        WATER_FLOW("Water flow", ReadingTypeCodeBuilder.of(Commodity.POTABLEWATER).aggregate(Aggregate.AVERAGE).accumulate(Accumulation.INDICATING).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.CUBICMETERPERHOUR)) {
            @Override
            public boolean needsAccumulationExpansion() {
                return false;
            }
        },;

        final String name;
        final ReadingTypeCodeBuilder readingTypeCodeBuilder;

        ReadingTypesForPossibleLoadProfileUsage(String name, ReadingTypeCodeBuilder readingTypeCodeBuilder) {
            this.name = name;
            this.readingTypeCodeBuilder = readingTypeCodeBuilder;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ReadingTypeCodeBuilder getReadingTypeCodeBuilder() {
            return readingTypeCodeBuilder;
        }

        @Override
        public boolean needsTimeAttributeExpansion() {
            return true;
        }

        @Override
        public boolean needsAccumulationExpansion() {
            return true;
        }

        @Override
        public boolean needsMetricMultiplierExpansion() {
            return true;
        }
    }

    private enum ReadingTypesForPossibleDailyMonthlyLoadProfileUsage implements ReadingTypeTemplate {

        DAILY_WATER_VOLUME_M3("Water volume", ReadingTypeCodeBuilder.of(Commodity.POTABLEWATER).period(MacroPeriod.DAILY).flow(FlowDirection.FORWARD).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.CUBICMETER)),
        DAILY_WATER_VOLUME_LITRE("Water volume", ReadingTypeCodeBuilder.of(Commodity.POTABLEWATER).period(MacroPeriod.DAILY).flow(FlowDirection.FORWARD).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.LITRE)),
        DAILY_WATER_VOLUME_BACKFLOW_M3("Water volume backflow", ReadingTypeCodeBuilder.of(Commodity.POTABLEWATER).period(MacroPeriod.DAILY).flow(FlowDirection.REVERSE).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.CUBICMETERCOMPENSATED)),
        DAILY_WATER_VOLUME_BACKFLOW_LITRE("Water volume backflow", ReadingTypeCodeBuilder.of(Commodity.POTABLEWATER).period(MacroPeriod.DAILY).flow(FlowDirection.REVERSE).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.LITRECOMPENSATED)),
        MONTHLY_WATER_VOLUME_M3("Water volume", ReadingTypeCodeBuilder.of(Commodity.POTABLEWATER).period(MacroPeriod.MONTHLY).flow(FlowDirection.FORWARD).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.CUBICMETER)),
        MONTHLY_WATER_VOLUME_LITRE("Water volume", ReadingTypeCodeBuilder.of(Commodity.POTABLEWATER).period(MacroPeriod.MONTHLY).flow(FlowDirection.FORWARD).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.LITRE)),
        MONTHLY_WATER_VOLUME_BACKFLOW_M3("Water volume backflow", ReadingTypeCodeBuilder.of(Commodity.POTABLEWATER).period(MacroPeriod.MONTHLY).flow(FlowDirection.REVERSE).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.CUBICMETERCOMPENSATED)),
        MONTHLY_WATER_VOLUME_BACKFLOW_LITRE("Water volume backflow", ReadingTypeCodeBuilder.of(Commodity.POTABLEWATER).period(MacroPeriod.MONTHLY).flow(FlowDirection.REVERSE).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.LITRECOMPENSATED)),;

        final String name;
        final ReadingTypeCodeBuilder readingTypeCodeBuilder;

        ReadingTypesForPossibleDailyMonthlyLoadProfileUsage(String name, ReadingTypeCodeBuilder readingTypeCodeBuilder) {
            this.name = name;
            this.readingTypeCodeBuilder = readingTypeCodeBuilder;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ReadingTypeCodeBuilder getReadingTypeCodeBuilder() {
            return readingTypeCodeBuilder;
        }

        @Override
        public boolean needsAccumulationExpansion() {
            return true;
        }

        @Override
        public boolean needsMetricMultiplierExpansion() {
            return true;
        }
    }

    private enum ReadingTypesForBilling implements ReadingTypeTemplate {

        BILLING_WATER_VOLUME_M3("Billing water volume", ReadingTypeCodeBuilder.of(Commodity.POTABLEWATER).period(MacroPeriod.BILLINGPERIOD).accumulate(Accumulation.SUMMATION).flow(FlowDirection.FORWARD).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.CUBICMETER)),
        BILLING_WATER_VOLUME_LITRE("Billing water volume", ReadingTypeCodeBuilder.of(Commodity.POTABLEWATER).period(MacroPeriod.BILLINGPERIOD).accumulate(Accumulation.SUMMATION).flow(FlowDirection.FORWARD).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.LITRE)),;

        final String name;
        final ReadingTypeCodeBuilder readingTypeCodeBuilder;

        ReadingTypesForBilling(String name, ReadingTypeCodeBuilder readingTypeCodeBuilder) {
            this.name = name;
            this.readingTypeCodeBuilder = readingTypeCodeBuilder;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ReadingTypeCodeBuilder getReadingTypeCodeBuilder() {
            return readingTypeCodeBuilder;
        }

        @Override
        public boolean needsAccumulationExpansion() {
            return true;
        }

        @Override
        public boolean needsMetricMultiplierExpansion() {
            return true;
        }
    }

    ReadingTypeGeneratorForWater() {
        super();
    }

    @Override
    Stream<ReadingTypeTemplate> getReadingTypeTemplates() {
        return Stream.of(ReadingTypesForPossibleLoadProfileUsage.values(),
                ReadingTypesForPossibleDailyMonthlyLoadProfileUsage.values(),
                ReadingTypesForBilling.values()).flatMap(Arrays::stream);
    }

}
