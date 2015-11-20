package com.elster.insight.app.impl;

import java.util.Arrays;
import java.util.List;

class InsightAppPrivileges {

    static List<String> getApplicationPrivileges() {
        return Arrays.asList(
        
        	//com.elster.jupiter.metering.security - usage points
            com.elster.jupiter.metering.security.Privileges.Constants.ADMIN_ANY,
            com.elster.jupiter.metering.security.Privileges.Constants.ADMIN_OWN,
            com.elster.jupiter.metering.security.Privileges.Constants.BROWSE_ANY,
            com.elster.jupiter.metering.security.Privileges.Constants.BROWSE_OWN,
        		
            //validation
            com.elster.jupiter.validation.security.Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION,
            com.elster.jupiter.validation.security.Privileges.Constants.VIEW_VALIDATION_CONFIGURATION,
            com.elster.jupiter.validation.security.Privileges.Constants.VALIDATE_MANUAL,
            com.elster.jupiter.validation.security.Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE,
            com.elster.jupiter.validation.security.Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION,

            //metrology configuration
            com.elster.insight.usagepoint.config.Privileges.Constants.ADMIN_ANY_METROLOGY_CONFIG,
            com.elster.insight.usagepoint.config.Privileges.Constants.BROWSE_ANY_METROLOGY_CONFIG,
            
            //com.elster.jupiter.time
            com.elster.jupiter.time.security.Privileges.Constants.ADMINISTRATE_RELATIVE_PERIOD,
            com.elster.jupiter.time.security.Privileges.Constants.VIEW_RELATIVE_PERIOD,
            

            //com.elster.jupiter.cps
            com.elster.jupiter.cps.Privileges.Constants.VIEW_CUSTOM_PROPERTIES_1,
            com.elster.jupiter.cps.Privileges.Constants.VIEW_CUSTOM_PROPERTIES_2,
            com.elster.jupiter.cps.Privileges.Constants.VIEW_CUSTOM_PROPERTIES_3,
            com.elster.jupiter.cps.Privileges.Constants.VIEW_CUSTOM_PROPERTIES_4,

            com.elster.jupiter.cps.Privileges.Constants.EDIT_CUSTOM_PROPERTIES_1,
            com.elster.jupiter.cps.Privileges.Constants.EDIT_CUSTOM_PROPERTIES_2,
            com.elster.jupiter.cps.Privileges.Constants.EDIT_CUSTOM_PROPERTIES_3,
            com.elster.jupiter.cps.Privileges.Constants.EDIT_CUSTOM_PROPERTIES_4,

           	//import
        	com.elster.jupiter.fileimport.security.Privileges.Constants.VIEW_IMPORT_SERVICES);

    }
}
