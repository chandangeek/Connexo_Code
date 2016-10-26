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
                com.elster.jupiter.metering.security.Privileges.Constants.VIEW_ANY_USAGEPOINT,
                com.elster.jupiter.metering.security.Privileges.Constants.VIEW_OWN_USAGEPOINT,
                com.elster.jupiter.metering.security.Privileges.Constants.VIEW_READINGTYPE,

                //validation
                com.elster.jupiter.validation.security.Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION,
                com.elster.jupiter.validation.security.Privileges.Constants.VIEW_VALIDATION_CONFIGURATION,
                com.elster.jupiter.validation.security.Privileges.Constants.VALIDATE_MANUAL,

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

                //Calendars
                com.elster.jupiter.calendar.security.Privileges.Constants.MANAGE_TOU_CALENDARS
        );
    }
}
