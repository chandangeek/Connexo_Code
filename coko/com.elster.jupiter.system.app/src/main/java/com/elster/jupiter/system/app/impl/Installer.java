package com.elster.jupiter.system.app.impl;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.system.app.SysAppService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.logging.Logger;

final class Installer implements FullInstaller, Upgrader {

    private final UserService userService;

    @Inject
    Installer(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        grantPrivileges();
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        grantPrivileges();
    }

    private String[] getAdminPrivileges() {
        return SysAppPrivileges.getApplicationPrivileges().stream().toArray(String[]::new);
    }

    private void grantPrivileges() {
        String[] adminPrivileges = adminPrivileges();
        userService.grantGroupWithPrivilege(UserService.DEFAULT_ADMIN_ROLE, SysAppService.APPLICATION_KEY, adminPrivileges);
        userService.grantGroupWithPrivilege(UserService.BATCH_EXECUTOR_ROLE, SysAppService.APPLICATION_KEY, adminPrivileges);
        userService.grantGroupWithPrivilege(UserService.DEFAULT_INSTALLER_ROLE, SysAppService.APPLICATION_KEY, installerPrivileges());
    }

    private String[] installerPrivileges() {
        return new String[]{
                //license
                com.elster.jupiter.license.security.Privileges.Constants.VIEW_LICENSE,
                com.elster.jupiter.license.security.Privileges.Constants.UPLOAD_LICENSE,
                //users
                com.elster.jupiter.users.security.Privileges.Constants.ADMINISTRATE_USER_ROLE,
                com.elster.jupiter.users.security.Privileges.Constants.VIEW_USER_ROLE
        };
    }

    private String[] adminPrivileges() {
        return new String[]{
                //users
                com.elster.jupiter.users.security.Privileges.Constants.ADMINISTRATE_USER_ROLE,
                com.elster.jupiter.users.security.Privileges.Constants.VIEW_USER_ROLE,
        };
    }
}
