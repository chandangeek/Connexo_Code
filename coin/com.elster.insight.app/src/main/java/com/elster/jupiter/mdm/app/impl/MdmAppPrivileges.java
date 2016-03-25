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

                //validation
                com.elster.jupiter.validation.security.Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION,
                com.elster.jupiter.validation.security.Privileges.Constants.VIEW_VALIDATION_CONFIGURATION,

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
                com.elster.jupiter.bpm.security.Privileges.Constants.EXECUTE_PROCESSES_LVL_4
        );
    }
}
