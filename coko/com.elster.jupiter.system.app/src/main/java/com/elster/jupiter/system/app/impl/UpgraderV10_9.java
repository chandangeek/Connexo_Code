/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.system.app.impl;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.system.app.SysAppService;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;

public class UpgraderV10_9 implements Upgrader {
    private final UserService userService;

    @Inject
    public UpgraderV10_9(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        userService.grantGroupWithPrivilege(UserService.SYSTEM_ADMIN_ROLE, SysAppService.APPLICATION_KEY, getNewSysAdminPrivileges());
    }

    private String[] getNewSysAdminPrivileges() {
        return new String[]{
                com.elster.jupiter.soap.whiteboard.cxf.security.Privileges.Constants.VIEW_HISTORY_WEB_SERVICES,
                com.elster.jupiter.soap.whiteboard.cxf.security.Privileges.Constants.RETRY_WEB_SERVICES,
                com.elster.jupiter.soap.whiteboard.cxf.security.Privileges.Constants.CANCEL_WEB_SERVICES,
                com.elster.jupiter.tasks.security.Privileges.Constants.ADMINISTER_TASK_OVERVIEW,
        };
    }
}