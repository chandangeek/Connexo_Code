package com.elster.insight.app.impl;

import com.elster.jupiter.metering.security.Privileges;

import java.util.Arrays;
import java.util.List;

class InsightAppPrivileges {

    static List<String> getApplicationAllPrivileges() {  // Add these privileges to 'Insight data expert' and 'admin' role
        return Arrays.asList(

                //com.elster.jupiter.metering.security - usage points
                com.elster.jupiter.metering.security.Privileges.Constants.ADMIN_ANY,
                com.elster.jupiter.metering.security.Privileges.Constants.ADMIN_OWN,
                com.elster.jupiter.metering.security.Privileges.Constants.BROWSE_ANY,
                com.elster.jupiter.metering.security.Privileges.Constants.BROWSE_OWN,
                com.elster.jupiter.metering.security.Privileges.Constants.VIEW_READINGTYPE,
                com.elster.jupiter.metering.security.Privileges.Constants.VIEW_SERVICECATEGORY,

                //com.elster.jupiter.time
                com.elster.jupiter.time.security.Privileges.Constants.VIEW_RELATIVE_PERIOD,

                //validation
                com.elster.jupiter.validation.security.Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION,
                com.elster.jupiter.validation.security.Privileges.Constants.VIEW_VALIDATION_CONFIGURATION,
                com.elster.jupiter.validation.security.Privileges.Constants.VALIDATE_MANUAL,
                com.elster.jupiter.validation.security.Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE,
                com.elster.jupiter.validation.security.Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION,

                //com.elster.jupiter.cps
                com.elster.jupiter.cps.Privileges.Constants.VIEW_PRIVILEGES,

                //metrology configuration
                Privileges.Constants.BROWSE_ANY_METROLOGY_CONFIGURATION,
                Privileges.Constants.ADMINISTER_ANY_METROLOGY_CONFIGURATION);
    }

    static List<String> getApplicationViewPrivileges() {  // Add these privileges to 'Insight data operator' role
        return Arrays.asList(

                //com.elster.jupiter.metering.security - usage points
                com.elster.jupiter.metering.security.Privileges.Constants.BROWSE_ANY,
                com.elster.jupiter.metering.security.Privileges.Constants.BROWSE_OWN,
                com.elster.jupiter.metering.security.Privileges.Constants.VIEW_READINGTYPE,
                com.elster.jupiter.metering.security.Privileges.Constants.VIEW_SERVICECATEGORY,

                //com.elster.jupiter.time
                com.elster.jupiter.time.security.Privileges.Constants.VIEW_RELATIVE_PERIOD,

                //validation
                com.elster.jupiter.validation.security.Privileges.Constants.VIEW_VALIDATION_CONFIGURATION,
                com.elster.jupiter.validation.security.Privileges.Constants.VALIDATE_MANUAL,
                com.elster.jupiter.validation.security.Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE,
                com.elster.jupiter.validation.security.Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION,

                //metrology configuration
                Privileges.Constants.BROWSE_ANY_METROLOGY_CONFIGURATION);
    }

    static List<String> getAdminApplicationAllPrivileges() {  // Add these privileges to 'Insight data expert' role
        return Arrays.asList(

                // appserver
//        	com.elster.jupiter.appserver.security.Privileges.Constants.ADMINISTRATE_APPSEVER,
                com.elster.jupiter.appserver.security.Privileges.Constants.VIEW_APPSEVER,

                //com.elster.jupiter.cps
                com.elster.jupiter.cps.Privileges.Constants.ADMINISTER_PRIVILEGES,
                com.elster.jupiter.cps.Privileges.Constants.VIEW_PRIVILEGES,

                //com.elster.jupiter.time
                com.elster.jupiter.time.security.Privileges.Constants.ADMINISTRATE_RELATIVE_PERIOD,
                com.elster.jupiter.time.security.Privileges.Constants.VIEW_RELATIVE_PERIOD,

                //com.elster.jupiter.metering.security - usage points
                com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTRATE_READINGTYPE,
                com.elster.jupiter.metering.security.Privileges.Constants.VIEW_READINGTYPE,
                com.elster.jupiter.metering.security.Privileges.Constants.VIEW_SERVICECATEGORY,

                //Users
                com.elster.jupiter.users.security.Privileges.Constants.ADMINISTRATE_USER_ROLE,
                com.elster.jupiter.users.security.Privileges.Constants.VIEW_USER_ROLE,

                //import
                com.elster.jupiter.fileimport.security.Privileges.Constants.ADMINISTRATE_IMPORT_SERVICES,
                com.elster.jupiter.fileimport.security.Privileges.Constants.VIEW_IMPORT_SERVICES);

    }

    static List<String> getAdminApplicationViewPrivileges() { // Add these privileges to 'Insight data operator' role
        return Arrays.asList(

                // appserver
                com.elster.jupiter.appserver.security.Privileges.Constants.VIEW_APPSEVER,

                //com.elster.jupiter.cps
                com.elster.jupiter.cps.Privileges.Constants.VIEW_PRIVILEGES,

                //com.elster.jupiter.metering.security - usage points
                com.elster.jupiter.metering.security.Privileges.Constants.VIEW_READINGTYPE,
                com.elster.jupiter.metering.security.Privileges.Constants.VIEW_SERVICECATEGORY,

                //com.elster.jupiter.time
                com.elster.jupiter.time.security.Privileges.Constants.VIEW_RELATIVE_PERIOD,

                //Users
                com.elster.jupiter.users.security.Privileges.Constants.VIEW_USER_ROLE,

                //import
                com.elster.jupiter.fileimport.security.Privileges.Constants.VIEW_IMPORT_SERVICES);

    }

}
