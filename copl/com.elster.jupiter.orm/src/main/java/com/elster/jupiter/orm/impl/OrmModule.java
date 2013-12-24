package com.elster.jupiter.orm.impl;

import javax.sql.DataSource;
import javax.validation.ValidationProviderResolver;

import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Clock;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class OrmModule extends AbstractModule {

    public OrmModule() {
    }

    @Override
    protected void configure() {
        requireBinding(Clock.class);
        requireBinding(DataSource.class);
        requireBinding(JsonService.class);
        requireBinding(ThreadPrincipalService.class);
        requireBinding(Publisher.class);
        requireBinding(ValidationProviderResolver.class);
        bind(OrmService.class).to(OrmServiceImpl.class).in(Scopes.SINGLETON);
    }

  
}
