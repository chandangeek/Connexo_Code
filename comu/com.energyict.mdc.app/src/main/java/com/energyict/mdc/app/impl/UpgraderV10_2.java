/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.app.impl;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.app.MdcAppService;

import javax.inject.Inject;

/**
 * Created by bbl on 10/06/2016.
 */
public class UpgraderV10_2 implements Upgrader {

    private UserService userService;

    @Inject
    public UpgraderV10_2(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        String[] mdcPrivileges = getMdcPrivileges();

        userService.grantGroupWithPrivilege(MdcAppService.Roles.METER_OPERATOR.value(), MdcAppService.APPLICATION_KEY, mdcPrivileges);
        userService.grantGroupWithPrivilege(MdcAppService.Roles.METER_EXPERT.value(), MdcAppService.APPLICATION_KEY, mdcPrivileges);
        userService.grantGroupWithPrivilege(UserService.BATCH_EXECUTOR_ROLE, MdcAppService.APPLICATION_KEY, mdcPrivileges);
    }

    private String[] getMdcPrivileges() {
        return MdcAppPrivileges.getApplicationPrivileges().stream().toArray(String[]::new);
    }
}
