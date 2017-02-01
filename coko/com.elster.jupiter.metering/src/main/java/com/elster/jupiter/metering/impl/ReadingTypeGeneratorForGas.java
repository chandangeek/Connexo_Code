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
 * Generates Gas specific ReadingTypes<br/>
 * The generator contains several enums which serve as a sort of artificial <i>grouping</i>.
 * Each enum implements the {@link ReadingTypeTemplate} so the actual generator can generate ReadingTypes based on the templates.
 * Each template can indicate if it needs expansion for a certain attribute.
 */
class ReadingTypeGeneratorForGas extends AbstractReadingTypeGenerator {

    private enum ReadingTypesForPossibleLoadProfileUsage implements ReadingTypeTemplate {

        GAS_VOLUME_M3("Gas volume", ReadingTypeCodeBuilder.of(Commodity.NATURALGAS).flow(FlowDirection.FORWARD).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.CUBICMETER)),
        GAS_VOLUME_LITRE("Gas volume", ReadingTypeCodeBuilder.of(Commodity.NATURALGAS).flow(FlowDirection.FORWARD).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.LITRE)),
        GAS_VOLUME_NORM_M3("Gas volume norm", ReadingTypeCodeBuilder.of(Commodity.NATURALGAS).flow(FlowDirection.FORWARD).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.CUBICMETERCOMPENSATED)),
        GAS_VOLUME_NORM_LITRE("Gas volume norm", ReadingTypeCodeBuilder.of(Commodity.NATURALGAS).flow(FlowDirection.FORWARD).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.LITRECOMPENSATED)),

        GAS_PRESSURE_BAR("Gas pressure", ReadingTypeCodeBuilder.of(Commodity.NATURALGAS).aggregate(Aggregate.AVERAGE).accumulate(Accumulation.INDICATING).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.BAR)) {
            @Override
            public boolean needsAccumulationExpansion() {
                return false;
            }
        },
        GAS_PRESSURE_PASCAL("Gas pressure", ReadingTypeCodeBuilder.of(Commodity.NATURALGAS).aggregate(Aggregate.AVERAGE).accumulate(Accumulation.INDICATING).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.PASCAL)) {
            @Override
            public boolean needsAccumulationExpansion() {
                return false;
            }
        },
        GAS_FLOW("Gas flow", ReadingTypeCodeBuilder.of(Commodity.NATURALGAS).aggregate(Aggregate.AVERAGE).accumulate(Accumulation.INDICATING).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.CUBICMETERPERHOUR)) {
            @Override
            public boolean needsAccumulationExpansion() {
                return false;
            }
        },
        GAS_FLOW_NORM("Gas flow norm", ReadingTypeCodeBuilder.of(Commodity.NATURALGAS).aggregate(Aggregate.AVERAGE).accumulate(Accumulation.INDICATING).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.CUBICMETERPERHOURCOMPENSATED)) {
            @Override
            public boolean needsAccumulationExpansion() {
                return false;
            }
        },
        GAS_CONVERSION_FACTOR("Gas conversion factor", ReadingTypeCodeBuilder.of(Commodity.NATURALGAS).aggregate(Aggregate.AVERAGE).accumulate(Accumulation.INDICATING).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.WATTHOURPERCUBICMETER)) {
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

        DAILY_GAS_VOLUME_M3("Gas volume", ReadingTypeCodeBuilder.of(Commodity.NATURALGAS).period(MacroPeriod.DAILY).flow(FlowDirection.FORWARD).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.CUBICMETER)),
        DAILY_GAS_VOLUME_LITRE("Gas volume", ReadingTypeCodeBuilder.of(Commodity.NATURALGAS).period(MacroPeriod.DAILY).flow(FlowDirection.FORWARD).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.LITRE)),
        DAILY_GAS_VOLUME_NORM_M3("Gas volume", ReadingTypeCodeBuilder.of(Commodity.NATURALGAS).period(MacroPeriod.DAILY).flow(FlowDirection.FORWARD).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.CUBICMETERCOMPENSATED)),
        DAILY_GAS_VOLUME_NORM_LITRE("Gas volume", ReadingTypeCodeBuilder.of(Commodity.NATURALGAS).period(MacroPeriod.DAILY).flow(FlowDirection.FORWARD).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.LITRECOMPENSATED)),
        MONTHLY_GAS_VOLUME_M3("Gas volume", ReadingTypeCodeBuilder.of(Commodity.NATURALGAS).period(MacroPeriod.MONTHLY).flow(FlowDirection.FORWARD).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.CUBICMETER)),
        MONTHLY_GAS_VOLUME_LITRE("Gas volume", ReadingTypeCodeBuilder.of(Commodity.NATURALGAS).period(MacroPeriod.MONTHLY).flow(FlowDirection.FORWARD).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.LITRE)),
        MONTHLY_GAS_VOLUME_NORM_M3("Gas volume", ReadingTypeCodeBuilder.of(Commodity.NATURALGAS).period(MacroPeriod.MONTHLY).flow(FlowDirection.FORWARD).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.CUBICMETERCOMPENSATED)),
        MONTHLY_GAS_VOLUME_NORM_LITRE("Gas volume", ReadingTypeCodeBuilder.of(Commodity.NATURALGAS).period(MacroPeriod.MONTHLY).flow(FlowDirection.FORWARD).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.LITRECOMPENSATED)),;

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

        BILLING_GAS_VOLUME_M3("Billing gas volume", ReadingTypeCodeBuilder.of(Commodity.NATURALGAS).period(MacroPeriod.BILLINGPERIOD).accumulate(Accumulation.SUMMATION).flow(FlowDirection.FORWARD).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.CUBICMETER)),
        BILLING_GAS_VOLUME_LITRE("Billing gas volume", ReadingTypeCodeBuilder.of(Commodity.NATURALGAS).period(MacroPeriod.BILLINGPERIOD).accumulate(Accumulation.SUMMATION).flow(FlowDirection.FORWARD).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.LITRE)),
        BILLING_GAS_VOLUME_NORM_M3("Billing gas volume", ReadingTypeCodeBuilder.of(Commodity.NATURALGAS).period(MacroPeriod.BILLINGPERIOD).accumulate(Accumulation.SUMMATION).flow(FlowDirection.FORWARD).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.CUBICMETERCOMPENSATED)),
        BILLING_GAS_VOLUME_NORM_LITRE("Billing gas volume", ReadingTypeCodeBuilder.of(Commodity.NATURALGAS).period(MacroPeriod.BILLINGPERIOD).accumulate(Accumulation.SUMMATION).flow(FlowDirection.FORWARD).measure(MeasurementKind.VOLUME).in(ReadingTypeUnit.LITRECOMPENSATED)),;

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
        public boolean needsMetricMultiplierExpansion() {
            return true;
        }
    }

    private enum ReadingTypesForParameters implements ReadingTypeTemplate {

        GAS_TEMPERATURE_K("Gas temperature", ReadingTypeCodeBuilder.of(Commodity.NATURALGAS).accumulate(Accumulation.INSTANTANEOUS).measure(MeasurementKind.TEMPERATURE).in(ReadingTypeUnit.KELVIN)),
        GAS_TEMPERATURE_C("Gas temperature", ReadingTypeCodeBuilder.of(Commodity.NATURALGAS).accumulate(Accumulation.INSTANTANEOUS).measure(MeasurementKind.TEMPERATURE).in(ReadingTypeUnit.DEGREESCELSIUS)),
        GAS_TEMPERATURE_F("Gas temperature", ReadingTypeCodeBuilder.of(Commodity.NATURALGAS).accumulate(Accumulation.INSTANTANEOUS).measure(MeasurementKind.TEMPERATURE).in(ReadingTypeUnit.DEGREESFAHRENHEIT)),;

        final String name;
        final ReadingTypeCodeBuilder readingTypeCodeBuilder;

        ReadingTypesForParameters(String name, ReadingTypeCodeBuilder readingTypeCodeBuilder) {
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
    }

    ReadingTypeGeneratorForGas() {
        super();
    }

    @Override
    Stream<ReadingTypeTemplate> getReadingTypeTemplates() {
        return Stream.of(ReadingTypesForPossibleLoadProfileUsage.values(),
                ReadingTypesForPossibleDailyMonthlyLoadProfileUsage.values(),
                ReadingTypesForBilling.values(),
                ReadingTypesForParameters.values()).flatMap(Arrays::stream);
    }

}
