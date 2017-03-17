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

class MetrologyConfigurationsInstaller {

    private static final String SERVICE_CATEGORY_NOT_FOUND = "Service category not found: ";
    private static final String SERVICEKIND = "SERVICEKIND";
    private static final String DETAIL_PHASE_CODE = "detail.phaseCode";
    private static final String ROLE_NOT_FOUND = "Default meter role not found";
    private static final String REACTIVE_ENERGY_PLUS = "Reactive energy+";
    static final String MONTHLY_A_PLUS_WH = "13.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    static final String MONTHLY_A_MINUS_WH = "13.0.0.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0";
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
        residentialNonSmartInstallation();
        residentialGasNonSmartInstallation();
    }

    private void residentialProsumerWith1Meter() {
        if (metrologyConfigurationService.findMetrologyConfiguration("Residential prosumer with 1 meter").isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = this.findElectricityServiceCategoryOrThrowException();
        UsagePointMetrologyConfiguration config = metrologyConfigurationService.newUsagePointMetrologyConfiguration("Residential prosumer with 1 meter", serviceCategory)
                .withDescription("Typical installation for residential prosumers with smart meter").create();

        config.addUsagePointRequirement(
                getUsagePointRequirement(
                        SERVICEKIND,
                        SearchablePropertyOperator.EQUAL,
                        ServiceKind.ELECTRICITY.name()));
        config.addUsagePointRequirement(
                getUsagePointRequirement(
                        DETAIL_PHASE_CODE,
                        SearchablePropertyOperator.EQUAL,
                        PhaseCode.S1N.name(),
                        PhaseCode.S2N.name(),
                        PhaseCode.S12N.name(),
                        PhaseCode.S1.name(),
                        PhaseCode.S2.name(),
                        PhaseCode.S12.name()));
        config.addUsagePointRequirement(
                getUsagePointRequirement(
                        "type",
                        SearchablePropertyOperator.EQUAL,
                        UsagePointTypeInfo.UsagePointType.MEASURED_SDP.name()));

        MeterRole meterRole = findMeterRoleOrThrowException(DefaultMeterRole.DEFAULT);
        config.addMeterRole(meterRole);

        ReadingType readingTypeMonthlyAplusWh = this.findOrCreateReadingType(MONTHLY_A_PLUS_WH, "A+");
        ReadingType readingTypeMonthlyAminusWh = this.findOrCreateReadingType(MONTHLY_A_MINUS_WH, "A-");
        ReadingType readingTypeDailyAplusWh = this.findOrCreateReadingType(DAILY_A_PLUS_WH, "A+");
        ReadingType readingTypeDailyAminusWh = this.findOrCreateReadingType(DAILY_A_MINUS_WH, "A-");
        ReadingType readingType15minAplusWh = this.findOrCreateReadingType(MIN15_A_PLUS_WH, "A+");
        ReadingType readingType15minAminusWh = this.findOrCreateReadingType(MIN15_A_MINUS_WH, "A-");
        ReadingType readingTypeAverageVoltagePhaseA = this.findOrCreateReadingType("0.2.7.6.0.1.158.0.0.0.0.0.0.0.128.0.29.0", "Average voltage");
        ReadingType readingTypeAverageVoltagePhaseB = this.findOrCreateReadingType("0.2.7.6.0.1.158.0.0.0.0.0.0.0.64.0.29.0", "Average voltage");
        ReadingType readingTypeAverageVoltagePhaseC = this.findOrCreateReadingType("0.2.7.6.0.1.158.0.0.0.0.0.0.0.32.0.29.0", "Average voltage");

        MetrologyContract billingContract = config.addMandatoryMetrologyContract(findPurposeOrThrowException(DefaultMetrologyPurpose.BILLING));
        MetrologyContract informationContract = config.addMetrologyContract(findPurposeOrThrowException(DefaultMetrologyPurpose.INFORMATION));
        MetrologyContract voltageMonitoringContract = config.addMetrologyContract(findPurposeOrThrowException(DefaultMetrologyPurpose.VOLTAGE_MONITORING));

        ReadingTypeRequirement requirementAplus =
                config
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS.getNameTranslation().getDefaultFormat(), meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS));

        ReadingTypeRequirement requirementAminus =
                config
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.A_MINUS.getNameTranslation().getDefaultFormat(), meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_MINUS));

        ReadingTypeRequirement requirementAverageVoltagePhaseA =
                config
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE.getNameTranslation().getDefaultFormat() + " phase A", meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE))
                    .overrideAttribute(ReadingTypeTemplateAttributeName.PHASE, 128);

        ReadingTypeRequirement requirementAverageVoltagePhaseB =
                config
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE.getNameTranslation().getDefaultFormat() + " phase B", meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE))
                    .overrideAttribute(ReadingTypeTemplateAttributeName.PHASE, 64);

        ReadingTypeRequirement requirementAverageVoltagePhaseC =
                config
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE.getNameTranslation().getDefaultFormat() + " phase C", meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE))
                    .overrideAttribute(ReadingTypeTemplateAttributeName.PHASE, 32);

        billingContract.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyAplusWh, requirementAplus, "Monthly A+ kWh"));
        billingContract.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyAminusWh, requirementAminus, "Monthly A- kWh"));
        billingContract.addDeliverable(buildFormulaSingleRequirement(config, readingTypeDailyAplusWh, requirementAplus, "Daily A+ kWh"));
        billingContract.addDeliverable(buildFormulaSingleRequirement(config, readingTypeDailyAminusWh, requirementAminus, "Daily A- kWh"));
        informationContract.addDeliverable(buildFormulaSingleRequirement(config, readingType15minAplusWh, requirementAplus, "15-min A+ kWh"));
        informationContract.addDeliverable(buildFormulaSingleRequirement(config, readingType15minAminusWh, requirementAminus, "15-min A- kWh"));
        voltageMonitoringContract.addDeliverable(buildFormulaSingleRequirement(config, readingTypeAverageVoltagePhaseA, requirementAverageVoltagePhaseA, "Hourly average voltage V phase 1 vs N"));
        voltageMonitoringContract.addDeliverable(buildFormulaSingleRequirement(config, readingTypeAverageVoltagePhaseB, requirementAverageVoltagePhaseB, "Hourly average voltage V phase 2 vs N"));
        voltageMonitoringContract.addDeliverable(buildFormulaSingleRequirement(config, readingTypeAverageVoltagePhaseC, requirementAverageVoltagePhaseC, "Hourly average voltage V phase 3 vs N"));
    }

    private void residentialProsumerWith2Meters() {
        if (metrologyConfigurationService.findMetrologyConfiguration("Residential prosumer with 2 meters").isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = this.findElectricityServiceCategoryOrThrowException();
        UsagePointMetrologyConfiguration configuration =
                metrologyConfigurationService
                        .newUsagePointMetrologyConfiguration("Residential prosumer with 2 meters", serviceCategory)
                        .withDescription("Typical installation for residential prosumers with dumb meters")
                        .create();

        configuration.addUsagePointRequirement(
                getUsagePointRequirement(
                        SERVICEKIND,
                        SearchablePropertyOperator.EQUAL,
                        ServiceKind.ELECTRICITY.name()));
        configuration.addUsagePointRequirement(
                getUsagePointRequirement(
                        DETAIL_PHASE_CODE,
                        SearchablePropertyOperator.EQUAL,
                        PhaseCode.S1N.name(),
                        PhaseCode.S2N.name(),
                        PhaseCode.S12N.name(),
                        PhaseCode.S1.name(),
                        PhaseCode.S2.name(),
                        PhaseCode.S12.name()));
        configuration.addUsagePointRequirement(
                getUsagePointRequirement(
                        "type",
                        SearchablePropertyOperator.EQUAL,
                        UsagePointTypeInfo.UsagePointType.MEASURED_SDP.name()));

        MeterRole meterRoleConsumption = this.findMeterRoleOrThrowException(DefaultMeterRole.CONSUMPTION);
        configuration.addMeterRole(meterRoleConsumption);
        MeterRole meterRoleProduction = this.findMeterRoleOrThrowException(DefaultMeterRole.PRODUCTION);
        configuration.addMeterRole(meterRoleProduction);

        ReadingType readingTypeMonthlyAplusWh = this.findOrCreateReadingType(MONTHLY_A_PLUS_WH, "A+");
        ReadingType readingTypeMonthlyAminusWh = this.findOrCreateReadingType(MONTHLY_A_MINUS_WH, "A-");

        MetrologyContract billingContract = configuration.addMandatoryMetrologyContract(findPurposeOrThrowException(DefaultMetrologyPurpose.BILLING));

        ReadingTypeRequirement requirementAplus =
                configuration
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS.getNameTranslation().getDefaultFormat(), meterRoleConsumption)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS));

        ReadingTypeRequirement requirementAminus =
                configuration
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.A_MINUS.getNameTranslation().getDefaultFormat(), meterRoleProduction)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_MINUS));

        billingContract.addDeliverable(buildFormulaRequirementMax(configuration, readingTypeMonthlyAplusWh, requirementAplus, requirementAminus, "Monthly A+ kWh"));
        billingContract.addDeliverable(buildFormulaSingleRequirement(configuration, readingTypeMonthlyAminusWh, requirementAminus, "Monthly A- kWh"));
    }

    private void residentialNetMeteringProduction() {
        if (metrologyConfigurationService.findMetrologyConfiguration("Residential net metering (production)").isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = this.findElectricityServiceCategoryOrThrowException();
        UsagePointMetrologyConfiguration config =
                metrologyConfigurationService
                    .newUsagePointMetrologyConfiguration("Residential net metering (production)", serviceCategory)
                    .withDescription("Residential producer")
                        .create();

        config.addUsagePointRequirement(
                getUsagePointRequirement(
                        SERVICEKIND,
                        SearchablePropertyOperator.EQUAL,
                        ServiceKind.ELECTRICITY.name()));
        config.addUsagePointRequirement(
                getUsagePointRequirement(
                        DETAIL_PHASE_CODE,
                        SearchablePropertyOperator.EQUAL,
                        PhaseCode.S1N.name(),
                        PhaseCode.S2N.name(),
                        PhaseCode.S12N.name(),
                        PhaseCode.S1.name(),
                        PhaseCode.S2.name(),
                        PhaseCode.S12.name()));
        config.addUsagePointRequirement(
                getUsagePointRequirement(
                        "type",
                        SearchablePropertyOperator.EQUAL,
                        UsagePointTypeInfo.UsagePointType.MEASURED_SDP.name()));

        MeterRole meterRole = findMeterRoleOrThrowException(DefaultMeterRole.DEFAULT);
        config.addMeterRole(meterRole);

        ReadingType readingTypeDailyAMinusWh = this.findOrCreateReadingType(DAILY_A_MINUS_WH, "A-");
        ReadingType readingTypeMonthlyAMinusWh = this.findOrCreateReadingType(MONTHLY_A_MINUS_WH, "A-");
        ReadingType readingType15minAMinusWh = this.findOrCreateReadingType(MIN15_A_MINUS_WH, "A-");
        ReadingType readingTypeHourlyAMinusWh = this.findOrCreateReadingType(HOURLY_A_MINUS_WH, "A-");

        MetrologyContract billingContract = config.addMandatoryMetrologyContract(findPurposeOrThrowException(DefaultMetrologyPurpose.BILLING));
        MetrologyContract informationContract = config.addMandatoryMetrologyContract(findPurposeOrThrowException(DefaultMetrologyPurpose.INFORMATION));

        ReadingTypeRequirement requirementAMinus =
                config
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.A_MINUS.getNameTranslation().getDefaultFormat(), meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_MINUS));

        billingContract.addDeliverable(buildFormulaSingleRequirement(config, readingTypeDailyAMinusWh, requirementAMinus, "Daily A- kWh"));
        billingContract.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyAMinusWh, requirementAMinus, "Monthly A- kWh"));
        ReadingTypeDeliverable min15 = buildFormulaSingleRequirement(config, readingType15minAMinusWh, requirementAMinus, "15-min A- kWh");
        informationContract.addDeliverable(min15);
        informationContract.addDeliverable(buildFormulaSingleDeliverable(config, readingTypeHourlyAMinusWh, min15, "Hourly A- kWh"));
    }

    private void residentialNetMeteringConsumption() {
        if (metrologyConfigurationService.findMetrologyConfiguration("Residential net metering (consumption)").isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = this.findElectricityServiceCategoryOrThrowException();
        UsagePointMetrologyConfiguration config =
                metrologyConfigurationService
                    .newUsagePointMetrologyConfiguration("Residential net metering (consumption)", serviceCategory)
                    .withDescription("Residential consumer")
                        .create();

        config.addUsagePointRequirement(
                getUsagePointRequirement(
                        SERVICEKIND,
                        SearchablePropertyOperator.EQUAL,
                        ServiceKind.ELECTRICITY.name()));
        config.addUsagePointRequirement(
                getUsagePointRequirement(
                        DETAIL_PHASE_CODE,
                        SearchablePropertyOperator.EQUAL,
                        PhaseCode.S1N.name(),
                        PhaseCode.S2N.name(),
                        PhaseCode.S12N.name(),
                        PhaseCode.S1.name(),
                        PhaseCode.S2.name(),
                        PhaseCode.S12.name()));
        config.addUsagePointRequirement(
                getUsagePointRequirement(
                        "type",
                        SearchablePropertyOperator.EQUAL,
                        UsagePointTypeInfo.UsagePointType.MEASURED_SDP.name()));

        MeterRole meterRole = findMeterRoleOrThrowException(DefaultMeterRole.DEFAULT);
        config.addMeterRole(meterRole);

        ReadingType readingTypeDailyAplusWh = this.findOrCreateReadingType(DAILY_A_PLUS_WH, "A+");
        ReadingType readingTypeMonthlyAplusWh = this.findOrCreateReadingType(MONTHLY_A_PLUS_WH, "A+");
        ReadingType readingType15minAplusWh = this.findOrCreateReadingType(MIN15_A_PLUS_WH, "A+");
        ReadingType readingTypeAplusWh = this.findOrCreateReadingType(BULK_A_PLUS_WH, "A+");

        MetrologyContract billingContract = config.addMandatoryMetrologyContract(findPurposeOrThrowException(DefaultMetrologyPurpose.BILLING));
        MetrologyContract informationContract = config.addMandatoryMetrologyContract(findPurposeOrThrowException(DefaultMetrologyPurpose.INFORMATION));

        ReadingTypeRequirement requirementAplus =
                config
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS.getNameTranslation().getDefaultFormat(), meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS));

        ReadingTypeRequirement requirementAplusRegister =
                config
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.BULK_A_PLUS.getNameTranslation().getDefaultFormat(), meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.BULK_A_PLUS));

        billingContract.addDeliverable(buildFormulaSingleRequirement(config, readingTypeDailyAplusWh, requirementAplus, "Daily A+ kWh"));
        billingContract.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyAplusWh, requirementAplus, "Monthly A+ kWh"));
        informationContract.addDeliverable(buildFormulaSingleRequirement(config, readingType15minAplusWh, requirementAplus, "15-min A+ kWh"));
        informationContract.addDeliverable(buildFormulaSingleRequirement(config, readingTypeAplusWh, requirementAplusRegister, "A+ kWh"));
    }

    private void residentialNonSmartInstallation() {
        if (metrologyConfigurationService.findMetrologyConfiguration("Residential non-smart installation").isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = this.findElectricityServiceCategoryOrThrowException();
        UsagePointMetrologyConfiguration config =
                metrologyConfigurationService
                    .newUsagePointMetrologyConfiguration("Residential non-smart installation", serviceCategory)
                    .withDescription("Registers of different types (textual, numeric)")
                        .create();

        config.addUsagePointRequirement(
                getUsagePointRequirement(
                        SERVICEKIND,
                        SearchablePropertyOperator.EQUAL,
                        ServiceKind.ELECTRICITY.name()));
        config.addUsagePointRequirement(
                getUsagePointRequirement(
                        "type",
                        SearchablePropertyOperator.EQUAL,
                        UsagePointTypeInfo.UsagePointType.MEASURED_SDP.name()));

        MeterRole meterRole = findMeterRoleOrThrowException(DefaultMeterRole.DEFAULT);
        config.addMeterRole(meterRole);

        ReadingType readingTypeBatteryStatus = this.findOrCreateReadingType(BATTERY_STATUS, "Battery status");
        ReadingType readingTypeAplusWh = this.findOrCreateReadingType(BULK_A_PLUS_WH, "A+");

        MetrologyContract informationContract = config.addMandatoryMetrologyContract(findPurposeOrThrowException(DefaultMetrologyPurpose.INFORMATION));

        ReadingTypeRequirement requirementTextual =
                config
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.BATTERY_STATUS.getNameTranslation().getDefaultFormat(), meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.BATTERY_STATUS));

        ReadingTypeRequirement requirementNumerical =
                config
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.BULK_A_PLUS.getNameTranslation().getDefaultFormat(), meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.BULK_A_PLUS));

        informationContract.addDeliverable(buildFormulaSingleRequirement(config, DeliverableType.TEXT, readingTypeBatteryStatus, requirementTextual, "Battery status"));
        informationContract.addDeliverable(buildFormulaSingleRequirement(config, DeliverableType.NUMERICAL, readingTypeAplusWh, requirementNumerical, "A+ kWh"));
    }

    private void residentialGasNonSmartInstallation() {
        if (metrologyConfigurationService.findMetrologyConfiguration("Residential gas non-smart installation").isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = this.findElectricityServiceCategoryOrThrowException();
        UsagePointMetrologyConfiguration config =
                metrologyConfigurationService
                    .newUsagePointMetrologyConfiguration("Residential gas non-smart installation", serviceCategory)
                    .withDescription("Billing register").create();

        config.addUsagePointRequirement(
                getUsagePointRequirement(
                        SERVICEKIND,
                        SearchablePropertyOperator.EQUAL,
                        ServiceKind.GAS.name()));
        config.addUsagePointRequirement(
                getUsagePointRequirement(
                        "type",
                        SearchablePropertyOperator.EQUAL,
                        UsagePointTypeInfo.UsagePointType.MEASURED_SDP.name()));

        MeterRole meterRole = findMeterRoleOrThrowException(DefaultMeterRole.DEFAULT);
        config.addMeterRole(meterRole);

        ReadingType readingTypeGasFlow = this.findOrCreateReadingType(BILLING_GAS_FLOW, "Billing gas flow");

        MetrologyContract informationContract = config.addMandatoryMetrologyContract(findPurposeOrThrowException(DefaultMetrologyPurpose.INFORMATION));

        ReadingTypeRequirement requirementBilling =
                config
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.GAS_FLOW_BILLING.getNameTranslation().getDefaultFormat(), meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.GAS_FLOW_BILLING));

        informationContract.addDeliverable(buildFormulaSingleRequirement(config, DeliverableType.BILLING, readingTypeGasFlow, requirementBilling, "Billing Gas flow m3/h"));
    }

    private void threePhasedConsumerWith2ToU() {
        if (metrologyConfigurationService.findMetrologyConfiguration("C&I 3-phased consumer with smart meter with 2 ToU").isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = this.findElectricityServiceCategoryOrThrowException();
        UsagePointMetrologyConfiguration config =
                metrologyConfigurationService
                    .newUsagePointMetrologyConfiguration("C&I 3-phased consumer with smart meter with 2 ToU", serviceCategory)
                    .withDescription("C&I 3-phased consumer with smart meter 2 ToU")
                    .create();

        config.addUsagePointRequirement(
                getUsagePointRequirement(
                        SERVICEKIND,
                        SearchablePropertyOperator.EQUAL,
                        ServiceKind.ELECTRICITY.name()));
        config.addUsagePointRequirement(
                getUsagePointRequirement(
                        DETAIL_PHASE_CODE,
                        SearchablePropertyOperator.EQUAL,
                        PhaseCode.ABC.name(),
                        PhaseCode.ABCN.name()));
        config.addUsagePointRequirement(
                getUsagePointRequirement(
                        "type",
                        SearchablePropertyOperator.EQUAL,
                        UsagePointTypeInfo.UsagePointType.MEASURED_SDP.name()));

        MeterRole meterRole = findMeterRoleOrThrowException(DefaultMeterRole.DEFAULT);
        config.addMeterRole(meterRole);

        ReadingType readingTypeDailyActiveEnergyToU1 = this.findOrCreateReadingType("11.0.0.4.1.1.12.0.0.0.0.1.0.0.0.3.72.0", "A+");
        ReadingType readingTypeDailyActiveEnergyToU2 = this.findOrCreateReadingType("11.0.0.4.1.1.12.0.0.0.0.2.0.0.0.3.72.0", "A+");
        ReadingType readingTypeDailyReactiveEnergyToU1 = this.findOrCreateReadingType("11.0.0.4.2.1.12.0.0.0.0.1.0.0.0.3.73.0", REACTIVE_ENERGY_PLUS);
        ReadingType readingTypeDailyReactiveEnergyToU2 = this.findOrCreateReadingType("11.0.0.4.2.1.12.0.0.0.0.2.0.0.0.3.73.0", REACTIVE_ENERGY_PLUS);
        ReadingType readingTypeMonthlyActiveEnergyToU1 = this.findOrCreateReadingType(ACTIVE_ENERGY_TOU1, "A+");
        ReadingType readingTypeMonthlyActiveEnergyToU2 = this.findOrCreateReadingType(ACTIVE_ENERGY_TOU2, "A+");
        ReadingType readingTypeMonthlyReactiveEnergyToU1 = this.findOrCreateReadingType("13.0.0.4.2.1.12.0.0.0.0.1.0.0.0.3.73.0", REACTIVE_ENERGY_PLUS);
        ReadingType readingTypeMonthlyReactiveEnergyToU2 = this.findOrCreateReadingType("13.0.0.4.2.1.12.0.0.0.0.2.0.0.0.3.73.0", REACTIVE_ENERGY_PLUS);
        ReadingType readingTypeAverageVoltagePhaseA = this.findOrCreateReadingType("0.2.7.6.0.1.158.0.0.0.0.0.0.0.128.0.29.0", "Average voltage");
        ReadingType readingTypeAverageVoltagePhaseB = this.findOrCreateReadingType("0.2.7.6.0.1.158.0.0.0.0.0.0.0.64.0.29.0", "Average voltage");
        ReadingType readingTypeAverageVoltagePhaseC = this.findOrCreateReadingType("0.2.7.6.0.1.158.0.0.0.0.0.0.0.32.0.29.0", "Average voltage");

        MetrologyContract billingContract = config.addMandatoryMetrologyContract(findPurposeOrThrowException(DefaultMetrologyPurpose.BILLING));
        MetrologyContract voltageMonitoringContract = config.addMandatoryMetrologyContract(findPurposeOrThrowException(DefaultMetrologyPurpose.VOLTAGE_MONITORING));

        ReadingTypeRequirement requirementAplusToU1 =
                config
                    .newReadingTypeRequirement("Active energy+ ToU1", meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS))
                    .overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 1);
        ReadingTypeRequirement requirementAplusToU2 =
                config
                    .newReadingTypeRequirement("Active energy+ ToU2", meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS))
                    .overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 2);
        ReadingTypeRequirement requirementReactiveEnergyPlusToU1 =
                config
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.REACTIVE_ENERGY_PLUS.getNameTranslation().getDefaultFormat() + " ToU1", meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.REACTIVE_ENERGY_PLUS))
                    .overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 1);
        ReadingTypeRequirement requirementReactiveEnergyPlusToU2 =
                config
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.REACTIVE_ENERGY_PLUS.getNameTranslation().getDefaultFormat() + " ToU2", meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.REACTIVE_ENERGY_PLUS))
                    .overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 2);
        ReadingTypeRequirement requirementAverageVoltagePhaseA =
                config
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE.getNameTranslation().getDefaultFormat() + " phase A", meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE))
                    .overrideAttribute(ReadingTypeTemplateAttributeName.PHASE, 128);
        ReadingTypeRequirement requirementAverageVoltagePhaseB =
                config
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE.getNameTranslation().getDefaultFormat() + " phase B", meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE))
                    .overrideAttribute(ReadingTypeTemplateAttributeName.PHASE, 64);
        ReadingTypeRequirement requirementAverageVoltagePhaseC =
                config
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE.getNameTranslation().getDefaultFormat() + " phase C", meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE))
                    .overrideAttribute(ReadingTypeTemplateAttributeName.PHASE, 32);

        billingContract.addDeliverable(buildFormulaSingleRequirement(config, readingTypeDailyActiveEnergyToU1, requirementAplusToU1, "Daily active energy kWh ToU1"));
        billingContract.addDeliverable(buildFormulaSingleRequirement(config, readingTypeDailyActiveEnergyToU2, requirementAplusToU2, "Daily active energy kWh ToU2"));
        billingContract.addDeliverable(buildFormulaSingleRequirement(config, readingTypeDailyReactiveEnergyToU1, requirementReactiveEnergyPlusToU1, "Daily reactive energy kVArh ToU1"));
        billingContract.addDeliverable(buildFormulaSingleRequirement(config, readingTypeDailyReactiveEnergyToU2, requirementReactiveEnergyPlusToU2, "Daily reactive energy kVArh ToU2"));
        billingContract.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyActiveEnergyToU1, requirementAplusToU1, "Monthly active energy kWh ToU1"));
        billingContract.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyActiveEnergyToU2, requirementAplusToU2, "Monthly active energy kWh ToU2"));
        billingContract.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyReactiveEnergyToU1, requirementReactiveEnergyPlusToU1, "Monthly reactive energy kVArh ToU1"));
        billingContract.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyReactiveEnergyToU2, requirementReactiveEnergyPlusToU2, "Monthly reactive energy kVArh ToU2"));
        voltageMonitoringContract.addDeliverable(buildFormulaSingleRequirement(config, readingTypeAverageVoltagePhaseA, requirementAverageVoltagePhaseA, "Hourly average voltage V phase 1 vs N"));
        voltageMonitoringContract.addDeliverable(buildFormulaSingleRequirement(config, readingTypeAverageVoltagePhaseB, requirementAverageVoltagePhaseB, "Hourly average voltage V phase 2 vs N"));
        voltageMonitoringContract.addDeliverable(buildFormulaSingleRequirement(config, readingTypeAverageVoltagePhaseC, requirementAverageVoltagePhaseC, "Hourly average voltage V phase 3 vs N"));
    }

    private void residentialConsumerWith4ToU() {
        if (metrologyConfigurationService.findMetrologyConfiguration("Residential consumer with 4 ToU").isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = this.findElectricityServiceCategoryOrThrowException();
        UsagePointMetrologyConfiguration config =
                metrologyConfigurationService
                    .newUsagePointMetrologyConfiguration("Residential consumer with 4 ToU", serviceCategory)
                    .withDescription("Residential consumer with 4 ToU")
                    .create();

        config.addUsagePointRequirement(
                getUsagePointRequirement(
                        SERVICEKIND,
                        SearchablePropertyOperator.EQUAL,
                        ServiceKind.ELECTRICITY.name()));
        config.addUsagePointRequirement(
                getUsagePointRequirement(
                        DETAIL_PHASE_CODE,
                        SearchablePropertyOperator.EQUAL,
                        PhaseCode.S1N.name(),
                        PhaseCode.S2N.name(),
                        PhaseCode.S12N.name(),
                        PhaseCode.S1.name(),
                        PhaseCode.S2.name(),
                        PhaseCode.S12.name()));
        config.addUsagePointRequirement(
                getUsagePointRequirement(
                        "type",
                        SearchablePropertyOperator.EQUAL,
                        UsagePointTypeInfo.UsagePointType.MEASURED_SDP.name()));

        MeterRole meterRole = findMeterRoleOrThrowException(DefaultMeterRole.DEFAULT);
        config.addMeterRole(meterRole);

        ReadingType readingTypeMonthlyAplusToU1 = this.findOrCreateReadingType(ACTIVE_ENERGY_TOU1, "A+");
        ReadingType readingTypeMonthlyAplusToU2 = this.findOrCreateReadingType(ACTIVE_ENERGY_TOU2, "A+");
        ReadingType readingTypeMonthlyAplusToU3 = this.findOrCreateReadingType("13.0.0.4.1.1.12.0.0.0.0.3.0.0.0.3.72.0", "A+");
        ReadingType readingTypeMonthlyAplusToU4 = this.findOrCreateReadingType("13.0.0.4.1.1.12.0.0.0.0.4.0.0.0.3.72.0", "A+");

        MetrologyContract billingContract = config.addMandatoryMetrologyContract(findPurposeOrThrowException(DefaultMetrologyPurpose.BILLING));

        ReadingTypeRequirement requirementAplusToU1 =
                config
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS.getNameTranslation().getDefaultFormat() + " ToU1", meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS))
                    .overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 1);
        ReadingTypeRequirement requirementAplusToU2 =
                config
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS.getNameTranslation().getDefaultFormat() + " ToU2", meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS))
                    .overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 2);
        ReadingTypeRequirement requirementAplusToU3 =
                config
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS.getNameTranslation().getDefaultFormat() + " ToU3", meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS))
                    .overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 3);
        ReadingTypeRequirement requirementAplusToU4 =
                config
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS.getNameTranslation().getDefaultFormat() + " ToU4", meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS))
                    .overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 4);

        billingContract.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyAplusToU1, requirementAplusToU1, "Monthly A+ kWh ToU1"));
        billingContract.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyAplusToU2, requirementAplusToU2, "Monthly A+ kWh ToU2"));
        billingContract.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyAplusToU3, requirementAplusToU3, "Monthly A+ kWh ToU3"));
        billingContract.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyAplusToU4, requirementAplusToU4, "Monthly A+ kWh ToU4"));
    }

    private void residentialGas() {
        if (metrologyConfigurationService.findMetrologyConfiguration("Residential gas").isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = this.findGasServiceCategoryOrThrowException();
        UsagePointMetrologyConfiguration config =
                metrologyConfigurationService
                    .newUsagePointMetrologyConfiguration("Residential gas", serviceCategory)
                    .withDescription("Residential gas installation")
                        .create();

        config.addUsagePointRequirement(
                getUsagePointRequirement(
                        SERVICEKIND,
                        SearchablePropertyOperator.EQUAL,
                        ServiceKind.GAS.name()));
        config.addUsagePointRequirement(
                getUsagePointRequirement(
                        "type",
                        SearchablePropertyOperator.EQUAL,
                        UsagePointTypeInfo.UsagePointType.MEASURED_SDP.name()));

        MeterRole meterRole = findMeterRoleOrThrowException(DefaultMeterRole.DEFAULT);
        config.addMeterRole(meterRole);

        ReadingType readingTypeDailyVolume = this.findOrCreateReadingType("11.0.0.4.1.7.58.0.0.0.0.0.0.0.0.0.42.0", "Daily volume m³");
        ReadingType readingTypeMonthlyVolume = this.findOrCreateReadingType("13.0.0.4.1.7.58.0.0.0.0.0.0.0.0.0.42.0", "Monthly volume m³");
        ReadingType readingTypeHourlyVolume = this.findOrCreateReadingType("0.0.7.4.1.7.58.0.0.0.0.0.0.0.0.0.42.0", "Hourly volume m³");

        MetrologyContract billingContract = config.addMandatoryMetrologyContract(findPurposeOrThrowException(DefaultMetrologyPurpose.BILLING));
        MetrologyContract informationContract = config.addMetrologyContract(findPurposeOrThrowException(DefaultMetrologyPurpose.INFORMATION));

        ReadingTypeRequirement requirementGasVolume =
                config
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.GAS_VOLUME.getNameTranslation().getDefaultFormat(), meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.GAS_VOLUME))
                    .overrideAttribute(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, 42);

        billingContract.addDeliverable(buildFormulaSingleRequirement(config, readingTypeDailyVolume, requirementGasVolume, "Daily volume m³"));
        billingContract.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyVolume, requirementGasVolume, "Monthly volume m³"));
        informationContract.addDeliverable(buildFormulaSingleRequirement(config, readingTypeHourlyVolume, requirementGasVolume, "Hourly volume m³"));
    }

    private void waterConfigurationCI() {
        if (metrologyConfigurationService.findMetrologyConfiguration("C&I water configuration").isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = this.findWaterServiceCategoryOrThrowException();
        UsagePointMetrologyConfiguration config =
                metrologyConfigurationService
                    .newUsagePointMetrologyConfiguration("C&I water configuration", serviceCategory)
                    .withDescription("C&I water configuration with 2 meters")
                        .create();

        config.addUsagePointRequirement(
                getUsagePointRequirement(
                        SERVICEKIND,
                        SearchablePropertyOperator.EQUAL,
                        ServiceKind.WATER.name()));
        config.addUsagePointRequirement(
                getUsagePointRequirement(
                        "type",
                        SearchablePropertyOperator.EQUAL,
                        UsagePointTypeInfo.UsagePointType.MEASURED_SDP.name()));

        MeterRole meterRolePeakConsumption = findMeterRoleOrThrowException(DefaultMeterRole.PEAK_CONSUMPTION);
        config.addMeterRole(meterRolePeakConsumption);
        MeterRole meterRoleOffPeakConsumption = this.findMeterRoleOrThrowException(DefaultMeterRole.OFF_PEAK_CONSUMPTION);
        config.addMeterRole(meterRoleOffPeakConsumption);

        ReadingType readingTypeMonthlyConsumption = this.findOrCreateReadingType("13.0.0.4.1.9.58.0.0.0.0.0.0.0.0.0.42.0", "Monthly consumption m³");

        MetrologyContract billingContract = config.addMandatoryMetrologyContract(findPurposeOrThrowException(DefaultMetrologyPurpose.BILLING));

        ReadingTypeRequirement requirementPeakConsumption =
                config
                    .newReadingTypeRequirement("Peak consumption", meterRolePeakConsumption)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.WATER_VOLUME))
                    .overrideAttribute(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, 42);
        ReadingTypeRequirement requirementOffPeakConsumption =
                config
                    .newReadingTypeRequirement("Off peak consumption", meterRoleOffPeakConsumption)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.WATER_VOLUME))
                    .overrideAttribute(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, 42);

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Monthly consumption m³", readingTypeMonthlyConsumption, Formula.Mode.AUTO);
        billingContract.addDeliverable(
                builder.build(
                        builder.plus(
                                builder.requirement(requirementPeakConsumption),
                                builder.requirement(requirementOffPeakConsumption))));
    }

    ReadingTypeDeliverable buildFormulaSingleRequirement(UsagePointMetrologyConfiguration config, ReadingType readingType, ReadingTypeRequirement requirement, String name) {
        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable(name, readingType, Formula.Mode.AUTO);
        return builder.build(builder.requirement(requirement));
    }

    ReadingTypeDeliverable buildFormulaSingleDeliverable(UsagePointMetrologyConfiguration config, ReadingType readingType, ReadingTypeDeliverable underlying, String name) {
        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable(name, readingType, Formula.Mode.AUTO);
        return builder.build(builder.deliverable(underlying));
    }

    ReadingTypeDeliverable buildFormulaSingleRequirement(UsagePointMetrologyConfiguration config, DeliverableType deliverableType, ReadingType readingType, ReadingTypeRequirement requirement, String name) {
        ReadingTypeDeliverableBuilder builder =
                config.newReadingTypeDeliverable(
                        name,
                        deliverableType,
                        readingType,
                        deliverableType.equals(DeliverableType.TEXT) ? Formula.Mode.EXPERT : Formula.Mode.AUTO);
        return builder.build(builder.requirement(requirement));
    }

    ReadingTypeDeliverable buildFormulaRequirementMax(UsagePointMetrologyConfiguration config, ReadingType readingType,
                                                              ReadingTypeRequirement requirementPlus, ReadingTypeRequirement requirementMinus, String name) {
        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable(name, readingType, Formula.Mode.AUTO);
        return builder.build(
                builder.maximum(
                        builder.minus(
                                builder.requirement(requirementPlus),
                                builder.requirement(requirementMinus)),
                        builder.constant(0)));
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

    private ServiceCategory findElectricityServiceCategoryOrThrowException() {
        return this.findServiceCategoryOrThrowException(ServiceKind.ELECTRICITY);
    }

    private ServiceCategory findGasServiceCategoryOrThrowException() {
        return this.findServiceCategoryOrThrowException(ServiceKind.GAS);
    }

    private ServiceCategory findWaterServiceCategoryOrThrowException() {
        return this.findServiceCategoryOrThrowException(ServiceKind.WATER);
    }

    private ServiceCategory findServiceCategoryOrThrowException(ServiceKind serviceKind) {
        return meteringService
                .getServiceCategory(serviceKind)
                .orElseThrow(() -> new NoSuchElementException(SERVICE_CATEGORY_NOT_FOUND + serviceKind));
    }

    private MeterRole findMeterRoleOrThrowException(DefaultMeterRole meterRole) {
        return metrologyConfigurationService
                .findMeterRole(meterRole.getKey())
                .orElseThrow(() -> new NoSuchElementException(ROLE_NOT_FOUND));
    }

    private MetrologyPurpose findPurposeOrThrowException(DefaultMetrologyPurpose purpose) {
        return metrologyConfigurationService.findMetrologyPurpose(purpose)
                .orElseThrow(() -> new NoSuchElementException(purpose.getName().getDefaultMessage() + " metrology purpose not found"));
    }

    private ReadingType findOrCreateReadingType(String mRID, String aliasName) {
        return this.meteringService
                .findReadingTypes(Collections.singletonList(mRID))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(mRID, aliasName));
    }

}