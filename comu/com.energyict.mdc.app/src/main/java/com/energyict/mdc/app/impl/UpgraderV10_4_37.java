/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.app.impl;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.app.MdcAppService;

import javax.inject.Inject;

public class UpgraderV10_4_37 implements Upgrader {

    private UserService userService;

    @Inject
    public UpgraderV10_4_37(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        userService.grantGroupWithPrivilege(UserService.BATCH_EXECUTOR_ROLE, MdcAppService.APPLICATION_KEY, newBatchExecutorPrivileges());
    }

    private String[] newBatchExecutorPrivileges() {
        return new String[]{
                //web-services
                com.elster.jupiter.soap.whiteboard.cxf.security.Privileges.Constants.INVOKE_WEB_SERVICES,
        };
    }

}