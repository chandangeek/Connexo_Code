/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;

class UpgraderV10_7 implements Upgrader {

    private final UserService userService;
    private final PrivilegesProvider10_7 privilegesProvider10_7;

    @Inject
    UpgraderV10_7(UserService userService, PrivilegesProvider10_7 privilegesProvider10_7) {
        this.userService = userService;
        this.privilegesProvider10_7 = privilegesProvider10_7;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        userService.addModulePrivileges(privilegesProvider10_7);
    }

}
