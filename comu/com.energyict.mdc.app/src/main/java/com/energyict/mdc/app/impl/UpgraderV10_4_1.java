/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.app.impl;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.app.MdcAppService;

import javax.inject.Inject;

public class UpgraderV10_4_1 implements Upgrader {
    private final UserService userService;

    @Inject
    public UpgraderV10_4_1(UserService userService) {
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
                com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_CRL_REQUEST
        };
    }

    private String[] getNewMeterExpertPrivileges() {
        return new String[]{
                com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_CRL_REQUEST,
                com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTER_CRL_REQUEST
        };
    }
}
