/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

public class UpgraderV10_9_13 implements Upgrader {
    private final DataModel dataModel;

    @Inject
    UpgraderV10_9_13(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        execute(dataModel, "UPDATE EVT_EVENTTYPE SET publish = 'Y' WHERE topic in ('"+EventType.MANUAL_COMTASKEXECUTION_COMPLETED.topic()+"','"+EventType.SCHEDULED_COMTASKEXECUTION_COMPLETED.topic()+"','"+EventType.MANUAL_COMTASKEXECUTION_FAILED.topic()+"','"+EventType.SCHEDULED_COMTASKEXECUTION_FAILED.topic()+"')");
    }
}
