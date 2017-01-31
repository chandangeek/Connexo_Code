/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.yellowfin.impl;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.yellowfin.YellowfinService;
import com.elster.jupiter.yellowfin.security.Privileges;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

class Installer implements FullInstaller, PrivilegesProvider {

    String REPORT_DESIGNER_ROLE = "Report designer";
    String REPORT_DESIGNER_ROLE_DESCRIPTION = "Reports designer privilege";

    private final Logger logger = Logger.getLogger(Installer.class.getName());
    private final UserService userService;
    private final YellowfinService yellowfinService;

    @Inject
    Installer(UserService userService, YellowfinService yellowfinService) {
        this.userService = userService;
        this.yellowfinService = yellowfinService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        userService.addModulePrivileges(this);
        doTry(
                "Create default roles for YFN",
                this::createDefaultRoles,
                logger
        );
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        return Arrays.asList(
                userService.createModuleResourceWithPrivileges(getModuleName(),
                        Privileges.RESOURCE_REPORTS.getKey(), Privileges.RESOURCE_REPORTS_DESCRIPTION.getKey(),
                        Arrays.asList(Privileges.Constants.VIEW_REPORTS, Privileges.Constants.DESIGN_REPORTS)));
    }

    @Override
    public String getModuleName() {
        return YellowfinService.COMPONENTNAME;
    }

    private void createDefaultRoles() {
        Group group = userService.createGroup(REPORT_DESIGNER_ROLE, REPORT_DESIGNER_ROLE_DESCRIPTION);
        userService.grantGroupWithPrivilege(group.getName(), "YFN", new String[]{"privilege.design.reports"});
    }

}
