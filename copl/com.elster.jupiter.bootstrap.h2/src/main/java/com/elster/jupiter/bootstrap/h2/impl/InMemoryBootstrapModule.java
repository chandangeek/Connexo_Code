package com.elster.jupiter.bootstrap.h2.impl;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class InMemoryBootstrapModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(BootstrapService.class).to(H2BootStrapService.class).in(Scopes.SINGLETON);
    }
}
