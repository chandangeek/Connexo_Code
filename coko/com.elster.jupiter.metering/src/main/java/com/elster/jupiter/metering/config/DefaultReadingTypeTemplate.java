package com.elster.jupiter.metering.config;

import com.elster.jupiter.nls.TranslationKey;

public enum DefaultReadingTypeTemplate {
    A_PLUS(TemplateTranslation.A_PLUS),
    A_MINUS(TemplateTranslation.A_MINUS),
    INDUCTIVE_ENERGY_Q1(TemplateTranslation.INDUCTIVE_ENERGY_Q1),
    INDUCTIVE_ENERGY_Q2(TemplateTranslation.INDUCTIVE_ENERGY_Q2),
    INDUCTIVE_ENERGY_Q3(TemplateTranslation.INDUCTIVE_ENERGY_Q3),
    INDUCTIVE_ENERGY_Q4(TemplateTranslation.INDUCTIVE_ENERGY_Q4),
    REACTIVE_ENERGY_PLUS(TemplateTranslation.REACTIVE_ENERGY_PLUS),
    Reactive_ENERGY_MINUS(TemplateTranslation.Reactive_ENERGY_MINUS),
    ACTIVE_POWER_FACTOR(TemplateTranslation.ACTIVE_POWER_FACTOR),
    AVERAGE_POWER_FACTOR(TemplateTranslation.AVERAGE_POWER_FACTOR),
    ACTIVE_POWER(TemplateTranslation.ACTIVE_POWER),
    AVERAGE_POWER(TemplateTranslation.AVERAGE_POWER),
    ACTIVE_CURRENT(TemplateTranslation.ACTIVE_CURRENT),
    AVERAGE_CURRENT(TemplateTranslation.AVERAGE_CURRENT),
    ACTIVE_VOLTAGE(TemplateTranslation.ACTIVE_VOLTAGE),
    AVERAGE_VOLTAGE(TemplateTranslation.AVERAGE_VOLTAGE),
    ACTIVE_ENERGY_MAX_DEMAND(TemplateTranslation.ACTIVE_ENERGY_MAX_DEMAND),
    ACTIVE_ENERGY_MAX_SUPPLY(TemplateTranslation.ACTIVE_ENERGY_MAX_SUPPLY),
    REACTIVE_ENERGY_MAX_DEMAND(TemplateTranslation.REACTIVE_ENERGY_MAX_DEMAND),
    ACTIVE_ENERGY_CUM_MAX_DEMAND(TemplateTranslation.ACTIVE_ENERGY_CUM_MAX_DEMAND),
    GAS_VOLUME(TemplateTranslation.GAS_VOLUME),
    GAS_VOLUME_NORM(TemplateTranslation.GAS_VOLUME_NORM),
    GAS_TEMPERATURE(TemplateTranslation.GAS_TEMPERATURE),
    GAS_PRESSURE(TemplateTranslation.GAS_PRESSURE),
    GAS_FLOW(TemplateTranslation.GAS_FLOW),
    GAS_FLOW_BILLING(TemplateTranslation.GAS_FLOW_BILLING),
    GAS_NORMALIZED_FLOW(TemplateTranslation.GAS_NORMALIZED_FLOW),
    GAS_CONVERSION_FACTOR(TemplateTranslation.GAS_CONVERSION_FACTOR),
    WATER_VOLUME(TemplateTranslation.WATER_VOLUME),
    WATER_VOLUME_BACKFLOW(TemplateTranslation.WATER_VOLUME_BACKFLOW),
    WATER_FLOW(TemplateTranslation.WATER_FLOW),
    DELTA_A_PLUS(TemplateTranslation.DELTA_A_PLUS),
    BATTERY_STATUS(TemplateTranslation.BATTERY_STATUS),;

    DefaultReadingTypeTemplate(TemplateTranslation nameTranslation) {
        this.nameTranslation = nameTranslation;
    }

    private TemplateTranslation nameTranslation;

    public TranslationKey getNameTranslation() {
        return this.nameTranslation;
    }

    public enum TemplateTranslation implements TranslationKey {
        A_PLUS("reading.type.template.a_plus", "A+"),
        A_MINUS("reading.type.template.a_minus", "A-"),
        INDUCTIVE_ENERGY_Q1("reading.type.template.inductive_energy_q1", "Inductive energy Q1"),
        INDUCTIVE_ENERGY_Q2("reading.type.template.inductive_energy_q2", "Inductive energy Q2"),
        INDUCTIVE_ENERGY_Q3("reading.type.template.inductive_energy_q3", "Inductive energy Q3"),
        INDUCTIVE_ENERGY_Q4("reading.type.template.inductive_energy_q4", "Inductive energy Q4"),
        REACTIVE_ENERGY_PLUS("reading.type.template.reactive_energy_plus", "Reactive energy+"),
        Reactive_ENERGY_MINUS("reading.type.template.reactive_energy_minus", "Reactive energy-"),
        ACTIVE_POWER_FACTOR("reading.type.template.active_power_factor", "Active Power Factor"),
        AVERAGE_POWER_FACTOR("reading.type.template.average_power_factor", "Average Power Factor"),
        ACTIVE_POWER("reading.type.template.active_power", "Active Power"),
        AVERAGE_POWER("reading.type.template.average_power", "Average Power"),
        ACTIVE_CURRENT("reading.type.template.active_current", "Active Current"),
        AVERAGE_CURRENT("reading.type.template.average_current", "Average Current"),
        ACTIVE_VOLTAGE("reading.type.template.active_voltage", "Active Voltage"),
        AVERAGE_VOLTAGE("reading.type.template.average_voltage", "Average Voltage"),
        ACTIVE_ENERGY_MAX_DEMAND("reading.type.template.active_energy_max_demand", "Active energy max demand"),
        ACTIVE_ENERGY_MAX_SUPPLY("reading.type.template.active_energy_max_supply", "Active energy max supply"),
        REACTIVE_ENERGY_MAX_DEMAND("reading.type.template.reactive_energy_max_demand", "Reactive energy max demand"),
        ACTIVE_ENERGY_CUM_MAX_DEMAND("reading.type.template.active_energy_cum_max_demand", "Active energy cum. max demand"),
        GAS_VOLUME("reading.type.template.gas_volume", "Gas volume"),
        GAS_VOLUME_NORM("reading.type.template.gas_volume_norm", "Gas volume norm"),
        GAS_TEMPERATURE("reading.type.template.gas_temperature", "Gas temperature"),
        GAS_PRESSURE("reading.type.template.gas_pressure", "Gas pressure"),
        GAS_FLOW("reading.type.template.gas_flow", "Gas flow"),
        GAS_FLOW_BILLING("reading.type.template.gas_flow_irregular", "Gas flow irregular"),
        GAS_NORMALIZED_FLOW("reading.type.template.gas_normalized_flow", "Gas normalized flow"),
        GAS_CONVERSION_FACTOR("reading.type.template.gas_conversion_factor", "Gas conversion factor"),
        WATER_VOLUME("reading.type.template.water_volume", "Water volume"),
        WATER_VOLUME_BACKFLOW("reading.type.template.water_volume_backflow", "Water volume backflow"),
        WATER_FLOW("reading.type.template.water_flow", "Water flow"),
        DELTA_A_PLUS("reading.type.template.delta_a_plus", "Delta A+"),
        BATTERY_STATUS("reading.type.template.battery_status", "Battery Status"),;

        TemplateTranslation(String key, String defaultFormat) {
            this.key = key;
            this.defaultFormat = defaultFormat;
        }

        private String key;
        private String defaultFormat;

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getDefaultFormat() {
            return defaultFormat;
        }
    }
}