package com.energyict.mdc.dashboard.rest;

import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.dashboard.rest.status.ComServerStatusSummaryResource;
import com.energyict.mdc.engine.config.security.Privileges;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-18 (10:32)
 */
@Component(name = "com.energyict.mdc.dashboard.rest.installer", service = { InstallService.class}, immediate = true, property = {"name=DSI"})
public class DashboardApplicationInstaller implements InstallService {
    private volatile UserService userService;

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("USR", "MDC");
    }

    @Override
    public void install() {
        Optional<User> comServerInternalAccessAccount = userService.findUser(ComServerStatusSummaryResource.COM_SERVER_INTERNAL_USER);
        if (!comServerInternalAccessAccount.isPresent()) {
            setupComServerInternalAccess();
        }
    }

    private User setupComServerInternalAccess() {
        Group comServerResourceGroup = userService.findGroup(ComServerStatusSummaryResource.COM_SERVER_INTERNAL_USER_GROUP).orElseGet(this::createComServerAccessGroup);
        User user = userService.createUser(ComServerStatusSummaryResource.COM_SERVER_INTERNAL_USER, "internal user");
        user.setPassword("comserver");
        user.join(comServerResourceGroup);
        user.save();
        return user;
    }

    private Group createComServerAccessGroup() {
        Group group = userService.createGroup(ComServerStatusSummaryResource.COM_SERVER_INTERNAL_USER_GROUP, "<INTERNAL> Regulates dashboard's inter-comserver communication");
        group.grant("MDC", Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION_INTERNAL);
        group.save();
        return group;
    }

}