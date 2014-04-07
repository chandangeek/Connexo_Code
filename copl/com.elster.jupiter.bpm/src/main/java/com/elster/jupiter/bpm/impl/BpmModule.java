package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.orm.OrmService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class BpmModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(OrmService.class);

        bind(BpmService.class).to(BpmServiceImpl.class).in(Scopes.SINGLETON);
    }
}
