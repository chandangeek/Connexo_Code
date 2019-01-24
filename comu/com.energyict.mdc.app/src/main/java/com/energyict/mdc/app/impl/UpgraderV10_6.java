/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.app.impl;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.app.MdcAppService;

import javax.inject.Inject;

import static com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTRATE_ZONE;
import static com.elster.jupiter.metering.security.Privileges.Constants.VIEW_ZONE;

public class UpgraderV10_6 implements Upgrader {

    private final UserService userService;

    @Inject
    public UpgraderV10_6(UserService userService) {
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
                com.energyict.mdc.tou.campaign.security.Privileges.Constants.VIEW_TOU_CAMPAIGNS
        };
    }

    private String[] getNewMeterExpertPrivileges() {
        return new String[]{
                // ZONE
                ADMINISTRATE_ZONE,
                VIEW_ZONE,
                // TOU
                com.energyict.mdc.tou.campaign.security.Privileges.Constants.ADMINISTER_TOU_CAMPAIGNS,
                com.energyict.mdc.tou.campaign.security.Privileges.Constants.VIEW_TOU_CAMPAIGNS
        };
    }
}