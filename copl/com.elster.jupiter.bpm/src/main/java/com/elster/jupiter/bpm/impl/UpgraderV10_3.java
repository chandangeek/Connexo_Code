/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_3 implements Upgrader {

    private final DataModel dataModel;
    private final InstallerImpl installerV10_3;

    @Inject
    UpgraderV10_3(DataModel dataModel, InstallerImpl installerV10_3) {
        this.dataModel = dataModel;
        this.installerV10_3 = installerV10_3;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 3));
        installerV10_3.createExecutorRole();
        installerV10_3.createProcessExecutor();
    }
}
