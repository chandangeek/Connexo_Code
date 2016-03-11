package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingTypeTemplate;
import com.elster.jupiter.metering.ReadingTypeTemplateAttributeName;

public class ReadingTypeTemplateInstaller {

    private final MeteringService meteringService;

    public ReadingTypeTemplateInstaller(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    public void install() {
        installElectricityTemplates();
        installGasTemplates();
        installWaterTemplates();
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println("");
    }

    private void installElectricityTemplates() {
        new Template(meteringService, "A+")
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.FORWARD)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED, Commodity.ELECTRICITY_PRIMARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.ENERGY)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.WATTHOUR)
                .done();
        new Template(meteringService, "A-")
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.REVERSE)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED, Commodity.ELECTRICITY_PRIMARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.ENERGY)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.WATTHOUR)
                .done();
        new Template(meteringService, "Inductive energy Q1")
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.Q1)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED, Commodity.ELECTRICITY_PRIMARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.ENERGY)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)
                .done();
        new Template(meteringService, "Inductive energy Q2")
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.Q2)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED, Commodity.ELECTRICITY_PRIMARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.ENERGY)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)
                .done();
        new Template(meteringService, "Inductive energy Q3")
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.Q3)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED, Commodity.ELECTRICITY_PRIMARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.ENERGY)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)
                .done();
        new Template(meteringService, "Inductive energy Q4")
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.Q4)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED, Commodity.ELECTRICITY_PRIMARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.ENERGY)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)
                .done();
        new Template(meteringService, "Reactive energy+")
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.LAGGING)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED, Commodity.ELECTRICITY_PRIMARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.ENERGY)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)
                .done();
        new Template(meteringService, "Reactive energy-")
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.LEADING)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED, Commodity.ELECTRICITY_PRIMARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.ENERGY)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)
                .done();
        new Template(meteringService, "Active Power Factor")
                .withValues(ReadingTypeTemplateAttributeName.MACRO_PERIOD, MacroPeriod.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.TIME, TimeAttribute.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.INSTANTANEOUS)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.FORWARD, FlowDirection.REVERSE)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED, Commodity.ELECTRICITY_PRIMARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.POWERFACTOR)
                .withValues(ReadingTypeTemplateAttributeName.METRIC_MULTIPLIER, MetricMultiplier.ZERO)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.NOTAPPLICABLE)
                .done();
        new Template(meteringService, "Average Power Factor")
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.AVERAGE)
                .withValues(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.INDICATING)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.FORWARD, FlowDirection.REVERSE)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED, Commodity.ELECTRICITY_PRIMARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.POWERFACTOR)
                .withValues(ReadingTypeTemplateAttributeName.METRIC_MULTIPLIER, MetricMultiplier.ZERO)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.NOTAPPLICABLE)
                .done();
        new Template(meteringService, "Active Power")
                .withValues(ReadingTypeTemplateAttributeName.MACRO_PERIOD, MacroPeriod.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.TIME, TimeAttribute.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.INSTANTANEOUS)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.FORWARD, FlowDirection.REVERSE)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED, Commodity.ELECTRICITY_PRIMARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.POWER)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.WATT)
                .done();
        new Template(meteringService, "Average Power")
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.AVERAGE)
                .withValues(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.INDICATING)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.FORWARD, FlowDirection.REVERSE)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED, Commodity.ELECTRICITY_PRIMARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.POWER)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.WATT)
                .done();
        new Template(meteringService, "Active Current")
                .withValues(ReadingTypeTemplateAttributeName.MACRO_PERIOD, MacroPeriod.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.TIME, TimeAttribute.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.INSTANTANEOUS)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.FORWARD, FlowDirection.REVERSE)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.CURRENT)
                .withValues(ReadingTypeTemplateAttributeName.TIME_OF_USE, 0)
                .withValues(ReadingTypeTemplateAttributeName.CRITICAL_PEAK_PERIOD, 0)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.AMPERE)
                .done();
        new Template(meteringService, "Average Current")
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.AVERAGE)
                .withValues(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.INDICATING)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.FORWARD, FlowDirection.REVERSE)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.CURRENT)
                .withValues(ReadingTypeTemplateAttributeName.TIME_OF_USE, 0)
                .withValues(ReadingTypeTemplateAttributeName.CRITICAL_PEAK_PERIOD, 0)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.AMPERE)
                .done();
        new Template(meteringService, "Active Voltage")
                .withValues(ReadingTypeTemplateAttributeName.MACRO_PERIOD, MacroPeriod.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.TIME, TimeAttribute.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.INSTANTANEOUS)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.FORWARD, FlowDirection.REVERSE)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.RMSVOLTAGE)
                .withValues(ReadingTypeTemplateAttributeName.TIME_OF_USE, 0)
                .withValues(ReadingTypeTemplateAttributeName.CRITICAL_PEAK_PERIOD, 0)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.VOLT)
                .done();
        new Template(meteringService, "Average Voltage")
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.AVERAGE)
                .withValues(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.INDICATING)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.FORWARD, FlowDirection.REVERSE)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.RMSVOLTAGE)
                .withValues(ReadingTypeTemplateAttributeName.TIME_OF_USE, 0)
                .withValues(ReadingTypeTemplateAttributeName.CRITICAL_PEAK_PERIOD, 0)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.VOLT)
                .done();
        new Template(meteringService, "Active energy max demand")
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.MAXIMUM)
                .withValues(ReadingTypeTemplateAttributeName.TIME, TimeAttribute.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.LATCHINGQUANTITY)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.FORWARD)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED, Commodity.ELECTRICITY_PRIMARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.DEMAND)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.WATT)
                .done();
        new Template(meteringService, "Active energy max supply")
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.MAXIMUM)
                .withValues(ReadingTypeTemplateAttributeName.TIME, TimeAttribute.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.LATCHINGQUANTITY)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.REVERSE)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED, Commodity.ELECTRICITY_PRIMARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.DEMAND)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.WATT)
                .done();
        new Template(meteringService, "Reactive energy max demand")
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.MAXIMUM)
                .withValues(ReadingTypeTemplateAttributeName.TIME, TimeAttribute.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.LATCHINGQUANTITY)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.LAGGING, FlowDirection.LEADING)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED, Commodity.ELECTRICITY_PRIMARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.DEMAND)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.VOLTAMPEREREACTIVE)
                .done();
        new Template(meteringService, "Active energy cum. max demand")
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.MAXIMUM)
                .withValues(ReadingTypeTemplateAttributeName.TIME, TimeAttribute.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.CUMULATIVE)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.FORWARD, FlowDirection.REVERSE)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED, Commodity.ELECTRICITY_PRIMARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.DEMAND)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.WATT)
                .done();
    }

    private void installGasTemplates() {
        new Template(meteringService, "Gas volume")
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.FORWARD)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.NATURALGAS)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.VOLUME)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.CUBICMETER, ReadingTypeUnit.LITRE)
                .done();
        new Template(meteringService, "Gas volume norm")
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.FORWARD)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.NATURALGAS)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.VOLUME)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.LITRECOMPENSATED, ReadingTypeUnit.CUBICMETERCOMPENSATED)
                .done();
        new Template(meteringService, "Gas temperature")
                .withValues(ReadingTypeTemplateAttributeName.MACRO_PERIOD, MacroPeriod.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.TIME, TimeAttribute.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.NATURALGAS)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.TEMPERATURE)
                .withValues(ReadingTypeTemplateAttributeName.METRIC_MULTIPLIER, MetricMultiplier.ZERO)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.KELVIN, ReadingTypeUnit.DEGREESCELSIUS, ReadingTypeUnit.DEGREESFAHRENHEIT)
                .done();
        new Template(meteringService, "Gas pressure")
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.AVERAGE)
                .withValues(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.INDICATING)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.NATURALGAS)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.VOLUME)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.PASCAL, ReadingTypeUnit.BAR)
                .done();
        new Template(meteringService, "Gas flow")
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.AVERAGE)
                .withValues(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.INDICATING)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.NATURALGAS)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.VOLUME)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.CUBICMETERPERHOUR)
                .done();
        new Template(meteringService, "Gas normalized flow")
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.AVERAGE)
                .withValues(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.INDICATING)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.NATURALGAS)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.VOLUME)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.CUBICMETERPERHOURCOMPENSATED)
                .done();
        new Template(meteringService, "Gas conversion factor")
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.AVERAGE)
                .withValues(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.INDICATING)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.NATURALGAS)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.VOLUME)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.WATTHOURPERCUBICMETER)
                .done();
    }

    private void installWaterTemplates() {
        new Template(meteringService, "Water volume")
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.FORWARD)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.POTABLEWATER)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.VOLUME)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.CUBICMETER, ReadingTypeUnit.LITRE)
                .done();
        new Template(meteringService, "Water volume backflow")
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.REVERSE)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.POTABLEWATER)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.VOLUME)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.CUBICMETER, ReadingTypeUnit.LITRE)
                .done();
        new Template(meteringService, "Water flow")
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.AVERAGE)
                .withValues(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.INDICATING)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.NATURALGAS)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.VOLUME)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.CUBICMETERPERHOUR)
                .done();
    }

    private static class Template {

        private final ReadingTypeTemplate template;
        private ReadingTypeTemplate.ReadingTypeTemplateUpdater updater;

        Template(MeteringService meteringService, String name) {
            this.template = meteringService.createReadingTypeTemplate(name);
            this.updater = this.template.updater();
        }

        @SuppressWarnings("unchecked")
        <T> Integer convertToInteger(ReadingTypeTemplateAttributeName.ReadingTypeAttribute<?> definition, T value) {
            if (value == null) {
                return null;
            }
            if (!value.getClass().isAssignableFrom(definition.getType())) {
                throw new IllegalArgumentException("Values must be " + definition.getType());
            }
            return ((ReadingTypeTemplateAttributeName.ReadingTypeAttribute<T>) definition).getValueToCodeConverter().apply(value);
        }

        <T> Template withValues(ReadingTypeTemplateAttributeName attr, T... values) {
            if (values != null && values.length > 0) {
                Integer[] possibleValues = new Integer[values.length];
                for (int i = 0; i < values.length; i++) {
                    possibleValues[i] = convertToInteger(attr.getDefinition(), values[i]);
                }
                this.updater.setAttribute(attr, values.length == 1 ? possibleValues[0] : null, possibleValues);
            }
            return this;
        }

        void done() {
            this.updater.done();
            System.out.println(this.template);
        }
    }
}
