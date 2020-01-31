package com.elster.jupiter.upgrade;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;

import javax.inject.Inject;

public class V10_4_9SimpleUpgrader implements Upgrader{

    public static final Version VERSION = Version.version(10, 4, 9);
    private final DataModel dataModel;

    @Inject
    public V10_4_9SimpleUpgrader(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, VERSION);
    }
}
