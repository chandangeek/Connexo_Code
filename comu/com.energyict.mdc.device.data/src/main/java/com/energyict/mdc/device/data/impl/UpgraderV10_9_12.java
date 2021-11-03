/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

public class UpgraderV10_9_12 implements Upgrader {
    private final DataModel dataModel;

    @Inject
    UpgraderV10_9_12(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        execute(dataModel, "UPDATE EVT_EVENTTYPE SET publish = 'Y' WHERE topic in ('com/energyict/mdc/device/data/manualcomtaskexecution/COMPLETED','com/energyict/mdc/device/data/scheduledcomtaskexecution/COMPLETED','com/energyict/mdc/device/data/manualcomtaskexecution/FAILED','com/energyict/mdc/device/data/scheduledcomtaskexecution/FAILED')");
    }
}
