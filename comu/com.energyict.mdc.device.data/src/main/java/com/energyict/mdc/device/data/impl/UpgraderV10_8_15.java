package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.util.logging.Logger;

public class UpgraderV10_8_15 implements Upgrader {

    private final InstallerV10_8_1Impl installerV10_8_1;
    private final static Logger logger = Logger.getLogger(UpgraderV10_8_15.class.getName());

    @Inject
    UpgraderV10_8_15(InstallerV10_8_1Impl installerV10_8_1) {
        this.installerV10_8_1 = installerV10_8_1;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        installerV10_8_1.createOrUpdateDashboardProcedures(logger);
    }
}
