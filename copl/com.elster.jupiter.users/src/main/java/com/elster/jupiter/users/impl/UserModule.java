package com.elster.jupiter.users.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.cache.CacheService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Clock;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class UserModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(Clock.class);
        requireBinding(OrmService.class);
        requireBinding(QueryService.class);
        requireBinding(CacheService.class);
        requireBinding(ThreadPrincipalService.class);

        bind(UserService.class).to(UserServiceImpl.class).in(Scopes.SINGLETON);
    }
}
