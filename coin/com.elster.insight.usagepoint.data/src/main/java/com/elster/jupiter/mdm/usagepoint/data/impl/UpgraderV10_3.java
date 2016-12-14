package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;

import static com.elster.jupiter.orm.Version.version;

class UpgraderV10_3 implements Upgrader {
    static final Version VERSION = version(10, 3);
    private final DataModel dataModel;
    private final UserService userService;
    private final UsagePointGroupPrivilegesProvider usagePointGroupPrivilegesProvider;

    @Inject
    public UpgraderV10_3(DataModel dataModel, UserService userService,
                         UsagePointGroupPrivilegesProvider usagePointGroupPrivilegesProvider) {
        this.dataModel = dataModel;
        this.userService = userService;
        this.usagePointGroupPrivilegesProvider = usagePointGroupPrivilegesProvider;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, VERSION);
        userService.addModulePrivileges(usagePointGroupPrivilegesProvider);
    }
}
