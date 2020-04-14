/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Arrays;
import java.util.logging.Logger;

public class UpgraderV10_8 implements Upgrader {
    private static final long PARTITIONSIZE = 86400L * 30L * 1000L;
    private final DataModel dataModel;
    private final Clock clock;
    private final InstallerV10_8Impl installerV10_8;

    @Inject
    public UpgraderV10_8(DataModel dataModel, Clock clock, InstallerV10_8Impl installerV10_8) {
        this.dataModel = dataModel;
        this.clock = clock;
        this.installerV10_8 = installerV10_8;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 8));
        installerV10_8.install(dataModelUpgrader, Logger.getAnonymousLogger());
        //append partition for next month and enable auto increment partition interval
        if (dataModel.getSqlDialect().hasPartitioning()) {
            Arrays.asList("DDC_COMSESSION", "DDC_COMTASKEXECSESSION").forEach(tableName ->
                    execute(dataModel, "LOCK TABLE " + tableName + " PARTITION FOR (" + clock.instant().plusMillis(PARTITIONSIZE).toEpochMilli() + ") IN SHARE MODE",
                            "ALTER TABLE " + tableName + " SET INTERVAL (" + PARTITIONSIZE + ")")
            );
        }
        execute(dataModel,
                "alter table DDC_COMTASKEXECJOURNALENTRY drop column ID",
                "drop sequence DDC_COMTASKEXECJOURNALENTRYID",
                "alter table DDC_COMTASKEXECJOURNALENTRY drop column MOD_DATE"
        );
    }
}
