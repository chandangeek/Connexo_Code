/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.time.Clock;

public class UpgraderV10_8 implements Upgrader {
    private static final long PARTITIONSIZE = 86400L * 30L * 1000L;
    private final DataModel dataModel;
    private final Clock clock;

    @Inject
    UpgraderV10_8(DataModel dataModel, Clock clock) {
        this.dataModel = dataModel;
        this.clock = clock;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        //append partition for next month and enable auto increment partition interval
        if (dataModel.getSqlDialect().hasPartitioning()) {
            execute(dataModel, "LOCK TABLE TSK_TASK_OCCURRENCE PARTITION FOR (" + clock.instant().plusMillis(PARTITIONSIZE).toEpochMilli() + ") IN SHARE MODE",
                    "ALTER TABLE TSK_TASK_OCCURRENCE SET INTERVAL (" + PARTITIONSIZE + ")");
        }
    }
}
