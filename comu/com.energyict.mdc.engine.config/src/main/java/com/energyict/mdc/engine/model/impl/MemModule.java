package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.OrmService;
import com.google.inject.AbstractModule;

public class MemModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(OrmService.class);
//        bind(ComServerService.class).to()
    }
}
