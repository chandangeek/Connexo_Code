package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.mdm.usagepoint.config.security.Privileges;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.DefaultReadingTypeTemplate;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeTemplate;
import com.elster.jupiter.metering.config.ReadingTypeTemplateAttributeName;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

class UpgraderV10_3 implements Upgrader, PrivilegesProvider {

    private final DataModel dataModel;
    private final UserService userService;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final MeteringService meteringService;

    @Inject
    UpgraderV10_3(DataModel dataModel, UserService userService, MetrologyConfigurationService metrologyConfigurationService, MeteringService meteringService) {
        super();
        this.dataModel = dataModel;
        this.userService = userService;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.meteringService = meteringService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        userService.addModulePrivileges(this);
        addOptionalContractsToResidentialProsumerWith1Meter();
    }

    @Override
    public String getModuleName() {
        return UsagePointConfigurationService.COMPONENTNAME;
    }

    private void addOptionalContractsToResidentialProsumerWith1Meter() {
        Optional<MetrologyConfiguration> usagePointMetrologyConfiguration = metrologyConfigurationService.findMetrologyConfiguration("Residential prosumer with 1 meter");
        if (usagePointMetrologyConfiguration.isPresent()) {
            UsagePointMetrologyConfiguration config = (UsagePointMetrologyConfiguration) usagePointMetrologyConfiguration
                    .get();
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

            MetrologyPurpose purposeVoltageMonitoring = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.VOLTAGE_MONITORING)
                    .orElseThrow(() -> new NoSuchElementException("Voltage monitoring metrology purpose not found"));

            MetrologyContract contractVoltageMonitoring = config.addMetrologyContract(purposeVoltageMonitoring);

            metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey()).ifPresent(
                    meterRole -> {
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

                        contractVoltageMonitoring.addDeliverable(buildFormulaSingleRequirement(config, readingTypeAverageVoltagePhaseA, requirementAverageVoltagePhaseA, "Hourly average voltage V phase 1 vs N"));
                        contractVoltageMonitoring.addDeliverable(buildFormulaSingleRequirement(config, readingTypeAverageVoltagePhaseB, requirementAverageVoltagePhaseB, "Hourly average voltage V phase 2 vs N"));
                        contractVoltageMonitoring.addDeliverable(buildFormulaSingleRequirement(config, readingTypeAverageVoltagePhaseC, requirementAverageVoltagePhaseC, "Hourly average voltage V phase 3 vs N"));
                    }
            );
        }
    }

    private ReadingTypeTemplate getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate defaultReadingTypeTemplate) {
        return metrologyConfigurationService.findReadingTypeTemplate(defaultReadingTypeTemplate.getNameTranslation()
                .getDefaultFormat())
                .orElseThrow(() -> new NoSuchElementException("Default reading type template not found"));
    }

    private ReadingTypeDeliverable buildFormulaSingleRequirement(UsagePointMetrologyConfiguration config, ReadingType readingType, ReadingTypeRequirement requirement, String name) {
        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable(name, readingType, Formula.Mode.AUTO);
        return builder.build(builder.requirement(requirement));
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(UsagePointConfigurationService.COMPONENTNAME, DefaultTranslationKey.RESOURCE_ESTIMATION_CONFIGURATION
                        .getKey(), DefaultTranslationKey.RESOURCE_ESTIMATION_CONFIGURATION_DESCRIPTION.getKey(),
                Arrays.asList(Privileges.Constants.VIEW_ESTIMATION_ON_METROLOGY_CONFIGURATION, Privileges.Constants.ADMINISTER_ESTIMATION_ON_METROLOGY_CONFIGURATION)));
        return resources;
    }
}