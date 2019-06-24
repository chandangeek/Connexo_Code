/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.app.impl;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.app.MdcAppService;

import javax.inject.Inject;

import static com.elster.jupiter.issue.security.Privileges.Constants.CREATE_ISSUE;

public class UpgraderV10_7 implements Upgrader {

    private final UserService userService;

    @Inject
    public UpgraderV10_7(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        userService.grantGroupWithPrivilege(UserService.BATCH_EXECUTOR_ROLE, MdcAppService.APPLICATION_KEY, getNewMeterExpertPrivileges());
        userService.grantGroupWithPrivilege(MdcAppService.Roles.METER_EXPERT.value(), MdcAppService.APPLICATION_KEY, getNewMeterExpertPrivileges());
        userService.grantGroupWithPrivilege(MdcAppService.Roles.METER_OPERATOR.value(), MdcAppService.APPLICATION_KEY, getNewMeterOperatorPrivileges());
    }

    private String[] getNewMeterOperatorPrivileges() {
        return new String[]{
                CREATE_ISSUE
        };
    }
    private String[] getNewMeterExpertPrivileges() {
        return new String[]{
                CREATE_ISSUE
        };
    }
}