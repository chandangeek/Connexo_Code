/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.system.app.impl;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.system.app.SysAppService;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;

public class UpgraderV10_9_19 implements Upgrader {
    private final UserService userService;

    @Inject
    public UpgraderV10_9_19(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        userService.grantGroupWithPrivilege(UserService.BATCH_EXECUTOR_ROLE, SysAppService.APPLICATION_KEY, newBatchExecutorPrivileges());
        userService.grantGroupWithPrivilege(UserService.SYSTEM_ADMIN_ROLE, SysAppService.APPLICATION_KEY, newSystemAdminPrivileges());
    }

    private String[] newBatchExecutorPrivileges() {
        return new String[]{
                //web-services
                com.elster.jupiter.soap.whiteboard.cxf.security.Privileges.Constants.INVOKE_WEB_SERVICES,
        };
    }

    private String[] newSystemAdminPrivileges() {
        return new String[]{
                //web-services
                com.elster.jupiter.soap.whiteboard.cxf.security.Privileges.Constants.INVOKE_WEB_SERVICES,
        };
    }
}
