/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.yellowfin.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;

import static com.elster.jupiter.orm.Version.version;

class UpgraderV10_3 implements Upgrader {

    private final DataModel dataModel;
    private final UserService userService;
    private final Installer installerV10_3;

    @Inject
    public UpgraderV10_3(DataModel dataModel, UserService userService, Installer installerV10_3) {
        this.dataModel = dataModel;
        this.userService = userService;
        this.installerV10_3 = installerV10_3;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 3));
        userService.addModulePrivileges(installerV10_3);
        installerV10_3.createAdministratorRole();
    }
}
