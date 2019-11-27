/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.data.impl.PrivilegesProviderV10_3;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.security.Privileges;

import javax.inject.Inject;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.orm.Version.version;

public class Upgrader_V10_4_7 implements Upgrader {

    private final UserService userService;

    @Inject
    public Upgrader_V10_4_7(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        userService.saveResourceWithPrivileges(EngineService.COMPONENTNAME,
                Privileges.RESOURCE_MOBILE_COMSERVER.getKey(), Privileges.RESOURCE_MOBILE_COMSERVER_DESCRIPTION.getKey(),
                new String[]{Privileges.Constants.OPERATE_MOBILE_COMSERVER});
    }
}
