/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
                // Usage point
                com.elster.jupiter.metering.security.Privileges.Constants.MANAGE_USAGE_POINT_ATTRIBUTES,
                com.elster.jupiter.metering.security.Privileges.Constants.ESTIMATE_WITH_RULE,
                com.elster.jupiter.metering.security.Privileges.Constants.EDIT_WITH_ESTIMATOR,

                //com.elster.jupiter.cps
                com.elster.jupiter.cps.Privileges.Constants.VIEW_CUSTOM_PROPERTIES_1,
                com.elster.jupiter.cps.Privileges.Constants.VIEW_CUSTOM_PROPERTIES_2,
                com.elster.jupiter.cps.Privileges.Constants.VIEW_CUSTOM_PROPERTIES_3,
                com.elster.jupiter.cps.Privileges.Constants.VIEW_CUSTOM_PROPERTIES_4,

                com.elster.jupiter.cps.Privileges.Constants.EDIT_CUSTOM_PROPERTIES_1,
                com.elster.jupiter.cps.Privileges.Constants.EDIT_CUSTOM_PROPERTIES_2,
                com.elster.jupiter.cps.Privileges.Constants.EDIT_CUSTOM_PROPERTIES_3,
                com.elster.jupiter.cps.Privileges.Constants.EDIT_CUSTOM_PROPERTIES_4,

                // Export
                com.elster.jupiter.export.security.Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK,
                com.elster.jupiter.export.security.Privileges.Constants.VIEW_DATA_EXPORT_TASK,
                com.elster.jupiter.export.security.Privileges.Constants.UPDATE_DATA_EXPORT_TASK,
                com.elster.jupiter.export.security.Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK,
                com.elster.jupiter.export.security.Privileges.Constants.RUN_DATA_EXPORT_TASK,
                com.elster.jupiter.export.security.Privileges.Constants.VIEW_HISTORY,

                // Import services
                com.elster.jupiter.fileimport.security.Privileges.Constants.VIEW_IMPORT_SERVICES,
                com.elster.jupiter.fileimport.security.Privileges.Constants.VIEW_HISTORY,

                // Usage point groups
                com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.ADMINISTER_USAGE_POINT_GROUP,
                com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.ADMINISTER_USAGE_POINT_ENUMERATED_GROUP,
                com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.VIEW_USAGE_POINT_GROUP_DETAIL,

                // Estimation
                com.elster.jupiter.estimation.security.Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION,
                com.elster.jupiter.estimation.security.Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION,
                com.elster.jupiter.estimation.security.Privileges.Constants.UPDATE_ESTIMATION_CONFIGURATION,
                com.elster.jupiter.estimation.security.Privileges.Constants.UPDATE_SCHEDULE_ESTIMATION_TASK,
                com.elster.jupiter.estimation.security.Privileges.Constants.RUN_ESTIMATION_TASK,
                com.elster.jupiter.estimation.security.Privileges.Constants.VIEW_ESTIMATION_TASK,
                com.elster.jupiter.estimation.security.Privileges.Constants.ADMINISTRATE_ESTIMATION_TASK,
                com.elster.jupiter.estimation.security.Privileges.Constants.ESTIMATE_MANUAL,

                // Estimation configuration on metrology configuration
                com.elster.jupiter.mdm.usagepoint.config.security.Privileges.Constants.VIEW_ESTIMATION_ON_METROLOGY_CONFIGURATION,
                com.elster.jupiter.mdm.usagepoint.config.security.Privileges.Constants.ADMINISTER_ESTIMATION_ON_METROLOGY_CONFIGURATION,

                // Usage point life cycle
                com.elster.jupiter.usagepoint.lifecycle.config.Privileges.Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER,
                com.elster.jupiter.usagepoint.lifecycle.config.Privileges.Constants.USAGE_POINT_LIFE_CYCLE_VIEW,
                com.elster.jupiter.usagepoint.lifecycle.config.Privileges.Constants.EXECUTE_TRANSITION_1,
                com.elster.jupiter.usagepoint.lifecycle.config.Privileges.Constants.EXECUTE_TRANSITION_2,
                com.elster.jupiter.usagepoint.lifecycle.config.Privileges.Constants.EXECUTE_TRANSITION_3,
                com.elster.jupiter.usagepoint.lifecycle.config.Privileges.Constants.EXECUTE_TRANSITION_4,

                //data quality kpi
                com.elster.jupiter.dataquality.security.Privileges.Constants.ADMINISTER_DATA_QUALITY_KPI_CONFIGURATION,
                com.elster.jupiter.dataquality.security.Privileges.Constants.VIEW_DATA_QUALITY_KPI_CONFIGURATION,
                com.elster.jupiter.dataquality.security.Privileges.Constants.VIEW_DATA_QUALITY_RESULTS
        };
    }

    private String[] getNewDataOperatorPrivileges() {
        return new String[]{
                // Export
                com.elster.jupiter.export.security.Privileges.Constants.VIEW_DATA_EXPORT_TASK,
                com.elster.jupiter.export.security.Privileges.Constants.RUN_DATA_EXPORT_TASK,
                com.elster.jupiter.export.security.Privileges.Constants.VIEW_HISTORY,

                // Import services
                com.elster.jupiter.fileimport.security.Privileges.Constants.VIEW_IMPORT_SERVICES,
                com.elster.jupiter.fileimport.security.Privileges.Constants.VIEW_HISTORY,


                // Usage point groups
                com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.VIEW_USAGE_POINT_GROUP_DETAIL,

                // Estimation
                com.elster.jupiter.estimation.security.Privileges.Constants.RUN_ESTIMATION_TASK,
                com.elster.jupiter.estimation.security.Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION,
                com.elster.jupiter.estimation.security.Privileges.Constants.VIEW_ESTIMATION_TASK,
                com.elster.jupiter.estimation.security.Privileges.Constants.ESTIMATE_MANUAL,

                // Usage point life cycle
                com.elster.jupiter.usagepoint.lifecycle.config.Privileges.Constants.USAGE_POINT_LIFE_CYCLE_VIEW,
                com.elster.jupiter.usagepoint.lifecycle.config.Privileges.Constants.EXECUTE_TRANSITION_1,
                com.elster.jupiter.usagepoint.lifecycle.config.Privileges.Constants.EXECUTE_TRANSITION_2,
                com.elster.jupiter.usagepoint.lifecycle.config.Privileges.Constants.EXECUTE_TRANSITION_3,
                com.elster.jupiter.usagepoint.lifecycle.config.Privileges.Constants.EXECUTE_TRANSITION_4,

                //data quality kpi
                com.elster.jupiter.dataquality.security.Privileges.Constants.VIEW_DATA_QUALITY_KPI_CONFIGURATION,
                com.elster.jupiter.dataquality.security.Privileges.Constants.VIEW_DATA_QUALITY_RESULTS
        };
    }
}
