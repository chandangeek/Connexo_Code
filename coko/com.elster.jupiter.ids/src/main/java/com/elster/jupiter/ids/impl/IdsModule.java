package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import java.time.Clock;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class IdsModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(Clock.class);
        requireBinding(OrmService.class);
        requireBinding(NlsService.class);

        bind(IdsService.class).to(IdsServiceImpl.class).in(Scopes.SINGLETON);
    }
}
