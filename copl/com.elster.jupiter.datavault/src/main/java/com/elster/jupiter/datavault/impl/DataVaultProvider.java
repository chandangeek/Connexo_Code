package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.datavault.DataVault;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by bvn on 11/7/14.
 */
public class DataVaultProvider implements Provider<DataVault> {

    private final DataModel dataModel;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public DataVaultProvider(DataModel dataModel, ExceptionFactory exceptionFactory) {
        this.dataModel = dataModel;
        this.exceptionFactory = exceptionFactory;
    }

    @Override
    public DataVault get() {
        List<OrmKeyStoreImpl> ormKeyStores = dataModel.mapper(OrmKeyStoreImpl.class).find();
        KeyStoreDataVault dataVault = dataModel.getInstance(KeyStoreDataVault.class);
        if (ormKeyStores.size()>1) {
            throw exceptionFactory.newException(MessageSeeds.AMBIGUOUS_KEYSTORE);
//        } else if (ormKeyStores.isEmpty()) {
//            throw exceptionFactory.newException(MessageSeeds.NO_KEYSTORE);
        } else if (ormKeyStores.size()==1) {
            try (InputStream inputStream = new ByteArrayInputStream(ormKeyStores.get(0).getKeyStoreBytes())) {
                dataVault.readKeyStore(inputStream);
            } catch (IOException e) {
                throw exceptionFactory.newException(MessageSeeds.KEYSTORE_CREATION_FAILED, ormKeyStores.get(0).getId());
            }
        }

        return dataVault;
    }
}
