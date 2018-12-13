/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.impl.webservicecall.DataExportServiceCallTypeImpl;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

public class UpgraderV10_5_1 implements Upgrader {
    public static final Version VERSION = Version.version(10, 5, 1);

    private final DataModel dataModel;
    private final DataExportServiceCallTypeImpl dataExportServiceCallType;

    @Inject
    public UpgraderV10_5_1(DataModel dataModel, DataExportServiceCallTypeImpl dataExportServiceCallType) {
        this.dataModel = dataModel;
        this.dataExportServiceCallType = dataExportServiceCallType;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, VERSION);
        dataExportServiceCallType.findOrCreate();
    }
}
