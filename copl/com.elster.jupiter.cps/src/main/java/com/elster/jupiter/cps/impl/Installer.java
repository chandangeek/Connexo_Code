package com.elster.jupiter.cps.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Installs the Custom Property Set bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-10 (14:36)
 */
public class Installer implements FullInstaller {

    private final DataModel dataModel;

    @Inject
    public Installer(DataModel dataModel) {
        super();
        this.dataModel = dataModel;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
    }
}