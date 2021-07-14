package com.elster.jupiter.metering.impl.upgraders;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import javax.inject.Inject;

public class UpgraderV10_4_22 implements Upgrader {

    private final DataModel dataModel;

    @Inject
    UpgraderV10_4_22(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 4, 19));
    }
}
