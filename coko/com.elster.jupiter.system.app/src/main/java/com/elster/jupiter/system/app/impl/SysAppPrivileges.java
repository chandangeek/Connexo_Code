package com.elster.jupiter.system.app.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Lucian on 6/29/2015.
 */
class SysAppPrivileges {

    //license
    static String VIEW_LICENSE = "privilege.view.license";
    static String UPLOAD_LICENSE = "privilege.upload.license";

    //users
    static String ADMINISTRATE_USER_ROLE = "privilege.administrate.userAndRole";
    static String VIEW_USER_ROLE = "privilege.view.userAndRole";
    
    //time periods
    static String ADMINISTRATE_RELATIVE_PERIOD = "privilege.administrate.period";
    static String VIEW_RELATIVE_PERIOD = "privilege.view.period";

    //export
    static String ADMINISTRATE_DATA_EXPORT_TASK = "privilege.administrate.dataExportTask";
    static String VIEW_DATA_EXPORT_TASK = "privilege.view.dataExportTask";
    static String UPDATE_DATA_EXPORT_TASK = "privilege.update.dataExportTask";
    static String UPDATE_SCHEDULE_DATA_EXPORT_TASK = "privilege.update.schedule.dataExportTask";
    static String RUN_DATA_EXPORT_TASK = "privilege.run.dataExportTask";

    //import
    static String ADMINISTRATE_IMPORT_SERVICES = "privilege.administrate.importServices";
    static String VIEW_IMPORT_SERVICES = "privilege.view.importServices";


    static List<String> getApplicationPrivileges(){
        return Arrays.asList(VIEW_LICENSE, UPLOAD_LICENSE,ADMINISTRATE_USER_ROLE, VIEW_USER_ROLE,
                ADMINISTRATE_RELATIVE_PERIOD, VIEW_RELATIVE_PERIOD,ADMINISTRATE_DATA_EXPORT_TASK,
                VIEW_DATA_EXPORT_TASK,UPDATE_DATA_EXPORT_TASK,UPDATE_SCHEDULE_DATA_EXPORT_TASK,
                RUN_DATA_EXPORT_TASK,ADMINISTRATE_IMPORT_SERVICES,VIEW_IMPORT_SERVICES);
    }

}
