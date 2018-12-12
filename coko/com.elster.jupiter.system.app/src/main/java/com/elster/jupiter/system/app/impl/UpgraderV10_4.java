/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.system.app.impl;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.system.app.SysAppService;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;

public class UpgraderV10_4 implements Upgrader {

    private UserService userService;

    @Inject
    public UpgraderV10_4(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        userService.grantGroupWithPrivilege(UserService.DEFAULT_INSTALLER_ROLE, SysAppService.APPLICATION_KEY, newInstallerPrivileges());
        userService.grantGroupWithPrivilege(UserService.DEFAULT_ADMIN_ROLE, SysAppService.APPLICATION_KEY, newUserAdminPrivileges());
    }

    private String[] newInstallerPrivileges() {
        return new String[]{
                //certificates
                com.elster.jupiter.pki.security.Privileges.Constants.VIEW_CERTIFICATES,
        };
    }

    private String[] newUserAdminPrivileges() {
        return new String[]{
                //certificates
                com.elster.jupiter.pki.security.Privileges.Constants.VIEW_CERTIFICATES,
        };
    }
}
