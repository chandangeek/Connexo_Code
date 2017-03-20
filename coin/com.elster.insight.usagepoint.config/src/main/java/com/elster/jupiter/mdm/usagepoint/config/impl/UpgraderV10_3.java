/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.mdm.usagepoint.config.security.Privileges;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.DefaultReadingTypeTemplate;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.PartiallySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeTemplate;
import com.elster.jupiter.metering.config.ReadingTypeTemplateAttributeName;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.elster.jupiter.orm.Version.version;

class UpgraderV10_3 implements Upgrader, PrivilegesProvider {

    private final DataModel dataModel;
    private final UserService userService;
    private final CalendarService calendarService;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final MeteringService meteringService;
    private final MetrologyConfigurationsInstaller metrologyConfigurationsInstaller;

    @Inject
    UpgraderV10_3(DataModel dataModel, MetrologyConfigurationService metrologyConfigurationService, MeteringService meteringService, UserService userService, CalendarService calendarService) {
        super();
        this.dataModel = dataModel;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.meteringService = meteringService;
        this.userService = userService;
        this.calendarService = calendarService;
        this.metrologyConfigurationsInstaller = new MetrologyConfigurationsInstaller(this.calendarService, metrologyConfigurationService, meteringService);
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10,3));
        metrologyConfigurationsInstaller.createMetrologyConfigurations();
        upgradeResidentialNetMeteringConsumption();
        upgradeResidentialNetMeteringProduction();
        upgradeResidentialProsumerWith1Meter();
        addResidentialNetMeteringConsumptionThickTimeOfUse();
        addResidentialNetMeteringConsumptionThinTimeOfUse();
        userService.addModulePrivileges(this);
    }

    private void upgradeResidentialNetMeteringConsumption() {
        Optional<MetrologyConfiguration> metrologyConfiguration = metrologyConfigurationService.findMetrologyConfiguration("Residential net metering (consumption)");
        if (metrologyConfiguration.isPresent() && metrologyConfiguration.get()
                .getDeliverables()
                .stream()
                .noneMatch(d -> "A+ kWh".equals(d.getName()))) {
            Optional<MeterRole> meterRole = metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey());
            Optional<ReadingTypeTemplate> readingTypeTemplate = metrologyConfigurationService.findReadingTypeTemplate(DefaultReadingTypeTemplate.BULK_A_PLUS
                    .getNameTranslation()
                    .getDefaultFormat());
            Optional<MetrologyPurpose> purposeInformation = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.INFORMATION);
            Optional<MetrologyContract> contract = metrologyConfiguration.flatMap(mc -> mc.getContracts()
                    .stream()
                    .filter(mct -> purposeInformation.isPresent())
                    .filter(mct -> purposeInformation.get().equals(mct.getMetrologyPurpose()))
                    .findFirst());

            if (contract.isPresent() && meterRole.isPresent() && readingTypeTemplate.isPresent()) {
                ReadingType readingTypeAplusWh = this.findOrCreateReadingType(MetrologyConfigurationsInstaller.BULK_A_PLUS_KWH, "A+");
                ReadingTypeRequirement requirementAplusRegister =
                        ((UsagePointMetrologyConfiguration) metrologyConfiguration.get())
                                .newReadingTypeRequirement(DefaultReadingTypeTemplate.BULK_A_PLUS.getNameTranslation().getDefaultFormat(), meterRole.get())
                                .withReadingTypeTemplate(readingTypeTemplate.get());
                contract.get()
                        .addDeliverable(
                                metrologyConfigurationsInstaller
                                        .buildFormulaSingleRequirement(
                                                ((UsagePointMetrologyConfiguration) metrologyConfiguration.get()),
                                                readingTypeAplusWh,
                                                requirementAplusRegister,
                                                "A+ kWh"));
            }
        }
    }

    private void upgradeResidentialNetMeteringProduction() {
        metrologyConfigurationService.findMetrologyConfiguration("Residential net metering (production)")
                .filter(mc -> mc instanceof UsagePointMetrologyConfiguration)
                .map(UsagePointMetrologyConfiguration.class::cast)
                .ifPresent(mc -> {
                    Optional<MetrologyPurpose> purposeInformationOptional = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.INFORMATION);
                    Optional<MeterRole> meterRoleOptional = metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey());
                    Optional<ReadingTypeTemplate> readingTypeTemplateOptional =
                            metrologyConfigurationService.findReadingTypeTemplate(DefaultReadingTypeTemplate.A_MINUS.getNameTranslation().getDefaultFormat());
                    if (purposeInformationOptional.isPresent() && meterRoleOptional.isPresent() && readingTypeTemplateOptional.isPresent()) {
                        MetrologyPurpose purposeInformation = purposeInformationOptional.get();
                        MeterRole meterRole = meterRoleOptional.get();
                        ReadingTypeTemplate readingTypeTemplate = readingTypeTemplateOptional.get();
                        // add meter role if not present yet
                        mc.addMeterRole(meterRole);
                        // add information contract if not present yet
                        MetrologyContract contractInformation = mc.addMandatoryMetrologyContract(purposeInformation);
                        // add first reading type if not found
                        ReadingType readingType15minAMinusWh = this.findOrCreateReadingType(MetrologyConfigurationsInstaller.MIN15_A_MINUS_KWH, "A-");
                        // add first deliverable if not found
                        ReadingTypeDeliverable min15Deliverable = contractInformation.getDeliverables().stream()
                                .filter(deliverable -> readingType15minAMinusWh.equals(deliverable.getReadingType()))
                                .findAny()
                                .orElseGet(() -> {
                                    // find or add requirement
                                    ReadingTypeRequirement requirementAMinus = mc.getRequirements(meterRole).stream()
                                            .filter(requirement -> requirement instanceof PartiallySpecifiedReadingTypeRequirement)
                                            .map(PartiallySpecifiedReadingTypeRequirement.class::cast)
                                            .filter(requirement -> readingTypeTemplate.equals(requirement.getReadingTypeTemplate()))
                                            .findAny()
                                            .orElseGet(() -> mc.newReadingTypeRequirement(DefaultReadingTypeTemplate.A_MINUS.getNameTranslation().getDefaultFormat(), meterRole)
                                                    .withReadingTypeTemplate(readingTypeTemplate));
                                    ReadingTypeDeliverable min15 =
                                            metrologyConfigurationsInstaller
                                                    .buildFormulaSingleRequirement(
                                                            mc,
                                                            readingType15minAMinusWh,
                                                            requirementAMinus,
                                                            "15-min A- kWh");
                                    contractInformation.addDeliverable(min15);
                                    return min15;
                                });
                        // add second reading type if not found
                        ReadingType readingTypeHourlyAMinusWh = this.findOrCreateReadingType(MetrologyConfigurationsInstaller.HOURLY_A_MINUS_WH, "A-");
                        // add second deliverable if not found
                        if (contractInformation.getDeliverables().stream()
                                .noneMatch(deliverable -> readingTypeHourlyAMinusWh.equals(deliverable.getReadingType()))) {
                            contractInformation.addDeliverable(
                                    metrologyConfigurationsInstaller
                                            .buildFormulaSingleDeliverable(
                                                    mc,
                                                    readingTypeHourlyAMinusWh,
                                                    min15Deliverable,
                                                    "Hourly A- kWh"));
                        }
                    }
                });
    }

    private void upgradeResidentialProsumerWith1Meter() {
        Optional<MetrologyConfiguration> usagePointMetrologyConfiguration = metrologyConfigurationService.findMetrologyConfiguration("Residential prosumer with 1 meter");
        if (usagePointMetrologyConfiguration.isPresent() && usagePointMetrologyConfiguration.get()
                .getDeliverables()
                .stream()
                .noneMatch(d -> "Hourly average voltage V phase 1 vs N".equals(d.getName()))) {
            UsagePointMetrologyConfiguration config = (UsagePointMetrologyConfiguration) usagePointMetrologyConfiguration.get();
            ReadingType readingTypeAverageVoltagePhaseA = this.findOrCreateReadingType("0.2.7.6.0.1.158.0.0.0.0.0.0.0.128.0.29.0", "Average voltage");
            ReadingType readingTypeAverageVoltagePhaseB = this.findOrCreateReadingType("0.2.7.6.0.1.158.0.0.0.0.0.0.0.64.0.29.0", "Average voltage");
            ReadingType readingTypeAverageVoltagePhaseC = this.findOrCreateReadingType("0.2.7.6.0.1.158.0.0.0.0.0.0.0.32.0.29.0", "Average voltage");

            MetrologyContract contractVoltageMonitoring = config.addMetrologyContract(this.findPurposeOrThrowException(DefaultMetrologyPurpose.VOLTAGE_MONITORING));

            metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey()).ifPresent(
                    meterRole -> {
                        ReadingTypeRequirement requirementAverageVoltagePhaseA =
                                config
                                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE.getNameTranslation().getDefaultFormat() + " phase A", meterRole)
                                    .withReadingTypeTemplate(metrologyConfigurationsInstaller.getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE))
                                    .overrideAttribute(ReadingTypeTemplateAttributeName.PHASE, MetrologyConfigurationsInstaller.PHASE_A);
                        ReadingTypeRequirement requirementAverageVoltagePhaseB =
                                config
                                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE.getNameTranslation().getDefaultFormat() + " phase B", meterRole)
                                    .withReadingTypeTemplate(metrologyConfigurationsInstaller.getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE))
                                    .overrideAttribute(ReadingTypeTemplateAttributeName.PHASE, MetrologyConfigurationsInstaller.PHASE_B);
                        ReadingTypeRequirement requirementAverageVoltagePhaseC =
                                config
                                    .newReadingTypeRequirement(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE.getNameTranslation().getDefaultFormat() + " phase C", meterRole)
                                    .withReadingTypeTemplate(metrologyConfigurationsInstaller.getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE))
                                    .overrideAttribute(ReadingTypeTemplateAttributeName.PHASE, MetrologyConfigurationsInstaller.PHASE_C);

                        contractVoltageMonitoring
                                .addDeliverable(
                                        metrologyConfigurationsInstaller
                                            .buildFormulaSingleRequirement(
                                                    config,
                                                    readingTypeAverageVoltagePhaseA,
                                                    requirementAverageVoltagePhaseA,
                                                    "Hourly average voltage V phase 1 vs N"));
                        contractVoltageMonitoring
                                .addDeliverable(
                                        metrologyConfigurationsInstaller
                                            .buildFormulaSingleRequirement(
                                                    config,
                                                    readingTypeAverageVoltagePhaseB,
                                                    requirementAverageVoltagePhaseB,
                                                    "Hourly average voltage V phase 2 vs N"));
                        contractVoltageMonitoring
                                .addDeliverable(
                                        metrologyConfigurationsInstaller
                                            .buildFormulaSingleRequirement(
                                                    config,
                                                    readingTypeAverageVoltagePhaseC,
                                                    requirementAverageVoltagePhaseC,
                                                    "Hourly average voltage V phase 3 vs N"));
                    }
            );
        }
    }

    private void addResidentialNetMeteringConsumptionThickTimeOfUse() {
        MetrologyConfigurationsInstaller installer = new MetrologyConfigurationsInstaller(calendarService, this.metrologyConfigurationService, this.meteringService);
        installer.residentialNetMeteringConsumptionThickTimeOfUse(installer.findOrCreateTimeOfUseEventSet());
    }

    private void addResidentialNetMeteringConsumptionThinTimeOfUse() {
        MetrologyConfigurationsInstaller installer = new MetrologyConfigurationsInstaller(calendarService, this.metrologyConfigurationService, this.meteringService);
        installer.residentialNetMeteringConsumptionThinTimeOfUse(installer.findOrCreateTimeOfUseEventSet());
    }

    @Override
    public String getModuleName() {
        return UsagePointConfigurationService.COMPONENTNAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        return Collections.singletonList(
                userService
                    .createModuleResourceWithPrivileges(
                            UsagePointConfigurationService.COMPONENTNAME,
                            DefaultTranslationKey.RESOURCE_ESTIMATION_CONFIGURATION.getKey(),
                            DefaultTranslationKey.RESOURCE_ESTIMATION_CONFIGURATION_DESCRIPTION.getKey(),
                            Arrays.asList(
                                    Privileges.Constants.VIEW_ESTIMATION_ON_METROLOGY_CONFIGURATION,
                                    Privileges.Constants.ADMINISTER_ESTIMATION_ON_METROLOGY_CONFIGURATION)));
    }

    private ReadingType findOrCreateReadingType(String mRID, String aliasName) {
        return this.meteringService
                .findReadingTypes(Collections.singletonList(mRID))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(mRID, aliasName));
    }

    private MetrologyPurpose findPurposeOrThrowException(DefaultMetrologyPurpose purpose) {
        return metrologyConfigurationService.findMetrologyPurpose(purpose)
                .orElseThrow(() -> new NoSuchElementException(purpose.getName().getDefaultMessage() + " metrology purpose not found"));
    }

}