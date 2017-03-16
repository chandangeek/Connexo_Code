/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.app.impl;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.app.MdcAppService;

import javax.inject.Inject;

public class UpgraderV10_3 implements Upgrader {
    private final UserService userService;

    @Inject
    public UpgraderV10_3(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        userService.grantGroupWithPrivilege(MdcAppService.Roles.METER_EXPERT.value(), MdcAppService.APPLICATION_KEY, getNewMeterExpertPrivileges());
        userService.grantGroupWithPrivilege(MdcAppService.Roles.METER_OPERATOR.value(), MdcAppService.APPLICATION_KEY, getNewMeterOperatorPrivileges());
    }

    private String[] getNewMeterExpertPrivileges() {
        return new String[] {com.elster.jupiter.fileimport.security.Privileges.Constants.IMPORT_FILE,
                com.energyict.mdc.device.data.security.Privileges.Constants.ESTIMATE_WITH_RULE,
                com.energyict.mdc.device.data.security.Privileges.Constants.EDIT_WITH_ESTIMATOR};
    }

    private String[] getNewMeterOperatorPrivileges() {
        return new String[] {com.elster.jupiter.fileimport.security.Privileges.Constants.IMPORT_FILE,
                com.energyict.mdc.device.data.security.Privileges.Constants.ESTIMATE_WITH_RULE,
                com.energyict.mdc.device.data.security.Privileges.Constants.EDIT_WITH_ESTIMATOR};
    }

}
