/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.domain.util.impl;

import com.elster.jupiter.domain.util.QueryService;
import java.time.Clock;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class DomainUtilModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(Clock.class);

        bind(QueryService.class).to(QueryServiceImpl.class).in(Scopes.SINGLETON);
    }
}
