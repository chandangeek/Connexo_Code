/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.logging.Logger;

public class InstallerV10_7_1Impl implements FullInstaller {
    private final DataModel dataModel;

    @Inject
    public InstallerV10_7_1Impl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        execute(dataModel, "ALTER INDEX PK_DDC_COMTASKEXECSESSION REBUILD REVERSE");
    }
}
