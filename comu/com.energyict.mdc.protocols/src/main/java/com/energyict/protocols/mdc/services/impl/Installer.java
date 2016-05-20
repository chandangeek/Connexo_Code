package com.energyict.protocols.mdc.services.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.logging.Logger;

public class Installer implements FullInstaller {

    private final Logger logger = Logger.getLogger(Installer.class.getName());

    private final DataModel dataModel;

    @Inject
    public Installer(DataModel dataModel) {
        super();
        this.dataModel = dataModel;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader) {
        try {
            dataModelUpgrader.upgrade(dataModel, Version.latest());
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }

    }

}