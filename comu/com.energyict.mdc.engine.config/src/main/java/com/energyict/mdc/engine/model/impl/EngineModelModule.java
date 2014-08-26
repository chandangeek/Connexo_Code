package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.energyict.mdc.engine.model.EngineModelService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class EngineModelModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(EventService.class);
        requireBinding(NlsService.class);
        bind(EngineModelService.class).to(EngineModelServiceImpl.class).in(Scopes.SINGLETON);
    }

}