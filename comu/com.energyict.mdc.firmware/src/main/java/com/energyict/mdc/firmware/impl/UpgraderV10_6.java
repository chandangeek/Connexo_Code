/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_6 implements Upgrader {
    private final DataModel dataModel;

    @Inject
    UpgraderV10_6(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 6));
        execute(dataModel,
                "merge into " + TableSpecs.FWC_FIRMWAREVERSION.name() + " fwv" +
                        " using (" +
                        " select ID, row_number() over (partition by " + FirmwareVersionImpl.Fields.DEVICETYPE.name() + " order by CREATETIME) rank" +
                        " from " + TableSpecs.FWC_FIRMWAREVERSION.name() + ") calc" +
                        " on (fwv.ID = calc.ID)" +
                        " when matched then update set fwv." + FirmwareVersionImpl.Fields.RANK.name() + " = calc.rank");
    }
}
