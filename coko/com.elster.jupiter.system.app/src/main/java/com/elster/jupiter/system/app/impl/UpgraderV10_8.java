package com.elster.jupiter.system.app.impl;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.system.app.SysAppService;
import com.elster.jupiter.systemproperties.security.Privileges;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;

public class UpgraderV10_8 implements Upgrader {
    private final UserService userService;

    @Inject
    public UpgraderV10_8(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        userService.grantGroupWithPrivilege(UserService.SYSTEM_ADMIN_ROLE, SysAppService.APPLICATION_KEY, getNewSysAdminPrivileges());
        userService.grantGroupWithPrivilege(UserService.BATCH_EXECUTOR_ROLE, SysAppService.APPLICATION_KEY, getNewSysAdminPrivileges());
    }

    private String[] getNewSysAdminPrivileges() {
        return new String[]{
                Privileges.Constants.VIEW_SYS_PROPS,
                Privileges.Constants.ADMINISTER_SYS_PROPS
        };
    }
}
