/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cps.impl.metrology;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {
    CPS_DOMAIN_NAME(UsagePoint.class.getName(), "Usage point"),

    CPS_GENERAL_SIMPLE_NAME("usage.point.cps.general.name", "General"),
    CPS_GENERAL_PROPERTIES_PREPAY("usage.point.cps.general.properties.general.prepay", "Prepay"),
    CPS_GENERAL_PROPERTIES_PREPAY_DESCRIPTION("usage.point.cps.general.properties.prepay.descr", "Actively delivered on prepayment or deactivated, only relevant for SDP"),
    CPS_GENERAL_PROPERTIES_MARKET_CODE_SECTOR("usage.point.cps.general.properties.market.code.sector", "Market code sector"),
    CPS_GENERAL_PROPERTIES_MARKED_CODE_SECTOR_DESCRIPTION("usage.point.cps.general.properties.market.code.sector.descr", "UsagePointTechElCPS classification of a usage point"),
    CPS_GENERAL_PROPERTIES_METERING_POINT_TYPE("usage.point.cps.general.properties.metering.point.type", "Metering point type"),
    CPS_GENERAL_PROPERTIES_METERING_POINT_TYPE_DESCRIPTION("usage.point.cps.general.properties.metering.point.type.descr", "Geeft de richting van de aansluiting aan: levering, teruglevering of beide"),

    CPS_TECHNICAL_SIMPLE_NAME("usage.point.cps.technical.name", "Technical Information"),
    CPS_TECHNICAL_GWT_SIMPLE_NAME("usage.point.cps.technical.gwt.name", "Technical Information (Gas, Water, Thermal)"),
    CPS_TECHNICAL_ELECTRICITY_SIMPLE_NAME("usage.point.cps.technical.electricity.name", "Technical Information (Electricity)"),
    CPS_TECHNICAL_PROPERTIES_PIPE_SIZE("usage.point.cps.tech.pipe.size", "Pipe size"),
    CPS_TECHNICAL_PROPERTIES_PIPE_SIZE_DESCRIPTION("usage.point.cps.tech.pipe.size.descr", "The gas/water pipe diameter (cross sectional area can be calculated based on this information)"),
    CPS_TECHNICAL_PROPERTIES_CROSS_SECTIONAL_AREA("usage.point.cps.tech.cross.sectional.area", "Cross sectional area"),
    CPS_TECHNICAL_PROPERTIES_CROSS_SECTIONAL_AREA_DESCRIPTION("usage.point.cps.tech.cross.sectional.area.descr", "square mm of the electric cable"),
    CPS_TECHNICAL_PROPERTIES_CABLE_TYPE("usage.point.cps.tech.cable.type", "Cable type"),
    //CPS_TECHNICAL_PROPERTIES_CABLE_TYPE_DESCRIPTION("usage.point.cps.tech.cable.type.descr", ""),
    CPS_TECHNICAL_PROPERTIES_PIPE_TYPE("usage.point.cps.tech.pipe.type", "Pipe type"),
    CPS_TECHNICAL_PROPERTIES_PIPE_TYPE_DESCRIPTION("usage.point.cps.tech.pipe.type.descr", "Type of the material. Eg. Lead pipes or asbetos pipes were used in the past but need to be replaced due to health concerns."),
    CPS_TECHNICAL_PROPERTIES_CABLE_LOCATION("usage.point.cps.tech.cable.location", "Cable location"),
    CPS_TECHNICAL_PROPERTIES_CABLE_LOCATION_DESCRIPTION("usage.point.cps.tech.cable.location.descr", "x meters above/under ground"),
    CPS_TECHNICAL_PROPERTIES_PIPE_LOCATION("usage.point.cps.tech.pipe.location", "Pipe location"),
    CPS_TECHNICAL_PROPERTIES_PIPE_LOCATION_DESCRIPTION("usage.point.cps.tech.pipe.location.descr", "x meters above/under ground"),
    CPS_TECHNICAL_PROPERTIES_VOLTAGE_LEVEL("usage.point.cps.tech.voltage.level", "Voltage level"),
    //CPS_TECHNICAL_PROPERTIES_VOLTAGE_LEVEL_DESCRIPTION("usage.point.cps.tech.voltage.level.descr", ""),
    CPS_TECHNICAL_PROPERTIES_PRESSURE_LEVEL("usage.point.cps.tech.pressure.level", "Pressure level"),
    //CPS_TECHNICAL_PROPERTIES_PRESSURE_LEVEL_DESCRIPTION("usage.point.cps.tech.pressure.level.descr", ""),

    CPS_LICENSE_SIMPLE_NAME("usage.point.cps.license.name", "License (\"Keuring\")"),
    CPS_LICENSE_PROPERTIES_NUMBER("usage.point.cps.license.number", "Number"),
    CPS_LICENSE_PROPERTIES_EXPIRATION_DATE("usage.point.cps.license.expiration.date", "Expiration date"),
    CPS_LICENSE_PROPERTIES_CERTIFICATION_DOC("usage.point.cps.license.certification.doc", "Certification doc"),
    CPS_LICENSE_PROPERTIES_METERING_SCHEME("usage.point.cps.license.metering.scheme", "Metering scheme"),

    CPS_METROLOGY_GENERAL_SIMPLE_NAME("usage.point.cps.metrology.general.name", "Metrology general"),
    CPS_METROLOGY_GENERAL_PROPERTIES_READ_CYCLE("usage.point.cps.metrology.general.read.cycle", "ReadCycle"),
    // CPS_METROLOGY_GENERAL_PROPERTIES_READ_CYCLE_DESCRIPTION("usage.point.cps.metrology.general.read.cycle.descr", ""),
    CPS_METROLOGY_GENERAL_PROPERTIES_INFORMATION_FREQUENCY("usage.point.cps.metrology.general.information.frequency", "Information frequency"),
    CPS_METROLOGY_GENERAL_PROPERTIES_INFORMATION_FREQUENCY_DESCRIPTION("usage.point.cps.metrology.general.information.frequency.descr", "The frequency with which a metering point will be read for energy consumption information purposes."),

    CPS_TECHNICAL_INSTALLATION_SIMPLE_NAME("usage.point.cps.tech.installation.name", "Technical installation"),
    CPS_TECHNICAL_INSTALLATION_ALL_SIMPLE_NAME("usage.point.cps.tech.installation.all.name", "Technical installation (All categories)"),
    CPS_TECHNICAL_INSTALLATION_ELECTRICITY_SIMPLE_NAME("usage.point.cps.tech.installation.electricity.name", "Technical Installation (Electricity)"),
    CPS_TECHNICAL_INSTALLATION_EG_SIMPLE_NAME("usage.point.cps.tech.installation.eg.name", "Technical Installation (Electricity, Gas)"),
    CPS_TECHNICAL_INSTALLATION_DISTANCE_FROM_THE_SUBSTATION("usage.point.cps.tech.installation.distance.from.the.substation", "Distance from the substation"),
    CPS_TECHNICAL_INSTALLATION_DISTANCE_FROM_THE_SUBSTATION_DESCRIPTION("usage.point.cps.tech.installation.distance.from.the.substation.decr", "Used for billing purposes"),
    CPS_TECHNICAL_INSTALLATION_SUBSTATION("usage.point.cps.tech.installation.substation", "Substation"),
    //CPS_TECHNICAL_INSTALLATION_SUBSTATION_DESCRIPTION("usage.point.cps.tech.installation.substation.descr", ""),
    CPS_TECHNICAL_INSTALLATION_FEEDER("usage.point.cps.tech.installation.feeder", "Feeder"),
    //CPS_TECHNICAL_INSTALLATION_FEEDER_DESCRIPTION("usage.point.cps.tech.installation.name.feeder.descr", ""),
    CPS_TECHNICAL_INSTALLATION_UTILIZATION_CATEGORY("usage.point.cps.tech.installation.utilization.category", "Utilization category"),
    CPS_TECHNICAL_INSTALLATION_UTILIZATION_CATEGORY_DESCRIPTION("usage.point.cps.tech.installation.utilization.category.descr", "An indication of specific category of energy usage. Example: public lighting."),

    CPS_TECHNICAL_INSTALLATION_ELECTRICITY_GAS_SIMPLE_NAME("usage.point.tech.installation.electricity.gas.name", "Technical Installation (Electricity, gas)"),
    CPS_TECHNICAL_INSTALLATION_LOSS_FACTOR("usage.point.cps.tech.installation.loss.factor", "Loss factor"),
    CPS_TECHNICAL_INSTALLATION_LOSS_FACTOR_DESCRIPTION("usage.point.cps.tech.installation.loss.factor.descr", "Energy losses which need to be accounted for specified as a factor of a measured value,( i.e. % of the total energy measured for Cupper losses)\t"),

    CPS_DECENTRALIZED_PRODUCTION_SIMPLE_NAME("usage.point.cps.decentralized.prod.name", "Decentralized production"),
    CPS_DECENTRALIZED_PRODUCTION_INSTALLED_POWER("usage.point.cps.decentralized.prod.installed.power", "Installed power"),
    CPS_DECENTRALIZED_PRODUCTION_CONVERTER_POWER("usage.point.cps.decentralized.converter.power", "Converter power"),
    CPS_DECENTRALIZED_PRODUCTION_TYPE_OF_DECENTRALIZED_PRODUCTION("usage.point.cps.decentralized.prod.type.of.decentralized.prod", "Type of decentalized production"),
    CPS_DECENTRALIZED_PRODUCTION_COMMISSIONING_DATE("usage.point.cps.decentralized.prod.commissioning.date", "Commissioning date"),

    CPS_CONTRACTUAL_SIMPLE_NAME("usage.point.cps.contractual.name", "Contractual"),
    CPS_CONTRACTUAL_BILLING_CYCLE("usage.point.cps.contractual.billing.cycle", "Billing cycle"),

    CPS_SETTLEMENT_SIMPLE_NAME("usage.point.cps.settlement.name", "Settlement"),
    CPS_SETTLEMENT_AREA("usage.point.cps.settlement.area", "Settlement area"),
    CPS_SETTLEMENT_METHOD("usage.point.cps.settlement.method", "Settlement method"),
    CPS_SETTLEMENT_METHOD_DESCRIPTION("usage.point.cps.settlement.method.descr", "Geeft aan of het een profiled- of telemetrie- (non profiled) aansluiting is"),
    CPS_SETTLEMENT_GRIDFEE_TIMEFRAME("usage.point.cps.settlement.gridfee.timeframe", "Gridfee timeframe"),
    CPS_SETTLEMENT_GRIDFEE_TARIFFCODE("usage.point.cps.settlement.gridfee.tariffcode", "Gridfee tariffcode"),
    CPS_SETTLEMENT_GRIDFEE_TARIFFCODE_DESCRIPTION("usage.point.cps.settlement.gridfee.tariffcode.descr", "Code of the Gridfee tariff assigned."),

    CPS_CONVERTOR_SIMPLE_NAME("usage.point.cps.convertor.name", "Convertor"),
    CPS_CONVERTOR_SERIAL_NUMBER("usage.point.cps.convertor.serial.number", "Serial number"),
    CPS_CONVERTOR_SERIAL_NUMBER_DESCRIPTION("usage.point.cps.convertor.serial.number.descr", "The manufacturer’s  convertor serial number. l number of the corrector from which the  convertor read should be taken.\t"),
    CPS_CONVERTOR_NO_OF_CORRECTED_DIALS("usage.point.cps.convertor.no.of.corrected.dials", "NO_OF_CORRECTED_DIALS"),
    CPS_CONVERTOR_NO_OF_CORRECTED_DIALS_DESCRIPTION("usage.point.cps.convertor.no.of.corrected.dials.descr", "The number of dials or digits present on the  convertor which must be taken into account when recording the convertor corrected read"),
    CPS_CONVERTOR_NO_OF_UNCORRECTED_DIALS("usage.point.cps.convertor.no.of.uncorrected.dials", "NO_OF_UNCORRECTED_DIALS"),
    CPS_CONVERTOR_NO_OF_UNCORRECTED_DIALS_DESCRIPTION("usage.point.cps.convertor.no.of.uncorrected.dials.descr", "The uncorrected number of dials or digits for the convertor"),

    CPS_METER_GENERAL_SIMPLE_NAME("usage.point.cps.meter.general.name", "Meter general CAS"),
    CPS_METER_GENERAL_MANUFACTURER("usage.point.cps.meter.general.manufacturer", "Manufacturer"),
    CPS_METER_GENERAL_MANUFACTURER_DESCRIPTION("usage.point.cps.meter.general.manufacturer.descr", "The manufacturer of the device, eg. Elster"),
    CPS_METER_GENERAL_MODEL("usage.point.cps.meter.general.model", "Model"),
    CPS_METER_GENERAL_MODEL_DESCRIPTION("usage.point.cps.meter.general.model.descr", "Depends on manufacturer, eg. AS300\n"),
    CPS_METER_GENERAL_CLAZZ("usage.point.cps.meter.general.class", "Class"),
    CPS_METER_GENERAL_CLAZZ_DESCRIPTION("usage.point.cps.meter.general.class.descr", "accuracy of meter, eg. class 1\t\n"),

    CPS_METER_TECH_INFORMATION_GTW_SIMPLE_NAME("usage.point.cps.meter.tech.information.gtw.name", "Meter technical information(Gas, Water, Thermal)"),
    CPS_METER_TECH_INFORMATION_ELECTRICITY_SIMPLE_NAME("usage.point.cps.meter.tech.information.electricity.name", "Meter technical information(Electricity)"),
    CPS_METER_TECH_INFORMATION_ALL_SIMPLE_NAME("usage.point.cps.meter.tech.information.all.name", "Meter technical information (All categories)"),
    CPS_METER_TECH_RECESSED_LENGTH("usage.point.cps.meter.tech.information.recessed.length", "Inbouwlengte"),
    CPS_METER_TECH_RECESSED_LENGTH_DESCRIPTION("usage.point.cps.meter.tech.information.recessed.length.descr", "De inbouwmaat van de meter (bv. ¾ x 110)."),
    CPS_METER_TECH_CONNECTION_TYPE("usage.point.cps.meter.tech.information.connection.type", "Aansluittype"),
    CPS_METER_TECH_CONNECTION_TYPE_DESCRIPTION("usage.point.cps.meter.tech.information.connection.type.descr", "Het soort en maat van de fysieke koppeling."),
    CPS_METER_TECH_CONVERSION_METROLOGY("usage.point.cps.meter.tech.information.conversion.mtr", "Herleidingsmethodiek"),
    CPS_METER_TECH_CONVERSION_METROLOGY_DESCRIPTION("usage.point.cps.meter.tech.information.conversion.mtr.descr", "Geeft aan op welke manier de te versturen meetdata van deze meter achteraf gecorrigeerd moet worden voor druk en temperatuur."),
    CPS_METER_TECH_CAPACITY_MINIMAL("usage.point.cps.meter.tech.information.capacity.minimal", "Capacity minimal"),
    CPS_METER_TECH_CAPACITY_MINIMAL_DESCRIPTION("usage.point.cps.meter.tech.information.capacity.minimal.descr", "Qmin: De minimaal benodigde doorstroom om een meting van voldoende kwaliteit te krijgen."),
    CPS_METER_TECH_CAPACITY_NOMINAL("usage.point.cps.meter.tech.information.capacity.nominal", "Capacity nominal"),
    CPS_METER_TECH_CAPACITY_NOMINAL_DESCRIPTION("usage.point.cps.meter.tech.information.capacity.nominal.descr", "Qnom: De maximale doorstroom, die de meter continu aankan.(Kan afwijken van de G-waarde, met name bij oudere meters)"),
    CPS_METER_TECH_CAPACITY_MAXIMAL("usage.point.cps.meter.tech.information.capacity.maximal", "Capacity maximal"),
    CPS_METER_TECH_CAPACITY_MAXIMAL_DESCRIPTION("usage.point.cps.meter.tech.information.capacity.maximal.descr", "Qmax: De maximale piek doorstroom waarbij de meting nog van voldoende kwaliteit is."),
    CPS_METER_TECH_PRESSURE_MAXIMAL("usage.point.cps.meter.tech.information.pressure.maximal", "Pressure maximal"),
    CPS_METER_TECH_PRESSURE_MAXIMAL_DESCRIPTION("usage.point.cps.meter.tech.information.pressure.maximal.descr", "Pmax: De maximale druk, die de meter aankan."),
    CPS_METER_TECH_CURRENT_NOMINAL("usage.point.cps.meter.tech.information.nominal", "Stroomsterkte nominaal"),
    CPS_METER_TECH_CURRENT_NOMINAL_DESCRIPTION("usage.point.cps.meter.tech.information.nominal.descr", "Inom: De maximale stroomsterkte, die het apparaat continu aankan."),
    CPS_METER_TECH_CURRENT_MAXIMAL("usage.point.cps.meter.tech.information.maximal", "Stroomsterkte maximaal"),
    CPS_METER_TECH_CURRENT_MAXIMAL_DESCRIPTION("usage.point.cps.meter.tech.information.maximal.descr", "Imax: De maximale piek stroomsterkte, die het apparaat aan kan."),
    CPS_METER_TECH_MECHANISM("usage.point.cps.meter.tech.information.mechanism", "Meter mechanizm"),
    CPS_METER_TECH_MECHANISM_DESCRIPTION("usage.point.cps.meter.tech.information.mechanism.descr", "The coded value of the description of the Meter Mechanism"),
    CPS_METER_TECH_TYPE("usage.point.cps.meter.tech.information.type", "Meter type"),
    CPS_METER_TECH_TYPE_DESCRIPTION("usage.point.cps.meter.tech.information.type.descr", "The type of meter"),

    CPS_CUSTOM_CONTRACTUAL_SIMPLE_NAME("usage.point.cps.custom.contractual.name", "Contractual custom (Electricity)"),
    CPS_CUSTOM_CONTRACTUAL_CONTRACTED_POWER("usage.point.cps.custom.contractual.contracted.power", "Contracted power"),

    SIMPLE_CPS_NAME("usage.point.cps.simple.name", "Simple usage point CAS"),
    VERSIONED_CPS_NAME("usage.point.cps.versioned.name", "Versioned usage point CAS"),
    CPS_PROPERTIES_NAME("usage.point.cps.properties.name", "Name"),
    CPS_PROPERTIES_NAME_DESCRIPTION("usage.point.cps.properties.name.descr", "Name for CAS"),
    CPS_PROPERTIES_ENHANCED_SUPPORT("usage.point.cps.properties.enhanced.support", "Enhanced support"),
    CPS_PROPERTIES_ENHANCED_SUPPORT_DESCRIPTION("usage.point.cps.properties.enhanced.support.descr", "Cool enhanced support"),
    CPS_PROPERTIES_COMBOBOX("usage.point.cps.properties.combobox", "Combobox"),
    CPS_PROPERTIES_COMBOBOX_DESCRIPTION("usage.point.cps.properties.combobox.descr", "Combobox for CAS"),

    CPS_ANTENNA_NAME("usage.point.cps.antenna.name", "Antenna"),
    CPS_ANTENNA_POWER_NAME("usage.point.cps.antenna.power.name", "Antenna power"),
    CPS_ANTENNA_COUNT_NAME("usage.point.cps.antenna.count.name", "Number of antennas");

    private String key;
    private String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }
}
