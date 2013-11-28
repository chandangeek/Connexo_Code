package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.util.time.Clock;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class IdsModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(Clock.class);
        requireBinding(OrmService.class);

        bind(IdsService.class).to(IdsServiceImpl.class).in(Scopes.SINGLETON);
    }
}
