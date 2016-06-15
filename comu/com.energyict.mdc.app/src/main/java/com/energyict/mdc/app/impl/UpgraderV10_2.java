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
        String[] newPrivileges = new String[]{com.energyict.mdc.engine.config.security.Privileges.Constants.VIEW_STATUS_COMMUNICATION_INFRASTRUCTURE};

        userService.grantGroupWithPrivilege(MdcAppService.Roles.METER_OPERATOR.value(), MdcAppService.APPLICATION_KEY, newPrivileges);
        userService.grantGroupWithPrivilege(MdcAppService.Roles.METER_EXPERT.value(), MdcAppService.APPLICATION_KEY, newPrivileges);
        userService.grantGroupWithPrivilege(UserService.BATCH_EXECUTOR_ROLE, MdcAppService.APPLICATION_KEY, newPrivileges);
    }
}
