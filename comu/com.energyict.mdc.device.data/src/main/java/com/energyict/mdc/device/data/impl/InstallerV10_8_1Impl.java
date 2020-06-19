package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.logging.Logger;

public class InstallerV10_8_1Impl implements FullInstaller {
    private final DataModel dataModel;

    @Inject
    public InstallerV10_8_1Impl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        execute(dataModel, createConnectionTaskIndex());
    }

    private String createConnectionTaskIndex() {
        return "CREATE INDEX IX_CONNECTIONTASK_IDASC ON DDC_CONNECTIONTASK (COMPORTPOOL, NEXTEXECUTIONTIMESTAMP, mod(ID, 100), ID)";
    }
}
