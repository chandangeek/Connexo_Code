package com.elster.insight.app.impl;

import java.util.Arrays;
import java.util.List;

class InsightAppPrivileges {

    static List<String> getApplicationAllPrivileges() {
        return Arrays.asList(

                //usage point
                com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTER_ANY_USAGEPOINT,
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
                com.elster.jupiter.metering.security.Privileges.Constants.VIEW_SERVICECATEGORY
        );
    }
}
