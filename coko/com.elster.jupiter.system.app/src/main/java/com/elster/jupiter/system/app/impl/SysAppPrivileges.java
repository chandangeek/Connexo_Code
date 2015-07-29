package com.elster.jupiter.system.app.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Lucian on 6/29/2015.
 */
class SysAppPrivileges {

    static List<String> getApplicationPrivileges(){
        return Arrays.asList(
                //appserver
                com.elster.jupiter.appserver.security.Privileges.ADMINISTRATE_APPSEVER,
                com.elster.jupiter.appserver.security.Privileges.VIEW_APPSEVER,
                //license
                com.elster.jupiter.license.security.Privileges.VIEW_LICENSE,
                com.elster.jupiter.license.security.Privileges.UPLOAD_LICENSE,
                //users
                com.elster.jupiter.users.security.Privileges.ADMINISTRATE_USER_ROLE,
                com.elster.jupiter.users.security.Privileges.VIEW_USER_ROLE,
                //time periods
                com.elster.jupiter.time.security.Privileges.ADMINISTRATE_RELATIVE_PERIOD,
                com.elster.jupiter.time.security.Privileges.VIEW_RELATIVE_PERIOD,
                //import
                com.elster.jupiter.fileimport.security.Privileges.ADMINISTRATE_IMPORT_SERVICES,
                com.elster.jupiter.fileimport.security.Privileges.VIEW_IMPORT_SERVICES,
                //license
                com.elster.jupiter.data.lifecycle.security.Privileges.VIEW_DATA_PURGE,
                com.elster.jupiter.data.lifecycle.security.Privileges.ADMINISTRATE_DATA_PURGE);
    }

}
