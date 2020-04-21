package com.elster.jupiter.systemproperties;

import com.elster.jupiter.orm.DataModelUpgrader;
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


    @Inject
    Installer(UserService userService) {
        super();
        this.userService = userService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        userService.addModulePrivileges(this);
    }


    @Override
    public String getModuleName() {
        return SystemPropertyApllication.COMPONENT_NAME;
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
