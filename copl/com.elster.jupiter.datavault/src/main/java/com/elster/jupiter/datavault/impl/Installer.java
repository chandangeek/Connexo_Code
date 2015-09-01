package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.datavault.DataVault;
import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.DataModel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

class Installer {
    private final Logger logger = Logger.getLogger(Installer.class.getName());

    private final DataModel dataModel;

    Installer(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public void install() {
        installDataModel();
        if (dataModel.mapper(OrmKeyStoreImpl.class).find().isEmpty()) {
            generateKeyStore();
        }
    }

    private void installDataModel() {
        try {
            this.dataModel.install(true, true);
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
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
