package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;

public class UpgraderV10_4_1 implements Upgrader {

    private final DataModel dataModel;
    private final UserService userService;
    private final PrivilegesProviderV10_4_1 privilegesProviderV10_4_1;

    @Inject
    public UpgraderV10_4_1(DataModel dataModel, UserService userService, PrivilegesProviderV10_4_1 privilegesProviderV10_4_1) {
        this.dataModel = dataModel;
        this.userService = userService;
        this.privilegesProviderV10_4_1 = privilegesProviderV10_4_1;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10,4,1));
        userService.addModulePrivileges(privilegesProviderV10_4_1);
    }
}
