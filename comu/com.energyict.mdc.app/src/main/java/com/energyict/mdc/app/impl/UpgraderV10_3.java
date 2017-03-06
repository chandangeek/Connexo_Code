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

    private UserService userService;

    @Inject
    public UpgraderV10_3(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        userService.grantGroupWithPrivilege(UserService.BATCH_EXECUTOR_ROLE, MdcAppService.APPLICATION_KEY, getNewMeterExpertPrivileges());
        userService.grantGroupWithPrivilege(MdcAppService.Roles.METER_EXPERT.value(), MdcAppService.APPLICATION_KEY, getNewMeterExpertPrivileges());
        userService.grantGroupWithPrivilege(MdcAppService.Roles.METER_OPERATOR.value(), MdcAppService.APPLICATION_KEY, getNewMeterOperatorPrivileges());
    }

    private String[] getNewMeterExpertPrivileges() {
        return new String[]{
                //data quality kpi
                com.elster.jupiter.dataquality.security.Privileges.Constants.ADMINISTER_DATA_QUALITY_KPI_CONFIGURATION,
                com.elster.jupiter.dataquality.security.Privileges.Constants.VIEW_DATA_QUALITY_KPI_CONFIGURATION,
                com.elster.jupiter.dataquality.security.Privileges.Constants.VIEW_DATA_QUALITY_RESULTS
        };
    }

    private String[] getNewMeterOperatorPrivileges() {
        return new String[]{
                //data quality kpi
                com.elster.jupiter.dataquality.security.Privileges.Constants.VIEW_DATA_QUALITY_KPI_CONFIGURATION,
                com.elster.jupiter.dataquality.security.Privileges.Constants.VIEW_DATA_QUALITY_RESULTS
        };
    }
}
