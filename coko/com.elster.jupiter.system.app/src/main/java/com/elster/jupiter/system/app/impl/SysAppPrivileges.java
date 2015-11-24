package com.elster.jupiter.system.app.impl;

import java.util.Arrays;
import java.util.List;

class SysAppPrivileges {

    static List<String> getApplicationPrivileges() {
        return Arrays.asList(
                //appserver
                com.elster.jupiter.appserver.security.Privileges.Constants.ADMINISTRATE_APPSEVER,
                com.elster.jupiter.appserver.security.Privileges.Constants.VIEW_APPSEVER,
                //license
                com.elster.jupiter.license.security.Privileges.Constants.VIEW_LICENSE,
                com.elster.jupiter.license.security.Privileges.Constants.UPLOAD_LICENSE,
                //users
                com.elster.jupiter.users.security.Privileges.Constants.ADMINISTRATE_USER_ROLE,
                com.elster.jupiter.users.security.Privileges.Constants.VIEW_USER_ROLE,
                //time periods
                com.elster.jupiter.time.security.Privileges.Constants.ADMINISTRATE_RELATIVE_PERIOD,
                com.elster.jupiter.time.security.Privileges.Constants.VIEW_RELATIVE_PERIOD,
                //import
                com.elster.jupiter.fileimport.security.Privileges.Constants.ADMINISTRATE_IMPORT_SERVICES,
                com.elster.jupiter.fileimport.security.Privileges.Constants.VIEW_IMPORT_SERVICES,
                //data lifecycle
                com.elster.jupiter.data.lifecycle.security.Privileges.Constants.VIEW_DATA_PURGE,
                com.elster.jupiter.data.lifecycle.security.Privileges.Constants.ADMINISTRATE_DATA_PURGE,
                //custom property sets
                com.elster.jupiter.cps.Privileges.Constants.ADMINISTER_PRIVILEGES,
                com.elster.jupiter.cps.Privileges.Constants.VIEW_PRIVILEGES,
                //deployment information
                com.elster.jupiter.system.security.Privileges.Constants.VIEW_DEPLOYMENT_INFORMATION,
                //metering
                com.elster.jupiter.metering.security.Privileges.Constants.VIEW_READINGTYPE,
                com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTRATE_READINGTYPE);
    }
}
