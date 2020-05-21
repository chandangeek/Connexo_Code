/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

public class UpgraderV10_8 implements Upgrader {
    private final DataModel dataModel;

    @Inject
    public UpgraderV10_8(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 8));
        execute(dataModel, "alter table DLD_DEVICE_LIFE_CYCLE drop " +
                "(MAXPASTEFFTIMESHIFTUNIT, MAXPASTEFFTIMESHIFTVALUE, MAXFUTUREEFFTIMESHIFTUNIT, MAXFUTUREEFFTIMESHIFTVALUE)",
                "alter table DLD_DEVICE_LIFE_CYCLEJRNL drop " +
                        "(MAXPASTEFFTIMESHIFTUNIT, MAXPASTEFFTIMESHIFTVALUE, MAXFUTUREEFFTIMESHIFTUNIT, MAXFUTUREEFFTIMESHIFTVALUE)");
    }
}
