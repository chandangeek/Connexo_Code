package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.DefaultReadingTypeTemplate;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeTemplate;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.mdm.usagepoint.config.security.Privileges;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class UpgraderV10_3 implements Upgrader, PrivilegesProvider {

    private final DataModel dataModel;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final MeteringService meteringService;
    private final UserService userService;

    @Inject
    UpgraderV10_3(DataModel dataModel, MetrologyConfigurationService metrologyConfigurationService, MeteringService meteringService, UserService userService) {
        super();
        this.dataModel = dataModel;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.meteringService = meteringService;
        this.userService = userService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        new MetrologyConfigurationsInstaller(metrologyConfigurationService, meteringService).createMetrologyConfigurations();
        upgradeMetrologyConfiguration();
        userService.addModulePrivileges(this);
    }

    private void upgradeMetrologyConfiguration() {
        Optional<MetrologyConfiguration> metrologyConfiguration = metrologyConfigurationService.findMetrologyConfiguration("Residential net metering (consumption)");
        Optional<MeterRole> meterRole = metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey());
        Optional<ReadingTypeTemplate> readingTypeTemplate = metrologyConfigurationService.findReadingTypeTemplate(DefaultReadingTypeTemplate.DELTA_A_PLUS
                .getNameTranslation()
                .getDefaultFormat());
        Optional<MetrologyPurpose> purposeInformation = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.INFORMATION);
        Optional<MetrologyContract> contract = metrologyConfiguration.flatMap(mc -> mc.getContracts()
                .stream()
                .filter(mct -> purposeInformation.isPresent() && purposeInformation.get()
                        .equals(mct.getMetrologyPurpose()))
                .findFirst());

        if (contract.isPresent() && meterRole.isPresent() && readingTypeTemplate.isPresent()) {
            ReadingType readingTypeAplusWh = meteringService.findReadingTypes(Collections.singletonList(MetrologyConfigurationsInstaller.DELTA_A_PLUS_WH))
                    .stream()
                    .findFirst()
                    .orElseGet(() -> meteringService.createReadingType(MetrologyConfigurationsInstaller.DELTA_A_PLUS_WH, "A+"));
            ReadingTypeRequirement requirementAplusRegister = ((UsagePointMetrologyConfiguration) metrologyConfiguration
                    .get()).newReadingTypeRequirement(DefaultReadingTypeTemplate.DELTA_A_PLUS.getNameTranslation()
                    .getDefaultFormat(), meterRole.get())
                    .withReadingTypeTemplate(readingTypeTemplate.get());
            contract.get()
                    .addDeliverable(buildFormulaSingleRequirement(((UsagePointMetrologyConfiguration) metrologyConfiguration
                            .get()), readingTypeAplusWh, requirementAplusRegister, "A+ kWh"));
        }
    }
    
    @Override
    public String getModuleName() {
        return UsagePointConfigurationService.COMPONENTNAME;
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