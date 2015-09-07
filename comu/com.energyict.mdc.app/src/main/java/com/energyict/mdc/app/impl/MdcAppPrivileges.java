package com.energyict.mdc.app.impl;

import java.util.Arrays;
import java.util.List;

class MdcAppPrivileges {

    static List<String> getApplicationPrivileges(){
        return Arrays.asList(

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

                //issue
                com.elster.jupiter.issue.security.Privileges.Constants.ACTION_ISSUE,
                com.elster.jupiter.issue.security.Privileges.Constants.ASSIGN_ISSUE,
                com.elster.jupiter.issue.security.Privileges.Constants.CLOSE_ISSUE,
                com.elster.jupiter.issue.security.Privileges.Constants.COMMENT_ISSUE,
                com.elster.jupiter.issue.security.Privileges.Constants.VIEW_ISSUE,

                //issue configuration
                com.elster.jupiter.issue.security.Privileges.Constants.ADMINISTRATE_CREATION_RULE,
                com.elster.jupiter.issue.security.Privileges.Constants.VIEW_ASSIGNMENT_RULE,
                com.elster.jupiter.issue.security.Privileges.Constants.VIEW_CREATION_RULE,

                //yellowfin reports
                com.elster.jupiter.yellowfin.security.Privileges.Constants.VIEW_REPORTS,

                //mdc.engine.config
                com.energyict.mdc.engine.config.security.Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION,
                com.energyict.mdc.engine.config.security.Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION,
                com.energyict.mdc.engine.config.security.Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION_INTERNAL,

                //mdc.device.data
                com.energyict.mdc.device.data.security.Privileges.Constants.ADD_DEVICE,
                com.energyict.mdc.device.data.security.Privileges.Constants.REMOVE_DEVICE,
                com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_DEVICE,

                //mdc.device.data - Device communication
                com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION,
                com.energyict.mdc.device.data.security.Privileges.Constants.OPERATE_DEVICE_COMMUNICATION,

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

                //mdc.device.config

                com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_1,
                com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_2,
                com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_3,
                com.energyict.mdc.device.config.security.Privileges.Constants.EXECUTE_DEVICE_MESSAGE_4,

                //mdc.device.config - Device security
                com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_1,
                com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_2,
                com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_3,
                com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_4,

                com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_1,
                com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_2,
                com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_3,
                com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_4,

                //mdc.device.config - Device type
                com.energyict.mdc.device.config.security.Privileges.Constants.ADMINISTRATE_DEVICE_TYPE,
                com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_TYPE,

                //mdc.device.config - Master data
                com.energyict.mdc.device.config.security.Privileges.Constants.ADMINISTRATE_MASTER_DATA,
                com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_MASTER_DATA,

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

                //import
                com.elster.jupiter.fileimport.security.Privileges.Constants.VIEW_IMPORT_SERVICES,

                //mdc.firmware
                com.energyict.mdc.firmware.security.Privileges.Constants.ADMINISTRATE_FIRMWARE_CAMPAIGN,
                com.energyict.mdc.firmware.security.Privileges.Constants.VIEW_FIRMWARE_CAMPAIGN,

                //mdc.device.lifecycle
                com.energyict.mdc.device.lifecycle.config.Privileges.Constants.INITIATE_ACTION_1,
                com.energyict.mdc.device.lifecycle.config.Privileges.Constants.INITIATE_ACTION_2,
                com.energyict.mdc.device.lifecycle.config.Privileges.Constants.INITIATE_ACTION_3,
                com.energyict.mdc.device.lifecycle.config.Privileges.Constants.INITIATE_ACTION_4,

                com.energyict.mdc.device.lifecycle.config.Privileges.Constants.CONFIGURE_DEVICE_LIFE_CYCLE,
                com.energyict.mdc.device.lifecycle.config.Privileges.Constants.VIEW_DEVICE_LIFE_CYCLE,

                //com.elster.jupiter.metering.security - usage points
                com.elster.jupiter.metering.security.Privileges.Constants.ADMIN_ANY,
                com.elster.jupiter.metering.security.Privileges.Constants.ADMIN_OWN,
                com.elster.jupiter.metering.security.Privileges.Constants.BROWSE_ANY,
                com.elster.jupiter.metering.security.Privileges.Constants.BROWSE_OWN,

                //mdc.scheduling.security
                com.energyict.mdc.scheduling.security.Privileges.Constants.ADMINISTRATE_SHARED_COMMUNICATION_SCHEDULE,
                com.energyict.mdc.scheduling.security.Privileges.Constants.VIEW_SHARED_COMMUNICATION_SCHEDULE,

                //com.elster.
                com.elster.jupiter.time.security.Privileges.Constants.ADMINISTRATE_RELATIVE_PERIOD,
                com.elster.jupiter.time.security.Privileges.Constants.VIEW_RELATIVE_PERIOD
        );
    }

}