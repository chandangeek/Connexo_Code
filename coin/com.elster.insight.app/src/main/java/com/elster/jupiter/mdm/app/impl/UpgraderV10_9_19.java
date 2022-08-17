/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.mdm.app.impl;

import com.elster.jupiter.mdm.app.MdmAppService;
import com.elster.jupiter.orm.DataModelUpgrader;
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
        userService.grantGroupWithPrivilege(UserService.BATCH_EXECUTOR_ROLE, MdmAppService.APPLICATION_KEY, newBatchExecutorPrivileges());
    }

    private String[] newBatchExecutorPrivileges() {
        return new String[]{
                //web-services
                com.elster.jupiter.soap.whiteboard.cxf.security.Privileges.Constants.INVOKE_WEB_SERVICES,
        };
    }
}
