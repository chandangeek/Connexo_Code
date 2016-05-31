package com.energyict.mdc.dashboard.rest;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.dashboard.rest.status.ComServerStatusSummaryResource;
import com.energyict.mdc.engine.config.security.Privileges;

import javax.inject.Inject;
import java.util.Optional;
import java.util.logging.Logger;

class DashboardApplicationInstaller implements FullInstaller {
    private final UserService userService;

    @Inject
    DashboardApplicationInstaller(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        Optional<User> comServerInternalAccessAccount = userService.findUser(ComServerStatusSummaryResource.COM_SERVER_INTERNAL_USER);
        if (!comServerInternalAccessAccount.isPresent()) {
            doTry(
                    "Set up Com Server internal access",
                    this::setupComServerInternalAccess,
                    logger
            );
        }
    }

    private User setupComServerInternalAccess() {
        Group comServerResourceGroup = userService.findGroup(ComServerStatusSummaryResource.COM_SERVER_INTERNAL_USER_GROUP).orElseGet(this::createComServerAccessGroup);
        User user = userService.createUser(ComServerStatusSummaryResource.COM_SERVER_INTERNAL_USER, "internal user");
        user.setPassword("comserver");
        user.join(comServerResourceGroup);
        user.update();
        return user;
    }

    private Group createComServerAccessGroup() {
        Group group = userService.createGroup(ComServerStatusSummaryResource.COM_SERVER_INTERNAL_USER_GROUP, "<INTERNAL> Regulates dashboard's inter-comserver communication");
        group.grant("MDC", Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION_INTERNAL);
        group.update();
        return group;
    }

}