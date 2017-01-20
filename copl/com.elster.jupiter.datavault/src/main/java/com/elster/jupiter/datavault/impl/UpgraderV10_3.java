package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

import static com.elster.jupiter.orm.Version.version;

/**
 * Upgrades the database to version 10.3.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-20 (16:52)
 */
class UpgraderV10_3 implements Upgrader {
    private final DataModel dataModel;

    @Inject
    UpgraderV10_3(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 2));
    }

}