package com.energyict.mdc.app.impl;

import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.app.MdcAppService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
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
public class MdcAppInstaller implements InstallService {

    public static final String PRIVILEGE_VIEW_REPORTS = "privilege.view.reports";

    private final Logger logger = Logger.getLogger(MdcAppInstaller.class.getName());
    private volatile UserService userService;

    @Override
    public void install() {
        createDefaultRoles();
        assignPrivilegesToDefaultRoles();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList(UserService.COMPONENTNAME, "APS", "ISU", "DTC", "DDC", "MDC", "SCH", "VAL", "YFN");
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
        userService.grantGroupWithPrivilege(MdcAppService.Roles.METER_EXPERT.value(), privilegesWithoutReports);
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

}
