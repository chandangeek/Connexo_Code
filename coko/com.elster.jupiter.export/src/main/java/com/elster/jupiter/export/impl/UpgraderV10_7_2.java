/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.export.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import com.google.inject.Inject;

public class UpgraderV10_7_2 implements Upgrader {
    private final DataModel dataModel;

    @Inject
    UpgraderV10_7_2(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 7, 2));
        execute(dataModel,
                "update DES_RTDATAEXPORTITEM set READING_INTERVAL = '1 hours' where READINGINTERVALCOUNT is not null and READINGINTERVALCOUNT != 0",
                "alter table DES_RTDATAEXPORTITEM drop column READINGINTERVALCOUNT",
                "alter table DES_RTDATAEXPORTITEM drop column READINGINTERVALUNIT");
    }
}
