package com.elster.jupiter.yellowfin.app.impl;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.yellowfin.YellowfinService;

import javax.inject.Inject;
import java.util.logging.Logger;

class YfnAppInstaller implements FullInstaller {

    String REPORT_DESIGNER_ROLE = "Report designer";
    String REPORT_DESIGNER_ROLE_DESCRIPTION = "Reports designer privilege";

    private final Logger logger = Logger.getLogger(YfnAppInstaller.class.getName());
    private final UserService userService;
    private final YellowfinService yellowfinService;

    @Inject
    YfnAppInstaller(UserService userService, YellowfinService yellowfinService) {
        this.userService = userService;
        this.yellowfinService = yellowfinService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        doTry(
                "Create default roles for YFN",
                this::createDefaultRoles,
                logger
        );
    }

    private void createDefaultRoles() {
        Group group = userService.createGroup(REPORT_DESIGNER_ROLE, REPORT_DESIGNER_ROLE_DESCRIPTION);
        userService.grantGroupWithPrivilege(group.getName(), "YFN", new String[]{"privilege.design.reports"});
        //TODO: workaround: attached Report designer to user admin !!! to remove this line when the user can be created/added to system
        userService.getUser(1).ifPresent(u -> u.join(group));
    }

}
