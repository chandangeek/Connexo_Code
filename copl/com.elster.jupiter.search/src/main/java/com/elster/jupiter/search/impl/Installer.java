package com.elster.jupiter.search.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.search.SearchCriteriaService;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.List;
import java.util.logging.Logger;

class Installer implements FullInstaller {
    private final DataModel dataModel;

    @Inject
    Installer(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 7));
    }

}