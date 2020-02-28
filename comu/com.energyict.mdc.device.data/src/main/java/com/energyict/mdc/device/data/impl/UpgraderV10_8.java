package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Arrays;

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
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 4, 9));
        //append partition for next month and enable auto increment partition interval
        Arrays.asList("DDC_COMSESSION", "DDC_COMTASKEXECSESSION", "DDC_COMTASKEXECJOURNALENTRY", "DDC_COMSESSIONJOURNALENTRY").forEach(tableName ->
                execute(dataModel, "LOCK TABLE " + tableName + " PARTITION FOR(" + clock.instant().plusMillis(PARTITIONSIZE) + ") IN SHARE MODE",
                        "ALTER TABLE " + tableName + " SET INTERVAL (" + PARTITIONSIZE + ")")
        );
    }
}
