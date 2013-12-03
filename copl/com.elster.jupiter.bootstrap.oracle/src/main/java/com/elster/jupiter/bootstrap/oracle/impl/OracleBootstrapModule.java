package com.elster.jupiter.bootstrap.oracle.impl;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class OracleBootstrapModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(BootstrapService.class).to(BootstrapServiceImpl.class).in(Scopes.SINGLETON);
    }
}
