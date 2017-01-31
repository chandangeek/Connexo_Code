/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.logging.Logger;

class DashboardApplicationInstaller implements FullInstaller {

    @Inject
    DashboardApplicationInstaller() {
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
    }


}