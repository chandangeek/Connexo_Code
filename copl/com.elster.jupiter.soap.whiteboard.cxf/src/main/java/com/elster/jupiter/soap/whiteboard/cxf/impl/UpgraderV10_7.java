package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;

public class UpgraderV10_7 implements Upgrader {
    public static final Version VERSION = Version.version(10, 7);
    private final DataModel dataModel;
    private final UserService userService;
    private final PrivilegesProviderV10_7 privilegesProviderV10_7;

    @Inject
    public UpgraderV10_7(DataModel dataModel,
                         UserService userService,
                         PrivilegesProviderV10_7 privilegesProviderV10_7) {
        this.dataModel = dataModel;
        this.userService = userService;
        this.privilegesProviderV10_7 = privilegesProviderV10_7;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, VERSION);
        userService.addModulePrivileges(privilegesProviderV10_7);
    }

}
