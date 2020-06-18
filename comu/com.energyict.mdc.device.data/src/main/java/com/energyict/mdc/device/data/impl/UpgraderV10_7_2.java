/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.util.logging.Logger;

public class UpgraderV10_7_2 implements Upgrader {

    private final DataModel dataModel;
    private final InstallerV10_7_2Impl installerV10_7_2;

    @Inject
    public UpgraderV10_7_2(DataModel dataModel, InstallerV10_7_2Impl installerV10_7_2) {
        this.dataModel = dataModel;
        this.installerV10_7_2 = installerV10_7_2;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 7, 2));
        installerV10_7_2.install(dataModelUpgrader, Logger.getAnonymousLogger());
    }
}
