/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

public class UpgraderV10_7_2 implements Upgrader {
    private final DataModel dataModel;

    @Inject
    public UpgraderV10_7_2(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 7, 2));
        execute(dataModel,
                "drop sequence DDC_COMTASKEXECJOURNALENTRYID",
                "alter table DDC_COMTASKEXECJOURNALENTRY drop column ID",
                "alter table DDC_COMTASKEXECJOURNALENTRY drop column MOD_DATE"
        );
    }
}
