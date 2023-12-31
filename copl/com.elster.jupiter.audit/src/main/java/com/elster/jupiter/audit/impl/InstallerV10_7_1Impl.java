/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.audit.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.logging.Logger;

public class InstallerV10_7_1Impl implements FullInstaller {
    private final DataModel dataModel;
    private final OrmService ormService;

    @Inject
    public InstallerV10_7_1Impl(DataModel dataModel, OrmService ormService) {
        this.dataModel = dataModel;
        this.ormService = ormService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        if(ormService.isTest()){
            return;
        }
        execute(dataModel, "ALTER INDEX ADT_PK_AUDIT_TAIL REBUILD REVERSE");
    }
}
