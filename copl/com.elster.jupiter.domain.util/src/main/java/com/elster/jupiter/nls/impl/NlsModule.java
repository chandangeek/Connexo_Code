package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.NlsService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

class NlsModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(NlsService.class).to(NlsServiceImpl.class).in(Scopes.SINGLETON);

    }
}
