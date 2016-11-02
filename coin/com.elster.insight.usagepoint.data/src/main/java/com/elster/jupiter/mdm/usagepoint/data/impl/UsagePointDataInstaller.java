package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataService;
import com.elster.jupiter.mdm.usagepoint.data.security.Privileges;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class UsagePointDataInstaller implements FullInstaller, PrivilegesProvider {
    private final DataModel dataModel;
    private final UserService userService;

    @Inject
    public UsagePointDataInstaller(DataModel dataModel, UserService userService) {
        this.dataModel = dataModel;
        this.userService = userService;
    }

    @Override
    public String getModuleName() {
        return UsagePointDataService.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        return Collections.singletonList(
                userService.createModuleResourceWithPrivileges(getModuleName(),
                        Privileges.RESOURCE_USAGE_POINT_GROUPS.getKey(),
                        Privileges.RESOURCE_USAGE_POINT_GROUPS_DESCRIPTION.getKey(),
                        Arrays.asList(
                                Privileges.Constants.ADMINISTRATE_USAGE_POINT_GROUP,
                                Privileges.Constants.ADMINISTRATE_USAGE_POINT_ENUMERATED_GROUP,
                                Privileges.Constants.VIEW_USAGE_POINT_GROUP_DETAIL))
        );
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        userService.addModulePrivileges(this);
    }
}
