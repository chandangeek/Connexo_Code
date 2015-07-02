package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.BpmAppService;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 27/01/2015
 * Time: 14:35
 */
@Component(name = "com.elster.jupiter.bpm.app.install", service = {InstallService.class}, property = "name=" + BpmAppService.COMPONENTNAME, immediate = true)
public class BpmAppInstaller implements InstallService{

    String BPM_DESIGNER_ROLE = "Business process designer";
    String BPM_DESIGNER_ROLE_DESCRIPTION = "Business process designer privilege";

    private final Logger logger = Logger.getLogger(BpmAppInstaller.class.getName());
    private volatile UserService userService;

    public BpmAppInstaller() {
    }

    @Override
    public void install() {
        createDefaultRoles();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList(UserService.COMPONENTNAME, "BPM");
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    private void createDefaultRoles() {
        try {
            Group group = userService.createGroup(BPM_DESIGNER_ROLE, BPM_DESIGNER_ROLE_DESCRIPTION);
            userService.getPrivilege("privilege.design.bpm").ifPresent(p -> group.grant(BpmService.COMPONENTNAME, p.getName()));
            //TODO: workaround: attached Bpm designer to user admin !!! to remove this line when the user can be created/added to system
            userService.getUser(1).ifPresent(u -> u.join(group));
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }
    }

}