package com.elster.jupiter.prepayment.impl.installer;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;

import java.util.stream.Stream;


/**
 * Generates specific ReadingTypes for the Prepayment application.<br/>
 * The generator contains several enums which serve as a sort of artificial <i>grouping</i>.
 * Each enum implements the {@link AbstractReadingTypeGenerator.ReadingTypeTemplate} so the actual generator can generate ReadingTypes based on the templates.
 * Each template can indicate if it needs expansion for a certain attribute.
 */
public class ReadingTypeGeneratorForPrepayment extends AbstractReadingTypeGenerator {

    private enum ReadingTypesForOther implements ReadingTypeTemplate {

        APPARENT_POWER_IMPORT("Apparent power+", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .aggregate(Aggregate.AVERAGE)
                .accumulate(Accumulation.INDICATING)
                .flow(FlowDirection.FORWARD)
                .measure(MeasurementKind.POWER)
                .in(ReadingTypeUnit.VOLTAMPERE)) {
        },
        APPARENT_POWER_EXPORT("Apparent power-", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .aggregate(Aggregate.AVERAGE)
                .accumulate(Accumulation.INDICATING)
                .flow(FlowDirection.REVERSE)
                .measure(MeasurementKind.POWER)
                .in(ReadingTypeUnit.VOLTAMPERE)) {
        },
        ACTIVE_POWER_FACTOR_IMPORT("Active power factor+", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .accumulate(Accumulation.INSTANTANEOUS)
                .flow(FlowDirection.FORWARD)
                .measure(MeasurementKind.POWERFACTOR)) {
            @Override
            public boolean needsMetricMultiplierExpansion() {
                return false;
            }
        },
        ACTIVE_POWER_FACTOR_EXPORT("Active power factor-", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .accumulate(Accumulation.INSTANTANEOUS)
                .flow(FlowDirection.REVERSE)
                .measure(MeasurementKind.POWERFACTOR)) {
            @Override
            public boolean needsMetricMultiplierExpansion() {
                return false;
            }
        },
        NEUTRAL_CURRENT_IMPORT("Active current+", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .accumulate(Accumulation.INSTANTANEOUS)
                .flow(FlowDirection.FORWARD)
                .measure(MeasurementKind.CURRENT)
                .phase(Phase.PHASEN)
                .in(ReadingTypeUnit.AMPERE)) {
            @Override
            public boolean needsPhaseExpansion() {
                return false;
            }
        },
        ACTIVE_CURRENT_IMPORT("Active current+", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .accumulate(Accumulation.INSTANTANEOUS)
                .flow(FlowDirection.FORWARD)
                .measure(MeasurementKind.CURRENT)
                .in(ReadingTypeUnit.AMPERE)),
        ACTIVE_CURRENT_EXPORt("Active current-", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .accumulate(Accumulation.INSTANTANEOUS)
                .flow(FlowDirection.REVERSE)
                .measure(MeasurementKind.CURRENT)
                .in(ReadingTypeUnit.AMPERE)),
        ACTIVE_POWER_IMPORT("Active power+", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .accumulate(Accumulation.INSTANTANEOUS)
                .flow(FlowDirection.FORWARD)
                .measure(MeasurementKind.POWER)
                .in(ReadingTypeUnit.WATT)),
        ACTIVE_POWER_EXPORT("Active power-", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .accumulate(Accumulation.INSTANTANEOUS)
                .flow(FlowDirection.REVERSE)
                .measure(MeasurementKind.POWER)
                .in(ReadingTypeUnit.WATT)),
        REACTIVE_POWER_IMPORT("Reactive power+", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .accumulate(Accumulation.INSTANTANEOUS)
                .flow(FlowDirection.FORWARD)
                .measure(MeasurementKind.POWER)
                .in(ReadingTypeUnit.VOLTAMPEREREACTIVE)),
        REACTIVE_POWER_EXPORT("Reactive power-", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .accumulate(Accumulation.INSTANTANEOUS)
                .flow(FlowDirection.REVERSE)
                .measure(MeasurementKind.POWER)
                .in(ReadingTypeUnit.VOLTAMPEREREACTIVE)),
        ACTIVE_VOLTAGE("Active voltage", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .accumulate(Accumulation.INSTANTANEOUS)
                .measure(MeasurementKind.VOLTAGE)
                .in(ReadingTypeUnit.VOLT)),
        FREQUENCY("Frequency", ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .accumulate(Accumulation.INSTANTANEOUS)
                .measure(MeasurementKind.FREQUENCY)
                .in(ReadingTypeUnit.HERTZ)),;

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


    ReadingTypeGeneratorForPrepayment() {
        super();
    }

    @Override
    Stream<ReadingTypeTemplate> getReadingTypeTemplates() {
        return Stream.of(ReadingTypesForOther.values());
    }
}