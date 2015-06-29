package com.energyict.mdc.app.impl;

import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.app.MdcAppService;
import com.energyict.mdc.device.lifecycle.config.Privileges;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Copyrights EnergyICT
 * Date: 19/01/2015
 * Time: 17:48
 */
@Component(name = "com.energyict.mdc.app.install", service = {InstallService.class}, property = "name=" + MdcAppService.COMPONENTNAME, immediate = true)
@SuppressWarnings("unused")
public class MdcAppInstaller implements InstallService {

    public static final String PRIVILEGE_VIEW_REPORTS = "privilege.view.reports";

    public static final String PRIVILEGE_RUN_EXPORT = "privilege.run.dataExportTask";
    public static final String PRIVILEGE_VIEW_EXPORT = "privilege.view.dataExportTask";

    public static final String PRIVILEGE_VIEW_COMMUNICATION = "privilege.view.communicationAdministration";
    public static final String PRIVILEGE_VIEW_DEVICE = "privilege.view.device";
    public static final String PRIVILEGE_OPERATE_DEVICECOMMUNICATION = "privilege.operate.deviceCommunication";
    public static final String PRIVILEGE_ADMINISTRATE_DEVICEDATA = "privilege.administrate.deviceData";
    public static final String PRIVILEGE_VIEW_DEVICETYPE = "privilege.view.deviceType";
    public static final String PRIVILEGE_ACTION_ISSUE_ACTION = "privilege.action.issue";
    public static final String PRIVILEGE_ASSIGN_ISSUE = "privilege.assign.issue";
    public static final String PRIVILEGE_CLOSE_ISSUE = "privilege.close.issue";
    public static final String PRIVILEGE_COMMENT_ISSUE = "privilege.comment.issue";
    public static final String PRIVILEGE_VIEW_ISSUE = "privilege.view.issue";
    public static final String PRIVILEGE_VIEW_MASTER_DATA = "privilege.view.masterData";
    public static final String PRIVILEGE_VIEW_VALIDATION = "privilege.view.validationConfiguration";
    public static final String VIEW_MDC_IMPORT_SERVICES = "privilege.view.mdc.importServices";
    public static final String VIEW_FWC_CAMPAIGNS = "privilege.view.firmware.campaign";


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
        List<Privilege> availablePrivileges =  getApplicationPrivileges();
        String[] privilegesWithoutReports = availablePrivileges.stream().map(HasName::getName).filter(p -> !p.equals(PRIVILEGE_VIEW_REPORTS)).toArray(String[]::new);
        String[] privilegesMeterOperator = getPrivilegesMeterOperator();

        userService.grantGroupWithPrivilege(MdcAppService.Roles.METER_OPERATOR.value(), privilegesMeterOperator);
        userService.grantGroupWithPrivilege(MdcAppService.Roles.METER_EXPERT.value(), privilegesWithoutReports);
        userService.grantGroupWithPrivilege(MdcAppService.Roles.METER_EXPERT.value(), this.deviceLifeCycleAdministrationPrivileges());
        userService.grantGroupWithPrivilege(MdcAppService.Roles.METER_EXPERT.value(), this.allDeviceLifeCycleActionPrivileges());
        userService.grantGroupWithPrivilege(MdcAppService.Roles.METER_OPERATOR.value(), this.meterOperatorDeviceLifeCycleActionPrivileges());
        userService.grantGroupWithPrivilege(UserService.BATCH_EXECUTOR_ROLE, privilegesWithoutReports);
        userService.grantGroupWithPrivilege(MdcAppService.Roles.REPORT_VIEWER.value(), availablePrivileges.stream().map(HasName::getName).filter(p -> p.equals(PRIVILEGE_VIEW_REPORTS)).toArray(String[]::new));
        //TODO: workaround: attached Meter expert to user admin !!! to remove this line when the user can be created/added to system
        userService.getUser(1).ifPresent(u -> u.join(userService.getGroups().stream().filter(e -> e.getName().equals(MdcAppService.Roles.METER_EXPERT.value())).findFirst().get()));
        //TODO: workaround: attached Report viewer to user admin !!! to remove this line when the user can be created/added to system
        userService.getUser(1).ifPresent(u -> u.join(userService.getGroups().stream().filter(e -> e.getName().equals(MdcAppService.Roles.REPORT_VIEWER.value())).findFirst().get()));
    }

    private List<Privilege> getApplicationPrivileges() {
        return userService.getResources(MdcAppService.APPLICATION_KEY).stream().flatMap(resource -> resource.getPrivileges().stream()).collect(Collectors.toList());
    }

    private String[] getPrivilegesMeterOperator(){
        return new String[] {
                PRIVILEGE_RUN_EXPORT, PRIVILEGE_VIEW_EXPORT,
                PRIVILEGE_VIEW_COMMUNICATION, PRIVILEGE_VIEW_DEVICE,
                PRIVILEGE_OPERATE_DEVICECOMMUNICATION, PRIVILEGE_ADMINISTRATE_DEVICEDATA, PRIVILEGE_VIEW_DEVICETYPE,
                PRIVILEGE_ACTION_ISSUE_ACTION, PRIVILEGE_ASSIGN_ISSUE, PRIVILEGE_CLOSE_ISSUE, PRIVILEGE_COMMENT_ISSUE, PRIVILEGE_VIEW_ISSUE,
                PRIVILEGE_VIEW_MASTER_DATA, PRIVILEGE_VIEW_VALIDATION, VIEW_MDC_IMPORT_SERVICES, VIEW_FWC_CAMPAIGNS
        };
    }

    private String[] deviceLifeCycleAdministrationPrivileges(){
        return new String[] {Privileges.VIEW_DEVICE_LIFE_CYCLE, Privileges.CONFIGURE_DEVICE_LIFE_CYCLE};
    }

    private String[] allDeviceLifeCycleActionPrivileges() {
        return new String[]{
                Privileges.INITIATE_ACTION_1,
                Privileges.INITIATE_ACTION_2,
                Privileges.INITIATE_ACTION_3,
                Privileges.INITIATE_ACTION_4};
    }

    private String[] meterOperatorDeviceLifeCycleActionPrivileges() {
        return new String[]{
                Privileges.INITIATE_ACTION_1,
                Privileges.INITIATE_ACTION_2,
                Privileges.INITIATE_ACTION_3};
    }

}