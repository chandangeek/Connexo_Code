package com.elster.jupiter.system.app.impl;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.system.app.SysAppService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.logging.Logger;

final class Installer implements FullInstaller {

    private final UserService userService;

    @Inject
    Installer(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        String[] adminPrivileges = getAdminPrivileges();
        userService.grantGroupWithPrivilege(UserService.DEFAULT_ADMIN_ROLE, SysAppService.APPLICATION_KEY, adminPrivileges);
        userService.grantGroupWithPrivilege(UserService.BATCH_EXECUTOR_ROLE, SysAppService.APPLICATION_KEY, adminPrivileges);
    }

    private String[] getAdminPrivileges() {
        return SysAppPrivileges.getApplicationPrivileges().stream().toArray(String[]::new);
    }

}
