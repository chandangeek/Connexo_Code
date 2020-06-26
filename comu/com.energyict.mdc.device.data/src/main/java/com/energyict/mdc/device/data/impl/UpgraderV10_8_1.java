/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.util.logging.Logger;

public class UpgraderV10_8_1 implements Upgrader {
    private final DataModel dataModel;
    private final InstallerV10_8_1Impl installerV10_8_1;

    @Inject
    public UpgraderV10_8_1(DataModel dataModel, InstallerV10_8_1Impl installerV10_8_1) {
        this.dataModel = dataModel;
        this.installerV10_8_1 = installerV10_8_1;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 8, 1));
        /*execute(dataModel,
                "merge into " + TableSpecs.DDC_COMTASKEXECJOURNALENTRY.name() + " ctej" +
                        " using (" +
                        " select ID, row_number() over (partition by COMTASKEXECSESSION order by ID) position" +
                        " from " + TableSpecs.DDC_COMTASKEXECJOURNALENTRY.name() + ") calc" +
                        " on (ctej.ID = calc.ID)" +
                        " when matched then update set ctej.POSITION = calc.position",
                "alter table DDC_COMTASKEXECJOURNALENTRY add constraint PK_DDC_COMTASKJOURNALENTRY primary key (COMTASKEXECSESSION, POSITION)",
                "alter table DDC_COMTASKEXECJOURNALENTRY drop column ID",
                "drop sequence DDC_COMTASKEXECJOURNALENTRYID",
                "alter table DDC_COMTASKEXECJOURNALENTRY drop column MOD_DATE"
        );
        execute(dataModel, "CREATE INDEX IX_CONNECTIONTASK_IDASC ON DDC_CONNECTIONTASK (COMPORTPOOL, NEXTEXECUTIONTIMESTAMP, mod(ID, 100), ID)");*/
        installerV10_8_1.prepareDashboard(Logger.getAnonymousLogger());
    }
}
