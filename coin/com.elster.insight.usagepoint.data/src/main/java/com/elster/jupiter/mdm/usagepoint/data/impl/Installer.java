package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.logging.Logger;

class Installer implements FullInstaller {

    private final UserService userService;
    private final UsagePointGroupPrivilegesProvider usagePointGroupPrivilegesProvider;

    @Inject
    public Installer(UserService userService, UsagePointGroupPrivilegesProvider usagePointGroupPrivilegesProvider) {
        this.userService = userService;
        this.usagePointGroupPrivilegesProvider = usagePointGroupPrivilegesProvider;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        userService.addModulePrivileges(usagePointGroupPrivilegesProvider);
    }
}
