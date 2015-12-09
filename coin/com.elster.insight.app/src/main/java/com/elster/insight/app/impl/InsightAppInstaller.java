package com.elster.insight.app.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.UserService;
import com.elster.insight.app.InsightAppService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@Component(name = "com.elster.insight.app.install", service = {InstallService.class}, property = "name=" + InsightAppService.COMPONENTNAME, immediate = true)
@SuppressWarnings("unused")
public class InsightAppInstaller implements InstallService {

    private final Logger logger = Logger.getLogger(InsightAppInstaller.class.getName());
    private volatile UserService userService;

    @Override
    public void install() {
        createDefaultRoles();
        assignPrivilegesToDefaultRoles();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList(UserService.COMPONENTNAME, "MTR", "UPC", "VAL", "CPS");
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void createDefaultRoles() {
        try {
            userService.createGroup(InsightAppService.Roles.METER_EXPERT.value(), InsightAppService.Roles.METER_EXPERT.description());
            userService.createGroup(InsightAppService.Roles.METER_OPERATOR.value(), InsightAppService.Roles.METER_OPERATOR.description());
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }
    }

    private void assignPrivilegesToDefaultRoles() {
    	String adminAppKey = com.elster.jupiter.system.app.SysAppService.APPLICATION_KEY;
        userService.grantGroupWithPrivilege(UserService.DEFAULT_ADMIN_ROLE, InsightAppService.APPLICATION_KEY, getApplicationAllPrivileges().stream().toArray(String[]::new));
        userService.grantGroupWithPrivilege(UserService.BATCH_EXECUTOR_ROLE, InsightAppService.APPLICATION_KEY, getApplicationAllPrivileges().stream().toArray(String[]::new));
        
        userService.grantGroupWithPrivilege(InsightAppService.Roles.METER_EXPERT.value(), InsightAppService.APPLICATION_KEY, getApplicationAllPrivileges().stream().toArray(String[]::new));
        userService.grantGroupWithPrivilege(InsightAppService.Roles.METER_EXPERT.value(), adminAppKey, getAdminApplicationAllPrivileges().stream().toArray(String[]::new));

        userService.grantGroupWithPrivilege(InsightAppService.Roles.METER_OPERATOR.value(), InsightAppService.APPLICATION_KEY, getApplicationViewPrivileges().stream().toArray(String[]::new));
        userService.grantGroupWithPrivilege(InsightAppService.Roles.METER_OPERATOR.value(), adminAppKey, getAdminApplicationViewPrivileges().stream().toArray(String[]::new));


        //TODO: workaround: attached Meter expert to user admin !!! to remove this line when the user can be created/added to system
//        userService.getUser(1).ifPresent(u -> u.join(userService.getGroups().stream().filter(e -> e.getName().equals(InsightAppService.Roles.METER_EXPERT.value())).findFirst().get()));
        //TODO: workaround: attached Report viewer to user admin !!! to remove this line when the user can be created/added to system
//        userService.getUser(1).ifPresent(u -> u.join(userService.getGroups().stream().filter(e -> e.getName().equals(InsightAppService.Roles.REPORT_VIEWER.value())).findFirst().get())); 
    }

    private List<String> getApplicationAllPrivileges() {
        return InsightAppPrivileges.getApplicationAllPrivileges();
    }

    private List<String> getApplicationViewPrivileges() {
        return InsightAppPrivileges.getApplicationViewPrivileges();
    }
    
    private List<String> getAdminApplicationAllPrivileges() {
        return InsightAppPrivileges.getAdminApplicationAllPrivileges();
    }
    
    private List<String> getAdminApplicationViewPrivileges() {
        return InsightAppPrivileges.getAdminApplicationViewPrivileges();
    }

}
