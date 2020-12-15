/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.app.impl;

import com.elster.jupiter.mdm.app.MdmAppService;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;

public class UpgraderV10_9 implements Upgrader {

    private UserService userService;

    @Inject
    public UpgraderV10_9(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        userService.grantGroupWithPrivilege(UserService.BATCH_EXECUTOR_ROLE, MdmAppService.APPLICATION_KEY, getNewBatchExecutorPrivileges());
        userService.grantGroupWithPrivilege(MdmAppService.Roles.DATA_EXPERT.value(), MdmAppService.APPLICATION_KEY, getNewDataExpertPrivileges());
    }

    private String[] getNewBatchExecutorPrivileges() {
        return getNewDataExpertPrivileges();
    }

    private String[] getNewDataExpertPrivileges() {
        return new String[]{
                com.elster.jupiter.soap.whiteboard.cxf.security.Privileges.Constants.CANCEL_WEB_SERVICES
        };
    }
}