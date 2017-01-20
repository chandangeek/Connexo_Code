package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;

/**
 * Created by bvn on 11/7/14.
 */
public class DataVaultProvider implements Provider<ServerDataVault> {

    private final DataModel dataModel;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public DataVaultProvider(DataModel dataModel, ExceptionFactory exceptionFactory) {
        this.dataModel = dataModel;
        this.exceptionFactory = exceptionFactory;
    }

    @Override
    public ServerDataVault get() {
        KeyStoreDataVault dataVault = dataModel.getInstance(KeyStoreDataVault.class);
        List<DataVaultKeyStore> ormKeyStores = dataModel.mapper(DataVaultKeyStore.class).find();
        if (ormKeyStores.size() > 1) {
            throw exceptionFactory.newException(MessageSeeds.AMBIGUOUS_KEYSTORE);
        } else if (ormKeyStores.size() == 1) {
            dataVault.readKeyStore(ormKeyStores.get(0));
        }

        return dataVault;
    }
}
