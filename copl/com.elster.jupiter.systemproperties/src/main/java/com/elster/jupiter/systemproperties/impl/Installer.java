package com.elster.jupiter.systemproperties.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.systemproperties.SystemPropertyService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class Installer implements FullInstaller, PrivilegesProvider {

    private final UserService userService;
    private final DataModel dataModel;


    @Inject
    Installer(UserService userService, DataModel dataModel) {
        super();
        this.userService = userService;
        this.dataModel = dataModel;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        userService.addModulePrivileges(this);
    }

    @Override
    public String getModuleName() {
        return SystemPropertyService.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(getModuleName(),
                Privileges.RESOURCE_SYS_PROPS.getKey(), Privileges.RESOURCE_SYS_PROPS_DESCRIPTION.getKey(),
                Arrays.asList(Privileges.Constants.ADMINISTRATE_SYS_PROPS, Privileges.Constants.VIEW_SYS_PROPS)));
        return resources;
    }
}
