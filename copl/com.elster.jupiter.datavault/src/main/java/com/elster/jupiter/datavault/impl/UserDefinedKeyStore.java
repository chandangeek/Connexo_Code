package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;

/**
 * Models a {@link com.elster.jupiter.datavault.PersistentKeyStore}
 * that is defined by the user, i.e. its name is specified by the user
 * so the name uniqueness should be checked against all other
 * user defined key stores.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-20 (12:00)
 */
class UserDefinedKeyStore extends NamedPersistentKeyStoreImpl {
    @Inject
    UserDefinedKeyStore(DataModel dataModel, ExceptionFactory exceptionFactory) {
        super(dataModel, exceptionFactory);
    }

    UserDefinedKeyStore initialize(String name, String type) {
        this.setName(name);
        this.setType(type);
        return this;
    }
}