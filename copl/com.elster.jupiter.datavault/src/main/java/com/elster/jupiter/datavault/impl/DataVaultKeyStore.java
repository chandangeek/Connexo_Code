package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;

/**
 * Models the KeyStore that backs the {@link com.elster.jupiter.datavault.DataVault}.
 * This keyStore does not have a name and there can be only one.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-20 (12:02)
 */
class DataVaultKeyStore extends PersistentKeyStoreImpl {

    @Inject
    DataVaultKeyStore(DataModel dataModel, ExceptionFactory exceptionFactory) {
        super(dataModel, exceptionFactory);
    }

    DataVaultKeyStore initialize(String type) {
        this.setType(type);
        return this;
    }

    @Override
    public String getName() {
        return null;
    }
}