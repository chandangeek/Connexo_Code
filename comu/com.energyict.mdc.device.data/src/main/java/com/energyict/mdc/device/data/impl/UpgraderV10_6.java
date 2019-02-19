/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;

public class UpgraderV10_6 implements Upgrader {

    private final DataModel dataModel;
    private final UserService userService;
    private final PrivilegesProviderV10_6 privilegesProviderV10_6;

    @Inject
    public UpgraderV10_6(DataModel dataModel, UserService userService, PrivilegesProviderV10_6 privilegesProviderV10_6) {
        this.dataModel = dataModel;
        this.userService = userService;
        this.privilegesProviderV10_6 = privilegesProviderV10_6;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 6));
        userService.addModulePrivileges(privilegesProviderV10_6);
    }

}