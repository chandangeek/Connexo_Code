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
        return Arrays.asList(UserService.COMPONENTNAME, "APS", "ISU", "DTC", "DDC", "MDC", "SCH", "VAL", "YFN", "DES", "FIM", "FWC");
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    private void createDefaultRoles() {
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
        return MdcAppPrivileges.getApplicationPrivileges().stream().filter(p -> !p.equals(com.elster.jupiter.yellowfin.security.Privileges.VIEW_REPORTS)).toArray(String[]::new);
    }

    private String[] getPrivilegesReportViewer() {
        return new String[]{
                com.elster.jupiter.yellowfin.security.Privileges.VIEW_REPORTS
        };
    }

    private String[] getPrivilegesMeterOperator(){
        return new String[] {
                com.elster.jupiter.export.security.Privileges.RUN_DATA_EXPORT_TASK,
                com.elster.jupiter.export.security.Privileges.VIEW_DATA_EXPORT_TASK,
                com.energyict.mdc.engine.config.security.Privileges.VIEW_COMMUNICATION_ADMINISTRATION,
                com.energyict.mdc.device.data.security.Privileges.VIEW_DEVICE,
                com.energyict.mdc.device.data.security.Privileges.OPERATE_DEVICE_COMMUNICATION,
                com.energyict.mdc.device.data.security.Privileges.ADMINISTRATE_DEVICE_DATA,
                com.energyict.mdc.device.config.security.Privileges.VIEW_DEVICE_TYPE,
                com.elster.jupiter.issue.security.Privileges.ACTION_ISSUE,
                com.elster.jupiter.issue.security.Privileges.ASSIGN_ISSUE,
                com.elster.jupiter.issue.security.Privileges.CLOSE_ISSUE,
                com.elster.jupiter.issue.security.Privileges.COMMENT_ISSUE,
                com.elster.jupiter.issue.security.Privileges.VIEW_ISSUE,
                com.energyict.mdc.device.config.security.Privileges.VIEW_MASTER_DATA,
                com.elster.jupiter.validation.security.Privileges.VIEW_VALIDATION_CONFIGURATION,
                com.energyict.mdc.device.lifecycle.config.Privileges.INITIATE_ACTION_1,
                com.energyict.mdc.device.lifecycle.config.Privileges.INITIATE_ACTION_2,
                com.energyict.mdc.device.lifecycle.config.Privileges.INITIATE_ACTION_3
        };
    }
    
}