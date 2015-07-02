package com.elster.jupiter.yellowfin.app.impl;

import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;

import com.elster.jupiter.yellowfin.app.YfnAppService;
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
@Component(name = "com.elster.jupiter.yellowfin.app.install", service = {InstallService.class}, property = "name=" + YfnAppService.COMPONENTNAME , immediate = true)
public class YfnAppInstaller implements InstallService {

    String REPORT_DESIGNER_ROLE = "Report designer";
    String REPORT_DESIGNER_ROLE_DESCRIPTION = "Reports designer privilege";

    private final Logger logger = Logger.getLogger(YfnAppInstaller.class.getName());
    private volatile UserService userService;


    @Override
    public void install() {
        createDefaultRoles();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList(UserService.COMPONENTNAME, "YFN");
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    private void createDefaultRoles() {
        try {
            Group group = userService.createGroup(REPORT_DESIGNER_ROLE, REPORT_DESIGNER_ROLE_DESCRIPTION);
            userService.getPrivilege("privilege.design.reports").ifPresent(p -> group.grant("YFN", p.getName()));
            //TODO: workaround: attached Report designer to user admin !!! to remove this line when the user can be created/added to system
            userService.getUser(1).ifPresent(u -> u.join(group));
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }
    }

}
