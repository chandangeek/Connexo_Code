/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.Privileges;
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

/**
 * Installs the Custom Property Set bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-10 (14:36)
 */
public class Installer implements FullInstaller, PrivilegesProvider {

    private final DataModel dataModel;
    private final UserService userService;

    @Inject
    public Installer(DataModel dataModel, UserService userService) {
        super();
        this.dataModel = dataModel;
        this.userService = userService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        userService.addModulePrivileges(this);
    }

    @Override
    public String getModuleName() {
        return CustomPropertySetService.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(getModuleName(),
                Privileges.RESOURCE_CUSTOM_PROPERTIES.getKey(), Privileges.RESOURCE_CUSTOM_PROPERTIES_DESCRIPTION.getKey(),
                Arrays.asList(Privileges.Constants.ADMINISTER_PRIVILEGES, Privileges.Constants.VIEW_PRIVILEGES)));
        resources.add(userService.createModuleResourceWithPrivileges(getModuleName(),
                Privileges.RESOURCE_CUSTOM_PRIVILEGES.getKey(), Privileges.RESOURCE_CUSTOM_PRIVILEGES_DESCRIPTION.getKey(),
                Arrays.asList(
                        Privileges.Constants.VIEW_CUSTOM_PROPERTIES_1, Privileges.Constants.VIEW_CUSTOM_PROPERTIES_2,
                        Privileges.Constants.VIEW_CUSTOM_PROPERTIES_3, Privileges.Constants.VIEW_CUSTOM_PROPERTIES_4,
                        Privileges.Constants.EDIT_CUSTOM_PROPERTIES_1, Privileges.Constants.EDIT_CUSTOM_PROPERTIES_2,
                        Privileges.Constants.EDIT_CUSTOM_PROPERTIES_3, Privileges.Constants.EDIT_CUSTOM_PROPERTIES_4)));
        return resources;
    }


}