/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;

import com.energyict.mdc.common.tasks.security.Privileges;

import javax.inject.Inject;

import static com.elster.jupiter.orm.Version.version;

class UpgraderV10_7 implements Upgrader {

    private final DataModel dataModel;
    private final UserService userService;
    private final PrivilegesProvider10_7 privilegesProvider10_7;

    @Inject
    UpgraderV10_7(DataModel dataModel, UserService userService, PrivilegesProvider10_7 privilegesProvider10_7) {
        this.dataModel = dataModel;
        this.userService = userService;
        this.privilegesProvider10_7 = privilegesProvider10_7;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 7));
        userService.addModulePrivileges(privilegesProvider10_7);
        userService.grantGroupWithPrivilege(UserService.BATCH_EXECUTOR_ROLE, "MDC", getExecuteComTaskPrivileges());
    }

    private String[] getExecuteComTaskPrivileges() {
        return new String[] { Privileges.Constants.EXECUTE_SCHEDULE_PLAN_COM_TASK_1,
                Privileges.Constants.EXECUTE_SCHEDULE_PLAN_COM_TASK_2,
                Privileges.Constants.EXECUTE_SCHEDULE_PLAN_COM_TASK_3,
                Privileges.Constants.EXECUTE_SCHEDULE_PLAN_COM_TASK_4, };
    }
}
