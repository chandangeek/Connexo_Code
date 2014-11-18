package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * Module intended for use by integration tests
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:49)
 */
public class DataVaultModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(NlsService.class);

        bind(DataVaultService.class).to(DataVaultServiceImpl.class).in(Scopes.SINGLETON);
    }

}