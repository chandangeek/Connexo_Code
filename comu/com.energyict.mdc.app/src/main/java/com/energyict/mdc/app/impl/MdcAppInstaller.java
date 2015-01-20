package com.energyict.mdc.app.impl;

import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.app.MdcAppService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 19/01/2015
 * Time: 17:48
 */
@Component(name = "com.energyict.mdc.app.install", service = {InstallService.class}, property = "name=" + MdcAppService.COMPONENTNAME, immediate = true)
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
        return Arrays.asList(UserService.COMPONENTNAME, "ISU", "DTC", "DDC", "MDC", "SCH", "VAL");
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    private void createDefaultRoles() {
        try {
            userService.createGroup(MdcAppService.Roles.METER_EXPERT.value(), MdcAppService.Roles.METER_EXPERT.description());
            userService.createGroup(MdcAppService.Roles.METER_OPERATOR.value(), MdcAppService.Roles.METER_OPERATOR.description());
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }
    }

    private void assignPrivilegesToDefaultRoles() {
        List<String> availablePrivileges = getAvailablePrivileges();
        userService.grantGroupWithPrivilege(MdcAppService.Roles.METER_EXPERT.value(), availablePrivileges.toArray(new String[availablePrivileges.size()]));
        //TODO: workaround: attached Meter expert to user admin !!! to remove this line when the user can be created/added to system
        userService.getUser(1).ifPresent(u -> u.join(userService.getGroups().stream().filter(e -> e.getName().equals(MdcAppService.Roles.METER_EXPERT.value())).findFirst().get()));
    }

    private List<String> getAvailablePrivileges() {
        List<String> privileges = new ArrayList<String>();
        userService.getResources(MdcAppService.APPLICATION_KEY).forEach(e -> e.getPrivileges().forEach(p -> privileges.add(p.getName())));
        return privileges;
    }

}
