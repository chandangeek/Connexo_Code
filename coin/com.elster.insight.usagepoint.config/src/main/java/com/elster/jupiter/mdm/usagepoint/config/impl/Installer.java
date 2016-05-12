package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;

class Installer implements FullInstaller {

    private final DataModel dataModel;

    @Inject
    Installer(DataModel dataModel) {
        super();
        this.dataModel = dataModel;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());

    }

}