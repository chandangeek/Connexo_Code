/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePointTypeInfo;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.DefaultReadingTypeTemplate;
import com.elster.jupiter.metering.config.DeliverableType;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeTemplate;
import com.elster.jupiter.metering.config.ReadingTypeTemplateAttributeName;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.search.SearchablePropertyValue;

import java.util.Arrays;
import java.util.Collections;
import java.util.NoSuchElementException;

import static com.elster.jupiter.mdm.usagepoint.config.impl.MetrologyConfigurationsInstaller.OOTBMetrologyConfiguration.CI_3_PHASED_CONSUMER_WITH_SMART_METER_WITH_2_TOU;
import static com.elster.jupiter.mdm.usagepoint.config.impl.MetrologyConfigurationsInstaller.OOTBMetrologyConfiguration.CI_WATER_CONFIGURATION;
import static com.elster.jupiter.mdm.usagepoint.config.impl.MetrologyConfigurationsInstaller.OOTBMetrologyConfiguration.RESIDENTIAL_CONSUMER_WITH_4_TOU;
import static com.elster.jupiter.mdm.usagepoint.config.impl.MetrologyConfigurationsInstaller.OOTBMetrologyConfiguration.RESIDENTIAL_GAS;
import static com.elster.jupiter.mdm.usagepoint.config.impl.MetrologyConfigurationsInstaller.OOTBMetrologyConfiguration.RESIDENTIAL_GAS_NON_SMART_INSTALLATION;
import static com.elster.jupiter.mdm.usagepoint.config.impl.MetrologyConfigurationsInstaller.OOTBMetrologyConfiguration.RESIDENTIAL_NET_METERING_CONSUMPTION;
import static com.elster.jupiter.mdm.usagepoint.config.impl.MetrologyConfigurationsInstaller.OOTBMetrologyConfiguration.RESIDENTIAL_NET_METERING_PRODUCTION;
import static com.elster.jupiter.mdm.usagepoint.config.impl.MetrologyConfigurationsInstaller.OOTBMetrologyConfiguration.RESIDENTIAL_NON_SMART_INSTALLATION;
import static com.elster.jupiter.mdm.usagepoint.config.impl.MetrologyConfigurationsInstaller.OOTBMetrologyConfiguration.RESIDENTIAL_PROSUMER_WITH_1_METER;
import static com.elster.jupiter.mdm.usagepoint.config.impl.MetrologyConfigurationsInstaller.OOTBMetrologyConfiguration.RESIDENTIAL_PROSUMER_WITH_2_METERS;

class MetrologyConfigurationsInstaller {

    private static final String SERVICE_CATEGORY_NOT_FOUND = "Service category not found: ";
    private static final String SERVICEKIND = "SERVICEKIND";
    private static final String DETAIL_PHASE_CODE = "detail.phaseCode";
    private static final String ROLE_NOT_FOUND = "Default meter role not found";
    private static final String REACTIVE_ENERGY_PLUS = "Reactive energy+";
    static final String YEARLY_A_MINUS_WH = "1001.0.0.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0";
    static final String YEARLY_NET_WH = "1001.0.0.4.4.1.12.0.0.0.0.0.0.0.0.3.72.0";
    static final String MONTHLY_A_PLUS_WH = "13.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    static final String MONTHLY_A_MINUS_WH = "13.0.0.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0";
    static final String MONTHLY_NET_WH = "13.0.0.4.4.1.12.0.0.0.0.0.0.0.0.3.72.0";
    static final String DAILY_A_PLUS_WH = "11.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    static final String DAILY_A_MINUS_WH = "11.0.0.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0";
    static final String HOURLY_A_MINUS_WH = "0.0.7.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0";
    static final String MIN15_A_PLUS_WH = "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    static final String MIN15_A_MINUS_WH = "0.0.2.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0";
    static final String ACTIVE_ENERGY_TOU1 = "13.0.0.4.1.1.12.0.0.0.0.1.0.0.0.3.72.0";
    static final String ACTIVE_ENERGY_TOU2 = "13.0.0.4.1.1.12.0.0.0.0.2.0.0.0.3.72.0";
    static final String BULK_A_PLUS_WH = "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    static final String BATTERY_STATUS = "0.0.0.12.0.41.11.0.0.0.0.0.0.0.0.-2.0.0";
    static final String BILLING_GAS_FLOW = "8.2.0.6.0.7.58.0.0.0.0.0.0.0.0.0.125.0";
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final MeteringService meteringService;

    MetrologyConfigurationsInstaller(MetrologyConfigurationService metrologyConfigurationService, MeteringService meteringService) {
        super();
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.meteringService = meteringService;
    }

    private void install() {
        residentialProsumerWith1Meter();
        residentialProsumerWith2Meters();
        residentialNetMeteringProduction();
        residentialNetMeteringConsumption();
        threePhasedConsumerWith2ToU();
        residentialConsumerWith4ToU();
        waterConfigurationCI();
        residentialGas();
        residentialWater();
        residentialNonSmartInstallation();
        residentialGasNonSmartInstallation();
    }

    /**
     * {@link OOTBMetrologyConfiguration} describes OOTB metrology configurations
     */
    public enum OOTBMetrologyConfiguration {
        RESIDENTIAL_PROSUMER_WITH_1_METER("Residential prosumer with 1 meter", "Typical installation for residential " +
                "prosumers with smart meter", false),
        RESIDENTIAL_PROSUMER_WITH_2_METERS("Residential prosumer with 2 meters", "Typical installation for " +
                "residential prosumers with dumb meters", false),
        RESIDENTIAL_NET_METERING_PRODUCTION("Residential net metering (production)", "Residential producer", true),
        RESIDENTIAL_NET_METERING_CONSUMPTION("Residential net metering (consumption)", "Residential consumer", true),
        RESIDENTIAL_NON_SMART_INSTALLATION("Residential non-smart installation", "Registers of different types " +
                "(textual, numeric)", true),
        RESIDENTIAL_GAS_NON_SMART_INSTALLATION("Residential gas non-smart installation", "Billing register", true),
        CI_3_PHASED_CONSUMER_WITH_SMART_METER_WITH_2_TOU("C&I 3-phased consumer with smart meter with 2 ToU", "C&I " +
                "3-phased consumer with smart meter 2 ToU", true),
        RESIDENTIAL_CONSUMER_WITH_4_TOU("Residential consumer with 4 ToU", "Residential consumer with 4 ToU", true),
        RESIDENTIAL_GAS("Residential gas", "Residential gas installation", true),
        CI_WATER_CONFIGURATION("C&I water configuration", "C&I water configuration with 2 meters", true);

        OOTBMetrologyConfiguration(String name, String description, boolean isGapAllowed) {
            this.name = name;
            this.description = description;
            this.isGapAllowed = isGapAllowed;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public boolean isGapAllowed() {
            return isGapAllowed;
        }

        private String name;
        private String description;
        private boolean isGapAllowed;

    }

    private void residentialProsumerWith1Meter() {
        if (metrologyConfigurationService.findMetrologyConfiguration(RESIDENTIAL_PROSUMER_WITH_1_METER.getName())
                .isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                .orElseThrow(() -> new NoSuchElementException(SERVICE_CATEGORY_NOT_FOUND + ServiceKind.ELECTRICITY));
        UsagePointMetrologyConfiguration config = metrologyConfigurationService.newUsagePointMetrologyConfiguration(RESIDENTIAL_PROSUMER_WITH_1_METER
                .getName(), serviceCategory)
                .withDescription(RESIDENTIAL_PROSUMER_WITH_1_METER.getDescription())
                .withGapAllowed(RESIDENTIAL_PROSUMER_WITH_1_METER.isGapAllowed())
                .create();

        config.addUsagePointRequirement(getUsagePointRequirement(SERVICEKIND, SearchablePropertyOperator.EQUAL, ServiceKind.ELECTRICITY
                .name()));
        config.addUsagePointRequirement(getUsagePointRequirement(DETAIL_PHASE_CODE, SearchablePropertyOperator.EQUAL,
                PhaseCode.S1N.name(),
                PhaseCode.S2N.name(),
                PhaseCode.S12N.name(),
                PhaseCode.S1.name(),
                PhaseCode.S2.name(),
                PhaseCode.S12.name()));
        config.addUsagePointRequirement(getUsagePointRequirement("type", SearchablePropertyOperator.EQUAL, UsagePointTypeInfo.UsagePointType.MEASURED_SDP
                .name()));

        MeterRole meterRole = metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey())
                .orElseThrow(() -> new NoSuchElementException(ROLE_NOT_FOUND));
        config.addMeterRole(meterRole);

        ReadingType readingTypeMonthlyAplusWh = meteringService.findReadingTypes(Collections.singletonList(MONTHLY_A_PLUS_WH))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(MONTHLY_A_PLUS_WH, "A+"));
        ReadingType readingTypeMonthlyAminusWh = meteringService.findReadingTypes(Collections.singletonList(MONTHLY_A_MINUS_WH))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(MONTHLY_A_MINUS_WH, "A-"));
        ReadingType readingTypeDailyAplusWh = meteringService.findReadingTypes(Collections.singletonList(DAILY_A_PLUS_WH))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(DAILY_A_PLUS_WH, "A+"));
        ReadingType readingTypeDailyAminusWh = meteringService.findReadingTypes(Collections.singletonList(DAILY_A_MINUS_WH))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(DAILY_A_MINUS_WH, "A-"));
        ReadingType readingType15minAplusWh = meteringService.findReadingTypes(Collections.singletonList(MIN15_A_PLUS_WH))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(MIN15_A_PLUS_WH, "A+"));
        ReadingType readingType15minAminusWh = meteringService.findReadingTypes(Collections.singletonList(MIN15_A_MINUS_WH))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(MIN15_A_MINUS_WH, "A-"));
        ReadingType readingTypeAverageVoltagePhaseA = meteringService.findReadingTypes(Collections.singletonList("0.2.7.6.0.1.158.0.0.0.0.0.0.0.128.0.29.0"))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType("0.2.7.6.0.1.158.0.0.0.0.0.0.0.128.0.29.0", "Average voltage"));
        ReadingType readingTypeAverageVoltagePhaseB = meteringService.findReadingTypes(Collections.singletonList("0.2.7.6.0.1.158.0.0.0.0.0.0.0.64.0.29.0"))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType("0.2.7.6.0.1.158.0.0.0.0.0.0.0.64.0.29.0", "Average voltage"));
        ReadingType readingTypeAverageVoltagePhaseC = meteringService.findReadingTypes(Collections.singletonList("0.2.7.6.0.1.158.0.0.0.0.0.0.0.32.0.29.0"))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType("0.2.7.6.0.1.158.0.0.0.0.0.0.0.32.0.29.0", "Average voltage"));


        MetrologyPurpose purposeBilling = findPurposeOrThrowException(DefaultMetrologyPurpose.BILLING);
        MetrologyPurpose purposeInformation = findPurposeOrThrowException(DefaultMetrologyPurpose.INFORMATION);
        MetrologyPurpose purposeVoltageMonitoring = findPurposeOrThrowException(DefaultMetrologyPurpose.VOLTAGE_MONITORING);

        MetrologyContract contractBilling = config.addMandatoryMetrologyContract(purposeBilling);
        MetrologyContract contractInformation = config.addMetrologyContract(purposeInformation);
        MetrologyContract contractVoltageMonitoring = config.addMetrologyContract(purposeVoltageMonitoring);

        ReadingTypeRequirement requirementAplus = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS.getNameTranslation()
                .getDefaultFormat(), meterRole)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS));

        ReadingTypeRequirement requirementAminus = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.A_MINUS.getNameTranslation()
                .getDefaultFormat(), meterRole)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_MINUS));

        ReadingTypeRequirement requirementAverageVoltagePhaseA = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE
                .getNameTranslation().getDefaultFormat() + " phase A", meterRole)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE))
                .overrideAttribute(ReadingTypeTemplateAttributeName.PHASE, 128);

        ReadingTypeRequirement requirementAverageVoltagePhaseB = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE
                .getNameTranslation().getDefaultFormat() + " phase B", meterRole)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE))
                .overrideAttribute(ReadingTypeTemplateAttributeName.PHASE, 64);

        ReadingTypeRequirement requirementAverageVoltagePhaseC = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE
                .getNameTranslation().getDefaultFormat() + " phase C", meterRole)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE))
                .overrideAttribute(ReadingTypeTemplateAttributeName.PHASE, 32);

        buildFormulaSingleRequirement(contractBilling, readingTypeMonthlyAplusWh, requirementAplus, "Monthly A+ kWh");
        buildFormulaSingleRequirement(contractBilling, readingTypeMonthlyAminusWh, requirementAminus, "Monthly A- kWh");
        buildFormulaSingleRequirement(contractBilling, readingTypeDailyAplusWh, requirementAplus, "Daily A+ kWh");
        buildFormulaSingleRequirement(contractBilling, readingTypeDailyAminusWh, requirementAminus, "Daily A- kWh");
        buildFormulaSingleRequirement(contractInformation, readingType15minAplusWh, requirementAplus, "15-min A+ kWh");
        buildFormulaSingleRequirement(contractInformation, readingType15minAminusWh, requirementAminus, "15-min A- kWh");
        buildFormulaSingleRequirement(contractVoltageMonitoring, readingTypeAverageVoltagePhaseA, requirementAverageVoltagePhaseA, "Hourly average voltage V phase 1 vs N");
        buildFormulaSingleRequirement(contractVoltageMonitoring, readingTypeAverageVoltagePhaseB, requirementAverageVoltagePhaseB, "Hourly average voltage V phase 2 vs N");
        buildFormulaSingleRequirement(contractVoltageMonitoring, readingTypeAverageVoltagePhaseC, requirementAverageVoltagePhaseC, "Hourly average voltage V phase 3 vs N");
    }

    private void residentialProsumerWith2Meters() {
        if (metrologyConfigurationService.findMetrologyConfiguration(RESIDENTIAL_PROSUMER_WITH_2_METERS.getName())
                .isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                .orElseThrow(() -> new NoSuchElementException(SERVICE_CATEGORY_NOT_FOUND + ServiceKind.ELECTRICITY));
        UsagePointMetrologyConfiguration config = metrologyConfigurationService.newUsagePointMetrologyConfiguration(RESIDENTIAL_PROSUMER_WITH_2_METERS
                .getName(), serviceCategory)
                .withDescription(RESIDENTIAL_PROSUMER_WITH_2_METERS.getDescription())
                .withGapAllowed(RESIDENTIAL_PROSUMER_WITH_2_METERS.isGapAllowed())
                .create();

        config.addUsagePointRequirement(getUsagePointRequirement(SERVICEKIND, SearchablePropertyOperator.EQUAL, ServiceKind.ELECTRICITY
                .name()));
        config.addUsagePointRequirement(getUsagePointRequirement(DETAIL_PHASE_CODE, SearchablePropertyOperator.EQUAL,
                PhaseCode.S1N.name(),
                PhaseCode.S2N.name(),
                PhaseCode.S12N.name(),
                PhaseCode.S1.name(),
                PhaseCode.S2.name(),
                PhaseCode.S12.name()));
        config.addUsagePointRequirement(getUsagePointRequirement("type", SearchablePropertyOperator.EQUAL, UsagePointTypeInfo.UsagePointType.MEASURED_SDP
                .name()));

        MeterRole meterRoleConsumption = metrologyConfigurationService.findMeterRole(DefaultMeterRole.CONSUMPTION.getKey())
                .orElseThrow(() -> new NoSuchElementException("Consumption meter role not found"));
        config.addMeterRole(meterRoleConsumption);
        MeterRole meterRoleProduction = metrologyConfigurationService.findMeterRole(DefaultMeterRole.PRODUCTION.getKey())
                .orElseThrow(() -> new NoSuchElementException("Production meter role not found"));
        config.addMeterRole(meterRoleProduction);

        ReadingType readingTypeMonthlyNetWh = meteringService.findReadingTypes(Collections.singletonList(MONTHLY_NET_WH))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(MONTHLY_NET_WH, "Monthly net kWh"));
        ReadingType readingTypeMonthlyAminusWh = meteringService.findReadingTypes(Collections.singletonList(MONTHLY_A_MINUS_WH))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(MONTHLY_A_MINUS_WH, "A-"));
        ReadingType readingTypeYearlyNetWh = meteringService.findReadingTypes(Collections.singletonList(YEARLY_NET_WH))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(YEARLY_NET_WH, "Yearly net kWh"));
        ReadingType readingTypeYearlyAminusWh = meteringService.findReadingTypes(Collections.singletonList(YEARLY_A_MINUS_WH))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(YEARLY_A_MINUS_WH, "A-"));

        MetrologyPurpose purposeBilling = findPurposeOrThrowException(DefaultMetrologyPurpose.BILLING);

        MetrologyContract contractBilling = config.addMandatoryMetrologyContract(purposeBilling);

        ReadingTypeRequirement requirementAplus = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS.getNameTranslation()
                .getDefaultFormat(), meterRoleConsumption)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS));

        ReadingTypeRequirement requirementAminus = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.A_MINUS.getNameTranslation()
                .getDefaultFormat(), meterRoleProduction)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_MINUS));

        buildNonNegativeNetFormula(contractBilling, readingTypeMonthlyNetWh, requirementAplus, requirementAminus, "Monthly Net kWh");
        buildFormulaSingleRequirement(contractBilling, readingTypeMonthlyAminusWh, requirementAminus, "Monthly A- kWh");
        buildNonNegativeNetFormula(contractBilling, readingTypeYearlyNetWh, requirementAplus, requirementAminus, "Yearly Net kWh");
        buildFormulaSingleRequirement(contractBilling, readingTypeYearlyAminusWh, requirementAminus, "Yearly A- kWh");
    }

    private void residentialNetMeteringProduction() {
        if (metrologyConfigurationService.findMetrologyConfiguration(RESIDENTIAL_NET_METERING_PRODUCTION.getName())
                .isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                .orElseThrow(() -> new NoSuchElementException(SERVICE_CATEGORY_NOT_FOUND + ServiceKind.ELECTRICITY));
        UsagePointMetrologyConfiguration config = metrologyConfigurationService.newUsagePointMetrologyConfiguration(RESIDENTIAL_NET_METERING_PRODUCTION
                .getName(), serviceCategory)
                .withDescription(RESIDENTIAL_NET_METERING_PRODUCTION.getDescription())
                .withGapAllowed(RESIDENTIAL_NET_METERING_PRODUCTION.isGapAllowed())
                .create();

        config.addUsagePointRequirement(getUsagePointRequirement(SERVICEKIND, SearchablePropertyOperator.EQUAL, ServiceKind.ELECTRICITY
                .name()));
        config.addUsagePointRequirement(getUsagePointRequirement(DETAIL_PHASE_CODE, SearchablePropertyOperator.EQUAL,
                PhaseCode.S1N.name(),
                PhaseCode.S2N.name(),
                PhaseCode.S12N.name(),
                PhaseCode.S1.name(),
                PhaseCode.S2.name(),
                PhaseCode.S12.name()));
        config.addUsagePointRequirement(getUsagePointRequirement("type", SearchablePropertyOperator.EQUAL, UsagePointTypeInfo.UsagePointType.MEASURED_SDP
                .name()));

        MeterRole meterRole = metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey())
                .orElseThrow(() -> new NoSuchElementException(ROLE_NOT_FOUND));
        config.addMeterRole(meterRole);

        ReadingType readingTypeDailyAMinusWh = meteringService.findReadingTypes(Collections.singletonList(DAILY_A_MINUS_WH))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(DAILY_A_MINUS_WH, "A-"));
        ReadingType readingTypeMonthlyAMinusWh = meteringService.findReadingTypes(Collections.singletonList(MONTHLY_A_MINUS_WH))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(MONTHLY_A_MINUS_WH, "A-"));
        ReadingType readingType15minAMinusWh = meteringService.findReadingTypes(Collections.singletonList(MIN15_A_MINUS_WH))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(MIN15_A_MINUS_WH, "A-"));
        ReadingType readingTypeHourlyAMinusWh = meteringService.findReadingTypes(Collections.singletonList(HOURLY_A_MINUS_WH))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(HOURLY_A_MINUS_WH, "A-"));

        MetrologyPurpose purposeBilling = findPurposeOrThrowException(DefaultMetrologyPurpose.BILLING);
        MetrologyPurpose purposeInformation = findPurposeOrThrowException(DefaultMetrologyPurpose.INFORMATION);
        MetrologyContract contractBilling = config.addMandatoryMetrologyContract(purposeBilling);
        MetrologyContract contractInformation = config.addMandatoryMetrologyContract(purposeInformation);

        ReadingTypeRequirement requirementAMinus = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.A_MINUS.getNameTranslation()
                .getDefaultFormat(), meterRole)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_MINUS));

        buildFormulaSingleRequirement(contractBilling, readingTypeDailyAMinusWh, requirementAMinus, "Daily A- kWh");
        buildFormulaSingleRequirement(contractBilling, readingTypeMonthlyAMinusWh, requirementAMinus, "Monthly A- kWh");
        ReadingTypeDeliverable min15 = buildFormulaSingleRequirement(contractInformation, readingType15minAMinusWh, requirementAMinus, "15-min A- kWh");
        buildFormulaSingleDeliverable(contractInformation, readingTypeHourlyAMinusWh, min15, "Hourly A- kWh");
    }

    private void residentialNetMeteringConsumption() {
        if (metrologyConfigurationService.findMetrologyConfiguration(RESIDENTIAL_NET_METERING_CONSUMPTION.getName())
                .isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                .orElseThrow(() -> new NoSuchElementException(SERVICE_CATEGORY_NOT_FOUND + ServiceKind.ELECTRICITY));
        UsagePointMetrologyConfiguration config = metrologyConfigurationService.newUsagePointMetrologyConfiguration(RESIDENTIAL_NET_METERING_CONSUMPTION
                .getName(), serviceCategory)
                .withDescription(RESIDENTIAL_NET_METERING_CONSUMPTION.getDescription())
                .withGapAllowed(RESIDENTIAL_NET_METERING_CONSUMPTION.isGapAllowed())
                .create();

        config.addUsagePointRequirement(getUsagePointRequirement(SERVICEKIND, SearchablePropertyOperator.EQUAL, ServiceKind.ELECTRICITY
                .name()));
        config.addUsagePointRequirement(getUsagePointRequirement(DETAIL_PHASE_CODE, SearchablePropertyOperator.EQUAL,
                PhaseCode.S1N.name(),
                PhaseCode.S2N.name(),
                PhaseCode.S12N.name(),
                PhaseCode.S1.name(),
                PhaseCode.S2.name(),
                PhaseCode.S12.name()));
        config.addUsagePointRequirement(getUsagePointRequirement("type", SearchablePropertyOperator.EQUAL, UsagePointTypeInfo.UsagePointType.MEASURED_SDP
                .name()));

        MeterRole meterRole = metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey())
                .orElseThrow(() -> new NoSuchElementException(ROLE_NOT_FOUND));
        config.addMeterRole(meterRole);

        ReadingType readingTypeDailyAplusWh = meteringService.findReadingTypes(Collections.singletonList(DAILY_A_PLUS_WH))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(DAILY_A_PLUS_WH, "A+"));
        ReadingType readingTypeMonthlyAplusWh = meteringService.findReadingTypes(Collections.singletonList(MONTHLY_A_PLUS_WH))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(MONTHLY_A_PLUS_WH, "A+"));
        ReadingType readingType15minAplusWh = meteringService.findReadingTypes(Collections.singletonList(MIN15_A_PLUS_WH))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(MIN15_A_PLUS_WH, "A+"));
        ReadingType readingTypeAplusWh = meteringService.findReadingTypes(Collections.singletonList(BULK_A_PLUS_WH))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(BULK_A_PLUS_WH, "A+"));

        MetrologyPurpose purposeBilling = findPurposeOrThrowException(DefaultMetrologyPurpose.BILLING);
        MetrologyPurpose purposeInformation = findPurposeOrThrowException(DefaultMetrologyPurpose.INFORMATION);

        MetrologyContract contractBilling = config.addMandatoryMetrologyContract(purposeBilling);
        MetrologyContract contractInformation = config.addMandatoryMetrologyContract(purposeInformation);

        ReadingTypeRequirement requirementAplus = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS.getNameTranslation()
                .getDefaultFormat(), meterRole)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS));

        ReadingTypeRequirement requirementAplusRegister = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.BULK_A_PLUS
                .getNameTranslation()
                .getDefaultFormat(), meterRole)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.BULK_A_PLUS));

        buildFormulaSingleRequirement(contractBilling, readingTypeDailyAplusWh, requirementAplus, "Daily A+ kWh");
        buildFormulaSingleRequirement(contractBilling, readingTypeMonthlyAplusWh, requirementAplus, "Monthly A+ kWh");
        buildFormulaSingleRequirement(contractInformation, readingType15minAplusWh, requirementAplus, "15-min A+ kWh");
        buildFormulaSingleRequirement(contractInformation, readingTypeAplusWh, requirementAplusRegister, "A+ kWh");
    }

    private void residentialNonSmartInstallation() {
        if (metrologyConfigurationService.findMetrologyConfiguration(RESIDENTIAL_NON_SMART_INSTALLATION.getName())
                .isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                .orElseThrow(() -> new NoSuchElementException(SERVICE_CATEGORY_NOT_FOUND + ServiceKind.ELECTRICITY));
        UsagePointMetrologyConfiguration config = metrologyConfigurationService.newUsagePointMetrologyConfiguration(RESIDENTIAL_NON_SMART_INSTALLATION
                .getName(), serviceCategory)
                .withDescription(RESIDENTIAL_NON_SMART_INSTALLATION.getDescription())
                .withGapAllowed(RESIDENTIAL_NON_SMART_INSTALLATION.isGapAllowed())
                .create();

        config.addUsagePointRequirement(getUsagePointRequirement(SERVICEKIND, SearchablePropertyOperator.EQUAL, ServiceKind.ELECTRICITY
                .name()));
        config.addUsagePointRequirement(getUsagePointRequirement("type", SearchablePropertyOperator.EQUAL, UsagePointTypeInfo.UsagePointType.MEASURED_SDP
                .name()));

        MeterRole meterRole = metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey())
                .orElseThrow(() -> new NoSuchElementException(ROLE_NOT_FOUND));
        config.addMeterRole(meterRole);

        ReadingType readingTypeBatteryStatus = meteringService.findReadingTypes(Collections.singletonList(BATTERY_STATUS))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(BATTERY_STATUS, "Battery status"));
        ReadingType readingTypeAplusWh = meteringService.findReadingTypes(Collections.singletonList(BULK_A_PLUS_WH))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(BULK_A_PLUS_WH, "A+"));

        MetrologyPurpose purposeInformation = findPurposeOrThrowException(DefaultMetrologyPurpose.INFORMATION);

        MetrologyContract contractInformation = config.addMandatoryMetrologyContract(purposeInformation);

        ReadingTypeRequirement requirementTextual = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.BATTERY_STATUS
                .getNameTranslation()
                .getDefaultFormat(), meterRole)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.BATTERY_STATUS));

        ReadingTypeRequirement requirementNumerical = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.BULK_A_PLUS
                .getNameTranslation()
                .getDefaultFormat(), meterRole)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.BULK_A_PLUS));

        buildFormulaSingleRequirement(contractInformation, DeliverableType.TEXT, readingTypeBatteryStatus, requirementTextual, "Battery status");
        buildFormulaSingleRequirement(contractInformation, DeliverableType.NUMERICAL, readingTypeAplusWh, requirementNumerical, "A+ kWh");
    }

    private void residentialGasNonSmartInstallation() {
        if (metrologyConfigurationService.findMetrologyConfiguration(RESIDENTIAL_GAS_NON_SMART_INSTALLATION.getName())
                .isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                .orElseThrow(() -> new NoSuchElementException(SERVICE_CATEGORY_NOT_FOUND + ServiceKind.ELECTRICITY));
        UsagePointMetrologyConfiguration config = metrologyConfigurationService.newUsagePointMetrologyConfiguration(RESIDENTIAL_GAS_NON_SMART_INSTALLATION
                .getName(), serviceCategory)
                .withDescription(RESIDENTIAL_GAS_NON_SMART_INSTALLATION.getDescription())
                .withGapAllowed(RESIDENTIAL_GAS_NON_SMART_INSTALLATION.isGapAllowed())
                .create();

        config.addUsagePointRequirement(getUsagePointRequirement(SERVICEKIND, SearchablePropertyOperator.EQUAL, ServiceKind.GAS
                .name()));
        config.addUsagePointRequirement(getUsagePointRequirement("type", SearchablePropertyOperator.EQUAL, UsagePointTypeInfo.UsagePointType.MEASURED_SDP
                .name()));

        MeterRole meterRole = metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey())
                .orElseThrow(() -> new NoSuchElementException(ROLE_NOT_FOUND));
        config.addMeterRole(meterRole);

        ReadingType readingTypeGasFlow = meteringService.findReadingTypes(Collections.singletonList(BILLING_GAS_FLOW))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(BILLING_GAS_FLOW, "Billing gas flow"));

        MetrologyPurpose purposeInformation = findPurposeOrThrowException(DefaultMetrologyPurpose.INFORMATION);

        MetrologyContract contractInformation = config.addMandatoryMetrologyContract(purposeInformation);

        ReadingTypeRequirement requirementBilling = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.GAS_FLOW_BILLING
                .getNameTranslation()
                .getDefaultFormat(), meterRole)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.GAS_FLOW_BILLING));

        buildFormulaSingleRequirement(contractInformation, DeliverableType.BILLING, readingTypeGasFlow, requirementBilling, "Billing Gas flow m3/h");
    }

    private void threePhasedConsumerWith2ToU() {
        if (metrologyConfigurationService.findMetrologyConfiguration(CI_3_PHASED_CONSUMER_WITH_SMART_METER_WITH_2_TOU.getName())
                .isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                .orElseThrow(() -> new NoSuchElementException(SERVICE_CATEGORY_NOT_FOUND + ServiceKind.ELECTRICITY));
        UsagePointMetrologyConfiguration config = metrologyConfigurationService.newUsagePointMetrologyConfiguration(CI_3_PHASED_CONSUMER_WITH_SMART_METER_WITH_2_TOU
                .getName(), serviceCategory)
                .withDescription(CI_3_PHASED_CONSUMER_WITH_SMART_METER_WITH_2_TOU.getDescription())
                .withGapAllowed(CI_3_PHASED_CONSUMER_WITH_SMART_METER_WITH_2_TOU.isGapAllowed())
                .create();

        config.addUsagePointRequirement(getUsagePointRequirement(SERVICEKIND, SearchablePropertyOperator.EQUAL, ServiceKind.ELECTRICITY
                .name()));
        config.addUsagePointRequirement(getUsagePointRequirement(DETAIL_PHASE_CODE, SearchablePropertyOperator.EQUAL,
                PhaseCode.ABC.name(),
                PhaseCode.ABCN.name()));
        config.addUsagePointRequirement(getUsagePointRequirement("type", SearchablePropertyOperator.EQUAL, UsagePointTypeInfo.UsagePointType.MEASURED_SDP
                .name()));

        MeterRole meterRole = metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey())
                .orElseThrow(() -> new NoSuchElementException(ROLE_NOT_FOUND));
        config.addMeterRole(meterRole);

        ReadingType readingTypeDailyActiveEnergyToU1 = meteringService.findReadingTypes(Collections.singletonList("11.0.0.4.1.1.12.0.0.0.0.1.0.0.0.3.72.0"))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType("11.0.0.4.1.1.12.0.0.0.0.1.0.0.0.3.72.0", "A+"));
        ReadingType readingTypeDailyActiveEnergyToU2 = meteringService.findReadingTypes(Collections.singletonList("11.0.0.4.1.1.12.0.0.0.0.2.0.0.0.3.72.0"))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType("11.0.0.4.1.1.12.0.0.0.0.2.0.0.0.3.72.0", "A+"));
        ReadingType readingTypeDailyReactiveEnergyToU1 = meteringService.findReadingTypes(Collections.singletonList("11.0.0.4.2.1.12.0.0.0.0.1.0.0.0.3.73.0"))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType("11.0.0.4.2.1.12.0.0.0.0.1.0.0.0.3.73.0", REACTIVE_ENERGY_PLUS));
        ReadingType readingTypeDailyReactiveEnergyToU2 = meteringService.findReadingTypes(Collections.singletonList("11.0.0.4.2.1.12.0.0.0.0.2.0.0.0.3.73.0"))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType("11.0.0.4.2.1.12.0.0.0.0.2.0.0.0.3.73.0", REACTIVE_ENERGY_PLUS));
        ReadingType readingTypeMonthlyActiveEnergyToU1 = meteringService.findReadingTypes(Collections.singletonList(ACTIVE_ENERGY_TOU1))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(ACTIVE_ENERGY_TOU1, "A+"));
        ReadingType readingTypeMonthlyActiveEnergyToU2 = meteringService.findReadingTypes(Collections.singletonList(ACTIVE_ENERGY_TOU2))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(ACTIVE_ENERGY_TOU2, "A+"));
        ReadingType readingTypeMonthlyReactiveEnergyToU1 = meteringService.findReadingTypes(Collections.singletonList("13.0.0.4.2.1.12.0.0.0.0.1.0.0.0.3.73.0"))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType("13.0.0.4.2.1.12.0.0.0.0.1.0.0.0.3.73.0", REACTIVE_ENERGY_PLUS));
        ReadingType readingTypeMonthlyReactiveEnergyToU2 = meteringService.findReadingTypes(Collections.singletonList("13.0.0.4.2.1.12.0.0.0.0.2.0.0.0.3.73.0"))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType("13.0.0.4.2.1.12.0.0.0.0.2.0.0.0.3.73.0", REACTIVE_ENERGY_PLUS));
        ReadingType readingTypeAverageVoltagePhaseA = meteringService.findReadingTypes(Collections.singletonList("0.2.7.6.0.1.158.0.0.0.0.0.0.0.128.0.29.0"))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType("0.2.7.6.0.1.158.0.0.0.0.0.0.0.128.0.29.0", "Average voltage"));
        ReadingType readingTypeAverageVoltagePhaseB = meteringService.findReadingTypes(Collections.singletonList("0.2.7.6.0.1.158.0.0.0.0.0.0.0.64.0.29.0"))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType("0.2.7.6.0.1.158.0.0.0.0.0.0.0.64.0.29.0", "Average voltage"));
        ReadingType readingTypeAverageVoltagePhaseC = meteringService.findReadingTypes(Collections.singletonList("0.2.7.6.0.1.158.0.0.0.0.0.0.0.32.0.29.0"))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType("0.2.7.6.0.1.158.0.0.0.0.0.0.0.32.0.29.0", "Average voltage"));

        MetrologyPurpose purposeBilling = findPurposeOrThrowException(DefaultMetrologyPurpose.BILLING);
        MetrologyPurpose purposeVoltageMonitoring = findPurposeOrThrowException(DefaultMetrologyPurpose.VOLTAGE_MONITORING);

        MetrologyContract contractBilling = config.addMandatoryMetrologyContract(purposeBilling);
        MetrologyContract contractVoltageMonitoring = config.addMandatoryMetrologyContract(purposeVoltageMonitoring);

        ReadingTypeRequirement requirementAplusToU1 = config.newReadingTypeRequirement("Active energy+ ToU1", meterRole)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS))
                .overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 1);
        ReadingTypeRequirement requirementAplusToU2 = config.newReadingTypeRequirement("Active energy+ ToU2", meterRole)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS))
                .overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 2);
        ReadingTypeRequirement requirementReactiveEnergyPlusToU1 = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.REACTIVE_ENERGY_PLUS
                .getNameTranslation().getDefaultFormat() + " ToU1", meterRole)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.REACTIVE_ENERGY_PLUS))
                .overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 1);
        ReadingTypeRequirement requirementReactiveEnergyPlusToU2 = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.REACTIVE_ENERGY_PLUS
                .getNameTranslation().getDefaultFormat() + " ToU2", meterRole)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.REACTIVE_ENERGY_PLUS))
                .overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 2);
        ReadingTypeRequirement requirementAverageVoltagePhaseA = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE
                .getNameTranslation().getDefaultFormat() + " phase A", meterRole)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE))
                .overrideAttribute(ReadingTypeTemplateAttributeName.PHASE, 128);
        ReadingTypeRequirement requirementAverageVoltagePhaseB = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE
                .getNameTranslation().getDefaultFormat() + " phase B", meterRole)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE))
                .overrideAttribute(ReadingTypeTemplateAttributeName.PHASE, 64);
        ReadingTypeRequirement requirementAverageVoltagePhaseC = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE
                .getNameTranslation().getDefaultFormat() + " phase C", meterRole)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE))
                .overrideAttribute(ReadingTypeTemplateAttributeName.PHASE, 32);

        buildFormulaSingleRequirement(contractBilling, readingTypeDailyActiveEnergyToU1, requirementAplusToU1, "Daily active energy kWh ToU1");
        buildFormulaSingleRequirement(contractBilling, readingTypeDailyActiveEnergyToU2, requirementAplusToU2, "Daily active energy kWh ToU2");
        buildFormulaSingleRequirement(contractBilling, readingTypeDailyReactiveEnergyToU1, requirementReactiveEnergyPlusToU1, "Daily reactive energy kVArh ToU1");
        buildFormulaSingleRequirement(contractBilling, readingTypeDailyReactiveEnergyToU2, requirementReactiveEnergyPlusToU2, "Daily reactive energy kVArh ToU2");
        buildFormulaSingleRequirement(contractBilling, readingTypeMonthlyActiveEnergyToU1, requirementAplusToU1, "Monthly active energy kWh ToU1");
        buildFormulaSingleRequirement(contractBilling, readingTypeMonthlyActiveEnergyToU2, requirementAplusToU2, "Monthly active energy kWh ToU2");
        buildFormulaSingleRequirement(contractBilling, readingTypeMonthlyReactiveEnergyToU1, requirementReactiveEnergyPlusToU1, "Monthly reactive energy kVArh ToU1");
        buildFormulaSingleRequirement(contractBilling, readingTypeMonthlyReactiveEnergyToU2, requirementReactiveEnergyPlusToU2, "Monthly reactive energy kVArh ToU2");
        buildFormulaSingleRequirement(contractVoltageMonitoring, readingTypeAverageVoltagePhaseA, requirementAverageVoltagePhaseA, "Hourly average voltage V phase 1 vs N");
        buildFormulaSingleRequirement(contractVoltageMonitoring, readingTypeAverageVoltagePhaseB, requirementAverageVoltagePhaseB, "Hourly average voltage V phase 2 vs N");
        buildFormulaSingleRequirement(contractVoltageMonitoring, readingTypeAverageVoltagePhaseC, requirementAverageVoltagePhaseC, "Hourly average voltage V phase 3 vs N");
    }

    private void residentialConsumerWith4ToU() {
        if (metrologyConfigurationService.findMetrologyConfiguration(RESIDENTIAL_CONSUMER_WITH_4_TOU.getName())
                .isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                .orElseThrow(() -> new NoSuchElementException(SERVICE_CATEGORY_NOT_FOUND + ServiceKind.ELECTRICITY));
        UsagePointMetrologyConfiguration config = metrologyConfigurationService.newUsagePointMetrologyConfiguration(RESIDENTIAL_CONSUMER_WITH_4_TOU
                .getName(), serviceCategory)
                .withDescription(RESIDENTIAL_CONSUMER_WITH_4_TOU.getDescription())
                .withGapAllowed(RESIDENTIAL_CONSUMER_WITH_4_TOU.isGapAllowed())
                .create();

        config.addUsagePointRequirement(getUsagePointRequirement(SERVICEKIND, SearchablePropertyOperator.EQUAL, ServiceKind.ELECTRICITY
                .name()));
        config.addUsagePointRequirement(getUsagePointRequirement(DETAIL_PHASE_CODE, SearchablePropertyOperator.EQUAL,
                PhaseCode.S1N.name(),
                PhaseCode.S2N.name(),
                PhaseCode.S12N.name(),
                PhaseCode.S1.name(),
                PhaseCode.S2.name(),
                PhaseCode.S12.name()));
        config.addUsagePointRequirement(getUsagePointRequirement("type", SearchablePropertyOperator.EQUAL, UsagePointTypeInfo.UsagePointType.MEASURED_SDP
                .name()));

        MeterRole meterRole = metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey())
                .orElseThrow(() -> new NoSuchElementException(ROLE_NOT_FOUND));
        config.addMeterRole(meterRole);

        ReadingType readingTypeMonthlyAplusToU1 = meteringService.findReadingTypes(Collections.singletonList(ACTIVE_ENERGY_TOU1))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(ACTIVE_ENERGY_TOU1, "A+"));
        ReadingType readingTypeMonthlyAplusToU2 = meteringService.findReadingTypes(Collections.singletonList(ACTIVE_ENERGY_TOU2))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(ACTIVE_ENERGY_TOU2, "A+"));
        ReadingType readingTypeMonthlyAplusToU3 = meteringService.findReadingTypes(Collections.singletonList("13.0.0.4.1.1.12.0.0.0.0.3.0.0.0.3.72.0"))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType("13.0.0.4.1.1.12.0.0.0.0.3.0.0.0.3.72.0", "A+"));
        ReadingType readingTypeMonthlyAplusToU4 = meteringService.findReadingTypes(Collections.singletonList("13.0.0.4.1.1.12.0.0.0.0.4.0.0.0.3.72.0"))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType("13.0.0.4.1.1.12.0.0.0.0.4.0.0.0.3.72.0", "A+"));

        MetrologyPurpose purposeBilling = findPurposeOrThrowException(DefaultMetrologyPurpose.BILLING);
        MetrologyContract contractBilling = config.addMandatoryMetrologyContract(purposeBilling);

        ReadingTypeRequirement requirementAplusToU1 = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS
                .getNameTranslation().getDefaultFormat() + " ToU1", meterRole)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS))
                .overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 1);
        ReadingTypeRequirement requirementAplusToU2 = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS
                .getNameTranslation().getDefaultFormat() + " ToU2", meterRole)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS))
                .overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 2);
        ReadingTypeRequirement requirementAplusToU3 = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS
                .getNameTranslation().getDefaultFormat() + " ToU3", meterRole)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS))
                .overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 3);
        ReadingTypeRequirement requirementAplusToU4 = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS
                .getNameTranslation().getDefaultFormat() + " ToU4", meterRole)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS))
                .overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 4);

        buildFormulaSingleRequirement(contractBilling, readingTypeMonthlyAplusToU1, requirementAplusToU1, "Monthly A+ kWh ToU1");
        buildFormulaSingleRequirement(contractBilling, readingTypeMonthlyAplusToU2, requirementAplusToU2, "Monthly A+ kWh ToU2");
        buildFormulaSingleRequirement(contractBilling, readingTypeMonthlyAplusToU3, requirementAplusToU3, "Monthly A+ kWh ToU3");
        buildFormulaSingleRequirement(contractBilling, readingTypeMonthlyAplusToU4, requirementAplusToU4, "Monthly A+ kWh ToU4");
    }

    private void residentialGas() {
        if (metrologyConfigurationService.findMetrologyConfiguration(RESIDENTIAL_GAS.getName()).isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.GAS)
                .orElseThrow(() -> new NoSuchElementException(SERVICE_CATEGORY_NOT_FOUND + ServiceKind.GAS));
        UsagePointMetrologyConfiguration config = metrologyConfigurationService.newUsagePointMetrologyConfiguration(RESIDENTIAL_GAS
                .getName(), serviceCategory)
                .withDescription(RESIDENTIAL_GAS.getDescription())
                .withGapAllowed(RESIDENTIAL_GAS.isGapAllowed())
                .create();

        config.addUsagePointRequirement(getUsagePointRequirement(SERVICEKIND, SearchablePropertyOperator.EQUAL, ServiceKind.GAS
                .name()));
        config.addUsagePointRequirement(getUsagePointRequirement("type", SearchablePropertyOperator.EQUAL, UsagePointTypeInfo.UsagePointType.MEASURED_SDP
                .name()));

        MeterRole meterRole = metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey())
                .orElseThrow(() -> new NoSuchElementException(ROLE_NOT_FOUND));
        config.addMeterRole(meterRole);

        ReadingType readingTypeDailyVolume = meteringService.findReadingTypes(Collections.singletonList("11.0.0.4.1.7.58.0.0.0.0.0.0.0.0.0.42.0"))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType("11.0.0.4.1.7.58.0.0.0.0.0.0.0.0.0.42.0", "Daily volume m³"));
        ReadingType readingTypeMonthlyVolume = meteringService.findReadingTypes(Collections.singletonList("13.0.0.4.1.7.58.0.0.0.0.0.0.0.0.0.42.0"))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType("13.0.0.4.1.7.58.0.0.0.0.0.0.0.0.0.42.0", "Monthly volume m³"));
        ReadingType readingTypeHourlyVolume = meteringService.findReadingTypes(Collections.singletonList("0.0.7.4.1.7.58.0.0.0.0.0.0.0.0.0.42.0"))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType("0.0.7.4.1.7.58.0.0.0.0.0.0.0.0.0.42.0", "Hourly volume m³"));


        MetrologyPurpose purposeBilling = findPurposeOrThrowException(DefaultMetrologyPurpose.BILLING);
        MetrologyContract contractBilling = config.addMandatoryMetrologyContract(purposeBilling);
        MetrologyPurpose purposeInformation = findPurposeOrThrowException(DefaultMetrologyPurpose.INFORMATION);
        MetrologyContract contractInformation = config.addMetrologyContract(purposeInformation);

        ReadingTypeRequirement requirementGasVolume = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.GAS_VOLUME
                .getNameTranslation().getDefaultFormat(), meterRole)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.GAS_VOLUME))
                .overrideAttribute(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, 42);

        buildFormulaSingleRequirement(contractBilling, readingTypeDailyVolume, requirementGasVolume, "Daily volume m³");
        buildFormulaSingleRequirement(contractBilling, readingTypeMonthlyVolume, requirementGasVolume, "Monthly volume m³");
        buildFormulaSingleRequirement(contractInformation, readingTypeHourlyVolume, requirementGasVolume, "Hourly volume m³");
    }

    void residentialWater() {
        if (metrologyConfigurationService.findMetrologyConfiguration("Residential water").isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.WATER)
                .orElseThrow(() -> new NoSuchElementException(SERVICE_CATEGORY_NOT_FOUND + ServiceKind.WATER));
        UsagePointMetrologyConfiguration config = metrologyConfigurationService.newUsagePointMetrologyConfiguration("Residential water", serviceCategory)
                .withDescription("Residential water").create();

        config.addUsagePointRequirement(getUsagePointRequirement(SERVICEKIND, SearchablePropertyOperator.EQUAL, ServiceKind.WATER
                .name()));
        config.addUsagePointRequirement(getUsagePointRequirement("type", SearchablePropertyOperator.EQUAL, UsagePointTypeInfo.UsagePointType.MEASURED_SDP
                .name()));

        MeterRole meterRole = metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey())
                .orElseThrow(() -> new NoSuchElementException(ROLE_NOT_FOUND));
        config.addMeterRole(meterRole);

        ReadingType readingTypeYearlyVolume = meteringService.findReadingTypes(Collections.singletonList("1001.0.0.4.1.9.58.0.0.0.0.0.0.0.0.0.42.0"))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType("1001.0.0.4.1.9.58.0.0.0.0.0.0.0.0.0.42.0", "Water volume"));

        ReadingType readingTypeDailyVolume = meteringService.findReadingTypes(Collections.singletonList("11.0.0.4.1.9.58.0.0.0.0.0.0.0.0.0.42.0"))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType("11.0.0.4.1.9.58.0.0.0.0.0.0.0.0.0.42.0", "Water volume"));

        MetrologyPurpose purposeBilling = findPurposeOrThrowException(DefaultMetrologyPurpose.BILLING);
        MetrologyContract contractBilling = config.addMandatoryMetrologyContract(purposeBilling);
        MetrologyPurpose purposeInformation = findPurposeOrThrowException(DefaultMetrologyPurpose.INFORMATION);
        MetrologyContract contractInformation = config.addMetrologyContract(purposeInformation);

        ReadingTypeRequirement requirementWaterVolume = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.WATER_VOLUME
                .getNameTranslation().getDefaultFormat(), meterRole)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.WATER_VOLUME))
                .overrideAttribute(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, 42);

        buildFormulaSingleRequirement(contractBilling, readingTypeYearlyVolume, requirementWaterVolume, "Yearly volume m³");
        buildFormulaSingleRequirement(contractInformation, readingTypeDailyVolume, requirementWaterVolume, "Daily volume m³");
    }

    private void waterConfigurationCI() {
        if (metrologyConfigurationService.findMetrologyConfiguration(CI_WATER_CONFIGURATION.getName()).isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.WATER)
                .orElseThrow(() -> new NoSuchElementException(SERVICE_CATEGORY_NOT_FOUND + ServiceKind.WATER));
        UsagePointMetrologyConfiguration config = metrologyConfigurationService.newUsagePointMetrologyConfiguration(CI_WATER_CONFIGURATION
                .getName(), serviceCategory)
                .withDescription(CI_WATER_CONFIGURATION.getDescription())
                .withGapAllowed(CI_WATER_CONFIGURATION.isGapAllowed())
                .create();

        config.addUsagePointRequirement(getUsagePointRequirement(SERVICEKIND, SearchablePropertyOperator.EQUAL, ServiceKind.WATER
                .name()));
        config.addUsagePointRequirement(getUsagePointRequirement("type", SearchablePropertyOperator.EQUAL, UsagePointTypeInfo.UsagePointType.MEASURED_SDP
                .name()));

        MeterRole meterRolePeakConsumption = metrologyConfigurationService.findMeterRole(DefaultMeterRole.PEAK_CONSUMPTION
                .getKey())
                .orElseThrow(() -> new NoSuchElementException(ROLE_NOT_FOUND));
        config.addMeterRole(meterRolePeakConsumption);
        MeterRole meterRoleOffPeakConsumption = metrologyConfigurationService.findMeterRole(DefaultMeterRole.OFF_PEAK_CONSUMPTION
                .getKey())
                .orElseThrow(() -> new NoSuchElementException(ROLE_NOT_FOUND));
        config.addMeterRole(meterRoleOffPeakConsumption);

        ReadingType readingTypeMonthlyConsumption = meteringService.findReadingTypes(Collections.singletonList("13.0.0.4.1.9.58.0.0.0.0.0.0.0.0.0.42.0"))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType("13.0.0.4.1.9.58.0.0.0.0.0.0.0.0.0.42.0", "Monthly consumption m³"));

        MetrologyPurpose purposeBilling = findPurposeOrThrowException(DefaultMetrologyPurpose.BILLING);
        MetrologyContract contractBilling = config.addMandatoryMetrologyContract(purposeBilling);

        ReadingTypeRequirement requirementPeakConsumption = config.newReadingTypeRequirement("Peak consumption", meterRolePeakConsumption)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.WATER_VOLUME))
                .overrideAttribute(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, 42);
        ReadingTypeRequirement requirementOffPeakConsumption = config.newReadingTypeRequirement("Off peak consumption", meterRoleOffPeakConsumption)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.WATER_VOLUME))
                .overrideAttribute(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, 42);

        ReadingTypeDeliverableBuilder builder = contractBilling.newReadingTypeDeliverable("Monthly consumption m³", readingTypeMonthlyConsumption, Formula.Mode.AUTO);
        builder.build(builder.plus(builder.requirement(requirementPeakConsumption), builder
                .requirement(requirementOffPeakConsumption)));
    }

    ReadingTypeDeliverable buildFormulaSingleRequirement(MetrologyContract contract, ReadingType readingType, ReadingTypeRequirement requirement, String name) {
        ReadingTypeDeliverableBuilder builder = contract.newReadingTypeDeliverable(name, readingType, Formula.Mode.AUTO);
        return builder.build(builder.requirement(requirement));
    }

    ReadingTypeDeliverable buildFormulaSingleDeliverable(MetrologyContract contract, ReadingType readingType, ReadingTypeDeliverable underlying, String name) {
        ReadingTypeDeliverableBuilder builder = contract.newReadingTypeDeliverable(name, readingType, Formula.Mode.AUTO);
        return builder.build(builder.deliverable(underlying));
    }

    ReadingTypeDeliverable buildFormulaSingleRequirement(MetrologyContract contract, DeliverableType deliverableType, ReadingType readingType, ReadingTypeRequirement requirement, String name) {
        ReadingTypeDeliverableBuilder builder = contract.newReadingTypeDeliverable(name, deliverableType, readingType, deliverableType
                .equals(DeliverableType.TEXT) ? Formula.Mode.EXPERT : Formula.Mode.AUTO);
        return builder.build(builder.requirement(requirement));
    }

    ReadingTypeDeliverable buildNonNegativeNetFormula(MetrologyContract contract, ReadingType readingType,
                                                      ReadingTypeRequirement requirementPlus, ReadingTypeRequirement requirementMinus, String name) {

        ReadingTypeDeliverableBuilder builder = contract.newReadingTypeDeliverable(name, readingType, Formula.Mode.AUTO);
        return builder.build(builder.maximum(builder.minus(builder.requirement(requirementPlus), builder.requirement(requirementMinus)), builder
                .constant(0)));
    }

    ReadingTypeTemplate getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate defaultReadingTypeTemplate) {
        return metrologyConfigurationService.findReadingTypeTemplate(defaultReadingTypeTemplate.getNameTranslation().getDefaultFormat())
                .orElseThrow(() -> new NoSuchElementException("Default reading type template not found"));
    }

    SearchablePropertyValue.ValueBean getUsagePointRequirement(String property, SearchablePropertyOperator operator, String... values) {
        SearchablePropertyValue.ValueBean valueBean = new SearchablePropertyValue.ValueBean();
        valueBean.propertyName = property;
        valueBean.operator = operator;
        valueBean.values = Arrays.asList(values);
        return valueBean;
    }

    void createMetrologyConfigurations() {
        this.install();
    }

    private MetrologyPurpose findPurposeOrThrowException(DefaultMetrologyPurpose purpose) {
        return metrologyConfigurationService.findMetrologyPurpose(purpose)
                .orElseThrow(() -> new NoSuchElementException(purpose.getName().getDefaultMessage() + " metrology purpose not found"));
    }
}
