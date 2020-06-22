/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.logging.Logger;

public class InstallerV10_8_1Impl implements FullInstaller {
    private final DataModel dataModel;
    private final OrmService ormService;

    @Inject
    public InstallerV10_8_1Impl(DataModel dataModel, OrmService ormService) {
        this.dataModel = dataModel;
        this.ormService = ormService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        if (!ormService.isTest()) {
            execute(dataModel, createConnectionTaskIndex());
        }
    }

    private String createConnectionTaskIndex() {
        return "CREATE INDEX IX_CONNECTIONTASK_IDASC ON DDC_CONNECTIONTASK (COMPORTPOOL, NEXTEXECUTIONTIMESTAMP, mod(ID, 100), ID)";
    }
}
