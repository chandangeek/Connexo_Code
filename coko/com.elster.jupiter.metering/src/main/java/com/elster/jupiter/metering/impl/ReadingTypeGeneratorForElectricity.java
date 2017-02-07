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

import java.util.Arrays;
import java.util.stream.Stream;


/**
 * Generates electricity specific ReadingTypes.<br/>
 * The generator contains several enums which serve as a sort of artificial <i>grouping</i>.
 * Each enum implements the {@link ReadingTypeTemplate} so the actual generator can generate ReadingTypes based on the templates.
 * Each template can indicate if it needs expansion for a certain attribute.
 */
class ReadingTypeGeneratorForElectricity extends AbstractReadingTypeGenerator {

    enum ReadingTypesForPossibleLoadProfileUsage implements ReadingTypeTemplate {

        ACTIVE_IMPORT("A+", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).flow(FlowDirection.FORWARD).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.WATTHOUR)),
        ACTIVE_EXPORT("A-", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).flow(FlowDirection.REVERSE).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.WATTHOUR)),

        REACTIVE_IMPORT("Reactive energy+", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).flow(FlowDirection.LAGGING).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)),
        REACTIVE_EXPORT("Reactive energy-", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).flow(FlowDirection.LEADING).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)),

        INDUCTIVE_ENERGY_Q1("Inductive energy Q1", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).flow(FlowDirection.Q1).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)),
        INDUCTIVE_ENERGY_Q2("Inductive energy Q2", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).flow(FlowDirection.Q2).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)),
        INDUCTIVE_ENERGY_Q3("Inductive energy Q3", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).flow(FlowDirection.Q3).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)),
        INDUCTIVE_ENERGY_Q4("Inductive energy Q4", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).flow(FlowDirection.Q4).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)),

        AVERAGE_POWER_FACTOR_IMPORT("Average power factor+", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).aggregate(Aggregate.AVERAGE).accumulate(Accumulation.INDICATING).flow(FlowDirection.FORWARD).measure(MeasurementKind.POWERFACTOR)) {
            @Override
            public boolean needsAccumulationExpansion() {
                return false;
            }

            @Override
            public boolean needsMetricMultiplierExpansion() {
                return false;
            }

            @Override
            public boolean needsTOUExpansion() {
                return false;
            }
        },
        AVERAGE_POWER_FACTOR_EXPORT("Average power factor-", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).aggregate(Aggregate.AVERAGE).accumulate(Accumulation.INDICATING).flow(FlowDirection.REVERSE).measure(MeasurementKind.POWERFACTOR)) {
            @Override
            public boolean needsAccumulationExpansion() {
                return false;
            }

            @Override
            public boolean needsMetricMultiplierExpansion() {
                return false;
            }

            @Override
            public boolean needsTOUExpansion() {
                return false;
            }
        },
        AVERAGE_CURRENT_IMPORT("Average current+", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).aggregate(Aggregate.AVERAGE).accumulate(Accumulation.INDICATING).flow(FlowDirection.FORWARD).measure(MeasurementKind.CURRENT).in(ReadingTypeUnit.AMPERE)) {
            @Override
            public boolean needsAccumulationExpansion() {
                return false;
            }

            @Override
            public boolean needsTOUExpansion() {
                return false;
            }
        },
        AVERAGE_CURRENT_EXPORT("Average current-", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).aggregate(Aggregate.AVERAGE).accumulate(Accumulation.INDICATING).flow(FlowDirection.REVERSE).measure(MeasurementKind.CURRENT).in(ReadingTypeUnit.AMPERE)) {
            @Override
            public boolean needsAccumulationExpansion() {
                return false;
            }

            @Override
            public boolean needsTOUExpansion() {
                return false;
            }
        },
        AVERAGE_POWER_IMPORT("Average power+", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).aggregate(Aggregate.AVERAGE).accumulate(Accumulation.INDICATING).flow(FlowDirection.FORWARD).measure(MeasurementKind.POWER).in(ReadingTypeUnit.WATT)) {
            @Override
            public boolean needsAccumulationExpansion() {
                return false;
            }

            @Override
            public boolean needsTOUExpansion() {
                return false;
            }
        },
        AVERAGE_POWER_EXPORT("Average power-", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).aggregate(Aggregate.AVERAGE).accumulate(Accumulation.INDICATING).flow(FlowDirection.REVERSE).measure(MeasurementKind.POWER).in(ReadingTypeUnit.WATT)) {
            @Override
            public boolean needsAccumulationExpansion() {
                return false;
            }

            @Override
            public boolean needsTOUExpansion() {
                return false;
            }
        },
        AVERAGE_VOLTAGE("Average voltage", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).aggregate(Aggregate.AVERAGE).accumulate(Accumulation.INDICATING).measure(MeasurementKind.VOLTAGE).in(ReadingTypeUnit.VOLT)) {
            @Override
            public boolean needsAccumulationExpansion() {
                return false;
            }

            @Override
            public boolean needsTOUExpansion() {
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
        public boolean needsTOUExpansion() {
            return true;
        }

        @Override
        public boolean needsPhaseExpansion() {
            return true;
        }

        @Override
        public boolean needsMetricMultiplierExpansion() {
            return true;
        }
    }

    private enum ReadingTypesForPossibleDailyMonthlyLoadProfileUsage implements ReadingTypeTemplate {

        DAILY_ACTIVE_IMPORT("A+", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.DAILY).flow(FlowDirection.FORWARD).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.WATTHOUR)),
        DAILY_ACTIVE_EXPORT("A-", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.DAILY).flow(FlowDirection.REVERSE).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.WATTHOUR)),
        MONTHLY_ACTIVE_IMPORT("A+", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.MONTHLY).flow(FlowDirection.FORWARD).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.WATTHOUR)),
        MONTHLY_ACTIVE_EXPORT("A-", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.MONTHLY).flow(FlowDirection.REVERSE).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.WATTHOUR)),
        YEARLY_ACTIVE_IMPORT("A+", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.YEARLY).flow(FlowDirection.FORWARD).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.WATTHOUR)),
        YEARLY_ACTIVE_EXPORT("A-", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.YEARLY).flow(FlowDirection.REVERSE).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.WATTHOUR)),

        DAILY_INDUCTIVE_ENERGY_Q1("Inductive energy Q1", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.DAILY).flow(FlowDirection.Q1).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)),
        DAILY_INDUCTIVE_ENERGY_Q2("Inductive energy Q2", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.DAILY).flow(FlowDirection.Q2).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)),
        DAILY_INDUCTIVE_ENERGY_Q3("Inductive energy Q3", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.DAILY).flow(FlowDirection.Q3).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)),
        DAILY_INDUCTIVE_ENERGY_Q4("Inductive energy Q4", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.DAILY).flow(FlowDirection.Q4).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)),
        MONTHLY_INDUCTIVE_ENERGY_Q1("Inductive energy Q1", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.MONTHLY).flow(FlowDirection.Q1).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)),
        MONTHLY_INDUCTIVE_ENERGY_Q2("Inductive energy Q2", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.MONTHLY).flow(FlowDirection.Q2).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)),
        MONTHLY_INDUCTIVE_ENERGY_Q3("Inductive energy Q3", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.MONTHLY).flow(FlowDirection.Q3).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)),
        MONTHLY_INDUCTIVE_ENERGY_Q4("Inductive energy Q4", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.MONTHLY).flow(FlowDirection.Q4).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)),
        YEARLY_INDUCTIVE_ENERGY_Q1("Inductive energy Q1", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.YEARLY).flow(FlowDirection.Q1).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)),
        YEARLY_INDUCTIVE_ENERGY_Q2("Inductive energy Q2", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.YEARLY).flow(FlowDirection.Q2).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)),
        YEARLY_INDUCTIVE_ENERGY_Q3("Inductive energy Q3", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.YEARLY).flow(FlowDirection.Q3).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)),
        YEARLY_INDUCTIVE_ENERGY_Q4("Inductive energy Q4", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.YEARLY).flow(FlowDirection.Q4).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)),

        DAILY_REACTIVE_IMPORT("Reactive energy+", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.DAILY).flow(FlowDirection.LAGGING).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)),
        DAILY_REACTIVE_EXPORT("Reactive energy-", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.DAILY).flow(FlowDirection.LEADING).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)),
        MONTHLY_REACTIVE_IMPORT("Reactive energy+", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.MONTHLY).flow(FlowDirection.LAGGING).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)),
        MONTHLY_REACTIVE_EXPORT("Reactive energy-", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.MONTHLY).flow(FlowDirection.LEADING).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)),
        YEARLY_REACTIVE_IMPORT("Reactive energy+", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.YEARLY).flow(FlowDirection.LAGGING).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)),
        YEARLY_REACTIVE_EXPORT("Reactive energy-", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.YEARLY).flow(FlowDirection.LEADING).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR));

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
        public boolean needsTOUExpansion() {
            return true;
        }

        @Override
        public boolean needsPhaseExpansion() {
            return true;
        }

        @Override
        public boolean needsMetricMultiplierExpansion() {
            return true;
        }
    }

    private enum ReadingTypesForDemands implements ReadingTypeTemplate {

        DAILY_ACTIVE_ENERGY_MAX_DEMAND("Active energy max demand", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.DAILY).aggregate(Aggregate.MAXIMUM).accumulate(Accumulation.INDICATING).flow(FlowDirection.FORWARD).measure(MeasurementKind.DEMAND).in(ReadingTypeUnit.WATT)),
        MONTHLY_ACTIVE_ENERGY_MAX_DEMAND("Active energy max demand", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.MONTHLY).aggregate(Aggregate.MAXIMUM).accumulate(Accumulation.INDICATING).flow(FlowDirection.FORWARD).measure(MeasurementKind.DEMAND).in(ReadingTypeUnit.WATT)),
        YEARLY_ACTIVE_ENERGY_MAX_DEMAND("Active energy max demand", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.YEARLY).aggregate(Aggregate.MAXIMUM).accumulate(Accumulation.INDICATING).flow(FlowDirection.FORWARD).measure(MeasurementKind.DEMAND).in(ReadingTypeUnit.WATT)),
        BILLING_ACTIVE_ENERGY_MAX_DEMAND("Billing active energy max demand", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.BILLINGPERIOD).aggregate(Aggregate.MAXIMUM).accumulate(Accumulation.INDICATING).flow(FlowDirection.FORWARD).measure(MeasurementKind.DEMAND).in(ReadingTypeUnit.WATT)),

        DAILY_ACTIVE_ENERGY_MAX_SUPPLY("Active energy max supply", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.DAILY).aggregate(Aggregate.MAXIMUM).accumulate(Accumulation.INDICATING).flow(FlowDirection.REVERSE).measure(MeasurementKind.DEMAND).in(ReadingTypeUnit.WATT)),
        MONTHLY_ACTIVE_ENERGY_MAX_SUPPLY("Active energy max supply", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.MONTHLY).aggregate(Aggregate.MAXIMUM).accumulate(Accumulation.INDICATING).flow(FlowDirection.REVERSE).measure(MeasurementKind.DEMAND).in(ReadingTypeUnit.WATT)),
        YEARLY_ACTIVE_ENERGY_MAX_SUPPLY("Active energy max supply", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.YEARLY).aggregate(Aggregate.MAXIMUM).accumulate(Accumulation.INDICATING).flow(FlowDirection.REVERSE).measure(MeasurementKind.DEMAND).in(ReadingTypeUnit.WATT)),
        BILLING_ACTIVE_ENERGY_MAX_SUPPLY("Billing active energy max supply", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.BILLINGPERIOD).aggregate(Aggregate.MAXIMUM).accumulate(Accumulation.INDICATING).flow(FlowDirection.REVERSE).measure(MeasurementKind.DEMAND).in(ReadingTypeUnit.WATT)),

        DAILY_REACTIVE_ENERGY_MAX_DEMAND("Reactive energy max demand", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.DAILY).aggregate(Aggregate.MAXIMUM).accumulate(Accumulation.INDICATING).flow(FlowDirection.LAGGING).measure(MeasurementKind.DEMAND).in(ReadingTypeUnit.VOLTAMPEREREACTIVE)),
        MONTHLY_REACTIVE_ENERGY_MAX_DEMAND("Reactive energy max demand", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.MONTHLY).aggregate(Aggregate.MAXIMUM).accumulate(Accumulation.INDICATING).flow(FlowDirection.LAGGING).measure(MeasurementKind.DEMAND).in(ReadingTypeUnit.VOLTAMPEREREACTIVE)),
        YEARLY_REACTIVE_ENERGY_MAX_DEMAND("Reactive energy max demand", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.YEARLY).aggregate(Aggregate.MAXIMUM).accumulate(Accumulation.INDICATING).flow(FlowDirection.LAGGING).measure(MeasurementKind.DEMAND).in(ReadingTypeUnit.VOLTAMPEREREACTIVE)),
        BILLING_REACTIVE_ENERGY_MAX_DEMAND("Billing reactive energy max demand", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.BILLINGPERIOD).aggregate(Aggregate.MAXIMUM).accumulate(Accumulation.INDICATING).flow(FlowDirection.LAGGING).measure(MeasurementKind.DEMAND).in(ReadingTypeUnit.VOLTAMPEREREACTIVE)),

        DAILY_REACTIVE_ENERGY_MAX_SUPPLY("Reactive energy max supply", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.DAILY).aggregate(Aggregate.MAXIMUM).accumulate(Accumulation.INDICATING).flow(FlowDirection.LEADING).measure(MeasurementKind.DEMAND).in(ReadingTypeUnit.VOLTAMPEREREACTIVE)),
        MONTHLY_REACTIVE_ENERGY_MAX_SUPPLY("Reactive energy max supply", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.MONTHLY).aggregate(Aggregate.MAXIMUM).accumulate(Accumulation.INDICATING).flow(FlowDirection.LEADING).measure(MeasurementKind.DEMAND).in(ReadingTypeUnit.VOLTAMPEREREACTIVE)),
        YEARLY_REACTIVE_ENERGY_MAX_SUPPLY("Reactive energy max supply", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.YEARLY).aggregate(Aggregate.MAXIMUM).accumulate(Accumulation.INDICATING).flow(FlowDirection.LEADING).measure(MeasurementKind.DEMAND).in(ReadingTypeUnit.VOLTAMPEREREACTIVE)),
        BILLING_REACTIVE_ENERGY_MAX_SUPPLY("Billing reactive energy max supply", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.BILLINGPERIOD).aggregate(Aggregate.MAXIMUM).accumulate(Accumulation.INDICATING).flow(FlowDirection.LEADING).measure(MeasurementKind.DEMAND).in(ReadingTypeUnit.VOLTAMPEREREACTIVE)),

        DAILY_ACTIVE_ENERGY_CUM_MAX_DEMAND("Active energy cum. max demand", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.DAILY).aggregate(Aggregate.MAXIMUM).accumulate(Accumulation.CUMULATIVE).flow(FlowDirection.FORWARD).measure(MeasurementKind.DEMAND).in(ReadingTypeUnit.WATT)),
        MONTHLY_ACTIVE_ENERGY_CUM_MAX_DEMAND("Active energy cum. max demand", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.MONTHLY).aggregate(Aggregate.MAXIMUM).accumulate(Accumulation.CUMULATIVE).flow(FlowDirection.FORWARD).measure(MeasurementKind.DEMAND).in(ReadingTypeUnit.WATT)),
        YEARLY_ACTIVE_ENERGY_CUM_MAX_DEMAND("Active energy cum. max demand", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.YEARLY).aggregate(Aggregate.MAXIMUM).accumulate(Accumulation.CUMULATIVE).flow(FlowDirection.FORWARD).measure(MeasurementKind.DEMAND).in(ReadingTypeUnit.WATT)),
        BILLING_ACTIVE_ENERGY_CUM_MAX_DEMAND("Billing active energy cum. max demand", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.BILLINGPERIOD).aggregate(Aggregate.MAXIMUM).accumulate(Accumulation.CUMULATIVE).flow(FlowDirection.FORWARD).measure(MeasurementKind.DEMAND).in(ReadingTypeUnit.WATT)),

        DAILY_ACTIVE_ENERGY_CUM_MAX_SUPPLY("Active energy cum. max supply", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.DAILY).aggregate(Aggregate.MAXIMUM).accumulate(Accumulation.CUMULATIVE).flow(FlowDirection.REVERSE).measure(MeasurementKind.DEMAND).in(ReadingTypeUnit.WATT)),
        MONTHLY_ACTIVE_ENERGY_CUM_MAX_SUPPLY("Active energy cum. max supply", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.MONTHLY).aggregate(Aggregate.MAXIMUM).accumulate(Accumulation.CUMULATIVE).flow(FlowDirection.REVERSE).measure(MeasurementKind.DEMAND).in(ReadingTypeUnit.WATT)),
        YEARLY_ACTIVE_ENERGY_CUM_MAX_SUPPLY("Active energy cum. max supply", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.YEARLY).aggregate(Aggregate.MAXIMUM).accumulate(Accumulation.CUMULATIVE).flow(FlowDirection.REVERSE).measure(MeasurementKind.DEMAND).in(ReadingTypeUnit.WATT)),
        BILLING_ACTIVE_ENERGY_CUM_MAX_SUPPLY("Billing active energy cum. max supply", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.BILLINGPERIOD).aggregate(Aggregate.MAXIMUM).accumulate(Accumulation.CUMULATIVE).flow(FlowDirection.REVERSE).measure(MeasurementKind.DEMAND).in(ReadingTypeUnit.WATT)),;

        final String name;
        final ReadingTypeCodeBuilder readingTypeCodeBuilder;

        ReadingTypesForDemands(String name, ReadingTypeCodeBuilder readingTypeCodeBuilder) {
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
        public boolean needsTOUExpansion() {
            return true;
        }

        @Override
        public boolean needsPhaseExpansion() {
            return true;
        }

        @Override
        public boolean needsMetricMultiplierExpansion() {
            return true;
        }
    }

    private enum ReadingTypesForBilling implements ReadingTypeTemplate {

        BILLING_PERIOD_ACTIVE_ENERGY_IMPORT("Billing period A+", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.BILLINGPERIOD).accumulate(Accumulation.SUMMATION).flow(FlowDirection.FORWARD).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.WATTHOUR)),
        BILLING_PERIOD_ACTIVE_ENERGY_EXPORT("Billing period A-", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.BILLINGPERIOD).accumulate(Accumulation.SUMMATION).flow(FlowDirection.REVERSE).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.WATTHOUR)),
        BILLING_PERIOD_REACTIVE_ENERGY_IMPORT("Billing period reactive energy+", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.BILLINGPERIOD).accumulate(Accumulation.SUMMATION).flow(FlowDirection.LAGGING).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)),
        BILLING_PERIOD_REACTIVE_ENERGY_EXPORT("Billing period reactive energy-", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.BILLINGPERIOD).accumulate(Accumulation.SUMMATION).flow(FlowDirection.LEADING).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)),
        BILLING_PERIOD_INDUCTIVE_ENERGY_Q1("Billing period inductive energy Q1", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.BILLINGPERIOD).accumulate(Accumulation.SUMMATION).flow(FlowDirection.Q1).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)),
        BILLING_PERIOD_INDUCTIVE_ENERGY_Q2("Billing period inductive energy Q2", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.BILLINGPERIOD).accumulate(Accumulation.SUMMATION).flow(FlowDirection.Q2).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)),
        BILLING_PERIOD_INDUCTIVE_ENERGY_Q3("Billing period inductive energy Q3", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.BILLINGPERIOD).accumulate(Accumulation.SUMMATION).flow(FlowDirection.Q3).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)),
        BILLING_PERIOD_INDUCTIVE_ENERGY_Q4("Billing period inductive energy Q4", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).period(MacroPeriod.BILLINGPERIOD).accumulate(Accumulation.SUMMATION).flow(FlowDirection.Q4).measure(MeasurementKind.ENERGY).in(ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)),;

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
        public boolean needsTOUExpansion() {
            return true;
        }

        @Override
        public boolean needsPhaseExpansion() {
            return true;
        }

        @Override
        public boolean needsMetricMultiplierExpansion() {
            return true;
        }
    }

    private enum ReadingTypesForOther implements ReadingTypeTemplate {

        APPARENT_POWER_IMPORT("Apparent power+", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).aggregate(Aggregate.AVERAGE).accumulate(Accumulation.INDICATING).flow(FlowDirection.FORWARD).measure(MeasurementKind.POWER).in(ReadingTypeUnit.VOLTAMPERE)) {
            @Override
            public boolean needsTOUExpansion() {
                return true;
            }
        },
        APPARENT_POWER_EXPORT("Apparent power-", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).aggregate(Aggregate.AVERAGE).accumulate(Accumulation.INDICATING).flow(FlowDirection.REVERSE).measure(MeasurementKind.POWER).in(ReadingTypeUnit.VOLTAMPERE)) {
            @Override
            public boolean needsTOUExpansion() {
                return true;
            }
        },
        ACTIVE_POWER_FACTOR_IMPORT("Active power factor+", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).accumulate(Accumulation.INSTANTANEOUS).flow(FlowDirection.FORWARD).measure(MeasurementKind.POWERFACTOR)) {
            @Override
            public boolean needsMetricMultiplierExpansion() {
                return false;
            }
        },
        ACTIVE_POWER_FACTOR_EXPORT("Active power factor-", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).accumulate(Accumulation.INSTANTANEOUS).flow(FlowDirection.REVERSE).measure(MeasurementKind.POWERFACTOR)) {
            @Override
            public boolean needsMetricMultiplierExpansion() {
                return false;
            }
        },
        ACTIVE_CURRENT_IMPORT("Active current+", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).accumulate(Accumulation.INSTANTANEOUS).flow(FlowDirection.FORWARD).measure(MeasurementKind.CURRENT).in(ReadingTypeUnit.AMPERE)),
        ACTIVE_CURRENT_EXPORt("Active current-", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).accumulate(Accumulation.INSTANTANEOUS).flow(FlowDirection.REVERSE).measure(MeasurementKind.CURRENT).in(ReadingTypeUnit.AMPERE)),
        ACTIVE_POWER_IMPORT("Active power+", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).accumulate(Accumulation.INSTANTANEOUS).flow(FlowDirection.FORWARD).measure(MeasurementKind.POWER).in(ReadingTypeUnit.WATT)),
        ACTIVE_POWER_EXPORT("Active power-", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).accumulate(Accumulation.INSTANTANEOUS).flow(FlowDirection.REVERSE).measure(MeasurementKind.POWER).in(ReadingTypeUnit.WATT)),
        ACTIVE_VOLTAGE("Active voltage", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).accumulate(Accumulation.INSTANTANEOUS).measure(MeasurementKind.VOLTAGE).in(ReadingTypeUnit.VOLT)),;
        final String name;
        final ReadingTypeCodeBuilder readingTypeCodeBuilder;

        ReadingTypesForOther(String name, ReadingTypeCodeBuilder readingTypeCodeBuilder) {
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
        public boolean needsPhaseExpansion() {
            return true;
        }

        @Override
        public boolean needsMetricMultiplierExpansion() {
            return true;
        }
    }


    ReadingTypeGeneratorForElectricity() {
        super();
    }

    @Override
    Stream<ReadingTypeTemplate> getReadingTypeTemplates() {
        return Stream.of(ReadingTypesForPossibleLoadProfileUsage.values(),
                ReadingTypesForPossibleDailyMonthlyLoadProfileUsage.values(),
                ReadingTypesForDemands.values(),
                ReadingTypesForBilling.values(),
                ReadingTypesForOther.values()).flatMap(Arrays::stream);
    }

}
