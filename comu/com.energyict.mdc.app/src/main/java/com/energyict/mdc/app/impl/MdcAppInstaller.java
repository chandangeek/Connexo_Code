package com.energyict.mdc.app.impl;

import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.app.MdcAppService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 19/01/2015
 * Time: 17:48
 */
@Component(name = "com.energyict.mdc.app.install", service = {InstallService.class}, property = "name=" + MdcAppService.COMPONENTNAME, immediate = true)
@SuppressWarnings("unused")
public class MdcAppInstaller implements InstallService {

    private final Logger logger = Logger.getLogger(MdcAppInstaller.class.getName());
    private volatile UserService userService;

    @Override
    public void install() {
        createDefaultRoles();
        assignPrivilegesToDefaultRoles();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList(UserService.COMPONENTNAME, "APS", "ISU", "DTC", "DDC", "MDC", "SCH", "VAL", "YFN", "BPM", "DES", "FIM", "FWC", "CPS");
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void createDefaultRoles() {
        try {
            userService.createGroup(MdcAppService.Roles.METER_EXPERT.value(), MdcAppService.Roles.METER_EXPERT.description());
            userService.createGroup(MdcAppService.Roles.METER_OPERATOR.value(), MdcAppService.Roles.METER_OPERATOR.description());
            userService.createGroup(MdcAppService.Roles.REPORT_VIEWER.value(), MdcAppService.Roles.REPORT_VIEWER.description());
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }
    }

    private void assignPrivilegesToDefaultRoles() {
        String[] privilegesMeterExpert =  getPrivilegesMeterExpert();

        userService.grantGroupWithPrivilege(MdcAppService.Roles.METER_OPERATOR.value(), MdcAppService.APPLICATION_KEY, getPrivilegesMeterOperator());
        userService.grantGroupWithPrivilege(MdcAppService.Roles.METER_EXPERT.value(), MdcAppService.APPLICATION_KEY, privilegesMeterExpert);
        userService.grantGroupWithPrivilege(UserService.BATCH_EXECUTOR_ROLE, MdcAppService.APPLICATION_KEY, privilegesMeterExpert);
        userService.grantGroupWithPrivilege(MdcAppService.Roles.REPORT_VIEWER.value(), MdcAppService.APPLICATION_KEY, getPrivilegesReportViewer());
        //TODO: workaround: attached Meter expert to user admin !!! to remove this line when the user can be created/added to system
        userService.getUser(1).ifPresent(u -> u.join(userService.getGroups().stream().filter(e -> e.getName().equals(MdcAppService.Roles.METER_EXPERT.value())).findFirst().get()));
        //TODO: workaround: attached Report viewer to user admin !!! to remove this line when the user can be created/added to system
        userService.getUser(1).ifPresent(u -> u.join(userService.getGroups().stream().filter(e -> e.getName().equals(MdcAppService.Roles.REPORT_VIEWER.value())).findFirst().get()));
    }

    private String[] getPrivilegesMeterExpert() {
        return MdcAppPrivileges.getApplicationPrivileges().stream().filter(p -> !p.equals(com.elster.jupiter.yellowfin.security.Privileges.Constants.VIEW_REPORTS)).toArray(String[]::new);
    }

    private String[] getPrivilegesReportViewer() {
        return new String[]{
                com.elster.jupiter.yellowfin.security.Privileges.Constants.VIEW_REPORTS
        };
    }

    private String[] getPrivilegesMeterOperator(){
        return new String[] {
                //Assets inventory
                com.energyict.mdc.device.data.security.Privileges.Constants.IMPORT_INVENTORY_MANAGEMENT,
                com.energyict.mdc.device.data.security.Privileges.Constants.REVOKE_INVENTORY_MANAGEMENT,

                //Business processes
                com.elster.jupiter.bpm.security.Privileges.Constants.VIEW_BPM,

                //Communication
                com.energyict.mdc.engine.config.security.Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION,

                //Data collection KPI
                com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_DATA_COLLECTION_KPI,

                //Device communications
                com.energyict.mdc.device.data.security.Privileges.Constants.OPERATE_DEVICE_COMMUNICATION,

                //Device data
                com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_DATA,

                //Device groups
                com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_DEVICE_GROUP_DETAIL,

                //Device life cycle
                com.energyict.mdc.device.lifecycle.config.Privileges.Constants.VIEW_DEVICE_LIFE_CYCLE,

                //Device master data
                com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_MASTER_DATA,

                //Device types
                com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_TYPE,

                //Devices
                com.energyict.mdc.device.data.security.Privileges.Constants.ADD_DEVICE,
                com.energyict.mdc.device.data.security.Privileges.Constants.REMOVE_DEVICE,
                com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_DEVICE,
                com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_ATTRIBUTE,

                //Estimation
                com.elster.jupiter.estimation.security.Privileges.Constants.RUN_ESTIMATION_TASK,
                com.elster.jupiter.estimation.security.Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION,
                com.elster.jupiter.estimation.security.Privileges.Constants.VIEW_ESTIMATION_TASK,
                com.elster.jupiter.estimation.security.Privileges.Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE,

                //Export
                com.elster.jupiter.export.security.Privileges.Constants.RUN_DATA_EXPORT_TASK,
                com.elster.jupiter.export.security.Privileges.Constants.VIEW_DATA_EXPORT_TASK,

                //Firmware campaigns
                com.energyict.mdc.firmware.security.Privileges.Constants.VIEW_FIRMWARE_CAMPAIGN,

                //Import
                com.elster.jupiter.fileimport.security.Privileges.Constants.VIEW_IMPORT_SERVICES,

                //Issues
                com.elster.jupiter.issue.security.Privileges.Constants.ACTION_ISSUE,
                com.elster.jupiter.issue.security.Privileges.Constants.ASSIGN_ISSUE,
                com.elster.jupiter.issue.security.Privileges.Constants.CLOSE_ISSUE,
                com.elster.jupiter.issue.security.Privileges.Constants.COMMENT_ISSUE,
                com.elster.jupiter.issue.security.Privileges.Constants.VIEW_ISSUE,

                //Issues configuration
                com.elster.jupiter.issue.security.Privileges.Constants.VIEW_ASSIGNMENT_RULE,
                com.elster.jupiter.issue.security.Privileges.Constants.VIEW_CREATION_RULE,

                //Relative periods
                com.elster.jupiter.time.security.Privileges.Constants.VIEW_RELATIVE_PERIOD,

                //Usage points
                com.elster.jupiter.metering.security.Privileges.Constants.VIEW_ANY_USAGEPOINT,
                com.elster.jupiter.metering.security.Privileges.Constants.VIEW_OWN_USAGEPOINT,

                //User tasks
                com.elster.jupiter.bpm.security.Privileges.Constants.EXECUTE_TASK,
                com.elster.jupiter.bpm.security.Privileges.Constants.VIEW_TASK,
                com.elster.jupiter.bpm.security.Privileges.Constants.ASSIGN_TASK,

                //Validation
                com.elster.jupiter.validation.security.Privileges.Constants.VALIDATE_MANUAL,
                com.elster.jupiter.validation.security.Privileges.Constants.VIEW_VALIDATION_CONFIGURATION,
                com.elster.jupiter.validation.security.Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE,

                //Shared communication schedule
                com.energyict.mdc.scheduling.security.Privileges.Constants.VIEW_SHARED_COMMUNICATION_SCHEDULE,
        };
    }
}