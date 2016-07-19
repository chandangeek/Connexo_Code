package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.config.ReadingTypeTemplate;
import com.elster.jupiter.metering.config.ReadingTypeTemplateAttributeName;

public class ReadingTypeTemplateInstaller {

    private final ServerMetrologyConfigurationService metrologyConfigurationService;

    public ReadingTypeTemplateInstaller(ServerMetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    public void install() {
        installElectricityTemplates();
        installGasTemplates();
        installWaterTemplates();
    }

    private void installElectricityTemplates() {
        new Template(metrologyConfigurationService, DefaultReadingTypeTemplate.A_PLUS)
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.FORWARD)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED, Commodity.ELECTRICITY_PRIMARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.ENERGY)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.WATTHOUR)
                .done();
        new Template(metrologyConfigurationService, DefaultReadingTypeTemplate.A_MINUS)
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.REVERSE)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED, Commodity.ELECTRICITY_PRIMARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.ENERGY)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.WATTHOUR)
                .done();
        new Template(metrologyConfigurationService, DefaultReadingTypeTemplate.INDUCTIVE_ENERGY_Q1)
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.Q1)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED, Commodity.ELECTRICITY_PRIMARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.ENERGY)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)
                .done();
        new Template(metrologyConfigurationService, DefaultReadingTypeTemplate.INDUCTIVE_ENERGY_Q2)
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.Q2)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED, Commodity.ELECTRICITY_PRIMARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.ENERGY)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)
                .done();
        new Template(metrologyConfigurationService, DefaultReadingTypeTemplate.INDUCTIVE_ENERGY_Q3)
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.Q3)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED, Commodity.ELECTRICITY_PRIMARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.ENERGY)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)
                .done();
        new Template(metrologyConfigurationService, DefaultReadingTypeTemplate.INDUCTIVE_ENERGY_Q4)
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.Q4)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED, Commodity.ELECTRICITY_PRIMARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.ENERGY)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)
                .done();
        new Template(metrologyConfigurationService, DefaultReadingTypeTemplate.REACTIVE_ENERGY_PLUS)
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.LAGGING)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED, Commodity.ELECTRICITY_PRIMARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.ENERGY)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)
                .done();
        new Template(metrologyConfigurationService, DefaultReadingTypeTemplate.Reactive_ENERGY_MINUS)
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.LEADING)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED, Commodity.ELECTRICITY_PRIMARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.ENERGY)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR)
                .done();
        new Template(metrologyConfigurationService, DefaultReadingTypeTemplate.ACTIVE_POWER_FACTOR)
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
        new Template(metrologyConfigurationService, DefaultReadingTypeTemplate.AVERAGE_POWER_FACTOR)
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.AVERAGE)
                .withValues(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.INDICATING)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.FORWARD, FlowDirection.REVERSE)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED, Commodity.ELECTRICITY_PRIMARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.POWERFACTOR)
                .withValues(ReadingTypeTemplateAttributeName.METRIC_MULTIPLIER, MetricMultiplier.ZERO)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.NOTAPPLICABLE)
                .done();
        new Template(metrologyConfigurationService, DefaultReadingTypeTemplate.ACTIVE_POWER)
                .withValues(ReadingTypeTemplateAttributeName.MACRO_PERIOD, MacroPeriod.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.TIME, TimeAttribute.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.INSTANTANEOUS)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.FORWARD, FlowDirection.REVERSE)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED, Commodity.ELECTRICITY_PRIMARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.POWER)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.WATT)
                .done();
        new Template(metrologyConfigurationService, DefaultReadingTypeTemplate.AVERAGE_POWER)
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.AVERAGE)
                .withValues(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.INDICATING)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.FORWARD, FlowDirection.REVERSE)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED, Commodity.ELECTRICITY_PRIMARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.POWER)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.WATT)
                .done();
        new Template(metrologyConfigurationService, DefaultReadingTypeTemplate.ACTIVE_CURRENT)
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
        new Template(metrologyConfigurationService, DefaultReadingTypeTemplate.AVERAGE_CURRENT)
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.AVERAGE)
                .withValues(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.INDICATING)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.FORWARD, FlowDirection.REVERSE)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.CURRENT)
                .withValues(ReadingTypeTemplateAttributeName.TIME_OF_USE, 0)
                .withValues(ReadingTypeTemplateAttributeName.CRITICAL_PEAK_PERIOD, 0)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.AMPERE)
                .done();
        new Template(metrologyConfigurationService, DefaultReadingTypeTemplate.ACTIVE_VOLTAGE)
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
        new Template(metrologyConfigurationService, DefaultReadingTypeTemplate.AVERAGE_VOLTAGE)
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.AVERAGE)
                .withValues(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.INDICATING)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.FORWARD, FlowDirection.REVERSE)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.RMSVOLTAGE)
                .withValues(ReadingTypeTemplateAttributeName.TIME_OF_USE, 0)
                .withValues(ReadingTypeTemplateAttributeName.CRITICAL_PEAK_PERIOD, 0)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.VOLT)
                .done();
        new Template(metrologyConfigurationService, DefaultReadingTypeTemplate.ACTIVE_ENERGY_MAX_DEMAND)
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.MAXIMUM)
                .withValues(ReadingTypeTemplateAttributeName.TIME, TimeAttribute.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.LATCHINGQUANTITY)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.FORWARD)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED, Commodity.ELECTRICITY_PRIMARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.DEMAND)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.WATT)
                .done();
        new Template(metrologyConfigurationService, DefaultReadingTypeTemplate.ACTIVE_ENERGY_MAX_SUPPLY)
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.MAXIMUM)
                .withValues(ReadingTypeTemplateAttributeName.TIME, TimeAttribute.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.LATCHINGQUANTITY)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.REVERSE)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED, Commodity.ELECTRICITY_PRIMARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.DEMAND)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.WATT)
                .done();
        new Template(metrologyConfigurationService, DefaultReadingTypeTemplate.REACTIVE_ENERGY_MAX_DEMAND)
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.MAXIMUM)
                .withValues(ReadingTypeTemplateAttributeName.TIME, TimeAttribute.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.LATCHINGQUANTITY)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.LAGGING, FlowDirection.LEADING)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.ELECTRICITY_SECONDARY_METERED, Commodity.ELECTRICITY_PRIMARY_METERED)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.DEMAND)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.VOLTAMPEREREACTIVE)
                .done();
        new Template(metrologyConfigurationService, DefaultReadingTypeTemplate.ACTIVE_ENERGY_CUM_MAX_DEMAND)
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
        new Template(metrologyConfigurationService, DefaultReadingTypeTemplate.GAS_VOLUME)
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.FORWARD)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.NATURALGAS)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.VOLUME)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.CUBICMETER, ReadingTypeUnit.LITRE)
                .done();
        new Template(metrologyConfigurationService, DefaultReadingTypeTemplate.GAS_VOLUME_NORM)
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.FORWARD)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.NATURALGAS)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.VOLUME)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.LITRECOMPENSATED, ReadingTypeUnit.CUBICMETERCOMPENSATED)
                .done();
        new Template(metrologyConfigurationService, DefaultReadingTypeTemplate.GAS_TEMPERATURE)
                .withValues(ReadingTypeTemplateAttributeName.MACRO_PERIOD, MacroPeriod.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.TIME, TimeAttribute.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.NATURALGAS)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.TEMPERATURE)
                .withValues(ReadingTypeTemplateAttributeName.METRIC_MULTIPLIER, MetricMultiplier.ZERO)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.KELVIN, ReadingTypeUnit.DEGREESCELSIUS, ReadingTypeUnit.DEGREESFAHRENHEIT)
                .done();
        new Template(metrologyConfigurationService, DefaultReadingTypeTemplate.GAS_PRESSURE)
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.AVERAGE)
                .withValues(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.INDICATING)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.NATURALGAS)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.VOLUME)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.PASCAL, ReadingTypeUnit.BAR)
                .done();
        new Template(metrologyConfigurationService, DefaultReadingTypeTemplate.GAS_FLOW)
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.AVERAGE)
                .withValues(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.INDICATING)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.NATURALGAS)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.VOLUME)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.CUBICMETERPERHOUR)
                .done();
        new Template(metrologyConfigurationService, DefaultReadingTypeTemplate.GAS_NORMALIZED_FLOW)
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.AVERAGE)
                .withValues(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.INDICATING)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.NATURALGAS)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.VOLUME)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.CUBICMETERPERHOURCOMPENSATED)
                .done();
        new Template(metrologyConfigurationService, DefaultReadingTypeTemplate.GAS_CONVERSION_FACTOR)
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.AVERAGE)
                .withValues(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.INDICATING)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.NATURALGAS)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.VOLUME)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.WATTHOURPERCUBICMETER)
                .done();
    }

    private void installWaterTemplates() {
        new Template(metrologyConfigurationService, DefaultReadingTypeTemplate.WATER_VOLUME)
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.FORWARD)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.POTABLEWATER)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.VOLUME)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.CUBICMETER, ReadingTypeUnit.LITRE)
                .done();
        new Template(metrologyConfigurationService, DefaultReadingTypeTemplate.WATER_VOLUME_BACKFLOW)
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.REVERSE)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.POTABLEWATER)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.VOLUME)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.CUBICMETER, ReadingTypeUnit.LITRE)
                .done();
        new Template(metrologyConfigurationService, DefaultReadingTypeTemplate.WATER_FLOW)
                .withValues(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.AVERAGE)
                .withValues(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.INDICATING)
                .withValues(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.NOTAPPLICABLE)
                .withValues(ReadingTypeTemplateAttributeName.COMMODITY, Commodity.POTABLEWATER)
                .withValues(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.VOLUME)
                .withValues(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.CUBICMETERPERHOUR)
                .done();
    }

    private static class Template {

        private final ReadingTypeTemplate.ReadingTypeTemplateAttributeSetter setter;

        Template(ServerMetrologyConfigurationService metrologyConfigurationService, DefaultReadingTypeTemplate defaultTemplate) {
            this.setter = metrologyConfigurationService.createReadingTypeTemplate(defaultTemplate);
        }

        @SafeVarargs
        final <T> Template withValues(ReadingTypeTemplateAttributeName attr, T... values) {
            if (values != null && values.length > 0) {
                Integer[] possibleValues = new Integer[values.length];
                for (int i = 0; i < values.length; i++) {
                    possibleValues[i] = ReadingTypeTemplateAttributeName.getCodeFromAttributeValue(attr.getDefinition(), values[i]);
                }
                this.setter.setAttribute(attr, values.length == 1 ? possibleValues[0] : null, possibleValues);
            }
            return this;
        }

        void done() {
            this.setter.done();
        }
    }

}