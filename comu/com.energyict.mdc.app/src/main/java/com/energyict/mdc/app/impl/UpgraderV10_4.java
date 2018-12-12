/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.app.impl;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.app.MdcAppService;

import javax.inject.Inject;

public class UpgraderV10_4 implements Upgrader {

    private final UserService userService;

    @Inject
    public UpgraderV10_4(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        userService.grantGroupWithPrivilege(UserService.BATCH_EXECUTOR_ROLE, MdcAppService.APPLICATION_KEY, getNewMeterExpertPrivileges());
        userService.grantGroupWithPrivilege(MdcAppService.Roles.METER_EXPERT.value(), MdcAppService.APPLICATION_KEY, getNewMeterExpertPrivileges());
    }

    private String[] getNewMeterExpertPrivileges() {
        return new String[]{
                // REGISTERED DEVICES KPI
                com.energyict.mdc.device.topology.kpi.Privileges.Constants.ADMINISTRATE,
                com.energyict.mdc.device.topology.kpi.Privileges.Constants.VIEW,

                // TASKS
                com.elster.jupiter.tasks.security.Privileges.Constants.VIEW_TASK_OVERVIEW,

                // Security accessors management
                com.elster.jupiter.pki.security.Privileges.Constants.VIEW_SECURITY_ACCESSORS,
                com.elster.jupiter.pki.security.Privileges.Constants.EDIT_SECURITY_ACCESSORS
        };
    }
}
