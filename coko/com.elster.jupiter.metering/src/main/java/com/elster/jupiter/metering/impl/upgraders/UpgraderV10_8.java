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
        execute(dataModel, "begin \n " +
                "  FOR CHANNEL IN (SELECT \n " +
                "      MTR_CHANNEL.ID as CHANNELID,\n " +
                "      MTR_CHANNEL.MAINDERIVATIONRULE as MAINDERIVATIONRULE,\n " +
                "      MTR_CHANNEL.TIMESERIESID as TIMESERIESID,\n " +
                "      IDS_TIMESERIES.RECORDSPECID as RECORDSPECID\n " +
                "    FROM MTR_CHANNEL \n " +
                "       INNER JOIN IDS_TIMESERIES ON IDS_TIMESERIES.ID = MTR_CHANNEL.TIMESERIESID\n " +
                "       INNER JOIN MTR_READINGTYPEINCHANNEL ON MTR_READINGTYPEINCHANNEL.CHANNNELID = MTR_CHANNEL.ID)\n " +
                "    loop\n " +
                "       UPDATE MTR_CHANNEL SET MTR_CHANNEL.MAINDERIVATIONRULE = 1 WHERE  MTR_CHANNEL.ID = CHANNEL.CHANNELID;\n " +
                "       COMMIT;\n " +
                "       UPDATE IDS_TIMESERIES SET IDS_TIMESERIES.RECORDSPECID = 7 WHERE  IDS_TIMESERIES.ID = CHANNEL.TIMESERIESID;\n " +
                "       COMMIT;\n " +
                "    end loop;\n " +
                "end;");

    }
}
