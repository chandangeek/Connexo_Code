package com.elster.jupiter.parties.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.cache.CacheService;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Clock;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class PartyModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(Clock.class);
        requireBinding(OrmService.class);
        requireBinding(QueryService.class);
        requireBinding(UserService.class);
        requireBinding(CacheService.class);
        requireBinding(EventService.class);
        requireBinding(ThreadPrincipalService.class);

        bind(PartyService.class).to(PartyServiceImpl.class).in(Scopes.SINGLETON);
    }
}
