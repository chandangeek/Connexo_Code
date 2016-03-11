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

                //com.elster.jupiter.servicecall
                com.elster.jupiter.servicecall.security.Privileges.Constants.VIEW_SERVICE_CALLS,
                com.elster.jupiter.servicecall.security.Privileges.Constants.CHANGE_SERVICE_CALL_STATE
        );
    }
}
