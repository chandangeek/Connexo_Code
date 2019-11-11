/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.metering.impl.upgraders;

import com.elster.jupiter.metering.impl.InstallerV10_7_1Impl;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.util.logging.Logger;

public class UpgraderV10_7_1 implements Upgrader {
    private final DataModel dataModel;
    private final Logger logger;
    private final InstallerV10_7_1Impl installerV10_7_1;

    @Inject
    public UpgraderV10_7_1(DataModel dataModel, Logger logger, InstallerV10_7_1Impl installerV10_7_1) {
        this.dataModel = dataModel;
        this.logger = logger;
        this.installerV10_7_1 = installerV10_7_1;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 7, 1));
        installerV10_7_1.install(dataModelUpgrader, logger);
    }
}
