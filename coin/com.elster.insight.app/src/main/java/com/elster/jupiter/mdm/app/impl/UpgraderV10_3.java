package com.elster.jupiter.mdm.app.impl;

import com.elster.jupiter.mdm.app.MdmAppService;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;

public class UpgraderV10_3 implements Upgrader {

    private UserService userService;

    @Inject
    public UpgraderV10_3(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        userService.grantGroupWithPrivilege(UserService.BATCH_EXECUTOR_ROLE, MdmAppService.APPLICATION_KEY, getNewBatchExecutorPrivileges());
        userService.grantGroupWithPrivilege(MdmAppService.Roles.DATA_EXPERT.value(), MdmAppService.APPLICATION_KEY, getNewDataExpertPrivileges());
        userService.grantGroupWithPrivilege(MdmAppService.Roles.DATA_OPERATOR.value(), MdmAppService.APPLICATION_KEY, getNewDataOperatorPrivileges());
    }

    private String[] getNewBatchExecutorPrivileges() {
        return getNewDataExpertPrivileges();
    }

    private String[] getNewDataExpertPrivileges() {
        return new String[]{
                //Import services
                com.elster.jupiter.fileimport.security.Privileges.Constants.VIEW_IMPORT_SERVICES,

                //estimation
                com.elster.jupiter.estimation.security.Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION,
                com.elster.jupiter.estimation.security.Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION,
                com.elster.jupiter.estimation.security.Privileges.Constants.UPDATE_ESTIMATION_CONFIGURATION,
                com.elster.jupiter.estimation.security.Privileges.Constants.UPDATE_SCHEDULE_ESTIMATION_TASK,
                com.elster.jupiter.estimation.security.Privileges.Constants.RUN_ESTIMATION_TASK,
                com.elster.jupiter.estimation.security.Privileges.Constants.VIEW_ESTIMATION_TASK,
                com.elster.jupiter.estimation.security.Privileges.Constants.ADMINISTRATE_ESTIMATION_TASK
        };
    }

    private String[] getNewDataOperatorPrivileges() {
        return new String[]{
                //Import services
                com.elster.jupiter.fileimport.security.Privileges.Constants.VIEW_IMPORT_SERVICES,

                //estimation
                com.elster.jupiter.estimation.security.Privileges.Constants.RUN_ESTIMATION_TASK,
                com.elster.jupiter.estimation.security.Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION,
                com.elster.jupiter.estimation.security.Privileges.Constants.VIEW_ESTIMATION_TASK
        };
    }
}
