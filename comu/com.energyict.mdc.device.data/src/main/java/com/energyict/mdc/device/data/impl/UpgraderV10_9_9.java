package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

public class UpgraderV10_9_9 implements Upgrader {
    private final DataModel dataModel;

    @Inject
    UpgraderV10_9_9(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        boolean columnExistedBefore =  dataModel.doesColumnExist(TableSpecs.DDC_LOADPROFILE.name(), "LASTCONSECUTIVEREADING");
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 9, 9));
        if (!columnExistedBefore) {
            execute(dataModel, "UPDATE " + TableSpecs.DDC_LOADPROFILE.name() + " SET LASTCONSECUTIVEREADING = LASTREADING");
        }
    }
}
