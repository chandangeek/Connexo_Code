package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;

/**
 * Models a {@link com.elster.jupiter.datavault.PersistentKeyStore}
 * that is defined by the system and should therefore not be visible to the user.
 * Its name is specified by the system and is assumed to be unique.
 * The uniqueness will be checked against all other system defined key stores.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-20 (12:04)
 */
class SystemDefinedKeyStore extends NamedPersistentKeyStoreImpl {
    @Inject
    SystemDefinedKeyStore(DataModel dataModel, ExceptionFactory exceptionFactory) {
        super(dataModel, exceptionFactory);
    }

    SystemDefinedKeyStore initialize(String name, String type) {
        this.setName(name);
        this.setType(type);
        return this;
    }
}