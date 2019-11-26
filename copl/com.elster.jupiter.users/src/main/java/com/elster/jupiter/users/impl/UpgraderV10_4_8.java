/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.stream.Stream;

public class UpgraderV10_4_8 implements Upgrader {
    private final DataModel dataModel;
    private final UserService userService;

    @Inject
    public UpgraderV10_4_8(DataModel dataModel, UserService userService) {
        this.dataModel = dataModel;
        this.userService = userService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        updateMeterOperatorPreveleges();
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 4, 8));
    }

    private void updateMeterOperatorPreveleges() {
        Stream<String> privileges = Stream.of("view.custom.properties.level1", "view.custom.properties.level2",
                "view.custom.properties.level3", "view.custom.properties.level4");
        userService.grantGroupWithPrivilege("Meter operator", "MDC", privileges.toArray(String[]::new));
    }
}
