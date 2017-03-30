/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

class Installer implements FullInstaller {

    private final DataModel dataModel;

    @Inject
    Installer(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        if (dataModel.mapper(PersistentKeyStoreImpl.class).find().isEmpty()) {
            generateKeyStore(logger);
        }
    }

    private void generateKeyStore(Logger logger) {
        ServerDataVault dataVault = dataModel.getInstance(DataVaultProvider.class).get();
        dataVault.createVault();
        logger.log(Level.INFO, "Created Key Store");
    }

}