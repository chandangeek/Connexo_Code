/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import java.time.Clock;

public class IdsModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(Clock.class);
        requireBinding(OrmService.class);
        requireBinding(NlsService.class);
        requireBinding(ThreadPrincipalService.class);

        bind(IdsService.class).to(IdsServiceImpl.class).in(Scopes.SINGLETON);
    }
}
