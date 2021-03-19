/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.metering.impl.upgraders;

import com.elster.jupiter.metering.impl.DefaultDeviceEventTypesInstaller;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.util.logging.Logger;

public class UpgraderV10_7_3 implements Upgrader {
    private final DataModel dataModel;
    private final Logger logger;
    private final DefaultDeviceEventTypesInstaller defaultDeviceEventTypesInstaller;

    @Inject
    public UpgraderV10_7_3(DataModel dataModel, Logger logger, DefaultDeviceEventTypesInstaller defaultDeviceEventTypesInstaller) {
        this.dataModel = dataModel;
        this.logger = logger;
        this.defaultDeviceEventTypesInstaller = defaultDeviceEventTypesInstaller;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 7, 3));
        defaultDeviceEventTypesInstaller.installIfNotPresent(logger);
    }
}
