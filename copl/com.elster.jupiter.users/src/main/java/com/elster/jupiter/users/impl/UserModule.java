/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import java.time.Clock;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class UserModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(Clock.class);
        requireBinding(OrmService.class);
        requireBinding(QueryService.class);
        requireBinding(ThreadPrincipalService.class);
        requireBinding(NlsService.class);

        bind(UserService.class).to(UserServiceImpl.class).in(Scopes.SINGLETON);
    }
}
