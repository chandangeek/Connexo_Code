package com.energyict.mdc.app.impl;

import java.util.Arrays;
import java.util.List;

class MdcAppPrivileges {

    //export
    static String ADMINISTRATE_DATA_EXPORT_TASK = "privilege.administrate.dataExportTask";
    static String VIEW_DATA_EXPORT_TASK = "privilege.view.dataExportTask";
    static String UPDATE_DATA_EXPORT_TASK = "privilege.update.dataExportTask";
    static String UPDATE_SCHEDULE_DATA_EXPORT_TASK = "privilege.update.schedule.dataExportTask";
    static String RUN_DATA_EXPORT_TASK = "privilege.run.dataExportTask";

    //import
    static String VIEW_IMPORT_SERVICES = "privilege.view.importServices";

    //validation
    static String ADMINISTRATE_VALIDATION_CONFIGURATION = "privilege.administrate.validationConfiguration";
    static String VIEW_VALIDATION_CONFIGURATION = "privilege.view.validationConfiguration";

    static String VALIDATE_MANUAL = "privilege.view.validateManual";

    static String FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE = "privilege.view.fineTuneValidationConfiguration.onDevice";
    static String FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION = "privilege.view.fineTuneValidationConfiguration.onDeviceConfiguration";

    //yellowfin
    static String VIEW_REPORTS = "privilege.view.reports";



   static final String PRIVILEGE_VIEW_REPORTS = "privilege.view.reports";

   static final String PRIVILEGE_RUN_EXPORT = "privilege.run.dataExportTask";
   static final String PRIVILEGE_VIEW_EXPORT = "privilege.view.dataExportTask";

   static final String PRIVILEGE_VIEW_COMMUNICATION = "privilege.view.communicationAdministration";
   static final String PRIVILEGE_VIEW_DEVICE = "privilege.view.device";
   static final String PRIVILEGE_OPERATE_DEVICECOMMUNICATION = "privilege.operate.deviceCommunication";
   static final String PRIVILEGE_ADMINISTRATE_DEVICEDATA = "privilege.administrate.deviceData";
   static final String PRIVILEGE_VIEW_DEVICETYPE = "privilege.view.deviceType";
   static final String PRIVILEGE_ACTION_ISSUE_ACTION = "privilege.action.issue";
   static final String PRIVILEGE_ASSIGN_ISSUE = "privilege.assign.issue";
   static final String PRIVILEGE_CLOSE_ISSUE = "privilege.close.issue";
   static final String PRIVILEGE_COMMENT_ISSUE = "privilege.comment.issue";
   static final String PRIVILEGE_VIEW_ISSUE = "privilege.view.issue";
   static final String PRIVILEGE_VIEW_MASTER_DATA = "privilege.view.masterData";
   static final String PRIVILEGE_VIEW_VALIDATION = "privilege.view.validationConfiguration";
   static final String VIEW_MDC_IMPORT_SERVICES = "privilege.view.mdc.importServices";
   static final String VIEW_FWC_CAMPAIGNS = "privilege.view.firmware.campaign";



    static List<String> getApplicationPrivileges(){
        return Arrays.asList(ADMINISTRATE_DATA_EXPORT_TASK,
                VIEW_DATA_EXPORT_TASK, UPDATE_DATA_EXPORT_TASK, UPDATE_SCHEDULE_DATA_EXPORT_TASK,
                RUN_DATA_EXPORT_TASK,
                VIEW_IMPORT_SERVICES,

                ADMINISTRATE_VALIDATION_CONFIGURATION, VIEW_VALIDATION_CONFIGURATION,
                VALIDATE_MANUAL,FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE,
                FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION,
                VIEW_REPORTS,

                PRIVILEGE_VIEW_REPORTS,

                PRIVILEGE_RUN_EXPORT,
                PRIVILEGE_VIEW_EXPORT,

                PRIVILEGE_VIEW_COMMUNICATION,
                PRIVILEGE_VIEW_DEVICE,
                PRIVILEGE_OPERATE_DEVICECOMMUNICATION,
                PRIVILEGE_ADMINISTRATE_DEVICEDATA,
                PRIVILEGE_VIEW_DEVICETYPE,
                PRIVILEGE_ACTION_ISSUE_ACTION,
                PRIVILEGE_ASSIGN_ISSUE,
                PRIVILEGE_CLOSE_ISSUE,
                PRIVILEGE_COMMENT_ISSUE,
                PRIVILEGE_VIEW_ISSUE,
                PRIVILEGE_VIEW_MASTER_DATA,
                PRIVILEGE_VIEW_VALIDATION,
                VIEW_MDC_IMPORT_SERVICES,
                VIEW_FWC_CAMPAIGNS




        );
    }

}