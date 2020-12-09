/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.app.impl;

import java.util.Arrays;
import java.util.List;

class MdcAppPrivileges {

    static List<String> getApplicationPrivileges() {
        return Arrays.asList(

                //monitor
                com.energyict.mdc.engine.monitor.app.security.MdcMonitorAppPrivileges.MONITOR_COMMUNICATION_SERVER,

                //validation
                com.elster.jupiter.validation.security.Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION,
                com.elster.jupiter.validation.security.Privileges.Constants.VIEW_VALIDATION_CONFIGURATION,
                com.elster.jupiter.validation.security.Privileges.Constants.VALIDATE_MANUAL,
                com.elster.jupiter.validation.security.Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE,
                com.elster.jupiter.validation.security.Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION,

                //export
                com.elster.jupiter.export.security.Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK,
                com.elster.jupiter.export.security.Privileges.Constants.VIEW_DATA_EXPORT_TASK,
                com.elster.jupiter.export.security.Privileges.Constants.UPDATE_DATA_EXPORT_TASK,
                com.elster.jupiter.export.security.Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK,
                com.elster.jupiter.export.security.Privileges.Constants.RUN_DATA_EXPORT_TASK,
                com.elster.jupiter.export.security.Privileges.Constants.VIEW_HISTORY,

                //issue
                com.elster.jupiter.issue.security.Privileges.Constants.ACTION_ISSUE,
                com.elster.jupiter.issue.security.Privileges.Constants.ASSIGN_ISSUE,
                com.elster.jupiter.issue.security.Privileges.Constants.CLOSE_ISSUE,
                com.elster.jupiter.issue.security.Privileges.Constants.COMMENT_ISSUE,
                com.elster.jupiter.issue.security.Privileges.Constants.VIEW_ISSUE,
                com.elster.jupiter.issue.security.Privileges.Constants.CREATE_ISSUE,

                //issue configuration
                com.elster.jupiter.issue.security.Privileges.Constants.ADMINISTRATE_CREATION_RULE,
                com.elster.jupiter.issue.security.Privileges.Constants.VIEW_ASSIGNMENT_RULE,
                com.elster.jupiter.issue.security.Privileges.Constants.VIEW_CREATION_RULE,

                //alarm
                com.energyict.mdc.device.alarms.security.Privileges.Constants.ACTION_ALARM,
                com.energyict.mdc.device.alarms.security.Privileges.Constants.ASSIGN_ALARM,
                com.energyict.mdc.device.alarms.security.Privileges.Constants.CLOSE_ALARM,
                com.energyict.mdc.device.alarms.security.Privileges.Constants.COMMENT_ALARM,
                com.energyict.mdc.device.alarms.security.Privileges.Constants.VIEW_ALARM,

                //alarm configuration
                com.energyict.mdc.device.alarms.security.Privileges.Constants.ADMINISTRATE_ALARM_CREATION_RULE,
                com.energyict.mdc.device.alarms.security.Privileges.Constants.VIEW_ALARM_ASSIGNMENT_RULE,
                com.energyict.mdc.device.alarms.security.Privileges.Constants.VIEW_ALARM_CREATION_RULE,

                //yellowfin reports
                com.elster.jupiter.yellowfin.security.Privileges.Constants.VIEW_REPORTS,

                //mdc.engine
                com.energyict.mdc.engine.security.Privileges.Constants.OPERATE_MOBILE_COMSERVER,

                //mdc.engine.config
                com.energyict.mdc.engine.config.security.Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION,
                com.energyict.mdc.engine.config.security.Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION,
                com.energyict.mdc.engine.config.security.Privileges.Constants.VIEW_STATUS_COMMUNICATION_INFRASTRUCTURE,

                //mdc.device.data
                com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE,
                com.energyict.mdc.device.data.security.Privileges.Constants.REMOVE_DEVICE,
                com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_DEVICE,
                com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTER_DEVICE_TIME_SLICED_CPS,
                //mdc.device.data - Device communication
                com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION,
                com.energyict.mdc.device.data.security.Privileges.Constants.OPERATE_DEVICE_COMMUNICATION,
                com.energyict.mdc.device.data.security.Privileges.Constants.RUN_WITH_PRIO,

                // mdc.device.data - Device data
                com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_ATTRIBUTE,
                com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_DATA,
                com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTER_DECOMMISSIONED_DEVICE_DATA,

                //mdc.device.data - Devices group
                com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_GROUP,
                com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_ENUMERATED_GROUP,
                com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_DEVICE_GROUP_DETAIL,

                //mdc.device.data - Device data
                com.energyict.mdc.device.data.security.Privileges.Constants.IMPORT_INVENTORY_MANAGEMENT,
                com.energyict.mdc.device.data.security.Privileges.Constants.REVOKE_INVENTORY_MANAGEMENT,

                //mdc.device.data - Data collection kpi
                com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_DATA_COLLECTION_KPI,
                com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTER_DATA_COLLECTION_KPI,

                //mdc.device.data - CRL request
                com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_CRL_REQUEST,
                com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTER_CRL_REQUEST,

                //CommandRules
                com.energyict.mdc.device.command.security.Privileges.Constants.VIEW_COMMAND_LIMITATION_RULE,
                com.energyict.mdc.device.command.security.Privileges.Constants.ADMINISTRATE_COMMAND_LIMITATION_RULE,
                com.energyict.mdc.device.command.security.Privileges.Constants.APPROVE_COMMAND_LIMITATION_RULE,

                //mdc.device.config
                com.energyict.mdc.common.device.config.DeviceConfigConstants.EXECUTE_DEVICE_MESSAGE_1,
                com.energyict.mdc.common.device.config.DeviceConfigConstants.EXECUTE_DEVICE_MESSAGE_2,
                com.energyict.mdc.common.device.config.DeviceConfigConstants.EXECUTE_DEVICE_MESSAGE_3,
                com.energyict.mdc.common.device.config.DeviceConfigConstants.EXECUTE_DEVICE_MESSAGE_4,

                //pki - attributes of security accessors
                com.elster.jupiter.pki.security.Privileges.Constants.EDIT_SECURITY_PROPERTIES_1,
                com.elster.jupiter.pki.security.Privileges.Constants.EDIT_SECURITY_PROPERTIES_2,
                com.elster.jupiter.pki.security.Privileges.Constants.EDIT_SECURITY_PROPERTIES_3,
                com.elster.jupiter.pki.security.Privileges.Constants.EDIT_SECURITY_PROPERTIES_4,

                com.elster.jupiter.pki.security.Privileges.Constants.VIEW_SECURITY_PROPERTIES_1,
                com.elster.jupiter.pki.security.Privileges.Constants.VIEW_SECURITY_PROPERTIES_2,
                com.elster.jupiter.pki.security.Privileges.Constants.VIEW_SECURITY_PROPERTIES_3,
                com.elster.jupiter.pki.security.Privileges.Constants.VIEW_SECURITY_PROPERTIES_4,

                //mdc.device.config - Device type
                com.energyict.mdc.common.device.config.DeviceConfigConstants.ADMINISTRATE_DEVICE_TYPE,
                com.energyict.mdc.common.device.config.DeviceConfigConstants.VIEW_DEVICE_TYPE,

                //mdc.device.config - Master data
                com.energyict.mdc.common.device.config.DeviceConfigConstants.ADMINISTRATE_MASTER_DATA,
                com.energyict.mdc.common.device.config.DeviceConfigConstants.VIEW_MASTER_DATA,

                //estimation
                com.elster.jupiter.estimation.security.Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION,
                com.elster.jupiter.estimation.security.Privileges.Constants.ADMINISTRATE_ESTIMATION_TASK,
                com.elster.jupiter.estimation.security.Privileges.Constants.RUN_ESTIMATION_TASK,
                com.elster.jupiter.estimation.security.Privileges.Constants.UPDATE_ESTIMATION_CONFIGURATION,
                com.elster.jupiter.estimation.security.Privileges.Constants.UPDATE_SCHEDULE_ESTIMATION_TASK,
                com.elster.jupiter.estimation.security.Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION,
                com.elster.jupiter.estimation.security.Privileges.Constants.VIEW_ESTIMATION_TASK,
                com.elster.jupiter.estimation.security.Privileges.Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE,
                com.elster.jupiter.estimation.security.Privileges.Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE_CONFIGURATION,
                com.elster.jupiter.estimation.security.Privileges.Constants.ESTIMATE_WITH_RULE,
                com.elster.jupiter.estimation.security.Privileges.Constants.EDIT_WITH_ESTIMATOR,

                //import
                com.elster.jupiter.fileimport.security.Privileges.Constants.VIEW_IMPORT_SERVICES,
                com.elster.jupiter.fileimport.security.Privileges.Constants.VIEW_HISTORY,
                com.elster.jupiter.fileimport.security.Privileges.Constants.IMPORT_FILE,
                //mdc.firmware
                com.energyict.mdc.firmware.security.Privileges.Constants.ADMINISTRATE_FIRMWARE_CAMPAIGN,
                com.energyict.mdc.firmware.security.Privileges.Constants.VIEW_FIRMWARE_CAMPAIGN,

                //mdc.device.lifecycle
                com.energyict.mdc.common.device.lifecycle.config.Constants.INITIATE_ACTION_1,
                com.energyict.mdc.common.device.lifecycle.config.Constants.INITIATE_ACTION_2,
                com.energyict.mdc.common.device.lifecycle.config.Constants.INITIATE_ACTION_3,
                com.energyict.mdc.common.device.lifecycle.config.Constants.INITIATE_ACTION_4,

                com.energyict.mdc.common.device.lifecycle.config.Constants.CONFIGURE_DEVICE_LIFE_CYCLE,
                com.energyict.mdc.common.device.lifecycle.config.Constants.VIEW_DEVICE_LIFE_CYCLE,

                //REGISTERED DEVICES KPI
                com.energyict.mdc.device.topology.kpi.Privileges.Constants.ADMINISTRATE,
                com.energyict.mdc.device.topology.kpi.Privileges.Constants.VIEW,

                //com.elster.jupiter.metering.security - usage points
                com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTER_ANY_USAGEPOINT,
                com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTER_OWN_USAGEPOINT,
                com.elster.jupiter.metering.security.Privileges.Constants.VIEW_ANY_USAGEPOINT,
                com.elster.jupiter.metering.security.Privileges.Constants.VIEW_OWN_USAGEPOINT,
                com.elster.jupiter.metering.security.Privileges.Constants.VIEW_READINGTYPE,

                //mdc.scheduling.security
                com.energyict.mdc.scheduling.security.Privileges.Constants.ADMINISTRATE_SHARED_COMMUNICATION_SCHEDULE,
                com.energyict.mdc.scheduling.security.Privileges.Constants.VIEW_SHARED_COMMUNICATION_SCHEDULE,

                //com.elster.jupiter.time
                com.elster.jupiter.time.security.Privileges.Constants.ADMINISTRATE_RELATIVE_PERIOD,
                com.elster.jupiter.time.security.Privileges.Constants.VIEW_RELATIVE_PERIOD,

                //com.elster.jupiter.bpm.security
                com.elster.jupiter.bpm.security.Privileges.Constants.EXECUTE_TASK,
                com.elster.jupiter.bpm.security.Privileges.Constants.VIEW_TASK,
                com.elster.jupiter.bpm.security.Privileges.Constants.ASSIGN_TASK,
                com.elster.jupiter.bpm.security.Privileges.Constants.VIEW_BPM,
                com.elster.jupiter.bpm.security.Privileges.Constants.ADMINISTRATE_BPM,
                com.elster.jupiter.bpm.security.Privileges.Constants.EXECUTE_PROCESSES_LVL_1,
                com.elster.jupiter.bpm.security.Privileges.Constants.EXECUTE_PROCESSES_LVL_2,
                com.elster.jupiter.bpm.security.Privileges.Constants.EXECUTE_PROCESSES_LVL_3,
                com.elster.jupiter.bpm.security.Privileges.Constants.EXECUTE_PROCESSES_LVL_4,

                //com.elster.jupiter.cps
                com.elster.jupiter.cps.Privileges.Constants.VIEW_CUSTOM_PROPERTIES_1,
                com.elster.jupiter.cps.Privileges.Constants.VIEW_CUSTOM_PROPERTIES_2,
                com.elster.jupiter.cps.Privileges.Constants.VIEW_CUSTOM_PROPERTIES_3,
                com.elster.jupiter.cps.Privileges.Constants.VIEW_CUSTOM_PROPERTIES_4,

                com.elster.jupiter.cps.Privileges.Constants.EDIT_CUSTOM_PROPERTIES_1,
                com.elster.jupiter.cps.Privileges.Constants.EDIT_CUSTOM_PROPERTIES_2,
                com.elster.jupiter.cps.Privileges.Constants.EDIT_CUSTOM_PROPERTIES_3,
                com.elster.jupiter.cps.Privileges.Constants.EDIT_CUSTOM_PROPERTIES_4,

                //com.elster.jupiter.servicecall
                com.elster.jupiter.servicecall.security.Privileges.Constants.VIEW_SERVICE_CALLS,
                com.elster.jupiter.servicecall.security.Privileges.Constants.CHANGE_SERVICE_CALL_STATE,

                //metrology configuration
                com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION,
                com.elster.jupiter.metering.security.Privileges.Constants.VIEW_METROLOGY_CONFIGURATION,

                //service category
                com.elster.jupiter.metering.security.Privileges.Constants.VIEW_SERVICECATEGORY,

                //data quality kpi
                com.elster.jupiter.dataquality.security.Privileges.Constants.ADMINISTER_DATA_QUALITY_KPI_CONFIGURATION,
                com.elster.jupiter.dataquality.security.Privileges.Constants.VIEW_DATA_QUALITY_KPI_CONFIGURATION,
                com.elster.jupiter.dataquality.security.Privileges.Constants.VIEW_DATA_QUALITY_RESULTS,

                //device validation/estimation configuration
                com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTER_VALIDATION_CONFIGURATION,
                com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTER_ESTIMATION_CONFIGURATION,

                //calendars
                com.elster.jupiter.calendar.security.Privileges.Constants.MANAGE_TOU_CALENDARS,

                //tasks
                com.elster.jupiter.tasks.security.Privileges.Constants.VIEW_TASK_OVERVIEW,
                com.elster.jupiter.tasks.security.Privileges.Constants.SUSPEND_TASK_OVERVIEW,
                com.elster.jupiter.tasks.security.Privileges.Constants.ADMINISTER_TASK_OVERVIEW,
                com.elster.jupiter.tasks.security.Privileges.Constants.EXECUTE_ADD_CERTIFICATE_REQUEST_DATA_TASK,

                // security accessors management
                com.elster.jupiter.pki.security.Privileges.Constants.VIEW_SECURITY_ACCESSORS,
                com.elster.jupiter.pki.security.Privileges.Constants.EDIT_SECURITY_ACCESSORS,

                // zone
                com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTRATE_ZONE,
                com.elster.jupiter.metering.security.Privileges.Constants.VIEW_ZONE,

                com.elster.jupiter.audit.security.Privileges.Constants.VIEW_AUDIT_LOG,

                // time of use campaigns management
                com.energyict.mdc.tou.campaign.security.Privileges.Constants.ADMINISTER_TOU_CAMPAIGNS,
                com.energyict.mdc.tou.campaign.security.Privileges.Constants.VIEW_TOU_CAMPAIGNS,

                // communication task execution
                com.energyict.mdc.common.tasks.security.Privileges.Constants.EXECUTE_SCHEDULE_PLAN_COM_TASK_1,
                com.energyict.mdc.common.tasks.security.Privileges.Constants.EXECUTE_SCHEDULE_PLAN_COM_TASK_2,
                com.energyict.mdc.common.tasks.security.Privileges.Constants.EXECUTE_SCHEDULE_PLAN_COM_TASK_3,
                com.energyict.mdc.common.tasks.security.Privileges.Constants.EXECUTE_SCHEDULE_PLAN_COM_TASK_4,

                //web services
                com.elster.jupiter.soap.whiteboard.cxf.security.Privileges.Constants.VIEW_WEB_SERVICES,
                com.elster.jupiter.soap.whiteboard.cxf.security.Privileges.Constants.VIEW_HISTORY_WEB_SERVICES,
                com.elster.jupiter.soap.whiteboard.cxf.security.Privileges.Constants.RETRY_WEB_SERVICES,
                com.elster.jupiter.soap.whiteboard.cxf.security.Privileges.Constants.CANCEL_WEB_SERVICES,

                //sap soap webservices
                com.energyict.mdc.sap.soap.webservices.security.Privileges.Constants.SEND_WEB_SERVICE_REQUEST

        );
    }
}
