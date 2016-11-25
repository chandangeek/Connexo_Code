package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;

class UpgraderV10_3 implements Upgrader {

    private final UserService userService;
    private final UsagePointGroupPrivilegesProvider usagePointGroupPrivilegesProvider;

    @Inject
    public UpgraderV10_3(UserService userService, UsagePointGroupPrivilegesProvider usagePointGroupPrivilegesProvider) {
        this.userService = userService;
        this.usagePointGroupPrivilegesProvider = usagePointGroupPrivilegesProvider;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        userService.addModulePrivileges(usagePointGroupPrivilegesProvider);
    }
}
