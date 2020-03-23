/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.metering.impl.upgraders;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.metering.impl.DefaultDeviceEventTypesInstaller;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Arrays;
import java.util.logging.Logger;

public class UpgraderV10_8 implements Upgrader {

    private static final long PARTITIONSIZE = 86400L * 30L * 1000L;
    private final DataModel dataModel;
    private final Clock clock;
    private final DefaultDeviceEventTypesInstaller defaultDeviceEventTypesInstaller;
    private final Logger logger;

    @Inject
    UpgraderV10_8( DataModel dataModel, DefaultDeviceEventTypesInstaller defaultDeviceEventTypesInstaller, Clock clock ) {
        this.dataModel = dataModel;
        this.clock = clock;
        this.defaultDeviceEventTypesInstaller = defaultDeviceEventTypesInstaller;
        this.logger = Logger.getLogger(this.getClass().getName());
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        defaultDeviceEventTypesInstaller.installIfNotPresent(logger);
        //append partition for next month and enable auto increment partition interval
        if (dataModel.getSqlDialect().hasPartitioning()) {
            Arrays.asList("MTR_ENDDEVICEEVENTRECORD", "MTR_READINGQUALITY").forEach(tableName ->
                    execute(dataModel, "LOCK TABLE " + tableName + " PARTITION FOR (" + clock.instant().plusMillis(PARTITIONSIZE).toEpochMilli() + ") IN SHARE MODE",
                            "ALTER TABLE " + tableName + " SET INTERVAL (" + PARTITIONSIZE + ")")
            );
        }
    }
}
