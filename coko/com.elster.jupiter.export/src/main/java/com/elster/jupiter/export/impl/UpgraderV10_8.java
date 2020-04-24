/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.export.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import com.google.inject.Inject;

public class UpgraderV10_8 implements Upgrader {
    private final DataModel dataModel;

    @Inject
    UpgraderV10_8(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 8));
        updateDataExportItemTable();
    }

    private void updateDataExportItemTable() {
        if (dataModel.doesColumnExist("DES_RTDATAEXPORTITEM", "LASTEXPORTED")) {
            execute(dataModel, "ALTER TABLE DES_RTDATAEXPORTITEM RENAME COLUMN LASTEXPORTED TO LASTEXPORTEDCHANGEDDATA");
        }
        if (dataModel.doesColumnExist("DES_RTDATAEXPORTITEM", "LASTEXPORTEDPERIODEND")) {
            execute(dataModel, "ALTER TABLE DES_RTDATAEXPORTITEM RENAME COLUMN LASTEXPORTEDPERIODEND TO LASTEXPORTEDNEWDATA");
        }
    }
}
