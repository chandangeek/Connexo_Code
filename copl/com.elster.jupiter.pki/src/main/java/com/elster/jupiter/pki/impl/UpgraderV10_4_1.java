/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.pki.impl.importers.csr.CSRImporterQueueInstaller;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

public class UpgraderV10_4_1 implements Upgrader {
    private final DataModel dataModel;
    private final CSRImporterQueueInstaller csrImporterQueueInstaller;

    @Inject
    public UpgraderV10_4_1(DataModel dataModel, CSRImporterQueueInstaller csrImporterQueueInstaller) {
        this.dataModel = dataModel;
        this.csrImporterQueueInstaller = csrImporterQueueInstaller;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 4, 1));
        csrImporterQueueInstaller.installIfNotPresent();
    }
}
