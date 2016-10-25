package com.elster.jupiter.mdm.usagepoint.lifecycle.impl;

import com.elster.jupiter.mdm.usagepoint.lifecycle.Privileges;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class Installer implements FullInstaller, PrivilegesProvider {
    private final DataModel dataModel;
    private final UserService userService;

    @Inject
    public Installer(DataModel dataModel, UserService userService) {
        this.dataModel = dataModel;
        this.userService = userService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(this.dataModel, Version.latest());
        this.userService.addModulePrivileges(this);
    }

    @Override
    public String getModuleName() {
        return UsagePointLifeCycleService.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        return Arrays.asList(
                this.userService.createModuleResourceWithPrivileges(UsagePointLifeCycleService.COMPONENT_NAME, Privileges.RESOURCE_USAGE_POINT_LIFECYCLE.getKey(), Privileges.RESOURCE_USAGE_POINT_LIFECYCLE_DESCRIPTION
                                .getKey(),
                        Arrays.asList(Privileges.Constants.USAGE_POINT_LIFE_CYCLE_VIEW, Privileges.Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER)),
                this.userService.createModuleResourceWithPrivileges(UsagePointLifeCycleService.COMPONENT_NAME, Privileges.RESOURCE_USAGE_POINT_LIFECYCLE_LEVELS.getKey(), Privileges.RESOURCE_USAGE_POINT_LIFECYCLE_LEVELS_DESCRIPTION
                                .getKey(),
                        Arrays.asList(Privileges.Constants.EXECUTE_TRANSITION_1,
                                Privileges.Constants.EXECUTE_TRANSITION_2,
                                Privileges.Constants.EXECUTE_TRANSITION_3,
                                Privileges.Constants.EXECUTE_TRANSITION_4)));
    }
}
