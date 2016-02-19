package com.elster.insight.app.impl;

import java.util.Arrays;
import java.util.List;

class InsightAppPrivileges {

    static List<String> getApplicationAllPrivileges() {
        return Arrays.asList(

                //usage point
                com.elster.jupiter.metering.security.Privileges.Constants.ADMIN_ANY,
                com.elster.jupiter.metering.security.Privileges.Constants.ADMIN_OWN,
                com.elster.jupiter.metering.security.Privileges.Constants.BROWSE_ANY,
                com.elster.jupiter.metering.security.Privileges.Constants.BROWSE_OWN,

                //validation
                com.elster.jupiter.validation.security.Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION,
                com.elster.jupiter.validation.security.Privileges.Constants.VIEW_VALIDATION_CONFIGURATION,

                //metrology configuration
                com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTER_ANY_METROLOGY_CONFIGURATION,
                com.elster.jupiter.metering.security.Privileges.Constants.BROWSE_ANY_METROLOGY_CONFIGURATION,

                //service category
                com.elster.jupiter.metering.security.Privileges.Constants.VIEW_SERVICECATEGORY
        );
    }
}
