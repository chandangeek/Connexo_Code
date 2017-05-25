/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.EventSet;
import com.elster.jupiter.cbo.MacroPeriod;
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
import static com.elster.jupiter.mdm.usagepoint.config.impl.MetrologyConfigurationsInstaller.OOTBMetrologyConfiguration.MAIN_CHECK_CONSUMPTION;
import static com.elster.jupiter.mdm.usagepoint.config.impl.MetrologyConfigurationsInstaller.OOTBMetrologyConfiguration.RESIDENTAL_WATER;
import static com.elster.jupiter.mdm.usagepoint.config.impl.MetrologyConfigurationsInstaller.OOTBMetrologyConfiguration.RESIDENTIAL_CONSUMER_WITH_4_TOU;
import static com.elster.jupiter.mdm.usagepoint.config.impl.MetrologyConfigurationsInstaller.OOTBMetrologyConfiguration.RESIDENTIAL_GAS;
import static com.elster.jupiter.mdm.usagepoint.config.impl.MetrologyConfigurationsInstaller.OOTBMetrologyConfiguration.RESIDENTIAL_GAS_NON_SMART_INSTALLATION;
import static com.elster.jupiter.mdm.usagepoint.config.impl.MetrologyConfigurationsInstaller.OOTBMetrologyConfiguration.RESIDENTIAL_NET_METERING_CONSUMPTION;
import static com.elster.jupiter.mdm.usagepoint.config.impl.MetrologyConfigurationsInstaller.OOTBMetrologyConfiguration.RESIDENTIAL_NET_METERING_PRODUCTION;
import static com.elster.jupiter.mdm.usagepoint.config.impl.MetrologyConfigurationsInstaller.OOTBMetrologyConfiguration.RESIDENTIAL_NON_SMART_INSTALLATION;
import static com.elster.jupiter.mdm.usagepoint.config.impl.MetrologyConfigurationsInstaller.OOTBMetrologyConfiguration.RESIDENTIAL_PROSUMER_WITH_1_METER;
import static com.elster.jupiter.mdm.usagepoint.config.impl.MetrologyConfigurationsInstaller.OOTBMetrologyConfiguration.RESIDENTIAL_PROSUMER_WITH_2_METERS;

class MetrologyConfigurationsInstaller {

    private static final int PEAK_CODE = 11;     // Note that this is true for the Belgian market
    private static final int OFFPEAK_CODE = 10;  // Note that this is true for the Belgian market

    static final int PHASE_A = 128;
    static final int PHASE_B = 64;
    static final int PHASE_C = 32;

    private static final int CUBIC_METRE = 42;

    private static final String SERVICE_CATEGORY_NOT_FOUND = "Service category not found: ";
    private static final String SERVICEKIND = "SERVICEKIND";
    private static final String DETAIL_PHASE_CODE = "detail.phaseCode";
    private static final String ROLE_NOT_FOUND = "Default meter role not found";
    private static final String REACTIVE_ENERGY_PLUS = "Reactive energy+";
    private static final String DAILY_A_PLUS_KWH = "11.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private static final String DAILY_A_MINUS_KWH = "11.0.0.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private static final String ACTIVE_ENERGY_TOU1 = "13.0.0.4.1.1.12.0.0.0.0.1.0.0.0.3.72.0";
    private static final String ACTIVE_ENERGY_TOU2 = "13.0.0.4.1.1.12.0.0.0.0.2.0.0.0.3.72.0";
    private static final String BATTERY_STATUS = "0.0.0.12.0.41.11.0.0.0.0.0.0.0.0.-2.0.0";
    private static final String BILLING_GAS_FLOW = "8.2.0.6.0.7.58.0.0.0.0.0.0.0.0.0.125.0";

    public static final String MIN15_A_PLUS_KWH = "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    public static final String MIN15_A_MINUS_KWH = "0.0.2.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0";
    public static final String HOURLY_A_MINUS_KWH = "0.0.7.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0";
    public static final String MONTHLY_A_PLUS_KWH = "13.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    public static final String MONTHLY_A_MINUS_KWH = "13.0.0.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0";
    public static final String MONTHLY_NET_KWH = "13.0.0.4.4.1.12.0.0.0.0.0.0.0.0.3.72.0";
    public static final String YEARLY_A_MINUS_KWH = "1001.0.0.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0";
    public static final String YEARLY_NET_KWH = "1001.0.0.4.4.1.12.0.0.0.0.0.0.0.0.3.72.0";
    public static final String BULK_A_PLUS_KWH = "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    public static final String TIME_OF_USER_EVENT_SET_NAME = "Peak/Offpeak (Belgium)";

    private final MetrologyConfigurationService metrologyConfigurationService;
    private final MeteringService meteringService;
    private final CalendarService calendarService;

    MetrologyConfigurationsInstaller(CalendarService calendarService, MetrologyConfigurationService metrologyConfigurationService, MeteringService meteringService) {
        super();
        this.calendarService = calendarService;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.meteringService = meteringService;
    }

    private void install() {
        Arrays.stream(OOTBMetrologyConfiguration.values()).forEach(c -> c.install(this));
        EventSet eventSet = this.createTimeOfUseEventSet();
        residentialNetMeteringConsumptionThickTimeOfUse(eventSet);
        residentialNetMeteringConsumptionThinTimeOfUse(eventSet);
    }

    /**
     * {@link OOTBMetrologyConfiguration} describes OOTB metrology configurations
     */
    public enum OOTBMetrologyConfiguration {
        RESIDENTIAL_PROSUMER_WITH_1_METER("Residential prosumer with 1 meter", "Typical installation for residential " +
                "prosumers with smart meter", false, MetrologyConfigurationsInstaller::residentialProsumerWith1Meter),
        RESIDENTIAL_PROSUMER_WITH_2_METERS("Residential prosumer with 2 meters", "Typical installation for " +
                "residential prosumers with dumb meters", false, MetrologyConfigurationsInstaller::residentialProsumerWith2Meters),
        RESIDENTIAL_NET_METERING_PRODUCTION("Residential net metering (production)", "Residential producer", true, MetrologyConfigurationsInstaller::residentialNetMeteringProduction),
        RESIDENTIAL_NET_METERING_CONSUMPTION("Residential net metering (consumption)", "Residential consumer", true, MetrologyConfigurationsInstaller::residentialNetMeteringConsumption),
        RESIDENTIAL_NON_SMART_INSTALLATION("Residential non-smart installation", "Registers of different types " +
                "(textual, numeric)", true, MetrologyConfigurationsInstaller::residentialNonSmartInstallation),
        RESIDENTIAL_GAS_NON_SMART_INSTALLATION("Residential gas non-smart installation", "Billing register", true, MetrologyConfigurationsInstaller::residentialGasNonSmartInstallation),
        CI_3_PHASED_CONSUMER_WITH_SMART_METER_WITH_2_TOU("C&I 3-phased consumer with smart meter with 2 ToU", "C&I " +
                "3-phased consumer with smart meter 2 ToU", true, MetrologyConfigurationsInstaller::threePhasedConsumerWith2ToU),
        RESIDENTIAL_CONSUMER_WITH_4_TOU("Residential consumer with 4 ToU", "Residential consumer with 4 ToU", true, MetrologyConfigurationsInstaller::residentialConsumerWith4ToU),
        RESIDENTIAL_GAS("Residential gas", "Residential gas installation", true, MetrologyConfigurationsInstaller::residentialGas),
        RESIDENTAL_WATER("Residential water", "Residential water", true, MetrologyConfigurationsInstaller::residentialWater),
        CI_WATER_CONFIGURATION("C&I water configuration", "C&I water configuration with 2 meters", true, MetrologyConfigurationsInstaller::waterConfigurationCI),
        MAIN_CHECK_CONSUMPTION("Main/check (consumption)", "Metrology configuration with check meter", true, MetrologyConfigurationsInstaller::mainCheckConsumption);

        OOTBMetrologyConfiguration(String name, String description, boolean gapsAllowed, InstallationFunction installationFunction) {
            this.name = name;
            this.description = description;
            this.gapsAllowed = gapsAllowed;
            this.installationFunction = installationFunction;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public boolean areGapsAllowed() {
            return gapsAllowed;
        }

        public void install(MetrologyConfigurationsInstaller metrologyConfigurationsInstaller) {
            installationFunction.install(metrologyConfigurationsInstaller);
        }

        private String name;
        private String description;
        private boolean gapsAllowed;
        private InstallationFunction installationFunction;

        public interface InstallationFunction {
            void install(MetrologyConfigurationsInstaller metrologyConfigurationsInstaller);
        }

    }

    private void residentialProsumerWith1Meter() {
        if (metrologyConfigurationService.findMetrologyConfiguration(RESIDENTIAL_PROSUMER_WITH_1_METER.getName()).isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = this.findElectricityServiceCategoryOrThrowException();
        UsagePointMetrologyConfiguration configuration =
            metrologyConfigurationService
                    .newUsagePointMetrologyConfiguration(RESIDENTIAL_PROSUMER_WITH_1_METER.getName(), serviceCategory)
                    .withDescription(RESIDENTIAL_PROSUMER_WITH_1_METER.getDescription())
                    .withGapsAllowed(RESIDENTIAL_PROSUMER_WITH_1_METER.areGapsAllowed())
                    .withUsagePointRequirement(
                            getUsagePointRequirement(
                                    SERVICEKIND,
                                    SearchablePropertyOperator.EQUAL,
                                    ServiceKind.ELECTRICITY.name()))
                    .withUsagePointRequirement(
                            getUsagePointRequirement(
                                    DETAIL_PHASE_CODE,
                                    SearchablePropertyOperator.EQUAL,
                                    PhaseCode.S1N.name(),
                                    PhaseCode.S2N.name(),
                                    PhaseCode.S12N.name(),
                                    PhaseCode.S1.name(),
                                    PhaseCode.S2.name(),
                                    PhaseCode.S12.name()))
                    .withUsagePointRequirement(
                            getUsagePointRequirement(
                                    "type",
                                    SearchablePropertyOperator.EQUAL,
                                    UsagePointTypeInfo.UsagePointType.MEASURED_SDP.name()))
                    .create();

        MeterRole meterRole = findMeterRoleOrThrowException(DefaultMeterRole.DEFAULT);
        configuration.addMeterRole(meterRole);

        ReadingType readingTypeMonthlyAplusWh = this.findOrCreateReadingType(MONTHLY_A_PLUS_KWH, "A+");
        ReadingType readingTypeMonthlyAminusWh = this.findOrCreateReadingType(MONTHLY_A_MINUS_KWH, "A-");
        ReadingType readingTypeDailyAplusWh = this.findOrCreateReadingType(DAILY_A_PLUS_KWH, "A+");
        ReadingType readingTypeDailyAminusWh = this.findOrCreateReadingType(DAILY_A_MINUS_KWH, "A-");
        ReadingType readingType15minAplusWh = this.findOrCreateReadingType(MIN15_A_PLUS_KWH, "A+");
        ReadingType readingType15minAminusWh = this.findOrCreateReadingType(MIN15_A_MINUS_KWH, "A-");
        ReadingType readingTypeAverageVoltagePhaseA = this.findOrCreateReadingType("0.2.7.6.0.1.158.0.0.0.0.0.0.0.128.0.29.0", "Average voltage");
        ReadingType readingTypeAverageVoltagePhaseB = this.findOrCreateReadingType("0.2.7.6.0.1.158.0.0.0.0.0.0.0.64.0.29.0", "Average voltage");
        ReadingType readingTypeAverageVoltagePhaseC = this.findOrCreateReadingType("0.2.7.6.0.1.158.0.0.0.0.0.0.0.32.0.29.0", "Average voltage");

        MetrologyContract billingContract = configuration.addMandatoryMetrologyContract(findPurposeOrThrowException(DefaultMetrologyPurpose.BILLING));
        MetrologyContract informationContract = configuration.addMetrologyContract(findPurposeOrThrowException(DefaultMetrologyPurpose.INFORMATION));
        MetrologyContract voltageMonitoringContract = configuration.addMetrologyContract(findPurposeOrThrowException(DefaultMetrologyPurpose.VOLTAGE_MONITORING));

        ReadingTypeRequirement requirementAplus =
                configuration
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS.getNameTranslation().getDefaultFormat(), meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS));

        ReadingTypeRequirement requirementAminus =
                configuration
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.A_MINUS.getNameTranslation().getDefaultFormat(), meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_MINUS));

        ReadingTypeRequirement requirementAverageVoltagePhaseA =
                configuration
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE.getNameTranslation().getDefaultFormat() + " phase A", meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE))
                    .overrideAttribute(ReadingTypeTemplateAttributeName.PHASE, PHASE_A);

        ReadingTypeRequirement requirementAverageVoltagePhaseB =
                configuration
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE.getNameTranslation().getDefaultFormat() + " phase B", meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE))
                    .overrideAttribute(ReadingTypeTemplateAttributeName.PHASE, PHASE_B);

        ReadingTypeRequirement requirementAverageVoltagePhaseC =
                configuration
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE.getNameTranslation().getDefaultFormat() + " phase C", meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE))
                    .overrideAttribute(ReadingTypeTemplateAttributeName.PHASE, PHASE_C);

        buildFormulaSingleRequirement(billingContract, readingTypeMonthlyAplusWh, requirementAplus, "Monthly A+ kWh");
        buildFormulaSingleRequirement(billingContract, readingTypeMonthlyAminusWh, requirementAminus, "Monthly A- kWh");
        buildFormulaSingleRequirement(billingContract, readingTypeDailyAplusWh, requirementAplus, "Daily A+ kWh");
        buildFormulaSingleRequirement(billingContract, readingTypeDailyAminusWh, requirementAminus, "Daily A- kWh");
        buildFormulaSingleRequirement(informationContract, readingType15minAplusWh, requirementAplus, "15-min A+ kWh");
        buildFormulaSingleRequirement(informationContract, readingType15minAminusWh, requirementAminus, "15-min A- kWh");
        buildFormulaSingleRequirement(voltageMonitoringContract, readingTypeAverageVoltagePhaseA, requirementAverageVoltagePhaseA, "Hourly average voltage V phase 1 vs N");
        buildFormulaSingleRequirement(voltageMonitoringContract, readingTypeAverageVoltagePhaseB, requirementAverageVoltagePhaseB, "Hourly average voltage V phase 2 vs N");
        buildFormulaSingleRequirement(voltageMonitoringContract, readingTypeAverageVoltagePhaseC, requirementAverageVoltagePhaseC, "Hourly average voltage V phase 3 vs N");
    }

    private void residentialProsumerWith2Meters() {
        if (metrologyConfigurationService.findMetrologyConfiguration(RESIDENTIAL_PROSUMER_WITH_2_METERS.getName()).isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = this.findElectricityServiceCategoryOrThrowException();
        UsagePointMetrologyConfiguration configuration =
            metrologyConfigurationService
                .newUsagePointMetrologyConfiguration(RESIDENTIAL_PROSUMER_WITH_2_METERS.getName(), serviceCategory)
                .withDescription(RESIDENTIAL_PROSUMER_WITH_2_METERS.getDescription())
                .withGapsAllowed(RESIDENTIAL_PROSUMER_WITH_2_METERS.areGapsAllowed())
                .withUsagePointRequirement(
                        getUsagePointRequirement(
                                SERVICEKIND,
                                SearchablePropertyOperator.EQUAL,
                                ServiceKind.ELECTRICITY.name()))
                .withUsagePointRequirement(
                        getUsagePointRequirement(
                                DETAIL_PHASE_CODE,
                                SearchablePropertyOperator.EQUAL,
                                PhaseCode.S1N.name(),
                                PhaseCode.S2N.name(),
                                PhaseCode.S12N.name(),
                                PhaseCode.S1.name(),
                                PhaseCode.S2.name(),
                                PhaseCode.S12.name()))
                .withUsagePointRequirement(
                        getUsagePointRequirement(
                                "type",
                                SearchablePropertyOperator.EQUAL,
                                UsagePointTypeInfo.UsagePointType.MEASURED_SDP.name()))
                .create();

        MeterRole consumptionMeterRole = this.findMeterRoleOrThrowException(DefaultMeterRole.CONSUMPTION);
        configuration.addMeterRole(consumptionMeterRole);
        MeterRole productionMeterRole = this.findMeterRoleOrThrowException(DefaultMeterRole.PRODUCTION);
        configuration.addMeterRole(productionMeterRole);

        ReadingType readingTypeMonthlyNetWh = meteringService.findReadingTypes(Collections.singletonList(MONTHLY_NET_KWH))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(MONTHLY_NET_KWH, "Monthly net kWh"));
        ReadingType readingTypeMonthlyAminusWh = meteringService.findReadingTypes(Collections.singletonList(MONTHLY_A_MINUS_KWH))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(MONTHLY_A_MINUS_KWH, "A-"));
        ReadingType readingTypeYearlyNetWh = meteringService.findReadingTypes(Collections.singletonList(YEARLY_NET_KWH))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(YEARLY_NET_KWH, "Yearly net kWh"));
        ReadingType readingTypeYearlyAminusWh = meteringService.findReadingTypes(Collections.singletonList(YEARLY_A_MINUS_KWH))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(YEARLY_A_MINUS_KWH, "A-"));

        MetrologyContract billingContract = configuration.addMandatoryMetrologyContract(findPurposeOrThrowException(DefaultMetrologyPurpose.BILLING));

        ReadingTypeRequirement requirementAplus =
                configuration
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS.getNameTranslation().getDefaultFormat(), consumptionMeterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS));

        ReadingTypeRequirement requirementAminus =
                configuration
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.A_MINUS.getNameTranslation().getDefaultFormat(), productionMeterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_MINUS));

        buildNonNegativeNetFormula(billingContract, readingTypeMonthlyNetWh, requirementAplus, requirementAminus, "Monthly Net kWh");
        buildFormulaSingleRequirement(billingContract, readingTypeMonthlyAminusWh, requirementAminus, "Monthly A- kWh");
        buildNonNegativeNetFormula(billingContract, readingTypeYearlyNetWh, requirementAplus, requirementAminus, "Yearly Net kWh");
        buildFormulaSingleRequirement(billingContract, readingTypeYearlyAminusWh, requirementAminus, "Yearly A- kWh");
    }

    private void residentialNetMeteringProduction() {
        if (metrologyConfigurationService.findMetrologyConfiguration(RESIDENTIAL_NET_METERING_PRODUCTION.getName()).isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = this.findElectricityServiceCategoryOrThrowException();
        UsagePointMetrologyConfiguration config =
            metrologyConfigurationService
                .newUsagePointMetrologyConfiguration(RESIDENTIAL_NET_METERING_PRODUCTION.getName(), serviceCategory)
                .withDescription(RESIDENTIAL_NET_METERING_PRODUCTION.getDescription())
                .withGapsAllowed(RESIDENTIAL_NET_METERING_PRODUCTION.areGapsAllowed())
                .withUsagePointRequirement(
                        getUsagePointRequirement(
                                SERVICEKIND,
                                SearchablePropertyOperator.EQUAL,
                                ServiceKind.ELECTRICITY.name()))
                .withUsagePointRequirement(
                        getUsagePointRequirement(
                                DETAIL_PHASE_CODE,
                                SearchablePropertyOperator.EQUAL,
                                PhaseCode.S1N.name(),
                                PhaseCode.S2N.name(),
                                PhaseCode.S12N.name(),
                                PhaseCode.S1.name(),
                                PhaseCode.S2.name(),
                                PhaseCode.S12.name()))
                .withUsagePointRequirement(
                        getUsagePointRequirement(
                                "type",
                                SearchablePropertyOperator.EQUAL,
                                UsagePointTypeInfo.UsagePointType.MEASURED_SDP.name()))
                .create();

        MeterRole meterRole = findMeterRoleOrThrowException(DefaultMeterRole.DEFAULT);
        config.addMeterRole(meterRole);

        ReadingType readingTypeDailyAMinusWh = this.findOrCreateReadingType(DAILY_A_MINUS_KWH, "A-");
        ReadingType readingTypeMonthlyAMinusWh = this.findOrCreateReadingType(MONTHLY_A_MINUS_KWH, "A-");
        ReadingType readingType15minAMinusWh = this.findOrCreateReadingType(MIN15_A_MINUS_KWH, "A-");
        ReadingType readingTypeHourlyAMinusWh = this.findOrCreateReadingType(HOURLY_A_MINUS_KWH, "A-");

        MetrologyContract billingContract = config.addMandatoryMetrologyContract(findPurposeOrThrowException(DefaultMetrologyPurpose.BILLING));
        MetrologyContract informationContract = config.addMandatoryMetrologyContract(findPurposeOrThrowException(DefaultMetrologyPurpose.INFORMATION));

        ReadingTypeRequirement requirementAMinus =
                config
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.A_MINUS.getNameTranslation().getDefaultFormat(), meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_MINUS));

        buildFormulaSingleRequirement(billingContract, readingTypeDailyAMinusWh, requirementAMinus, "Daily A- kWh");
        buildFormulaSingleRequirement(billingContract, readingTypeMonthlyAMinusWh, requirementAMinus, "Monthly A- kWh");
        ReadingTypeDeliverable min15 = buildFormulaSingleRequirement(informationContract, readingType15minAMinusWh, requirementAMinus, "15-min A- kWh");
        buildFormulaSingleDeliverable(informationContract, readingTypeHourlyAMinusWh, min15, "Hourly A- kWh");
    }

    private void mainCheckConsumption() {
        if (metrologyConfigurationService.findMetrologyConfiguration(MAIN_CHECK_CONSUMPTION.getName())
                .isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                .orElseThrow(() -> new NoSuchElementException(SERVICE_CATEGORY_NOT_FOUND + ServiceKind.ELECTRICITY));
        UsagePointMetrologyConfiguration config = metrologyConfigurationService.newUsagePointMetrologyConfiguration(MAIN_CHECK_CONSUMPTION
                .getName(), serviceCategory)
                .withDescription(MAIN_CHECK_CONSUMPTION.getDescription())
                .withGapsAllowed(MAIN_CHECK_CONSUMPTION.areGapsAllowed())
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

        MeterRole meterRoleMain = metrologyConfigurationService.findMeterRole(DefaultMeterRole.MAIN.getKey())
                .orElseThrow(() -> new NoSuchElementException("Main meter role not found"));
        config.addMeterRole(meterRoleMain);
        MeterRole meterRoleCheck = metrologyConfigurationService.findMeterRole(DefaultMeterRole.CHECK.getKey())
                .orElseThrow(() -> new NoSuchElementException("Check meter role not found"));
        config.addMeterRole(meterRoleCheck);

        ReadingType readingTypeDailyAplusWhMain = meteringService.findReadingTypes(Collections.singletonList(DAILY_A_PLUS_KWH))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(DAILY_A_PLUS_KWH, "A+"));

        ReadingType readingTypeDailyAplusWhCheck = meteringService.findReadingTypes(Collections.singletonList(DAILY_A_PLUS_KWH))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(DAILY_A_PLUS_KWH, "A+"));

        MetrologyPurpose purposeBilling = findPurposeOrThrowException(DefaultMetrologyPurpose.BILLING);
        MetrologyPurpose purposeCheck = findPurposeOrThrowException(DefaultMetrologyPurpose.CHECK);

        MetrologyContract contractMain = config.addMandatoryMetrologyContract(purposeBilling);
        MetrologyContract contractCheck = config.addMandatoryMetrologyContract(purposeCheck);

        // examples: 'A+ from Main channel' or 'A+ from Check channel'
        String readingTypeRequirementNameFormat = "%s from %s channel";

        ReadingTypeRequirement requirementAplusMain = config.newReadingTypeRequirement(String.format(readingTypeRequirementNameFormat, DefaultReadingTypeTemplate.A_PLUS
                .getNameTranslation()
                .getDefaultFormat(), DefaultMeterRole.MAIN.getDefaultFormat()), meterRoleMain)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS));

        ReadingTypeRequirement requirementAplusCheck = config.newReadingTypeRequirement(String.format(readingTypeRequirementNameFormat, DefaultReadingTypeTemplate.A_PLUS
                .getNameTranslation()
                .getDefaultFormat(), DefaultMeterRole.CHECK.getDefaultFormat()), meterRoleCheck)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS));


        buildFormulaSingleRequirement(contractMain, readingTypeDailyAplusWhMain, requirementAplusMain, "Daily A+ kWh");
        buildFormulaSingleRequirement(contractCheck, readingTypeDailyAplusWhCheck, requirementAplusCheck, "Daily A+ kWh");
    }

    private void residentialNetMeteringConsumption() {
        if (metrologyConfigurationService.findMetrologyConfiguration(RESIDENTIAL_NET_METERING_CONSUMPTION.getName()).isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = this.findElectricityServiceCategoryOrThrowException();
        UsagePointMetrologyConfiguration config =
            metrologyConfigurationService
                .newUsagePointMetrologyConfiguration(RESIDENTIAL_NET_METERING_CONSUMPTION.getName(), serviceCategory)
                .withDescription(RESIDENTIAL_NET_METERING_CONSUMPTION.getDescription())
                .withGapsAllowed(RESIDENTIAL_NET_METERING_CONSUMPTION.areGapsAllowed())
                .withUsagePointRequirement(
                        getUsagePointRequirement(
                                SERVICEKIND,
                                SearchablePropertyOperator.EQUAL,
                                ServiceKind.ELECTRICITY.name()))
                .withUsagePointRequirement(
                        getUsagePointRequirement(
                                DETAIL_PHASE_CODE,
                                SearchablePropertyOperator.EQUAL,
                                PhaseCode.S1N.name(),
                                PhaseCode.S2N.name(),
                                PhaseCode.S12N.name(),
                                PhaseCode.S1.name(),
                                PhaseCode.S2.name(),
                                PhaseCode.S12.name()))
                .withUsagePointRequirement(
                        getUsagePointRequirement(
                                "type",
                                SearchablePropertyOperator.EQUAL,
                                UsagePointTypeInfo.UsagePointType.MEASURED_SDP.name()))
                .create();


        MeterRole meterRole = findMeterRoleOrThrowException(DefaultMeterRole.DEFAULT);
        config.addMeterRole(meterRole);

        ReadingType readingTypeDailyAplusWh = this.findOrCreateReadingType(DAILY_A_PLUS_KWH, "A+");
        ReadingType readingTypeMonthlyAplusWh = this.findOrCreateReadingType(MONTHLY_A_PLUS_KWH, "A+");
        ReadingType readingType15minAplusWh = this.findOrCreateReadingType(MIN15_A_PLUS_KWH, "A+");
        ReadingType readingTypeAplusWh = this.findOrCreateReadingType(BULK_A_PLUS_KWH, "A+");

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

        buildFormulaSingleRequirement(billingContract, readingTypeDailyAplusWh, requirementAplus, "Daily A+ kWh");
        buildFormulaSingleRequirement(billingContract, readingTypeMonthlyAplusWh, requirementAplus, "Monthly A+ kWh");
        buildFormulaSingleRequirement(informationContract, readingType15minAplusWh, requirementAplus, "15-min A+ kWh");
        buildFormulaSingleRequirement(informationContract, readingTypeAplusWh, requirementAplusRegister, "A+ kWh");
    }

    EventSet findOrCreateTimeOfUseEventSet() {
        return this.calendarService
                .findEventSetByName(TIME_OF_USER_EVENT_SET_NAME)
                .orElseGet(this::createTimeOfUseEventSet);
    }

    private EventSet createTimeOfUseEventSet() {
        return this.calendarService
                .newEventSet(TIME_OF_USER_EVENT_SET_NAME)
                .addEvent("Peak").withCode(PEAK_CODE)
                .addEvent("Offpeak").withCode(OFFPEAK_CODE)
                .add();
    }

    void residentialNetMeteringConsumptionThickTimeOfUse(EventSet eventSet) {
        String configurationName = "Residential net metering (consumption) and thick time of use";
        if (metrologyConfigurationService.findMetrologyConfiguration(configurationName).isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = this.findElectricityServiceCategoryOrThrowException();
        UsagePointMetrologyConfiguration configuration =
                metrologyConfigurationService
                        .newUsagePointMetrologyConfiguration(configurationName, serviceCategory)
                        .withDescription("Residential consumer (meter is providing time of use information)")
                        .withEventSet(eventSet)
                        .withUsagePointRequirement(
                                getUsagePointRequirement(
                                        SERVICEKIND,
                                        SearchablePropertyOperator.EQUAL,
                                        ServiceKind.ELECTRICITY.name()))
                        .withUsagePointRequirement(
                                getUsagePointRequirement(
                                        DETAIL_PHASE_CODE,
                                        SearchablePropertyOperator.EQUAL,
                                        PhaseCode.S1N.name(),
                                        PhaseCode.S2N.name(),
                                        PhaseCode.S12N.name(),
                                        PhaseCode.S1.name(),
                                        PhaseCode.S2.name(),
                                        PhaseCode.S12.name()))
                        .withUsagePointRequirement(
                                getUsagePointRequirement(
                                        "type",
                                        SearchablePropertyOperator.EQUAL,
                                        UsagePointTypeInfo.UsagePointType.MEASURED_SDP.name()))
                        .create();

        MeterRole meterRole = findMeterRoleOrThrowException(DefaultMeterRole.DEFAULT);
        configuration.addMeterRole(meterRole);

        ReadingType aPlusDaily_kWh_TOU_10 = this.findOrCreateReadingType("11.0.0.4.1.1.12.0.0.0.0." + OFFPEAK_CODE + ".0.0.0.3.72.0", "A+ (offpeak)");   // Offpeak in Belgian electricity market
        ReadingType aPlusDaily_kWh_TOU_11 = this.findOrCreateReadingType("11.0.0.4.1.1.12.0.0.0.0." + PEAK_CODE + ".0.0.0.3.72.0", "A+ (peak)");         // Peak in Belgian electricity market

        MetrologyContract billingContract = configuration.addMandatoryMetrologyContract(findPurposeOrThrowException(DefaultMetrologyPurpose.BILLING));

        ReadingTypeRequirement requirementAplusToU10 =
                configuration
                        .newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS.getNameTranslation().getDefaultFormat() + " ToU10", meterRole)
                        .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS))
                        .overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, OFFPEAK_CODE);
        ReadingTypeRequirement requirementAplusToU11 =
                configuration
                        .newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS.getNameTranslation().getDefaultFormat() + " ToU11", meterRole)
                        .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS))
                        .overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, PEAK_CODE);

        this.buildFormulaSingleRequirement(billingContract, aPlusDaily_kWh_TOU_10, requirementAplusToU10, "Daily A+ kWh ToU10");
        this.buildFormulaSingleRequirement(billingContract, aPlusDaily_kWh_TOU_11, requirementAplusToU11, "Daily A+ kWh ToU11");

        ReadingType aPlus15Min_kWh_TOU_10 = this.findOrCreateReadingType("0.0.2.4.1.1.12.0.0.0.0." + OFFPEAK_CODE + ".0.0.0.3.72.0", "A+ (offpeak)");   // Offpeak in Belgian electricity market
        ReadingType aPlus15Min_kWh_TOU_11 = this.findOrCreateReadingType("0.0.2.4.1.1.12.0.0.0.0." + PEAK_CODE + ".0.0.0.3.72.0", "A+ (peak)");         // Peak in Belgian electricity market
        ReadingType aPlusYearly_kWh_TOU_10 = this.findOrCreateReadingType(MacroPeriod.YEARLY.getId() + ".0.0.4.1.1.12.0.0.0.0." + OFFPEAK_CODE + ".0.0.0.3.72.0", "A+ (offpeak)");   // Offpeak in Belgian electricity market
        ReadingType aPlusYearly_kWh_TOU_11 = this.findOrCreateReadingType(MacroPeriod.YEARLY.getId() + ".0.0.4.1.1.12.0.0.0.0." + PEAK_CODE + ".0.0.0.3.72.0", "A+ (peak)");         // Peak in Belgian electricity market
        ReadingType aPlusYearly_kWh = this.findOrCreateReadingType(MacroPeriod.YEARLY.getId() + ".0.0.4.9.1.12.0.0.0.0.0.0.0.0.3.72.0", "A+");

        MetrologyContract informationContract = configuration.addMandatoryMetrologyContract(findPurposeOrThrowException(DefaultMetrologyPurpose.INFORMATION));
        this.buildFormulaSingleRequirement(informationContract, aPlus15Min_kWh_TOU_10, requirementAplusToU10, "15-min A+ kWh ToU10");
        this.buildFormulaSingleRequirement(informationContract, aPlus15Min_kWh_TOU_11, requirementAplusToU11, "15-min A+ kWh ToU11");
        ReadingTypeDeliverable yearly_TOU_10 = this.buildFormulaSingleRequirement(informationContract, aPlusYearly_kWh_TOU_10, requirementAplusToU10, "Yearly A+ kWh ToU10");
        ReadingTypeDeliverable yearly_TOU_11 = this.buildFormulaSingleRequirement(informationContract, aPlusYearly_kWh_TOU_11, requirementAplusToU11, "Yearly A+ kWh ToU11");
        this.buildFormulaDeliverableSum(informationContract, aPlusYearly_kWh, yearly_TOU_10, yearly_TOU_11, "Yearly A+ kWh");
    }

    void residentialNetMeteringConsumptionThinTimeOfUse(EventSet eventSet) {
        String configurationName = "Residential net metering (consumption) and thin time of use";
        if (metrologyConfigurationService.findMetrologyConfiguration(configurationName).isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = this.findElectricityServiceCategoryOrThrowException();
        UsagePointMetrologyConfiguration configuration =
                metrologyConfigurationService
                        .newUsagePointMetrologyConfiguration(configurationName, serviceCategory)
                        .withDescription("Residential consumer (meter is NOT providing time of use information)")
                        .withEventSet(eventSet)
                        .withUsagePointRequirement(
                                getUsagePointRequirement(
                                        SERVICEKIND,
                                        SearchablePropertyOperator.EQUAL,
                                        ServiceKind.ELECTRICITY.name()))
                        .withUsagePointRequirement(
                                getUsagePointRequirement(
                                        DETAIL_PHASE_CODE,
                                        SearchablePropertyOperator.EQUAL,
                                        PhaseCode.S1N.name(),
                                        PhaseCode.S2N.name(),
                                        PhaseCode.S12N.name(),
                                        PhaseCode.S1.name(),
                                        PhaseCode.S2.name(),
                                        PhaseCode.S12.name()))
                        .withUsagePointRequirement(
                                getUsagePointRequirement(
                                        "type",
                                        SearchablePropertyOperator.EQUAL,
                                        UsagePointTypeInfo.UsagePointType.MEASURED_SDP.name()))
                        .create();

        MeterRole meterRole = findMeterRoleOrThrowException(DefaultMeterRole.DEFAULT);
        configuration.addMeterRole(meterRole);

        ReadingType aPlusDaily_kWh_TOU_10 = this.findOrCreateReadingType("11.0.0.4.1.1.12.0.0.0.0." + OFFPEAK_CODE + ".0.0.0.3.72.0", "A+ (offpeak)");   // Offpeak in Belgian electricity market
        ReadingType aPlusDaily_kWh_TOU_11 = this.findOrCreateReadingType("11.0.0.4.1.1.12.0.0.0.0." + PEAK_CODE + ".0.0.0.3.72.0", "A+ (peak)");         // Peak in Belgian electricity market

        MetrologyContract billingContract = configuration.addMandatoryMetrologyContract(findPurposeOrThrowException(DefaultMetrologyPurpose.BILLING));

        ReadingTypeRequirement requirementAplus =
                configuration
                        .newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS.getNameTranslation().getDefaultFormat(), meterRole)
                        .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS));

        this.buildFormulaSingleRequirement(billingContract, aPlusDaily_kWh_TOU_10, requirementAplus, "Daily A+ kWh ToU10");
        this.buildFormulaSingleRequirement(billingContract, aPlusDaily_kWh_TOU_11, requirementAplus, "Daily A+ kWh ToU11");

        ReadingType aPlus15Min_kWh_TOU_10 = this.findOrCreateReadingType("0.0.2.4.1.1.12.0.0.0.0." + OFFPEAK_CODE + ".0.0.0.3.72.0", "A+ (offpeak)");   // Offpeak in Belgian electricity market
        ReadingType aPlus15Min_kWh_TOU_11 = this.findOrCreateReadingType("0.0.2.4.1.1.12.0.0.0.0." + PEAK_CODE + ".0.0.0.3.72.0", "A+ (peak)");         // Peak in Belgian electricity market

        MetrologyContract informationContract = configuration.addMandatoryMetrologyContract(findPurposeOrThrowException(DefaultMetrologyPurpose.INFORMATION));
        this.buildFormulaSingleRequirement(informationContract, aPlus15Min_kWh_TOU_10, requirementAplus, "15-min A+ kWh ToU10");
        this.buildFormulaSingleRequirement(informationContract, aPlus15Min_kWh_TOU_11, requirementAplus, "15-min A+ kWh ToU11");
    }

    private void residentialNonSmartInstallation() {
        if (metrologyConfigurationService.findMetrologyConfiguration(RESIDENTIAL_NON_SMART_INSTALLATION.getName()).isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                .orElseThrow(() -> new NoSuchElementException(SERVICE_CATEGORY_NOT_FOUND + ServiceKind.ELECTRICITY));
        UsagePointMetrologyConfiguration config =
                metrologyConfigurationService
                        .newUsagePointMetrologyConfiguration(RESIDENTIAL_NON_SMART_INSTALLATION.getName(), serviceCategory)
                        .withDescription(RESIDENTIAL_NON_SMART_INSTALLATION.getDescription())
                        .withGapsAllowed(RESIDENTIAL_NON_SMART_INSTALLATION.areGapsAllowed())
                        .create();

        MeterRole meterRole = findMeterRoleOrThrowException(DefaultMeterRole.DEFAULT);
        config.addMeterRole(meterRole);

        ReadingType readingTypeBatteryStatus = this.findOrCreateReadingType(BATTERY_STATUS, "Battery status");
        ReadingType readingTypeAplusWh = this.findOrCreateReadingType(BULK_A_PLUS_KWH, "A+");

        MetrologyContract informationContract = config.addMandatoryMetrologyContract(findPurposeOrThrowException(DefaultMetrologyPurpose.INFORMATION));

        ReadingTypeRequirement requirementTextual =
                config
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.BATTERY_STATUS.getNameTranslation().getDefaultFormat(), meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.BATTERY_STATUS));

        ReadingTypeRequirement requirementNumerical =
                config
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.BULK_A_PLUS.getNameTranslation().getDefaultFormat(), meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.BULK_A_PLUS));

        buildFormulaSingleRequirement(informationContract, DeliverableType.TEXT, readingTypeBatteryStatus, requirementTextual, "Battery status");
        buildFormulaSingleRequirement(informationContract, DeliverableType.NUMERICAL, readingTypeAplusWh, requirementNumerical, "A+ kWh");
    }

    private void residentialGasNonSmartInstallation() {
        if (metrologyConfigurationService.findMetrologyConfiguration(RESIDENTIAL_GAS_NON_SMART_INSTALLATION.getName()).isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                .orElseThrow(() -> new NoSuchElementException(SERVICE_CATEGORY_NOT_FOUND + ServiceKind.ELECTRICITY));
        UsagePointMetrologyConfiguration config =
                metrologyConfigurationService
                        .newUsagePointMetrologyConfiguration(RESIDENTIAL_GAS_NON_SMART_INSTALLATION.getName(), serviceCategory)
                        .withDescription(RESIDENTIAL_GAS_NON_SMART_INSTALLATION.getDescription())
                        .withGapsAllowed(RESIDENTIAL_GAS_NON_SMART_INSTALLATION.areGapsAllowed())
                        .create();

        MeterRole meterRole = findMeterRoleOrThrowException(DefaultMeterRole.DEFAULT);
        config.addMeterRole(meterRole);

        ReadingType readingTypeGasFlow = this.findOrCreateReadingType(BILLING_GAS_FLOW, "Billing gas flow");

        MetrologyContract informationContract = config.addMandatoryMetrologyContract(findPurposeOrThrowException(DefaultMetrologyPurpose.INFORMATION));

        ReadingTypeRequirement requirementBilling =
                config
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.GAS_FLOW_BILLING.getNameTranslation().getDefaultFormat(), meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.GAS_FLOW_BILLING));

        buildFormulaSingleRequirement(informationContract, DeliverableType.BILLING, readingTypeGasFlow, requirementBilling, "Billing Gas flow m3/h");
    }

    private void threePhasedConsumerWith2ToU() {
        if (metrologyConfigurationService.findMetrologyConfiguration(CI_3_PHASED_CONSUMER_WITH_SMART_METER_WITH_2_TOU.getName()).isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                .orElseThrow(() -> new NoSuchElementException(SERVICE_CATEGORY_NOT_FOUND + ServiceKind.ELECTRICITY));
        UsagePointMetrologyConfiguration config =
            metrologyConfigurationService
                .newUsagePointMetrologyConfiguration(CI_3_PHASED_CONSUMER_WITH_SMART_METER_WITH_2_TOU.getName(), serviceCategory)
                .withDescription(CI_3_PHASED_CONSUMER_WITH_SMART_METER_WITH_2_TOU.getDescription())
                .withGapsAllowed(CI_3_PHASED_CONSUMER_WITH_SMART_METER_WITH_2_TOU.areGapsAllowed())
                .withUsagePointRequirement(
                        getUsagePointRequirement(
                                SERVICEKIND,
                                SearchablePropertyOperator.EQUAL,
                                ServiceKind.ELECTRICITY.name()))
                .withUsagePointRequirement(
                        getUsagePointRequirement(
                                DETAIL_PHASE_CODE,
                                SearchablePropertyOperator.EQUAL,
                                PhaseCode.ABC.name(),
                                PhaseCode.ABCN.name()))
                .withUsagePointRequirement(
                        getUsagePointRequirement(
                                "type",
                                SearchablePropertyOperator.EQUAL,
                                UsagePointTypeInfo.UsagePointType.MEASURED_SDP.name()))
                .create();

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
                    .overrideAttribute(ReadingTypeTemplateAttributeName.PHASE, PHASE_A);
        ReadingTypeRequirement requirementAverageVoltagePhaseB =
                config
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE.getNameTranslation().getDefaultFormat() + " phase B", meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE))
                    .overrideAttribute(ReadingTypeTemplateAttributeName.PHASE, PHASE_B);
        ReadingTypeRequirement requirementAverageVoltagePhaseC =
                config
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE.getNameTranslation().getDefaultFormat() + " phase C", meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE))
                    .overrideAttribute(ReadingTypeTemplateAttributeName.PHASE, PHASE_C);

        buildFormulaSingleRequirement(billingContract, readingTypeDailyActiveEnergyToU1, requirementAplusToU1, "Daily active energy kWh ToU1");
        buildFormulaSingleRequirement(billingContract, readingTypeDailyActiveEnergyToU2, requirementAplusToU2, "Daily active energy kWh ToU2");
        buildFormulaSingleRequirement(billingContract, readingTypeDailyReactiveEnergyToU1, requirementReactiveEnergyPlusToU1, "Daily reactive energy kVArh ToU1");
        buildFormulaSingleRequirement(billingContract, readingTypeDailyReactiveEnergyToU2, requirementReactiveEnergyPlusToU2, "Daily reactive energy kVArh ToU2");
        buildFormulaSingleRequirement(billingContract, readingTypeMonthlyActiveEnergyToU1, requirementAplusToU1, "Monthly active energy kWh ToU1");
        buildFormulaSingleRequirement(billingContract, readingTypeMonthlyActiveEnergyToU2, requirementAplusToU2, "Monthly active energy kWh ToU2");
        buildFormulaSingleRequirement(billingContract, readingTypeMonthlyReactiveEnergyToU1, requirementReactiveEnergyPlusToU1, "Monthly reactive energy kVArh ToU1");
        buildFormulaSingleRequirement(billingContract, readingTypeMonthlyReactiveEnergyToU2, requirementReactiveEnergyPlusToU2, "Monthly reactive energy kVArh ToU2");
        buildFormulaSingleRequirement(voltageMonitoringContract, readingTypeAverageVoltagePhaseA, requirementAverageVoltagePhaseA, "Hourly average voltage V phase 1 vs N");
        buildFormulaSingleRequirement(voltageMonitoringContract, readingTypeAverageVoltagePhaseB, requirementAverageVoltagePhaseB, "Hourly average voltage V phase 2 vs N");
        buildFormulaSingleRequirement(voltageMonitoringContract, readingTypeAverageVoltagePhaseC, requirementAverageVoltagePhaseC, "Hourly average voltage V phase 3 vs N");
    }

    private void residentialConsumerWith4ToU() {
        if (metrologyConfigurationService.findMetrologyConfiguration(RESIDENTIAL_CONSUMER_WITH_4_TOU.getName()).isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = this.findElectricityServiceCategoryOrThrowException();
        UsagePointMetrologyConfiguration configuration =
            metrologyConfigurationService
                .newUsagePointMetrologyConfiguration(RESIDENTIAL_CONSUMER_WITH_4_TOU.getName(), serviceCategory)
                .withDescription(RESIDENTIAL_CONSUMER_WITH_4_TOU.getDescription())
                .withGapsAllowed(RESIDENTIAL_CONSUMER_WITH_4_TOU.areGapsAllowed())
                .withUsagePointRequirement(
                        getUsagePointRequirement(
                                SERVICEKIND,
                                SearchablePropertyOperator.EQUAL,
                                ServiceKind.ELECTRICITY.name()))
                .withUsagePointRequirement(
                        getUsagePointRequirement(
                                DETAIL_PHASE_CODE,
                                SearchablePropertyOperator.EQUAL,
                                PhaseCode.S1N.name(),
                                PhaseCode.S2N.name(),
                                PhaseCode.S12N.name(),
                                PhaseCode.S1.name(),
                                PhaseCode.S2.name(),
                                PhaseCode.S12.name()))
                .withUsagePointRequirement(
                        getUsagePointRequirement(
                                "type",
                                SearchablePropertyOperator.EQUAL,
                                UsagePointTypeInfo.UsagePointType.MEASURED_SDP.name()))
                .create();


        MeterRole meterRole = findMeterRoleOrThrowException(DefaultMeterRole.DEFAULT);
        configuration.addMeterRole(meterRole);

        ReadingType readingTypeMonthlyAplusToU1 = this.findOrCreateReadingType(ACTIVE_ENERGY_TOU1, "A+");
        ReadingType readingTypeMonthlyAplusToU2 = this.findOrCreateReadingType(ACTIVE_ENERGY_TOU2, "A+");
        ReadingType readingTypeMonthlyAplusToU3 = this.findOrCreateReadingType("13.0.0.4.1.1.12.0.0.0.0.3.0.0.0.3.72.0", "A+");
        ReadingType readingTypeMonthlyAplusToU4 = this.findOrCreateReadingType("13.0.0.4.1.1.12.0.0.0.0.4.0.0.0.3.72.0", "A+");

        MetrologyContract billingContract = configuration.addMandatoryMetrologyContract(findPurposeOrThrowException(DefaultMetrologyPurpose.BILLING));

        ReadingTypeRequirement requirementAplusToU1 =
                configuration
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS.getNameTranslation().getDefaultFormat() + " ToU1", meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS))
                    .overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 1);
        ReadingTypeRequirement requirementAplusToU2 =
                configuration
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS.getNameTranslation().getDefaultFormat() + " ToU2", meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS))
                    .overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 2);
        ReadingTypeRequirement requirementAplusToU3 =
                configuration
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS.getNameTranslation().getDefaultFormat() + " ToU3", meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS))
                    .overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 3);
        ReadingTypeRequirement requirementAplusToU4 =
                configuration
                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS.getNameTranslation().getDefaultFormat() + " ToU4", meterRole)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS))
                    .overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 4);

        buildFormulaSingleRequirement(billingContract, readingTypeMonthlyAplusToU1, requirementAplusToU1, "Monthly A+ kWh ToU1");
        buildFormulaSingleRequirement(billingContract, readingTypeMonthlyAplusToU2, requirementAplusToU2, "Monthly A+ kWh ToU2");
        buildFormulaSingleRequirement(billingContract, readingTypeMonthlyAplusToU3, requirementAplusToU3, "Monthly A+ kWh ToU3");
        buildFormulaSingleRequirement(billingContract, readingTypeMonthlyAplusToU4, requirementAplusToU4, "Monthly A+ kWh ToU4");
    }

    private void residentialGas() {
        if (metrologyConfigurationService.findMetrologyConfiguration(RESIDENTIAL_GAS.getName()).isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = this.findGasServiceCategoryOrThrowException();
        UsagePointMetrologyConfiguration config =
            metrologyConfigurationService
                .newUsagePointMetrologyConfiguration(RESIDENTIAL_GAS.getName(), serviceCategory)
                .withDescription(RESIDENTIAL_GAS.getDescription())
                .withGapsAllowed(RESIDENTIAL_GAS.areGapsAllowed())
                .create();

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
                    .overrideAttribute(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, CUBIC_METRE);

        buildFormulaSingleRequirement(billingContract, readingTypeDailyVolume, requirementGasVolume, "Daily volume m³");
        buildFormulaSingleRequirement(billingContract, readingTypeMonthlyVolume, requirementGasVolume, "Monthly volume m³");
        buildFormulaSingleRequirement(informationContract, readingTypeHourlyVolume, requirementGasVolume, "Hourly volume m³");
    }

    void residentialWater() {
        if (metrologyConfigurationService.findMetrologyConfiguration(RESIDENTAL_WATER.getName()).isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = this.findWaterServiceCategoryOrThrowException();
        UsagePointMetrologyConfiguration config =
            metrologyConfigurationService
                .newUsagePointMetrologyConfiguration("Residential water", serviceCategory)
                .withDescription(RESIDENTAL_WATER.getDescription())
                .withGapsAllowed(RESIDENTAL_WATER.areGapsAllowed())
                .withUsagePointRequirement(
                        getUsagePointRequirement(
                                SERVICEKIND,
                                SearchablePropertyOperator.EQUAL,
                                ServiceKind.WATER.name()))
                .withUsagePointRequirement(
                        getUsagePointRequirement(
                                "type",
                                SearchablePropertyOperator.EQUAL,
                                UsagePointTypeInfo.UsagePointType.MEASURED_SDP.name()))
                                
                .create();


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
        ServiceCategory serviceCategory = this.findWaterServiceCategoryOrThrowException();
        UsagePointMetrologyConfiguration config =
            metrologyConfigurationService
                .newUsagePointMetrologyConfiguration(CI_WATER_CONFIGURATION.getName(), serviceCategory)
                .withDescription(CI_WATER_CONFIGURATION.getDescription())
                .withGapsAllowed(CI_WATER_CONFIGURATION.areGapsAllowed())
                .create();

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
                    .overrideAttribute(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, CUBIC_METRE);
        ReadingTypeRequirement requirementOffPeakConsumption =
                config
                    .newReadingTypeRequirement("Off peak consumption", meterRoleOffPeakConsumption)
                    .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.WATER_VOLUME))
                    .overrideAttribute(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, CUBIC_METRE);

        ReadingTypeDeliverableBuilder builder = billingContract.newReadingTypeDeliverable("Monthly consumption m³", readingTypeMonthlyConsumption, Formula.Mode.AUTO);
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
        ReadingTypeDeliverableBuilder builder =
                contract.newReadingTypeDeliverable(
                        name,
                        deliverableType,
                        readingType,
                        deliverableType .equals(DeliverableType.TEXT) ? Formula.Mode.EXPERT : Formula.Mode.AUTO);
        return builder.build(builder.requirement(requirement));
    }

    ReadingTypeDeliverable buildFormulaDeliverableSum(MetrologyContract contract, ReadingType readingType, ReadingTypeDeliverable d1, ReadingTypeDeliverable d2, String name) {
        ReadingTypeDeliverableBuilder builder = contract.newReadingTypeDeliverable(name, readingType, Formula.Mode.AUTO);
        return builder.build(
                builder.plus(
                        builder.deliverable(d1),
                        builder.deliverable(d2)));
    }

    ReadingTypeDeliverable buildNonNegativeNetFormula(MetrologyContract contract, ReadingType readingType,
                                                      ReadingTypeRequirement requirementPlus, ReadingTypeRequirement requirementMinus, String name) {

        ReadingTypeDeliverableBuilder builder = contract.newReadingTypeDeliverable(name, readingType, Formula.Mode.AUTO);
        return builder.build(builder.maximum(builder.minus(builder.requirement(requirementPlus), builder.requirement(requirementMinus)), builder
                .constant(0)));
    }

    ReadingTypeTemplate getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate defaultReadingTypeTemplate) {
        return metrologyConfigurationService
                .findReadingTypeTemplate(defaultReadingTypeTemplate.getNameTranslation().getDefaultFormat())
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
                .orElseThrow(() -> new NoSuchElementException(purpose.getName()
                        .getDefaultMessage() + " metrology purpose not found"));
    }

    private ReadingType findOrCreateReadingType(String mRID, String aliasName) {
        return this.meteringService
                .findReadingTypes(Collections.singletonList(mRID))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(mRID, aliasName));
    }

}