package com.elster.jupiter.mdm.app.impl;

import java.util.Arrays;
import java.util.List;

class MdmAppPrivileges {

    static List<String> getApplicationAllPrivileges() {
        return Arrays.asList(

                //usage point
                com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTER_ANY_USAGEPOINT,
                com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTER_USAGEPOINT_TIME_SLICED_CPS,
                com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTER_OWN_USAGEPOINT,
                com.elster.jupiter.metering.security.Privileges.Constants.MANAGE_USAGE_POINT_ATTRIBUTES,
                com.elster.jupiter.metering.security.Privileges.Constants.VIEW_ANY_USAGEPOINT,
                com.elster.jupiter.metering.security.Privileges.Constants.VIEW_OWN_USAGEPOINT,
                com.elster.jupiter.metering.security.Privileges.Constants.VIEW_READINGTYPE,

                //export
                com.elster.jupiter.export.security.Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK,
                com.elster.jupiter.export.security.Privileges.Constants.VIEW_DATA_EXPORT_TASK,
                com.elster.jupiter.export.security.Privileges.Constants.UPDATE_DATA_EXPORT_TASK,
                com.elster.jupiter.export.security.Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK,
                com.elster.jupiter.export.security.Privileges.Constants.RUN_DATA_EXPORT_TASK,

                //validation
                com.elster.jupiter.validation.security.Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION,
                com.elster.jupiter.validation.security.Privileges.Constants.VIEW_VALIDATION_CONFIGURATION,
                com.elster.jupiter.validation.security.Privileges.Constants.VALIDATE_MANUAL,

                // com.elster.jupiter.cps
                com.elster.jupiter.cps.Privileges.Constants.VIEW_CUSTOM_PROPERTIES_1,
                com.elster.jupiter.cps.Privileges.Constants.VIEW_CUSTOM_PROPERTIES_2,
                com.elster.jupiter.cps.Privileges.Constants.VIEW_CUSTOM_PROPERTIES_3,
                com.elster.jupiter.cps.Privileges.Constants.VIEW_CUSTOM_PROPERTIES_4,

                com.elster.jupiter.cps.Privileges.Constants.EDIT_CUSTOM_PROPERTIES_1,
                com.elster.jupiter.cps.Privileges.Constants.EDIT_CUSTOM_PROPERTIES_2,
                com.elster.jupiter.cps.Privileges.Constants.EDIT_CUSTOM_PROPERTIES_3,
                com.elster.jupiter.cps.Privileges.Constants.EDIT_CUSTOM_PROPERTIES_4,

                //metrology configuration
                com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION,
                com.elster.jupiter.metering.security.Privileges.Constants.VIEW_METROLOGY_CONFIGURATION,

                //service category
                com.elster.jupiter.metering.security.Privileges.Constants.VIEW_SERVICECATEGORY,

                //com.elster.jupiter.bpm.security
                com.elster.jupiter.bpm.security.Privileges.Constants.EXECUTE_TASK,
                com.elster.jupiter.bpm.security.Privileges.Constants.VIEW_TASK,
                com.elster.jupiter.bpm.security.Privileges.Constants.ASSIGN_TASK,
                com.elster.jupiter.bpm.security.Privileges.Constants.VIEW_BPM,
                com.elster.jupiter.bpm.security.Privileges.Constants.ADMINISTRATE_BPM,
                com.elster.jupiter.bpm.security.Privileges.Constants.EXECUTE_PROCESSES_LVL_1,
                com.elster.jupiter.bpm.security.Privileges.Constants.EXECUTE_PROCESSES_LVL_2,
                com.elster.jupiter.bpm.security.Privileges.Constants.EXECUTE_PROCESSES_LVL_3,
                com.elster.jupiter.bpm.security.Privileges.Constants.EXECUTE_PROCESSES_LVL_4,

                //com.elster.jupiter.servicecall
                com.elster.jupiter.servicecall.security.Privileges.Constants.VIEW_SERVICE_CALLS,
                com.elster.jupiter.servicecall.security.Privileges.Constants.CHANGE_SERVICE_CALL_STATE,

                //Relative periods
                com.elster.jupiter.time.security.Privileges.Constants.VIEW_RELATIVE_PERIOD,

                //Validation configuration on metrology configuration
                com.elster.jupiter.mdm.usagepoint.config.security.Privileges.Constants.VIEW_VALIDATION_ON_METROLOGY_CONFIGURATION,
                com.elster.jupiter.mdm.usagepoint.config.security.Privileges.Constants.ADMINISTER_VALIDATION_ON_METROLOGY_CONFIGURATION,

                //Estimation configuration on metrology configuration
                com.elster.jupiter.mdm.usagepoint.config.security.Privileges.Constants.VIEW_ESTIMATION_ON_METROLOGY_CONFIGURATION,
                com.elster.jupiter.mdm.usagepoint.config.security.Privileges.Constants.ADMINISTER_ESTIMATION_ON_METROLOGY_CONFIGURATION,

                //Import services
                com.elster.jupiter.fileimport.security.Privileges.Constants.VIEW_IMPORT_SERVICES,

                //Calendars
                com.elster.jupiter.calendar.security.Privileges.Constants.MANAGE_TOU_CALENDARS,

               // Usage point groups
                com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.ADMINISTER_USAGE_POINT_GROUP,
                com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.ADMINISTER_USAGE_POINT_ENUMERATED_GROUP,
                com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.VIEW_USAGE_POINT_GROUP_DETAIL,


                  //estimation
                com.elster.jupiter.estimation.security.Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION,
                com.elster.jupiter.estimation.security.Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION,
                com.elster.jupiter.estimation.security.Privileges.Constants.UPDATE_ESTIMATION_CONFIGURATION,
                com.elster.jupiter.estimation.security.Privileges.Constants.UPDATE_SCHEDULE_ESTIMATION_TASK,
                com.elster.jupiter.estimation.security.Privileges.Constants.RUN_ESTIMATION_TASK,
                com.elster.jupiter.estimation.security.Privileges.Constants.VIEW_ESTIMATION_TASK,
                com.elster.jupiter.estimation.security.Privileges.Constants.ADMINISTRATE_ESTIMATION_TASK,
                com.elster.jupiter.estimation.security.Privileges.Constants.ESTIMATE_MANUAL,

                // Usage point life cycle
                com.elster.jupiter.usagepoint.lifecycle.config.Privileges.Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER,
                com.elster.jupiter.usagepoint.lifecycle.config.Privileges.Constants.USAGE_POINT_LIFE_CYCLE_VIEW,
                com.elster.jupiter.usagepoint.lifecycle.config.Privileges.Constants.EXECUTE_TRANSITION_1,
                com.elster.jupiter.usagepoint.lifecycle.config.Privileges.Constants.EXECUTE_TRANSITION_2,
                com.elster.jupiter.usagepoint.lifecycle.config.Privileges.Constants.EXECUTE_TRANSITION_3,
                com.elster.jupiter.usagepoint.lifecycle.config.Privileges.Constants.EXECUTE_TRANSITION_4
        );
    }
}
