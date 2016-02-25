package com.elster.insight.app.impl;

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
        userService.grantGroupWithPrivilege(UserService.BATCH_EXECUTOR_ROLE, InsightAppService.APPLICATION_KEY, getPrivilegesMeterExpert());
        userService.grantGroupWithPrivilege(InsightAppService.Roles.METER_EXPERT.value(), InsightAppService.APPLICATION_KEY, getPrivilegesMeterExpert());
        userService.grantGroupWithPrivilege(InsightAppService.Roles.METER_OPERATOR.value(), InsightAppService.APPLICATION_KEY, getPrivilegesMeterOperator());

        //TODO: workaround: attached Meter expert to user admin !!! to remove this line when the user can be created/added to system
        userService.getUser(1).ifPresent(u -> u.join(userService.getGroups().stream().filter(e -> e.getName().equals(InsightAppService.Roles.METER_EXPERT.value())).findFirst().get()));
    }

    private String[] getPrivilegesMeterExpert() {
        return InsightAppPrivileges.getApplicationAllPrivileges().stream().toArray(String[]::new);
    }

    private String[] getPrivilegesMeterOperator() {
        return new String[]{
                //usage point
                com.elster.jupiter.metering.security.Privileges.Constants.VIEW_ANY_USAGEPOINT,
                com.elster.jupiter.metering.security.Privileges.Constants.VIEW_OWN_USAGEPOINT,

                //validation
                com.elster.jupiter.validation.security.Privileges.Constants.VIEW_VALIDATION_CONFIGURATION,

                //metrology configuration
                com.elster.jupiter.metering.security.Privileges.Constants.VIEW_METROLOGY_CONFIGURATION,

                //service category
                com.elster.jupiter.metering.security.Privileges.Constants.VIEW_SERVICECATEGORY
        };
    }
}
