/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.mdm.usagepoint.config.security.Privileges;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

class Installer implements FullInstaller, PrivilegesProvider {

    private final DataModel dataModel;
    private final UserService userService;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final MeteringService meteringService;
    private final MetrologyConfigurationsInstaller metrologyConfigurationsInstaller;

    @Inject
    Installer(DataModel dataModel, UserService userService, MetrologyConfigurationService metrologyConfigurationService, MeteringService meteringService) {
        super();
        this.dataModel = dataModel;
        this.userService = userService;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.meteringService = meteringService;
        this.metrologyConfigurationsInstaller = new MetrologyConfigurationsInstaller(metrologyConfigurationService, meteringService);
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        doTry(
                "Create Metrology Configurations",
                this.metrologyConfigurationsInstaller::createMetrologyConfigurations,
                logger
        );
        userService.addModulePrivileges(this);
    }

    @Override
    public String getModuleName() {
        return UsagePointConfigurationService.COMPONENTNAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(UsagePointConfigurationService.COMPONENTNAME, DefaultTranslationKey.RESOURCE_VALIDATION_CONFIGURATION
                        .getKey(), DefaultTranslationKey.RESOURCE_VALIDATION_CONFIGURATION_DESCRIPTION.getKey(),
                Arrays.asList(Privileges.Constants.VIEW_VALIDATION_ON_METROLOGY_CONFIGURATION, Privileges.Constants.ADMINISTER_VALIDATION_ON_METROLOGY_CONFIGURATION)));
        resources.add(userService.createModuleResourceWithPrivileges(UsagePointConfigurationService.COMPONENTNAME, DefaultTranslationKey.RESOURCE_ESTIMATION_CONFIGURATION
                        .getKey(), DefaultTranslationKey.RESOURCE_ESTIMATION_CONFIGURATION_DESCRIPTION.getKey(),
                Arrays.asList(Privileges.Constants.VIEW_ESTIMATION_ON_METROLOGY_CONFIGURATION, Privileges.Constants.ADMINISTER_ESTIMATION_ON_METROLOGY_CONFIGURATION)));
        return resources;
    }
}