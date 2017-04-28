/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.security.Privileges;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_3 implements Upgrader {
    private final UserService userService;
    private final DataModel dataModel;

    @Inject
    public UpgraderV10_3(DataModel dataModel, UserService userService) {
        this.userService = userService;
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 3));
        createNewPrivileges();
    }

    private void createNewPrivileges() {
        userService.saveResourceWithPrivileges(EstimationService.COMPONENTNAME,
                Privileges.RESOURCE_ESTIMATION_RULES.getKey(),
                Privileges.RESOURCE_ESTIMATION_RULES_DESCRIPTION.getKey(),
                new String[]{
                        Privileges.Constants.ESTIMATE_MANUAL,
                        Privileges.Constants.ESTIMATE_WITH_RULE,
                        Privileges.Constants.EDIT_WITH_ESTIMATOR
                });
    }
}
