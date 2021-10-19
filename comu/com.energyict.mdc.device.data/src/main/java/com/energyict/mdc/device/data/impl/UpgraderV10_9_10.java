package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

public class UpgraderV10_9_10 implements Upgrader {
    private final DataModel dataModel;

    @Inject
    UpgraderV10_9_10(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        execute(dataModel, "UPDATE " + TableSpecs.DDC_LOADPROFILE.name() + " SET LASTCONSECUTIVEREADING = LASTREADING WHERE LASTCONSECUTIVEREADING IS NULL");
    }
}
