package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.datavault.DataVault;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

class Installer implements FullInstaller {
    private final Logger logger = Logger.getLogger(Installer.class.getName());

    private final DataModel dataModel;

    @Inject
    Installer(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader) {
        try {
            dataModelUpgrader.upgrade(dataModel, Version.latest());
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        if (dataModel.mapper(OrmKeyStoreImpl.class).find().isEmpty()) {
            generateKeyStore();
        }
    }

    private void generateKeyStore() {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            DataVault dataVault = dataModel.getInstance(DataVaultProvider.class).get();
            dataVault.createVault(byteArrayOutputStream);
            OrmKeyStoreImpl instance = dataModel.getInstance(OrmKeyStoreImpl.class);
            instance.setKeyStore(byteArrayOutputStream);
            instance.save();
        } catch (IOException e) {
            throw dataModel.getInstance(ExceptionFactory.class).newException(MessageSeeds.KEYSTORE_CREATION_FAILED);
        }
    }
}
