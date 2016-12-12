package com.elster.jupiter.dualcontrol.impl;

import com.elster.jupiter.dualcontrol.DualControlService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

class Installer implements FullInstaller, PrivilegesProvider {

    private final DataModel dataModel;
    private final UserService userService;

    @Inject
    Installer(DataModel dataModel, UserService userService) {
        this.dataModel = dataModel;
        this.userService = userService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        doTry(
                "Add dual control privileges",
                this::addModulePrivileges,
                logger
        );
    }

    private void addModulePrivileges() {
        userService.addModulePrivileges(this);
    }

    @Override
    public String getModuleName() {
        return DualControlService.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(getModuleName(),
                Privileges.RESOURCE_TOU_CALENDARS.getKey(), Privileges.RESOURCE_TOU_CALENDARS_DESCRIPTION.getKey(),
                Collections.singletonList(Privileges.Constants.MANAGE_TOU_CALENDARS)));
        return resources;
    }
}
