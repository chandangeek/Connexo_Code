package com.elster.jupiter.kore.api.impl;

import com.elster.jupiter.kore.api.security.Privileges;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.rest.api.util.Roles;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.Optional;

class UpgraderV10_3 implements Upgrader {

    private final UserService userService;

    @Inject
    public UpgraderV10_3(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        createDeveloperRoleWithPrivileges();
    }

    private void createDeveloperRoleWithPrivileges() {
        userService.addModulePrivileges(new PublicRestApplicationPrivilegesProvider(userService));
        createDeveloperRole();
        assignPrivilegesToDeveloperRole();
    }

    private void createDeveloperRole() {
        Optional<Group> developer = userService.findGroup(Roles.DEVELOPER.value());
        if (!developer.isPresent()) {
            userService.createGroup(Roles.DEVELOPER.value(), Roles.DEVELOPER.description());
        }
    }

    private void assignPrivilegesToDeveloperRole() {
        userService.grantGroupWithPrivilege(Roles.DEVELOPER.value(), PublicRestAppServiceImpl.APP_KEY, new String[]{Privileges.Constants.PUBLIC_REST_API});
    }
}
