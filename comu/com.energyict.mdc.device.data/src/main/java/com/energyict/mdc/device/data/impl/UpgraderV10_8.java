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

public class UpgraderV10_8 implements Upgrader {

    private final DataModel dataModel;
    private final InstallerV10_8Impl installerV10_8;


    @Inject
    public UpgraderV10_8(DataModel dataModel, InstallerV10_8Impl installerV10_8) {
        this.dataModel = dataModel;
        this.installerV10_8 = installerV10_8;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 8));
        installerV10_8.install(dataModelUpgrader, Logger.getAnonymousLogger());
    }
}