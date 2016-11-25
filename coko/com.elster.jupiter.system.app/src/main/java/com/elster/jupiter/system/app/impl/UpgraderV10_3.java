package com.elster.jupiter.system.app.impl;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.system.app.SysAppService;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;

class UpgraderV10_3 implements Upgrader {

    private UserService userService;

    @Inject
    public UpgraderV10_3(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        userService.grantGroupWithPrivilege(UserService.DEFAULT_ADMIN_ROLE, SysAppService.APPLICATION_KEY, getNewAdminPrivileges());
    }

    private String[] getNewAdminPrivileges() {
        return new String[]{
                //public api
                com.elster.jupiter.kore.api.security.Privileges.Constants.PUBLIC_REST_API
        };
    }
}
